package com.cx.plugin;

/**
 * Created by galn on 18/12/2016.
 */

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class CheckmarxTask implements TaskType {

    public static final Logger log = LoggerFactory.getLogger(CheckmarxTask.class);

    protected CxClientService cxClientService;
    protected java.net.URL url;//TODO add default directory
    private String workDirectory;//TODO add default directory
    private File zipTempFile;
    protected String projectStateLink;
    protected ScanConfiguration config;
    protected String scanResultsUrl;

    private BuildLogger buildLogger;
    private ConfigurationMap configurationMap;
    private BuildContext buildContext;

    public static final String PDF_REPORT_NAME = "CxReport";
    public static final String OSA_REPORT_NAME = "OSA_Report";

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.create(taskContext);
        buildLogger = taskContext.getBuildLogger();
        BuildLoggerAdapter buildLoggerAdapter = new BuildLoggerAdapter(buildLogger);
        buildContext = taskContext.getBuildContext();

        configurationMap = taskContext.getConfigurationMap();
        workDirectory = taskContext.getWorkingDirectory().getPath(); //     getRootDirectory()
        ScanResults scanResults = null;
        OSASummaryResults osaSummaryResults = null;
        boolean failed = false;

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
            if (config.isOsaEnabled()) {
                try {
                    buildLogger.addBuildLogEntry("Creating OSA scan");
                    buildLogger.addBuildLogEntry("Zipping dependencies");
                    File zipForOSA = createZipForOSA();
                    buildLogger.addBuildLogEntry("Sending OSA scan request");
                    osaScan = cxClientService.createOSAScan(createScanResponse.getProjectId(), zipForOSA); //TODO dont check the license!! where rhe async coming?
                    buildLogger.addBuildLogEntry("OSA scan created successfully");
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
                scanResults = cxClientService.retrieveScanResults(createScanResponse.getProjectId());
                scanResultsUrl = CxPluginHelper.composeScanLink(url.toString(), scanResults);
                printResultsToConsole(scanResults);

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

        //assert vulnerabilities under threshold
        if (failed ||  assertVulnerabilities(scanResults , osaSummaryResults)) {
            return taskResultBuilder.failedWithError().build();
        }

        return taskResultBuilder.success().build();
    }

    private CreateScanResponse createScan() { //TODO handle exceptions
        CreateScanResponse createScanResponse = null;
        try {
            //prepare sources to scan (zip them)
            buildLogger.addBuildLogEntry("Zipping sources");
            zipTempFile = zipWorkspaceFolder();

            //send sources to scan
            byte[] zippedSources = getBytesFromZippedSources();
            LocalScanConfiguration conf = generateScanConfiguration(zippedSources);
            createScanResponse = cxClientService.createLocalScan(conf);
            projectStateLink = CxPluginHelper.composeProjectStateLink(url.toString(), createScanResponse.getProjectId());
            buildLogger.addBuildLogEntry("Scan created successfully. Link to project state: " + projectStateLink);

            zipTempFile.delete(); //TODO check for null?
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

    private File zipWorkspaceFolder() throws IOException, InterruptedException { //TODO handle exceptions
        CxFolderPattern folderPattern = new CxFolderPattern();
        final String combinedFilterPattern = folderPattern.generatePattern(this.configurationMap, this.buildLogger, CxParam.FOLDER_EXCLUSION, true);

        CxZip cxZip = new CxZip();
        return cxZip.ZipWorkspaceFolder(getWorkspace(), combinedFilterPattern, this.buildLogger);

    }

    private LocalScanConfiguration generateScanConfiguration(byte[] zippedSources) {
        LocalScanConfiguration ret = new LocalScanConfiguration();
        ret.setProjectName(config.getProjectName());
        ret.setClientOrigin(ClientOrigin.BAMBOO);
        ret.setFolderExclusions(CxPluginHelper.convertArrayToString(config.getFolderExclusions()));
        ret.setFullTeamPath(config.getFullTeamPath());
        ret.setIncrementalScan(config.isIncrementalScan());
        ret.setPreset(config.getPreset());
        ret.setZippedSources(zippedSources);
        ret.setFileName(config.getProjectName());//TODO  ???

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
        buildLogger.addBuildLogEntry("preset: " + config.getPreset());
        buildLogger.addBuildLogEntry("isIncrementalScan: " + config.isIncrementalScan());
        buildLogger.addBuildLogEntry("folderExclusions: " + (config.getFolderExclusions().length > 0? Arrays.toString(config.getFolderExclusions()) :"" ));
        buildLogger.addBuildLogEntry("isSynchronous: " + config.isSynchronous());
        buildLogger.addBuildLogEntry("generatePDFReport: " + config.isGeneratePDFReport());
        buildLogger.addBuildLogEntry("thresholds enabled: " + config.isThresholdsEnabled());
        if (config.isSASTThresholdEnabled()) {
            buildLogger.addBuildLogEntry("highThreshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getHighThreshold()));
            buildLogger.addBuildLogEntry("mediumThreshold: " + (config.getMediumThreshold() == null ? "[No Threshold]" : config.getMediumThreshold()));
            buildLogger.addBuildLogEntry("lowThreshold: " + (config.getLowThreshold() == null ? "[No Threshold]" : config.getLowThreshold()));
        }
        buildLogger.addBuildLogEntry("osaEnabled: " + config.isOsaEnabled());
        if (config.isOsaEnabled()) {
            buildLogger.addBuildLogEntry("osaHighSeveritiesThreshold: " + (config.getOsaHighThreshold() == null ? "[No Threshold]" : config.getOsaHighThreshold()));
            buildLogger.addBuildLogEntry("osaMediumSeveritiesThreshold: " + (config.getOsaMediumThreshold() == null ? "[No Threshold]" : config.getOsaMediumThreshold()));
            buildLogger.addBuildLogEntry("osaLowSeveritiesThreshold: " + (config.getOsaLowThreshold() == null ? "[No Threshold]" : config.getOsaLowThreshold()));
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

    private int checkScanTimeout(Integer scanTimeout) {
        int timeout = -1;
        if (scanTimeout != null) {
            timeout = scanTimeout;
        }
        return timeout;
    }

    private File createZipForOSA() throws IOException, InterruptedException { //TODO handle exceptions{
        CxFolderPattern folderPattern = new CxFolderPattern();
        String combinedFilterPattern = folderPattern.generatePattern(this.configurationMap, this.buildLogger, null, false); //TODO change the 3 param null
        CxZip cxZip = new CxZip();
        return cxZip.zipSourceCode(getWorkspace(), combinedFilterPattern);
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
