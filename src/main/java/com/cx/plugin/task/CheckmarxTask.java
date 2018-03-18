package com.cx.plugin.task;

/**
 * Created by galn on 18/12/2016.
 */

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.cx.client.ConsoleScanWaitHandler;
import com.cx.client.CxClientService;
import com.cx.client.CxClientServiceImpl;
import com.cx.client.OSAConsoleScanWaitHandler;
import com.cx.client.dto.CreateScanResponse;
import com.cx.client.dto.LocalScanConfiguration;
import com.cx.client.dto.ReportType;
import com.cx.client.dto.ScanResults;
import com.cx.client.exception.CxClientException;
import com.cx.client.osa.dto.*;
import com.cx.plugin.dto.CxAbortException;
import com.cx.plugin.dto.CxResultsConst;
import com.cx.plugin.dto.CxScanConfig;
import com.cx.plugin.dto.CxXMLResults;
import com.cx.plugin.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.whitesource.fs.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.cx.client.CxPluginHelper.*;
import static com.cx.plugin.utils.CxEncryption.decrypt;
import static com.cx.plugin.utils.CxFileUtils.deleteTempFiles;
import static com.cx.plugin.utils.CxParam.MAX_ZIP_SIZE_BYTES;
import static com.cx.plugin.utils.CxParam.TEMP_FILE_NAME_TO_ZIP;
import static com.cx.plugin.utils.CxPrintUtils.*;
import static com.cx.plugin.utils.CxReportsUtils.*;
import static com.cx.plugin.utils.CxZipUtils.getBytesFromZippedSources;


public class CheckmarxTask implements TaskType {

    private CxClientService cxClientService;
    protected java.net.URL url;
    private File workDirectory;
    private String projectStateLink;
    private String osaProjectSummaryLink;
    private CxScanConfig config;
    private CxLoggerAdapter loggerAdapter;

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.newBuilder(taskContext);
        BuildLogger buildLogger = taskContext.getBuildLogger();
        loggerAdapter = new CxLoggerAdapter(buildLogger);
        CxConfigUtil configUtil = new CxConfigUtil();
        CxResultUtils resultUtils = new CxResultUtils();
        BuildContext buildContext = taskContext.getBuildContext();
        String buildId = buildContext.getBuildKey().getKey();
        Map<String, String> results = new HashMap<String, String>();
        HashMap<String, String> configurationMap = configUtil.resolveConfigurationMap(taskContext.getConfigurationMap(), loggerAdapter);
        workDirectory = taskContext.getWorkingDirectory();
        ScanResults scanResults = null;
        CreateScanResponse createScanResponse = null;
        OSASummaryResults osaSummaryResults = null;
        OSAScanStatus osaScanStatus;
        CxClientException osaException = null;
        Exception sastWaitException = null;
        String scanResultsUrl = "";
        try {
            config = new CxScanConfig(configurationMap);
            results.put(CxResultsConst.SAST_SYNC_MODE, String.valueOf(config.isSynchronous()));
            url = new URL(config.getUrl());
            printConfiguration(config, loggerAdapter);

            loggerAdapter.info("-----------------------------------Create CxSAST Scan:------------------------------------");
            //initialize cx client
            loggerAdapter.info("Initializing Cx client");
            cxClientService = new CxClientServiceImpl(url, config.getUsername(), decrypt(config.getPassword()));
            cxClientService.setLogger(loggerAdapter);

            cxClientService.checkServerConnectivity();
            //perform login to server
            loggerAdapter.info("Logging into the Checkmarx service.");
            cxClientService.loginToServer();

            if (config.isDenyProject()) {
                String projectName = config.getProjectName();
                if (cxClientService.isNewProject(projectName, config.getFullTeamPath())) {
                    StringBuilder str = new StringBuilder("Creation of the new project [" + projectName + "] is not authorized. Please use an existing project.");
                    str.append("\nYou can enable the creation of new projects by disabling the Deny new Checkmarx projects creation checkbox in the Checkmarx plugin global settings.\n");
                    printBuildFailure(str, null, null, loggerAdapter);
                    return taskResultBuilder.failed().build();
                }
            }

            //prepare sources (zip it) and send it to scan
            createScanResponse = createScan();
            //create OSA Scan
            CreateOSAScanResponse osaScan = null;

            if (config.isOsaEnabled()) {
                try {
                    loggerAdapter.info("------------------------------------Create CxOSA Scan:------------------------------------");
                    osaScan = createOSAScan(createScanResponse, buildId, buildLogger);

                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    loggerAdapter.error("Fail to create OSA Scan: " + e.getMessage());
                    osaException = new CxClientException(e);
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
                loggerAdapter.info("------------------------------------Get CxSAST Results:-----------------------------------");
                //wait for SAST scan to finish
                ConsoleScanWaitHandler consoleScanWaitHandler = new ConsoleScanWaitHandler();
                consoleScanWaitHandler.setLogger(loggerAdapter);
                loggerAdapter.info("Waiting for CxSAST scan to finish.");
                cxClientService.waitForScanToFinish(createScanResponse.getRunId(), config.getScanTimeoutInMinutes(), consoleScanWaitHandler);
                loggerAdapter.info("Scan finished. Retrieving scan results");
                //retrieve SAST scan results
                scanResults = cxClientService.retrieveScanResults(createScanResponse.getProjectId());
                scanResultsUrl = composeScanLink(url.toString(), scanResults);
                printResultsToConsole(scanResults, loggerAdapter, scanResultsUrl);

                //SAST detailed report
                byte[] cxReport = cxClientService.getScanReport(scanResults.getScanID(), ReportType.XML);
                CxXMLResults reportObj = resultUtils.convertToXMLResult(cxReport);

                scanResults.setScanDetailedReport(reportObj);
                resultUtils.addSASTResults(results, scanResults, config, projectStateLink, scanResultsUrl);

                if (config.isGeneratePDFReport()) {
                    createPDFReport(scanResults.getScanID(), getWorkspace(buildContext.getCheckoutLocation()), loggerAdapter, cxClientService);
                }

            } catch (CxClientException e) {
                loggerAdapter.error(" Failed to perform CxSAST scan: " + e.getMessage());
                sastWaitException = new CxClientException(" Failed to perform CxSAST scan: ", e);
            } catch (InterruptedException e) {
                throw e;

            } catch (Exception e) {
                loggerAdapter.error("Fail to perform CxSAST scan: " + e.getMessage(), e);
                sastWaitException = new Exception("Fail to perform CxSAST scan: ", e);
            }

            //OSA results
            if (config.isOsaEnabled()) {
                try {
                    if (osaException != null) {
                        throw osaException;
                    }
                    loggerAdapter.info("-------------------------------------Get CxOSA Results:-----------------------------------");
                    //wait for OSA scan to finish
                    OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
                    osaConsoleScanWaitHandler.setLogger(loggerAdapter);
                    loggerAdapter.info("Waiting for OSA scan to finish");
                    osaScanStatus = cxClientService.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler);
                    loggerAdapter.info("OSA scan finished successfully");

                    //retrieve OSA scan results
                    loggerAdapter.info("OSA scan finished. Retrieving OSA scan results");
                    osaSummaryResults = cxClientService.retrieveOSAScanSummaryResults(osaScan.getScanId());
                    printOSAResultsToConsole(osaSummaryResults, loggerAdapter, osaProjectSummaryLink);
                    resultUtils.addOSAResults(results, osaSummaryResults, config, osaProjectSummaryLink);
                    resultUtils.addOSAStatus(results, osaScanStatus);

                    loggerAdapter.info("Creating OSA reports");
                    String osaScanId = osaScan.getScanId();

                    //OSA json reports
                    createOSASummaryJsonReport(workDirectory, loggerAdapter, osaSummaryResults);
                    List<Library> libraries = createOSALibrariesJsonReport(workDirectory, osaScanId, loggerAdapter, cxClientService);
                    List<CVE> osaVulnerabilities = createOSAVulnerabilitiesJsonReport(workDirectory, osaScanId, loggerAdapter, cxClientService);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String osaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(osaVulnerabilities);
                    String osaLibrariesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(libraries);
                    resultUtils.addOsaCveAndLibLists(results, osaJson, osaLibrariesJson);

                } catch (Exception e) {
                    osaException = new CxClientException(e);
                    throw osaException;
                }
            }
            if (sastWaitException != null) {
                throw sastWaitException;
            }

        } catch (MalformedURLException e) {
            loggerAdapter.error("Invalid URL: " + config.getUrl() + ". Exception message: " + e.getMessage(), e);
            osaException = new CxClientException(e);

        } catch (CxClientException e) { //TODO redesign the exceptions
            loggerAdapter.error(e.getMessage(), e);

            if (osaException == null && sastWaitException == null) {
                sastWaitException = e;
            }

        } catch (NumberFormatException e) {
            loggerAdapter.error("Invalid preset id: " + e.getMessage(), e);
            osaException = new CxClientException(e);

        } catch (InterruptedException e) {
            buildLogger.addErrorLogEntry("Interrupted exception: " + e.getMessage(), e);

            if (cxClientService != null && createScanResponse != null) {
                loggerAdapter.error("Canceling scan on the Checkmarx server...");
                cxClientService.cancelScan(createScanResponse.getRunId());
            }
            throw new TaskException(e.getMessage());

        } catch (IllegalArgumentException e) {
            String errMsg = "";
            if (e.getMessage().contains("interface com.sun.xml.internal.ws.developer.WSBindingProvider is not visible from class loader")) {
                printAgentConfigError(loggerAdapter);
                errMsg = "Agent was not was not configured properly: ";
            }

            throw new IllegalArgumentException(errMsg, e);

        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Unexpected exception: " + e.getMessage(), e);
            throw new TaskException(e.getMessage());
        } finally {
            deleteTempFiles(loggerAdapter, TEMP_FILE_NAME_TO_ZIP);
            closeClient(cxClientService);
        }

        buildContext.getBuildResult().getCustomBuildData().putAll(results);

        //assert if expected exception is thrown  OR when vulnerabilities under threshold
        StringBuilder res = new StringBuilder("");
        if (configUtil.assertVulnerabilities(scanResults, osaSummaryResults, res, config) || sastWaitException != null || osaException != null) {
            printBuildFailure(res, sastWaitException, osaException, loggerAdapter);
            return taskResultBuilder.failed().build();
        }

        return taskResultBuilder.success().build();
    }

    private void closeClient(CxClientService cxClientService) {
        if (cxClientService != null) {
            cxClientService.close();
        }
    }

    private CreateScanResponse createScan() throws CxClientException, TaskException, IOException, InterruptedException {
        CxZipUtils zipUtils = new CxZipUtils();
        CxConfigUtil configUtil = new CxConfigUtil();
        //prepare sources to scan (zip them)
        loggerAdapter.info("Zipping sources");
        File zipTempFile = zipUtils.zipWorkspaceFolder(workDirectory.getPath(), config.getFolderExclusions(), config.getFilterPattern(), MAX_ZIP_SIZE_BYTES, true, loggerAdapter);

        //send sources to scan
        byte[] zippedSources = getBytesFromZippedSources(zipTempFile, loggerAdapter);
        LocalScanConfiguration conf = configUtil.generateScanConfiguration(zippedSources, config);
        CreateScanResponse createScanResponse = cxClientService.createLocalScan(conf);
        projectStateLink = composeProjectStateLink(url.toString(), createScanResponse.getProjectId());
        loggerAdapter.info("Scan created successfully. Link to project state: " + projectStateLink);

        if (zipTempFile.exists() && !zipTempFile.delete()) {
            loggerAdapter.info("Warning: failed to delete temporary zip file: " + zipTempFile.getAbsolutePath());
        } else {
            loggerAdapter.info("Temporary file deleted");
        }
        return createScanResponse;
    }

    private CreateOSAScanResponse createOSAScan(CreateScanResponse createScanResponse, String buildId, BuildLogger buildLogger) throws IOException, InterruptedException, CxClientException {
        loggerAdapter.info("Creating OSA scan");
        loggerAdapter.info("Scanning for CxOSA compatible files");

        String osaFilterPattern = StringUtils.isEmpty(config.getOsaFilterPattern()) ? " !**/Checkmarx/Reports/**" : config.getOsaFilterPattern() + ", !**/Checkmarx/Reports/**";
        Properties scannerProperties = generateOSAScanConfiguration(osaFilterPattern,
                config.getOsaArchiveIncludePatterns(),
                workDirectory.getAbsolutePath(), config.isOsaInstallBeforeScan());

        //we do this in order to redirect the logs from the filesystem agent component to the build console
        String appenderName = "cxAppender_" + buildId;
        Logger.getRootLogger().addAppender(new CxAppender(buildLogger, appenderName));

        ComponentScan componentScan = new ComponentScan(scannerProperties);
        String osaDependenciesJson;
        try {
            osaDependenciesJson = componentScan.scan();
        } finally {
            Logger.getRootLogger().removeAppender(appenderName);
        }

        writeOsaDependenciesJson(osaDependenciesJson, workDirectory, loggerAdapter);
        loggerAdapter.info("Sending OSA scan request");
        CreateOSAScanResponse osaScan = cxClientService.createOSAScan(createScanResponse.getProjectId(), osaDependenciesJson);
        osaProjectSummaryLink = composeProjectOSASummaryLink(config.getUrl(), createScanResponse.getProjectId());
        loggerAdapter.info("OSA scan created successfully");

        return osaScan;
    }


    private String getWorkspace(Map<Long, String> checkoutLocation) throws CxAbortException {
        if (checkoutLocation.isEmpty()) {
            throw new CxAbortException("No source code repository found");
        }
        if (checkoutLocation.size() > 1) {
            loggerAdapter.warn("Warning: more than one workspace found. Using the first one.");
        }
        return checkoutLocation.entrySet().iterator().next().getValue();
    }
}
