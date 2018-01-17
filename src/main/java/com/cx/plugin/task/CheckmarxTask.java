package com.cx.plugin.task;

/**
 * Created by galn on 18/12/2016.
 */

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.cx.client.*;
import com.cx.client.dto.CreateScanResponse;
import com.cx.client.dto.LocalScanConfiguration;
import com.cx.client.dto.ReportType;
import com.cx.client.dto.ScanResults;
import com.cx.client.exception.CxClientException;
import com.cx.client.osa.dto.*;
import com.cx.client.osa.utils.OSAScanner;
import com.cx.plugin.dto.CxAbortException;
import com.cx.plugin.dto.CxResultsConst;
import com.cx.plugin.dto.CxScanConfig;
import com.cx.plugin.dto.CxXMLResults;
import com.cx.plugin.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cx.plugin.dto.CxParam.MAX_ZIP_SIZE_BYTES;
import static com.cx.plugin.dto.CxParam.TEMP_FILE_NAME_TO_ZIP;


public class CheckmarxTask implements TaskType {

    public final Logger log = LoggerFactory.getLogger(CheckmarxTask.class);

    private CxClientService cxClientService;
    protected java.net.URL url;
    private File workDirectory;
    private File zipTempFile;
    private String projectStateLink;
    private String osaProjectSummaryLink;
    private CxScanConfig config;
    private String scanResultsUrl;
    private BuildLogger buildLogger;
    private CxLoggerAdapter loggerAdapter;
    private HashMap<String, String> configurationMap;
    private BuildContext buildContext;

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {


        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.newBuilder(taskContext);
        buildLogger = taskContext.getBuildLogger();
        loggerAdapter = new CxLoggerAdapter(buildLogger);

        CxPluginHelper pluginHelper = new CxPluginHelper();
        CxEncryption encryption = new CxEncryption();
        CxResultUtils resultUtils = new CxResultUtils();
        CxConfigUtil configUtil = new CxConfigUtil();
        CxPrintUtils printUtils = new CxPrintUtils();
        CxReportsUtils reportsUtils = new CxReportsUtils();

        buildContext = taskContext.getBuildContext();
        Map<String, String> results = new HashMap<String, String>();
        configurationMap = configUtil.resolveConfigurationMap(taskContext.getConfigurationMap(), loggerAdapter);//resolve the global configuration properties
        workDirectory = taskContext.getWorkingDirectory();
        ScanResults scanResults = null;
        CreateScanResponse createScanResponse = null;
        OSASummaryResults osaSummaryResults = null;
        OSAScanStatus osaScanStatus;
        Exception osaException = null;
        Exception sastWaitException = null;

        try {
            config = new CxScanConfig(configurationMap);
            results.put(CxResultsConst.SAST_SYNC_MODE, String.valueOf(config.isSynchronous()));
            url = new URL(config.getUrl());
            printUtils.printConfiguration(config, loggerAdapter);

            //initialize cx client
            loggerAdapter.info("Initializing Cx client");
            cxClientService = new CxClientServiceImpl(url, config.getUsername(), encryption.decrypt(config.getPassword()));
            cxClientService.setLogger(loggerAdapter);

            cxClientService.checkServerConnectivity();
            //perform login to server
            loggerAdapter.info("Logging into the Checkmarx service.");
            cxClientService.loginToServer();

            if (config.isDenyProject()) {
                String projectName = config.getProjectName();
                if (cxClientService.isNewProject(projectName, config.getFullTeamPath()))
                {
                    StringBuilder str = new StringBuilder("Creation of the new project [" + projectName + "] is not authorized. Please use an existing project.");
                    str.append("\nYou can enable the creation of new projects by disabling the Deny new Checkmarx projects creation checkbox in the Checkmarx plugin global settings.\n");
                    printUtils.printBuildFailure(str, null, null, loggerAdapter, log);
                    return taskResultBuilder.failed().build();
                }
            }

            //prepare sources (zip it) and send it to scan
            createScanResponse = createScan();

            //create OSA Scan
            CreateOSAScanResponse osaScan = null;

            if (config.isOsaEnabled()) {
                try {

                    osaScan = createOSAScan(createScanResponse);

                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    buildLogger.addErrorLogEntry("Fail to create OSA Scan: " + e.getMessage());
                    log.error("Fail to create OSA Scan: " + e.getMessage(), e);
                    osaException = e;
                }
            }
            //Asynchronous MODE
            if (!config.isSynchronous()) {
                if (osaException != null) {
                    throw osaException;
                }
                loggerAdapter.info("Running in Asynchronous mode. Not waiting for scan to finish");
                results.put(CxResultsConst.SAST_SUMMARY_RESULTS_LINK, StringUtils.defaultString(projectStateLink));
                buildContext.getBuildResult().getCustomBuildData().putAll(results);
                return taskResultBuilder.success().build();
            }

            //SAST Results
            try {
                //wait for SAST scan to finish
                ConsoleScanWaitHandler consoleScanWaitHandler = new ConsoleScanWaitHandler();
                consoleScanWaitHandler.setLogger(loggerAdapter);
                loggerAdapter.info("Waiting for CxSAST scan to finish.");
                cxClientService.waitForScanToFinish(createScanResponse.getRunId(), config.getScanTimeoutInMinutes(), consoleScanWaitHandler);
                loggerAdapter.info("Scan finished. Retrieving scan results");
                //retrieve SAST scan results
                scanResults = cxClientService.retrieveScanResults(createScanResponse.getProjectId());
                scanResultsUrl = pluginHelper.composeScanLink(url.toString(), scanResults);
                printUtils.printResultsToConsole(scanResults, loggerAdapter, scanResultsUrl);

                //SAST detailed report
                byte[] cxReport = cxClientService.getScanReport(scanResults.getScanID(), ReportType.XML);
                CxXMLResults reportObj = resultUtils.convertToXMLResult(cxReport);

                scanResults.setScanDetailedReport(reportObj);
                resultUtils.addSASTResults(results, scanResults, config, projectStateLink, scanResultsUrl);

                if (config.isGeneratePDFReport()) {
                    reportsUtils.createPDFReport(scanResults.getScanID(), getWorkspace(), loggerAdapter, cxClientService);
                }

            } catch (CxClientException e) {
                buildLogger.addErrorLogEntry("Fail to perform SAST scan: " + e.getMessage());
                log.error(e.getMessage(), e);
                sastWaitException = new CxClientException("Fail to perform SAST scan: ", e);
            } catch (InterruptedException e) {
                throw e;

            } catch (Exception e) {
                buildLogger.addErrorLogEntry("Fail to perform CxSAST scan: " + e.getMessage());
                log.error("Fail to perform CxSAST scan: " + e.getMessage(), e);
                sastWaitException = new Exception("Fail to perform CxSAST scan: ", e);
            }

            //OSA results
            if (config.isOsaEnabled()) {
                try {
                    if (osaException != null) {
                        throw osaException;
                    }
                    //wait for OSA scan to finish
                    OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
                    osaConsoleScanWaitHandler.setLogger(loggerAdapter);
                    loggerAdapter.info("Waiting for OSA scan to finish");
                    osaScanStatus = cxClientService.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler);
                    loggerAdapter.info("OSA scan finished successfully");

                    //retrieve OSA scan results
                    loggerAdapter.info("OSA scan finished. Retrieving OSA scan results");
                    osaSummaryResults = cxClientService.retrieveOSAScanSummaryResults(osaScan.getScanId());
                    printUtils.printOSAResultsToConsole(osaSummaryResults, loggerAdapter, osaProjectSummaryLink);
                    resultUtils.addOSAResults(results, osaSummaryResults, config, osaProjectSummaryLink);
                    resultUtils.addOSAStatus(results, osaScanStatus);

                    loggerAdapter.info("Creating OSA reports");
                    String osaScanId = osaScan.getScanId();
                    //OSA PDF report
                    reportsUtils.createOSAPDFReport(workDirectory, osaScanId, loggerAdapter, cxClientService);

                    //OSA HTML report
                    reportsUtils.createOSAHTMLReport(workDirectory, osaScanId, loggerAdapter, cxClientService);

                    //OSA json reports
                    reportsUtils.createOSASummaryJsonReport(workDirectory, loggerAdapter, osaSummaryResults);
                    List<Library> libraries = reportsUtils.createOSALibrariesJsonReport(workDirectory, osaScanId, loggerAdapter, cxClientService);
                    List<CVE> osaVulnerabilities = reportsUtils.createOSAVulnerabilitiesJsonReport(workDirectory, osaScanId, loggerAdapter, cxClientService);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String osaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(osaVulnerabilities);
                    String osaLibrariesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(libraries);
                    resultUtils.addOsaCveAndLibLists(results, osaJson, osaLibrariesJson);

                } catch (Exception e) {
                    osaException = e;
                    throw osaException;
                }
            }
            if (sastWaitException != null) {
                throw sastWaitException;
            }

        } catch (MalformedURLException e) {
            log.error("Invalid URL: " + config.getUrl() + ". Exception message: " + e.getMessage(), e);
            osaException = e;

        } catch (CxClientException e) { //TODO redesign the exceptions
            log.error(e.getMessage(), e);

            if (osaException == null && sastWaitException == null) {
                sastWaitException = e;
            }

        } catch (NumberFormatException e) {
            log.error("Invalid preset id: " + e.getMessage(), e);
            osaException = e;

        } catch (InterruptedException e) {
            buildLogger.addErrorLogEntry("Interrupted exception: " + e.getMessage());
            log.error("Interrupted exception: " + e.getMessage(), e);

            if (cxClientService != null && createScanResponse != null) {
                log.error("Canceling scan on the Checkmarx server...");
                cxClientService.cancelScan(createScanResponse.getRunId());
            }
            throw new TaskException(e.getMessage());

        } catch (IllegalArgumentException e) {
            String errMsg = "";
            if (e.getMessage().contains("interface com.sun.xml.internal.ws.developer.WSBindingProvider is not visible from class loader")) {
                printUtils.printAgentConfigError(loggerAdapter);
                errMsg = "Agent was not was not configured properly: ";
            }

            throw new IllegalArgumentException(errMsg, e);

        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Unexpected exception: " + e.toString());
            log.error("Unexpected exception: " + e.getMessage(), e);
            throw new TaskException(e.getMessage());
        } finally {
            CxFileUtils fileUtils = new CxFileUtils();
            fileUtils.deleteTempFiles(loggerAdapter, TEMP_FILE_NAME_TO_ZIP);
            closeClient(cxClientService);
        }

        buildContext.getBuildResult().getCustomBuildData().putAll(results);

        //assert if expected exception is thrown  OR when vulnerabilities under threshold
        StringBuilder res = new StringBuilder("");
        if (configUtil.assertVulnerabilities(scanResults, osaSummaryResults, res, config) || sastWaitException != null || osaException != null) {
            printUtils.printBuildFailure(res, sastWaitException, osaException, loggerAdapter, log);
            return taskResultBuilder.failed().build();
        }

        return taskResultBuilder.success().build();
    }

    private void closeClient(CxClientService cxClientService) {
        if (cxClientService != null) {
            try {
                cxClientService.close();
            } catch (Exception e) {
            }
        }
    }

    private CreateScanResponse createScan() throws CxClientException, TaskException, IOException, InterruptedException {
        CxPluginHelper pluginHelper = new CxPluginHelper();
        CxZipUtils zipUtils = new CxZipUtils();
        CxConfigUtil configUtil = new CxConfigUtil();
        //prepare sources to scan (zip them)
        loggerAdapter.info("Zipping sources");
        zipTempFile = zipUtils.zipWorkspaceFolder(workDirectory.getPath(), config.getFolderExclusions(), config.getFilterPattern(), MAX_ZIP_SIZE_BYTES, true, loggerAdapter);

        //send sources to scan
        byte[] zippedSources = zipUtils.getBytesFromZippedSources(zipTempFile, loggerAdapter);
        LocalScanConfiguration conf = configUtil.generateScanConfiguration(zippedSources, config);
        CreateScanResponse createScanResponse = cxClientService.createLocalScan(conf);
        projectStateLink = pluginHelper.composeProjectStateLink(url.toString(), createScanResponse.getProjectId());
        loggerAdapter.info("Scan created successfully. Link to project state: " + projectStateLink);

        if (zipTempFile.exists() && !zipTempFile.delete()) {
            loggerAdapter.info("Warning: failed to delete temporary zip file: " + zipTempFile.getAbsolutePath());
        } else {
            loggerAdapter.info("Temporary file deleted");
        }
        return createScanResponse;
    }

    private CreateOSAScanResponse createOSAScan(CreateScanResponse createScanResponse) throws IOException, InterruptedException, CxClientException {
        CxPluginHelper pluginHelper = new CxPluginHelper();
        loggerAdapter.info("Creating OSA scan");
        loggerAdapter.info("Scanning for CxOSA compatible files");
        String osaPattern = config.getOsaFilterPattern() + ", !**/Checkmarx/Reports/**";
        OSAScanner osaScanner = new OSAScanner(osaPattern, config.getOsaArchiveIncludePatterns(), loggerAdapter);
        List<OSAFile> osaFiles = osaScanner.scanFiles(workDirectory, FileUtils.getTempDirectory(), 4);
        loggerAdapter.info("Found " + osaFiles.size() + " Compatible Files for OSA Scan");
        writeToOsaListToTemp(osaFiles);
        loggerAdapter.info("Sending OSA scan request");
        CreateOSAScanResponse osaScan = cxClientService.createOSAScan(createScanResponse.getProjectId(), osaFiles);
        osaProjectSummaryLink = pluginHelper.composeProjectOSASummaryLink(config.getUrl(), createScanResponse.getProjectId());
        loggerAdapter.info("OSA scan created successfully");

        return osaScan;
    }


    private void writeToOsaListToTemp(List<OSAFile> osaFileList) {
        try {
            File temp = new File(FileUtils.getTempDirectory(), "CxOSAFileList.json");
            ObjectMapper om = new ObjectMapper();
            om.writeValue(temp, osaFileList);
            loggerAdapter.info("OSA file list saved to file: ["+temp.getAbsolutePath()+"]");
        } catch (Exception e) {
            loggerAdapter.info("Failed to write OSA file list to temp directory: " + e.getMessage());
        }

    }


    private String getWorkspace() throws CxAbortException {
        final Map<Long, String> checkoutLocations = this.buildContext.getCheckoutLocation();
        if (checkoutLocations.isEmpty()) {
            throw new CxAbortException("No source code repository found");
        }
        if (checkoutLocations.size() > 1) {
            loggerAdapter.warn("Warning: more than one workspace found. Using the first one.");
        }
        return checkoutLocations.entrySet().iterator().next().getValue();
    }

}
