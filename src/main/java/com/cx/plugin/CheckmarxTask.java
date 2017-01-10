package com.cx.plugin;

/**
 * Created by galn on 18/12/2016.
 */

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.cx.client.*;
import com.cx.client.dto.ClientOrigin;
import com.cx.client.dto.CreateScanResponse;
import com.cx.client.dto.LocalScanConfiguration;
import com.cx.client.dto.ScanResults;
import com.cx.client.exception.CxClientException;
//import com.cx.client.rest.dto.CreateOSAScanResponse;
//import com.cx.client.rest.dto.OSASummaryResults;
import com.cx.plugin.dto.CxAbortException;
import com.cx.plugin.dto.CxParam;
import com.cx.plugin.dto.ScanConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class CheckmarxTask implements TaskType {

    protected CxClientService cxClientService;
    protected java.net.URL url;//TODO add default directory
    private String workDirectory;//TODO add default directory
    private File zipTempFile;
    protected String projectStateLink;
    protected String projectName;
    protected ScanConfiguration config;
    protected String scanResultsUrl;

    private BuildLogger buildLogger;
    private ConfigurationMap configurationMap;
    private BuildContext buildContext;

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.create(taskContext);
        //BambooLoggerAdapter.setLogger(taskContext.getBuildLogger());
        buildLogger = taskContext.getBuildLogger();
        buildContext = taskContext.getBuildContext();
        projectName = buildContext.getProjectName();//TODO setBuildContext
        configurationMap = taskContext.getConfigurationMap();
        workDirectory = taskContext.getWorkingDirectory().getPath(); //     getRootDirectory()
        ScanResults scanResults = null;
       // OSASummaryResults osaSummaryResults = null;
        boolean failed = false;

        Exception osaCreateException = null;
        Exception scanWaitException = null;


        try {
            config = new ScanConfiguration(configurationMap, projectName);
            url = new URL(config.getUrl());//TODO -URL EMPTY
            printConfiguration(config);

            //initialize cx client
            buildLogger.addBuildLogEntry("Initializing Cx Client");
            cxClientService = new CxClientServiceImpl(url, config.getUsername(), config.getPassword());

            //perform login to server
            buildLogger.addBuildLogEntry("Logging In to Checkmarx Service.");
            cxClientService.loginToServer(); //TODO- handle exception when not login

            //prepare sources (zip it)  and send it to scan
            CreateScanResponse createScanResponse = createScan();

           // CreateOSAScanResponse osaScan = null;
          /*  if (config.isOsaEnabled()) {
                try {
                    buildLogger.addBuildLogEntry("creating OSA scan");
                    buildLogger.addBuildLogEntry("zipping dependencies");
                    File zipForOSA = createZipForOSA();
                    buildLogger.addBuildLogEntry("sending OSA scan request");
                    osaScan = cxClientService.createOSAScan(createScanResponse.getProjectId(), zipForOSA); //TODO dont check the license!! where rhe async coming?
                    buildLogger.addBuildLogEntry("OSA scan created successfully");
                } catch (Exception e) {
                    buildLogger.addErrorLogEntry("fail to create OSA Scan: " + e.getMessage());
                    osaCreateException = e;
                }
            }
*/
            if (!config.isSynchronous()) {
               /* if (osaCreateException != null) {
                    throw osaCreateException;
                }*/
                buildLogger.addBuildLogEntry("Running in Asynchronous Mode. Not Waiting for Scan to Finish");
                return taskResultBuilder.success().build();//TODO- change the return value
            }

            //wait for SAST scan to finish
            try {
                buildLogger.addBuildLogEntry("Waiting For Scan To Finish.");
                cxClientService.waitForScanToFinish(createScanResponse.getRunId(), checkScanTimeout(config.getScanTimeoutInMinutes()), new ConsoleScanWaitHandler());

                buildLogger.addBuildLogEntry("Scan Finished. Retrieving Scan Results");
                scanResults = cxClientService.retrieveScanResults(createScanResponse.getProjectId());
                scanResultsUrl = CxPluginHelper.composeScanLink(url.toString(), scanResults);
                printResultsToConsole(scanResults);
            } catch (Exception e) {
                buildLogger.addErrorLogEntry("fail to perform scan: " + e.getMessage());
                failed = true; //TODO handle exceptions or change the failed flag
                scanWaitException = e;
            }

     /*       if (config.isOsaEnabled()) {

                if (osaCreateException != null) {
                    throw osaCreateException;
                }
                //wait for OSA scan to finish
                buildLogger.addBuildLogEntry("Waiting for OSA Scan to Finish");
                cxClientService.waitForOSAScanToFinish(osaScan.getScanId(), -1, new OSAConsoleScanWaitHandler());
                buildLogger.addBuildLogEntry("OSA Scan Finished Successfully");
                buildLogger.addBuildLogEntry("Creating OSA Reports");
                osaSummaryResults = cxClientService.retrieveOSAScanSummaryResults(createScanResponse.getProjectId());
                printOSAResultsToConsole(osaSummaryResults);

               *//* String now = DateFormatUtils.format(new Date(), "dd_MM_yyyy-HH_mm_ss");
                if (osaGeneratePDFReport) {
                    byte[] osaPDF = cxClientService.retrieveOSAScanPDFResults(createScanResponse.getProjectId());
                    String pdfFileName = OSA_REPORT_NAME + "_" + now + ".pdf";
                    FileUtils.writeByteArrayToFile(new File(outputDirectory, pdfFileName), osaPDF);
                    buildLogger.addBuildLogEntry("OSA PDF Report Can Be Found in: " + outputDirectory + "\\" + pdfFileName);
                }

                if (osaGenerateHTMLReport) {
                    String osaHtml = cxClientService.retrieveOSAScanHtmlResults(createScanResponse.getProjectId());
                    String htmlFileName = OSA_REPORT_NAME + "_" + now + ".html";
                    FileUtils.writeStringToFile(new File(outputDirectory, htmlFileName), osaHtml, Charset.defaultCharset());
                    buildLogger.addBuildLogEntry("OSA HTML Report Can Be Found in: " + outputDirectory + "\\" + htmlFileName);
                }*//*

            }
            if (scanWaitException != null) {
                throw scanWaitException;
            }*/


        } catch (CxClientException e) {
            buildLogger.addErrorLogEntry("Caught Exception: ", e);
            //throw new TaskException(e.getMessage());    //TODO handle exceptions or change the failed flag

        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Unexpected Exception:", e);
            throw new TaskException(e.getMessage());
        }

        //assert vulnerabilities under threshold
        if (failed || (config.isSASTThresholdEnabled() && assertVulnerabilities(scanResults))) {
            return taskResultBuilder.failedWithError().build();
        }

        return taskResultBuilder.success().build();
    }

    private CreateScanResponse createScan() { //TODO handle exceptions
        CreateScanResponse createScanResponse = null;
        try {
            //prepare sources to scan (zip them)
            buildLogger.addBuildLogEntry("Zipping Sources");
            zipTempFile = zipWorkspaceFolder();

            //send sources to scan
            byte[] zippedSources = getBytesFromZippedSources();
            LocalScanConfiguration conf = generateScanConfiguration(zippedSources);
            createScanResponse = cxClientService.createLocalScanResolveFields(conf);
            projectStateLink = CxPluginHelper.composeProjectStateLink(url.toString(), createScanResponse.getProjectId());
            buildLogger.addBuildLogEntry("Scan Created Successfully. Link to Project State: " + projectStateLink);

            zipTempFile.delete(); //TODO check for null?
            buildLogger.addBuildLogEntry("Temporary file deleted");
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("fail to create scan: " + e.getMessage());//TODO return success when still have problems
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
        ret.setProjectName(projectName);
        ret.setClientOrigin(ClientOrigin.BAMBOO);
        ret.setFolderExclusions(CxPluginHelper.convertArrayToString(config.getFolderExclusions()));
        ret.setFullTeamPath(config.getFullTeamPath());
        ret.setIncrementalScan(config.isIncrementalScan());
        ret.setPreset(config.getPreset());
        ret.setZippedSources(zippedSources);
        ret.setFileName(projectName);//TODO  ???

        return ret;
    }

    protected byte[] getBytesFromZippedSources() throws MojoExecutionException {

        buildLogger.addBuildLogEntry("Converting Zipped Sources to Byte Array");
        byte[] zipFileByte;
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(zipTempFile);
            zipFileByte = IOUtils.toByteArray(fileStream);
        } catch (Exception e) {
            throw new MojoExecutionException("Fail to Set Zipped File Into Project: " + e.getMessage(), e);//TODO EXCEPTIONS
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
        buildLogger.addBuildLogEntry("scanTimeoutInMinutes: " + config.getScanTimeoutInMinutes());
        buildLogger.addBuildLogEntry("outputDirectory: " + config.getOutputDirectory());
        buildLogger.addBuildLogEntry("fullTeamPath: " + config.getFullTeamPath());
        buildLogger.addBuildLogEntry("preset: " + config.getPreset());
        buildLogger.addBuildLogEntry("isIncrementalScan: " + config.isIncrementalScan());
        //buildLogger.addBuildLogEntry("folderExclusions: " +  Arrays.toString(folderExclusions));
        //buildLogger.addBuildLogEntry("fileExclusions: " +  Arrays.toString(fileExclusions));
        buildLogger.addBuildLogEntry("isSynchronous: " + config.isSynchronous());
        //buildLogger.addBuildLogEntry("generatePDFReport: " + generatePDFReport);
        buildLogger.addBuildLogEntry("thresholds enabled: " + config.isThresholdsEnabled());
        if (config.isSASTThresholdEnabled()) {
            buildLogger.addBuildLogEntry("highThreshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getHighThreshold()));
            buildLogger.addBuildLogEntry("mediumThreshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getMediumThreshold()));
            buildLogger.addBuildLogEntry("lowThreshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getLowThreshold()));
        }
        buildLogger.addBuildLogEntry("osaEnabled: " + config.isOsaEnabled());
        if (config.isOsaEnabled()) {
            buildLogger.addBuildLogEntry("osaExclusions: " + Arrays.toString(config.getOsaExclusions()));
            buildLogger.addBuildLogEntry("osaHighSeveritiesThreshold: " + (config.getOsaHighThreshold() == null ? "[No Threshold]" : config.getOsaHighThreshold()));
            buildLogger.addBuildLogEntry("osaMediumSeveritiesThreshold: " + (config.getOsaMediumThreshold() == null ? "[No Threshold]" : config.getOsaMediumThreshold()));
            buildLogger.addBuildLogEntry("osaLowSeveritiesThreshold: " + (config.getOsaLowThreshold() < 0 ? "[No Threshold]" : config.getOsaLowThreshold()));
            //buildLogger.addBuildLogEntry("osaGeneratePDFReport: " + osaGeneratePDFReport);
            // buildLogger.addBuildLogEntry("osaGenerateHTMLReport: " + osaGenerateHTMLReport);
        }
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }

    private void printResultsToConsole(ScanResults scanResults) {
        buildLogger.addBuildLogEntry("----------------------------Scan Results:-------------------------------");
        buildLogger.addBuildLogEntry("High Severity Results: " + scanResults.getHighSeverityResults());
        buildLogger.addBuildLogEntry("Medium Severity Results: " + scanResults.getMediumSeverityResults());
        buildLogger.addBuildLogEntry("Low Severity Results: " + scanResults.getLowSeverityResults());
        buildLogger.addBuildLogEntry("Info Severity Results: " + scanResults.getInfoSeverityResults());
        buildLogger.addBuildLogEntry("Scan Results Can Be Found at: " + scanResultsUrl);
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }

  /*  private void printOSAResultsToConsole(OSASummaryResults osaSummaryResults) {
        buildLogger.addBuildLogEntry("----------------------------Checkmarx Scan Results(CxOSA):-------------------------------");
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("------------------------");
        buildLogger.addBuildLogEntry("Vulnerabilities Summary:");
        buildLogger.addBuildLogEntry("------------------------");
        buildLogger.addBuildLogEntry("OSA High Severity Results: " + osaSummaryResults.getHighVulnerabilities());
        buildLogger.addBuildLogEntry("OSA Medium Severity Results: " + osaSummaryResults.getMediumVulnerabilities());
        buildLogger.addBuildLogEntry("OSA Low Severity Results: " + osaSummaryResults.getLowVulnerabilities());
        buildLogger.addBuildLogEntry("Vulnerability Score: " + osaSummaryResults.getVulnerabilityScore());
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("-----------------------");
        buildLogger.addBuildLogEntry("Libraries Scan Results:");
        buildLogger.addBuildLogEntry("-----------------------");
        buildLogger.addBuildLogEntry("Open Source Libraries: " + osaSummaryResults.getTotalLibraries());
        buildLogger.addBuildLogEntry("Vulnerable And Outdated: " + osaSummaryResults.getVulnerableAndOutdated());
        buildLogger.addBuildLogEntry("Vulnerable And Updated: " + osaSummaryResults.getVulnerableAndUpdated());
        buildLogger.addBuildLogEntry("Non Vulnerable Libraries: " + osaSummaryResults.getNonVulnerableLibraries());
        buildLogger.addBuildLogEntry("");
        buildLogger.addBuildLogEntry("OSA Scan Results Can Be Found at: " + projectStateLink.replace("Summary", "OSA"));
        buildLogger.addBuildLogEntry("------------------------------------------------------------------------");
    }*/

    private boolean assertVulnerabilities(ScanResults scanResults) throws TaskException { //TODO ask dor regards the taskException (without exception but with build.unSuccess())

        StringBuilder res = new StringBuilder("");
        boolean fail = false;

        fail |= isFail(scanResults.getHighSeverityResults(), config.getHighThreshold(), res, "High");
        fail |= isFail(scanResults.getMediumSeverityResults(), config.getMediumThreshold(), res, "Medium");
        fail |= isFail(scanResults.getLowSeverityResults(), config.getLowThreshold(), res, "Low");

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

    private boolean isFail(int result, Integer threshold, StringBuilder res, String severity) {
        boolean fail = false;
        if (threshold != null && result > threshold) {
            res.append(severity + " Severity Results are Above Threshold. Results: ").append(result).append(". Threshold: ").append(threshold).append("\n");
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
        String combinedFilterPattern = folderPattern.generatePattern(this.configurationMap, this.buildLogger, CxParam.OSA_EXCLUSIONS, false);
        CxZip cxZip = new CxZip();
        return cxZip.zipSourceCode(getWorkspace(), combinedFilterPattern);
    }

}



/*

    */
/* automatically injected by Bamboo *//*

    public void setCustomVariableContext(CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }*/
