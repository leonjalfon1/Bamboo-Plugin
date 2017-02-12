package com.cx.plugin;

/**
 * Created by galn on 18/12/2016.
 */

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.spring.container.ContainerManager;
import com.cx.client.*;
import com.cx.client.dto.*;
import com.cx.client.exception.CxClientException;
import com.cx.client.rest.dto.CreateOSAScanResponse;
import com.cx.client.rest.dto.OSASummaryResults;
import com.cx.plugin.dto.CxAbortException;
import com.cx.plugin.dto.CxParam;
import com.cx.plugin.dto.ScanConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hsqldb.lib.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.HashMap;

public class CheckmarxTask implements TaskType {

    public static final Logger log = LoggerFactory.getLogger(CheckmarxTask.class);

    private static final long MAX_ZIP_SIZE_BYTES = 209715200;
    private static final long MAX_OSA_ZIP_SIZE_BYTES = 2146483647;

    protected CxClientService cxClientService;
    protected java.net.URL url;//TODO add default directory
    private String workDirectory;//TODO add default directory
    private File zipTempFile;
    protected String projectStateLink;
    protected ScanConfiguration config;
    protected String scanResultsUrl;

    private BuildLogger buildLogger;
    private HashMap<String, String> configurationMap;
    private BuildContext buildContext;

    public static final String PDF_REPORT_NAME = "CxReport";
    public static final String OSA_REPORT_NAME = "OSA_Report";

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.newBuilder(taskContext);
        buildLogger = taskContext.getBuildLogger();
        BuildLoggerAdapter buildLoggerAdapter = new BuildLoggerAdapter(buildLogger);
        buildContext = taskContext.getBuildContext();
        Map<String, String> results = new HashMap<String, String>();

        configurationMap = resolveConfigurationMap(taskContext.getConfigurationMap());//resolve the global configuration properties
        workDirectory = taskContext.getWorkingDirectory().getPath();
        ScanResults scanResults = null;
        OSASummaryResults osaSummaryResults = null;
        boolean failed = false;//TODO

        Exception osaCreateException = null;
        Exception scanWaitException = null;

        try {
            config = new ScanConfiguration(configurationMap);
            url = new URL(config.getUrl());//TODO -URL EMPTY
            printConfiguration(config);

            //initialize cx client
            buildLogger.addBuildLogEntry("Initializing Cx client");
            cxClientService = new CxClientServiceImpl(url, config.getUsername(), decrypt(config.getPassword()));
            cxClientService.setLogger(buildLoggerAdapter);

            //perform login to server
            buildLogger.addBuildLogEntry("Logging into the Checkmarx service.");
            cxClientService.loginToServer(); //TODO- handle exception when not login

            //prepare sources (zip it) and send it to scan
            CreateScanResponse createScanResponse = createScan();

            CreateOSAScanResponse osaScan = null;
            //OSA Scan
            if (config.isOsaEnabled()) {
                try {
                    buildLogger.addBuildLogEntry("Creating OSA scan");
                    buildLogger.addBuildLogEntry("Zipping dependencies");
                    //prepare sources (zip it) for the OSA scan and send it to OSA scan
                    File zipForOSA = zipWorkspaceFolder(workDirectory, "", "", MAX_OSA_ZIP_SIZE_BYTES);
                    buildLogger.addBuildLogEntry("Sending OSA scan request");
                    osaScan = cxClientService.createOSAScan(createScanResponse.getProjectId(), zipForOSA); //TODO dont check the license!! where rhe async coming?
                    buildLogger.addBuildLogEntry("OSA scan created successfully");
                    if(zipForOSA.exists() && !zipForOSA.delete()) {
                        buildLogger.addBuildLogEntry("Warning: fail to delete temporary zip file: " + zipForOSA.getAbsolutePath());
                    }
                    buildLogger.addBuildLogEntry("Temporary file deleted");
                } catch (Exception e) {
                    buildLogger.addErrorLogEntry("Fail to create OSA Scan: " + e.getMessage());
                    osaCreateException = e;
                }
            }

            if (!config.isSynchronous()) {
                if (osaCreateException != null) {
                    throw osaCreateException;
                }
                buildLogger.addBuildLogEntry("Running in Asynchronous mode. Not waiting for scan to finish");
                return taskResultBuilder.success().build();//TODO- change the return value
            }

            try {
                //wait for SAST scan to finish
                ConsoleScanWaitHandler consoleScanWaitHandler = new ConsoleScanWaitHandler();
                consoleScanWaitHandler.setLogger(buildLoggerAdapter);
                buildLogger.addBuildLogEntry("Waiting for CxSAST scan to finish.");
                cxClientService.waitForScanToFinish(createScanResponse.getRunId(), checkScanTimeout(config.getScanTimeoutInMinutes()), consoleScanWaitHandler);
                buildLogger.addBuildLogEntry("Scan finished. Retrieving scan results");
                //retrieve OSA scan results
                scanResults = cxClientService.retrieveScanResults(createScanResponse.getProjectId());
                scanResultsUrl = CxPluginHelper.composeScanLink(url.toString(), scanResults);
                printResultsToConsole(scanResults);
                addSastResults(results, scanResults, config);

                if (config.isGeneratePDFReport()) {
                    createPDFReport(scanResults.getScanID());
                }

            } catch (Exception e) {
                buildLogger.addErrorLogEntry("Fail to perform CxSAST scan: " + e.getMessage());
                failed = true; //TODO handle exceptions or change the failed flag
                scanWaitException = e;
            }

            if (config.isOsaEnabled()) {

                if (osaCreateException != null) {
                    throw osaCreateException;
                }
                //wait for OSA scan to finish
                OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
                osaConsoleScanWaitHandler.setLogger(buildLoggerAdapter);
                buildLogger.addBuildLogEntry("Waiting for OSA Scan to finish");
                cxClientService.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler);//TODO -1?
                buildLogger.addBuildLogEntry("OSA scan finished successfully");
                buildLogger.addBuildLogEntry("Creating OSA reports");
                osaSummaryResults = cxClientService.retrieveOSAScanSummaryResults(createScanResponse.getProjectId());
                printOSAResultsToConsole(osaSummaryResults);
                addOSAResults(results, osaSummaryResults, config);

                //OSA PDF and HTML reports
                SimpleDateFormat ft = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
                String now = ft.format(new Date());
                byte[] osaPDF = cxClientService.retrieveOSAScanPDFResults(createScanResponse.getProjectId());
                String pdfFileName = OSA_REPORT_NAME + "_" + now + ".pdf";
                FileUtils.writeByteArrayToFile(new File(workDirectory, pdfFileName), osaPDF);
                buildLogger.addBuildLogEntry("OSA PDF report location: " + workDirectory + "\\" + pdfFileName);
                String osaHtml = cxClientService.retrieveOSAScanHtmlResults(createScanResponse.getProjectId());
                String htmlFileName = OSA_REPORT_NAME + "_" + now + ".html";
                FileUtils.writeStringToFile(new File(workDirectory, htmlFileName), osaHtml, Charset.defaultCharset());
                buildLogger.addBuildLogEntry("OSA HTML report location: " + workDirectory + "\\" + htmlFileName);
            }
            if (scanWaitException != null) {
                throw scanWaitException;
            }

        } catch (CxClientException e) {
            buildLogger.addErrorLogEntry("Caught Exception: ", e);
            throw new TaskException(e.getMessage());    //TODO handle exceptions or change the failed flag

        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Unexpected Exception:", e);
            throw new TaskException(e.getMessage());
        }
        buildContext.getBuildResult().getCustomBuildData().putAll(results);

        //assert vulnerabilities under threshold
        if (failed || assertVulnerabilities(scanResults, osaSummaryResults)) {
            return taskResultBuilder.failedWithError().build();
        }

        return taskResultBuilder.success().build();
    }

    private HashMap<String, String> resolveConfigurationMap(ConfigurationMap configMap) {

        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION);
        configurationMap = new HashMap<String, String>();

        if (configMap.get(CxParam.DEFAULT_CREDENTIALS).equals(CxParam.COSTUME_CONFIGURATION_SERVER)) {
            configurationMap.put(CxParam.SERVER_URL, configMap.get(CxParam.SERVER_URL));
            configurationMap.put(CxParam.USER_NAME, configMap.get(CxParam.USER_NAME));
            configurationMap.put(CxParam.PASSWORD, configMap.get(CxParam.PASSWORD));
        } else {
            configurationMap.put(CxParam.SERVER_URL, adminConfig.getSystemProperty(CxParam.GLOBAL_SERVER_URL));
            configurationMap.put(CxParam.USER_NAME, adminConfig.getSystemProperty(CxParam.GLOBAL_USER_NAME));
            configurationMap.put(CxParam.PASSWORD, adminConfig.getSystemProperty(CxParam.GLOBAL_PASSWORD));
        }

        configurationMap.put(CxParam.PROJECT_NAME, configMap.get(CxParam.PROJECT_NAME));
        configurationMap.put(CxParam.PRESET_ID, configMap.get(CxParam.PRESET_ID));
        configurationMap.put(CxParam.PRESET_NAME, configMap.get(CxParam.PRESET_NAME));
        configurationMap.put(CxParam.PRESET_ID, configMap.get(CxParam.TEAM_PATH_ID));
        configurationMap.put(CxParam.TEAM_PATH_NAME, configMap.get(CxParam.TEAM_PATH_NAME));


        if (configMap.get(CxParam.DEFAULT_CXSAST).equals(CxParam.COSTUME_CONFIGURATION_CXSAST)) {
            configurationMap.put(CxParam.FOLDER_EXCLUSION, configMap.get(CxParam.FOLDER_EXCLUSION));
            configurationMap.put(CxParam.FILTER_PATTERN, configMap.get(CxParam.FILTER_PATTERN));
            configurationMap.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, configMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES));
        } else {
            configurationMap.put(CxParam.FOLDER_EXCLUSION, adminConfig.getSystemProperty(CxParam.GLOBAL_FOLDER_EXCLUSION));
            configurationMap.put(CxParam.FILTER_PATTERN, adminConfig.getSystemProperty(CxParam.GLOBAL_FILTER_PATTERN));
            configurationMap.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, adminConfig.getSystemProperty(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES));
        }
        configurationMap.put(CxParam.IS_INCREMENTAL_SCAN, configMap.get(CxParam.IS_INCREMENTAL_SCAN));
        configurationMap.put(CxParam.GENERATE_PDF_REPORT, configMap.get(CxParam.GENERATE_PDF_REPORT));
        configurationMap.put(CxParam.OSA_ENABLED, configMap.get(CxParam.OSA_ENABLED));

        if (configMap.get(CxParam.DEFAULT_SCAN_CONTROL).equals(CxParam.COSTUME_CONFIGURATION_CONTROL)) {
            configurationMap.put(CxParam.IS_SYNCHRONOUS, configMap.get(CxParam.IS_SYNCHRONOUS));
            configurationMap.put(CxParam.THRESHOLDS_ENABLED, configMap.get(CxParam.THRESHOLDS_ENABLED));
            configurationMap.put(CxParam.HIGH_THRESHOLD, configMap.get(CxParam.HIGH_THRESHOLD));
            configurationMap.put(CxParam.MEDIUM_THRESHOLD, configMap.get(CxParam.MEDIUM_THRESHOLD));
            configurationMap.put(CxParam.LOW_THRESHOLD, configMap.get(CxParam.LOW_THRESHOLD));
            configurationMap.put(CxParam.OSA_THRESHOLDS_ENABLED, configMap.get(CxParam.OSA_THRESHOLDS_ENABLED));
            configurationMap.put(CxParam.OSA_HIGH_THRESHOLD, configMap.get(CxParam.OSA_HIGH_THRESHOLD));
            configurationMap.put(CxParam.OSA_MEDIUM_THRESHOLD, configMap.get(CxParam.OSA_MEDIUM_THRESHOLD));
            configurationMap.put(CxParam.OSA_LOW_THRESHOLD, configMap.get(CxParam.OSA_LOW_THRESHOLD));
        } else {
            configurationMap.put(CxParam.IS_SYNCHRONOUS, resolveBooleanParam(adminConfig.getSystemProperty(CxParam.GLOBAL_IS_SYNCHRONOUS)));
            configurationMap.put(CxParam.THRESHOLDS_ENABLED, adminConfig.getSystemProperty(CxParam.GLOBAL_THRESHOLDS_ENABLED));
            configurationMap.put(CxParam.HIGH_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_HIGH_THRESHOLD));
            configurationMap.put(CxParam.MEDIUM_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_MEDIUM_THRESHOLD));
            configurationMap.put(CxParam.LOW_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_LOW_THRESHOLD));
            configurationMap.put(CxParam.OSA_THRESHOLDS_ENABLED, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_THRESHOLDS_ENABLED));
            configurationMap.put(CxParam.OSA_HIGH_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_HIGH_THRESHOLD));
            configurationMap.put(CxParam.OSA_MEDIUM_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_MEDIUM_THRESHOLD));
            configurationMap.put(CxParam.OSA_LOW_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_LOW_THRESHOLD));
        }

        return configurationMap;
    }

    private String resolveBooleanParam(String param) {
        if (StringUtil.isEmpty(param)) {
            param = "false";
        }
        return param;
    }

    private void addSastResults(Map<String, String> results, ScanResults scanResults, ScanConfiguration config) {
        results.put(CxResultsConst.HIGH_RESULTS, String.valueOf(scanResults.getHighSeverityResults()));
        results.put(CxResultsConst.MEDIUM_RESULTS, String.valueOf(scanResults.getMediumSeverityResults()));
        results.put(CxResultsConst.LOW_RESULTS, String.valueOf(scanResults.getLowSeverityResults()));

        results.put(CxResultsConst.THRESHOLD_ENABLED, String.valueOf(config.isSASTThresholdEnabled()));

        if (config.isSASTThresholdEnabled()) {
            String highThreshold = (config.getHighThreshold() == null ? "null" : String.valueOf(config.getHighThreshold()));
            String mediumThreshold = (config.getMediumThreshold() == null ? "null" : String.valueOf(config.getMediumThreshold()));
            String lowThreshold = (config.getLowThreshold() == null ? "null" : String.valueOf(config.getLowThreshold()));

            results.put(CxResultsConst.HIGH_THRESHOLD, highThreshold);
            results.put(CxResultsConst.MEDIUM_THRESHOLD, mediumThreshold);
            results.put(CxResultsConst.LOW_THRESHOLD, lowThreshold);
        }

        results.put(CxResultsConst.SAST_RESULTS_READY, "true");
    }

    private void addOSAResults(Map<String, String> results, OSASummaryResults osaSummaryResults, ScanConfiguration config) {

        results.put(CxResultsConst.OSA_HIGH_RESULTS, String.valueOf(osaSummaryResults.getHighVulnerabilities()));
        results.put(CxResultsConst.OSA_MEDIUM_RESULTS, String.valueOf(osaSummaryResults.getMediumVulnerabilities()));
        results.put(CxResultsConst.OSA_LOW_RESULTS, String.valueOf(osaSummaryResults.getLowVulnerabilities()));

        results.put(CxResultsConst.OSA_VULNERABLE_LIBRARIES, String.valueOf(osaSummaryResults.getHighVulnerabilityLibraries() + osaSummaryResults.getMediumVulnerabilityLibraries() + osaSummaryResults.getLowVulnerabilityLibraries()));
        results.put(CxResultsConst.OSA_OK_LIBRARIES, String.valueOf(osaSummaryResults.getNonVulnerableLibraries()));

        results.put(CxResultsConst.OSA_THRESHOLD_ENABLED, String.valueOf(config.isOSAThresholdEnabled()));

        if (config.isOSAThresholdEnabled()) {

            String osaHighThreshold = (config.getOsaHighThreshold() == null ? "null" : String.valueOf(config.getOsaHighThreshold()));
            String osaMediumThreshold = (config.getOsaMediumThreshold() == null ? "null" : String.valueOf(config.getOsaMediumThreshold()));
            String osaLowThreshold = (config.getOsaLowThreshold() == null ? "null" : String.valueOf(config.getOsaLowThreshold()));

            results.put(CxResultsConst.OSA_HIGH_THRESHOLD, osaHighThreshold);
            results.put(CxResultsConst.OSA_MEDIUM_THRESHOLD, osaMediumThreshold);
            results.put(CxResultsConst.OSA_LOW_THRESHOLD, osaLowThreshold);
        }

        results.put(CxResultsConst.OSA_RESULTS_READY, "true");
    }

    private CreateScanResponse createScan() { //TODO handle exceptions
        CreateScanResponse createScanResponse = null;
        try {
            //prepare sources to scan (zip them)
            buildLogger.addBuildLogEntry("Zipping sources");

            String folderExclusion = configurationMap.get(CxParam.FOLDER_EXCLUSION); //TODO add the ENV expansion
            String filterPattern = configurationMap.get(CxParam.FILTER_PATTERN);
            zipTempFile = zipWorkspaceFolder(workDirectory, folderExclusion, filterPattern, MAX_ZIP_SIZE_BYTES);

            //send sources to scan
            byte[] zippedSources = getBytesFromZippedSources();
            LocalScanConfiguration conf = generateScanConfiguration(zippedSources);
            createScanResponse = cxClientService.createLocalScan(conf);
            projectStateLink = CxPluginHelper.composeProjectStateLink(url.toString(), createScanResponse.getProjectId());
            buildLogger.addBuildLogEntry("Scan created successfully. Link to project state: " + projectStateLink);

            if(zipTempFile.exists() && !zipTempFile.delete()) {
                buildLogger.addBuildLogEntry("Warning: fail to delete temporary zip file: " + zipTempFile.getAbsolutePath());
            }
            buildLogger.addBuildLogEntry("Temporary file deleted");
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("fail to create scan: " + e.getMessage());//TODO return success when still have problems, need to throw excepion to fail the scan
        }
        return createScanResponse;
    }

    private String getWorkspace() throws CxAbortException {
        final Map<Long, String> checkoutLocations = this.buildContext.getCheckoutLocation();
        if (checkoutLocations.isEmpty()) {
            throw new CxAbortException("No source code repository found");
        }
        if (checkoutLocations.size() > 1) {
            this.buildLogger.addBuildLogEntry("Warning: more than one workspace found. Using the first one.");
        }
        return checkoutLocations.entrySet().iterator().next().getValue();
    }

    private File zipWorkspaceFolder(String baseZipDir, String folderExclusions, String filterPattern, long maxZipSizeInBytes) throws IOException, InterruptedException { //TODO handle exceptions
        final String combinedFilterPattern = CxFolderPattern.generatePattern(folderExclusions, filterPattern, buildLogger);
        CxZip cxZip = new CxZip().setMaxZipSizeInBytes(maxZipSizeInBytes);
        return cxZip.zipWorkspaceFolder(baseZipDir, combinedFilterPattern, buildLogger);
    }

    private LocalScanConfiguration generateScanConfiguration(byte[] zippedSources) {
        LocalScanConfiguration ret = new LocalScanConfiguration();
        ret.setProjectName(config.getProjectName());
        ret.setClientOrigin(ClientOrigin.BAMBOO);
        ret.setFolderExclusions(CxPluginHelper.convertArrayToString(config.getFolderExclusions()));
        ret.setFullTeamPath(config.getFullTeamPath());
        ret.setIncrementalScan(config.isIncrementalScan());
        long presetId;

        try {
            presetId = Long.parseLong(config.getPresetId());
        } catch (NumberFormatException e) {
            log.warn("failed to resolve preset ID");
            presetId = 7;
        }
        ret.setPresetId(presetId);
        ret.setZippedSources(zippedSources);
        ret.setFileName(config.getProjectName());

        return ret;
    }

    protected byte[] getBytesFromZippedSources() throws TaskException {

        buildLogger.addBuildLogEntry("Converting Zipped Sources to Byte Array");
        byte[] zipFileByte;
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(zipTempFile);
            zipFileByte = IOUtils.toByteArray(fileStream);
        } catch (Exception e) {
            throw new TaskException("Fail to Set Zipped File Into Project: " + e.getMessage(), e);//TODO EXCEPTIONS
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
        return zipFileByte;
    }

    private void printConfiguration(ScanConfiguration config) {
        buildLogger.addBuildLogEntry("----------------------------Configurations:-----------------------------");
        buildLogger.addBuildLogEntry("username: " + config.getUsername());
        buildLogger.addBuildLogEntry("url: " + config.getUrl());
        buildLogger.addBuildLogEntry("projectName: " + config.getProjectName());
        buildLogger.addBuildLogEntry("scanTimeoutInMinutes: " + (config.getScanTimeoutInMinutes() == null ? "" : config.getScanTimeoutInMinutes()));
        buildLogger.addBuildLogEntry("fullTeamPath: " + config.getFullTeamPath());
        buildLogger.addBuildLogEntry("preset: " + config.getPresetName());
        buildLogger.addBuildLogEntry("isIncrementalScan: " + config.isIncrementalScan());
        buildLogger.addBuildLogEntry("folderExclusions: " + (config.getFolderExclusions().length > 0 ? Arrays.toString(config.getFolderExclusions()) : ""));
        buildLogger.addBuildLogEntry("isSynchronous: " + config.isSynchronous());
        buildLogger.addBuildLogEntry("generatePDFReport: " + config.isGeneratePDFReport());
        buildLogger.addBuildLogEntry("thresholdsEnabled: " + config.isThresholdsEnabled());
        if (config.isSASTThresholdEnabled()) {
            buildLogger.addBuildLogEntry("CxSAST-HighThreshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getHighThreshold()));
            buildLogger.addBuildLogEntry("CxSAST-mediumThreshold: " + (config.getMediumThreshold() == null ? "[No Threshold]" : config.getMediumThreshold()));
            buildLogger.addBuildLogEntry("CxSAST-lowThreshold: " + (config.getLowThreshold() == null ? "[No Threshold]" : config.getLowThreshold()));
        }
        buildLogger.addBuildLogEntry("osaEnabled: " + config.isOsaEnabled());
        if (config.isOsaEnabled()) {
            buildLogger.addBuildLogEntry("CxOSA-HighSeveritiesThreshold: " + (config.getOsaHighThreshold() == null ? "[No Threshold]" : config.getOsaHighThreshold()));
            buildLogger.addBuildLogEntry("CxOSA-MediumSeveritiesThreshold: " + (config.getOsaMediumThreshold() == null ? "[No Threshold]" : config.getOsaMediumThreshold()));
            buildLogger.addBuildLogEntry("CxOSA-LowSeveritiesThreshold: " + (config.getOsaLowThreshold() == null ? "[No Threshold]" : config.getOsaLowThreshold()));
        }
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }

    private void printResultsToConsole(ScanResults scanResults) {
        buildLogger.addBuildLogEntry("----------------------------Scan Results:-------------------------------");
        buildLogger.addBuildLogEntry("High Severity Results: " + scanResults.getHighSeverityResults());
        buildLogger.addBuildLogEntry("Medium Severity Results: " + scanResults.getMediumSeverityResults());
        buildLogger.addBuildLogEntry("Low Severity Results: " + scanResults.getLowSeverityResults());
        buildLogger.addBuildLogEntry("Info Severity Results: " + scanResults.getInfoSeverityResults());
        buildLogger.addBuildLogEntry("Scan Results location: " + scanResultsUrl);
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }

    private void printOSAResultsToConsole(OSASummaryResults osaSummaryResults) {
        buildLogger.addBuildLogEntry("----------------------------Checkmarx Scan Results(CxOSA):-------------------------------");
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("------------------------");
        buildLogger.addBuildLogEntry("Vulnerabilities Summary:");
        buildLogger.addBuildLogEntry("------------------------");
        buildLogger.addBuildLogEntry("OSA High Severity results: " + osaSummaryResults.getHighVulnerabilities());
        buildLogger.addBuildLogEntry("OSA Medium Severity results: " + osaSummaryResults.getMediumVulnerabilities());
        buildLogger.addBuildLogEntry("OSA Low Severity results: " + osaSummaryResults.getLowVulnerabilities());
        buildLogger.addBuildLogEntry("Vulnerability Score: " + osaSummaryResults.getVulnerabilityScore());
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("-----------------------");
        buildLogger.addBuildLogEntry("Libraries Scan Results:");
        buildLogger.addBuildLogEntry("-----------------------");
        buildLogger.addBuildLogEntry("Open-source libraries: " + osaSummaryResults.getTotalLibraries());
        buildLogger.addBuildLogEntry("Vulnerable and outdated: " + osaSummaryResults.getVulnerableAndOutdated());
        buildLogger.addBuildLogEntry("Vulnerable and updated: " + osaSummaryResults.getVulnerableAndUpdated());
        buildLogger.addBuildLogEntry("Non-vulnerable libraries: " + osaSummaryResults.getNonVulnerableLibraries());
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("OSA scan results location: " + projectStateLink.replace("Summary", "OSA"));
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }

    private boolean assertVulnerabilities(ScanResults scanResults, OSASummaryResults osaSummaryResults) throws TaskException { //TODO ask dor regards the taskException (without exception but with build.unSuccess())

        StringBuilder res = new StringBuilder("");
        boolean fail = false;
        if (config.isSASTThresholdEnabled()) {
            fail |= isFail(scanResults.getHighSeverityResults(), config.getHighThreshold(), res, "High", "CxSAST ");
            fail |= isFail(scanResults.getMediumSeverityResults(), config.getMediumThreshold(), res, "Medium", "CxSAST ");
            fail |= isFail(scanResults.getLowSeverityResults(), config.getLowThreshold(), res, "Low", "CxSAST ");
        }
        if (config.isOSAThresholdEnabled() && osaSummaryResults != null) {
            fail |= isFail(osaSummaryResults.getHighVulnerabilities(), config.getOsaHighThreshold(), res, "High", "CxOSA ");
            fail |= isFail(osaSummaryResults.getMediumVulnerabilities(), config.getOsaMediumThreshold(), res, "Medium", "CxOSA ");
            fail |= isFail(osaSummaryResults.getLowVulnerabilities(), config.getOsaLowThreshold(), res, "Low", "CxOSA ");
        }

        if (fail) {
            //  throw new TaskException(res.toString());
            buildLogger.addErrorLogEntry("*************************");
            buildLogger.addErrorLogEntry("The Build Failed due to: ");
            buildLogger.addErrorLogEntry("*************************");
            String[] lines = res.toString().split("\\n");
            for (String s : lines) {
                buildLogger.addErrorLogEntry(s);
            }
            buildLogger.addErrorLogEntry("---------------------------------------------------------------------\n");
        }
        return fail;
    }

    private boolean isFail(int result, Integer threshold, StringBuilder res, String severity, String severityType) {
        boolean fail = false;
        if (threshold != null && result > threshold) {
            res.append(severityType + severity + " Severity results are above threshold. Results: ").append(result).append(". Threshold: ").append(threshold).append("\n");
            fail = true;
        }
        return fail;
    }

    //TODO
    private int checkScanTimeout(Integer scanTimeout) {
        int timeout = -1;
        if (scanTimeout != null) {
            timeout = scanTimeout;
        }
        return timeout;
    }

    private void createPDFReport(long scanId) {
        buildLogger.addBuildLogEntry("Generating PDF Report");
        byte[] scanReport;
        try {
            scanReport = cxClientService.getScanReport(scanId, ReportType.PDF);
            SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
            String now = df.format(new Date());
            String pdfFileName = PDF_REPORT_NAME + "_" + now + ".pdf";
            FileUtils.writeByteArrayToFile(new File(getWorkspace(), pdfFileName), scanReport);
            buildLogger.addBuildLogEntry("PDF report location: " + getWorkspace() + "\\" + pdfFileName);
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Fail to Generate PDF Report");
        }
    }

    private String decrypt(String password) {
        String encPass;
        try {
            encPass = new EncryptionServiceImpl().decrypt(password);
        } catch (EncryptionException e) {
            encPass = "";
        }

        return encPass;
    }
}
