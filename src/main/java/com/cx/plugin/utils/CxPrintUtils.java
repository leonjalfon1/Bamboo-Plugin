package com.cx.plugin.utils;

import com.cx.client.dto.ScanResults;
import com.cx.client.osa.dto.OSASummaryResults;
import com.cx.plugin.dto.CxScanConfig;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by Galn on 24/10/2017.
 */
public class CxPrintUtils {
    public static void printConfiguration(CxScanConfig config, CxLoggerAdapter buildLoggerAdapter) throws IOException {
        buildLoggerAdapter.info("----------------------------Configurations:-----------------------------");
        buildLoggerAdapter.info("Bamboo plugin version: " + config.getVersion());
        buildLoggerAdapter.info("Username: " + config.getUsername());
        buildLoggerAdapter.info("URL: " + config.getUrl());
        buildLoggerAdapter.info("Project name: " + config.getProjectName());
        buildLoggerAdapter.info("Deny project creation: " + config.isDenyProject());
        buildLoggerAdapter.info("Scan timeout in minutes: " + (config.getScanTimeoutInMinutes() <= 0 ? "" : config.getScanTimeoutInMinutes()));
        buildLoggerAdapter.info("Full team path: " + config.getFullTeamPath());
        buildLoggerAdapter.info("Preset: " + config.getPresetName());
        buildLoggerAdapter.info("Is incremental scan: " + config.isIncremental());
        buildLoggerAdapter.info("Is interval full scans enabled: " + config.isIntervals());
        if (config.isIntervals()) {
            buildLoggerAdapter.info("Interval- begins: " + config.getIntervalBegins());
            buildLoggerAdapter.info("Interval- ends: " + config.getIntervalEnds());
            String forceScan = config.isForceFullScan() ? "" : "NOT ";
            buildLoggerAdapter.info("Override full scan: " + config.isForceFullScan() + " (Interval based full scan " + forceScan + "activated.)");
        }
        buildLoggerAdapter.info("Folder exclusions: " + (config.getFolderExclusions()));
        buildLoggerAdapter.info("Is synchronous scan: " + config.isSynchronous());
        buildLoggerAdapter.info("Generate PDF report: " + config.isGeneratePDFReport());
        buildLoggerAdapter.info("CxSAST thresholds enabled: " + config.isThresholdsEnabled());
        if (config.isThresholdsEnabled()) {
            buildLoggerAdapter.info("CxSAST high threshold: " + (config.getHighThreshold() == null ? "[No Threshold]" : config.getHighThreshold()));
            buildLoggerAdapter.info("CxSAST medium threshold: " + (config.getMediumThreshold() == null ? "[No Threshold]" : config.getMediumThreshold()));
            buildLoggerAdapter.info("CxSAST low threshold: " + (config.getLowThreshold() == null ? "[No Threshold]" : config.getLowThreshold()));
        }
        buildLoggerAdapter.info("CxOSA enabled: " + config.isOsaEnabled());
        if (config.isOsaEnabled()) {
            buildLoggerAdapter.info("CxOSA filter patterns: " + config.getOsaFilterPattern());
            buildLoggerAdapter.info("CxOSA archive include patterns: " + config.getOsaArchiveIncludePatterns());
            buildLoggerAdapter.info("CxOSA thresholds enabled: " + config.isOsaThresholdsEnabled());
            if (config.isOsaThresholdsEnabled()) {
                buildLoggerAdapter.info("CxOSA high threshold: " + (config.getOsaHighThreshold() == null ? "[No Threshold]" : config.getOsaHighThreshold()));
                buildLoggerAdapter.info("CxOSA medium threshold: " + (config.getOsaMediumThreshold() == null ? "[No Threshold]" : config.getOsaMediumThreshold()));
                buildLoggerAdapter.info("CxOSA low threshold: " + (config.getOsaLowThreshold() == null ? "[No Threshold]" : config.getOsaLowThreshold()));
            }
        }
        buildLoggerAdapter.info("------------------------------------------------------------------------");
    }

    public static void printResultsToConsole(ScanResults scanResults, CxLoggerAdapter buildLoggerAdapter, String scanResultsUrl) {
        buildLoggerAdapter.info("----------------------------Checkmarx Scan Results(CxSAST):-------------------------------");
        buildLoggerAdapter.info("High severity results: " + scanResults.getHighSeverityResults());
        buildLoggerAdapter.info("Medium severity results: " + scanResults.getMediumSeverityResults());
        buildLoggerAdapter.info("Low severity results: " + scanResults.getLowSeverityResults());
        buildLoggerAdapter.info("Info severity results: " + scanResults.getInfoSeverityResults());
        buildLoggerAdapter.info("");
        buildLoggerAdapter.info("Scan results location: " + scanResultsUrl);
        buildLoggerAdapter.info("------------------------------------------------------------------------------------------\n");
    }

    public static void printOSAResultsToConsole(OSASummaryResults osaSummaryResults, CxLoggerAdapter buildLoggerAdapter, String osaProjectSummaryLink) {
        buildLoggerAdapter.info("----------------------------Checkmarx Scan Results(CxOSA):-------------------------------");
        buildLoggerAdapter.info("");
        buildLoggerAdapter.info("------------------------");
        buildLoggerAdapter.info("Vulnerabilities Summary:");
        buildLoggerAdapter.info("------------------------");
        buildLoggerAdapter.info("OSA high severity results: " + osaSummaryResults.getTotalHighVulnerabilities());
        buildLoggerAdapter.info("OSA medium severity results: " + osaSummaryResults.getTotalMediumVulnerabilities());
        buildLoggerAdapter.info("OSA low severity results: " + osaSummaryResults.getTotalLowVulnerabilities());
        buildLoggerAdapter.info("Vulnerability score: " + osaSummaryResults.getVulnerabilityScore());
        buildLoggerAdapter.info("");
        buildLoggerAdapter.info("-----------------------");
        buildLoggerAdapter.info("Libraries Scan Results:");
        buildLoggerAdapter.info("-----------------------");
        buildLoggerAdapter.info("Open-source libraries: " + osaSummaryResults.getTotalLibraries());
        buildLoggerAdapter.info("Vulnerable and outdated: " + osaSummaryResults.getVulnerableAndOutdated());
        buildLoggerAdapter.info("Vulnerable and updated: " + osaSummaryResults.getVulnerableAndUpdated());
        buildLoggerAdapter.info("Non-vulnerable libraries: " + osaSummaryResults.getNonVulnerableLibraries());
        buildLoggerAdapter.info("");
        buildLoggerAdapter.info("OSA scan results location: " + osaProjectSummaryLink);
        buildLoggerAdapter.info("-----------------------------------------------------------------------------------------");
    }

    public static void printBuildFailure(StringBuilder res, Exception sastBuildFailException, Exception osaBuildFailException, CxLoggerAdapter buildLoggerAdapter, Logger log) {
        buildLoggerAdapter.error("********************************************");
        buildLoggerAdapter.error(" The Build Failed for the Following Reasons: ");
        buildLoggerAdapter.error("********************************************");

        if (sastBuildFailException != null) {
            buildLoggerAdapter.error(sastBuildFailException.getMessage() + (sastBuildFailException.getCause() == null ? "" : sastBuildFailException.getCause().getMessage()));
        }
        if (osaBuildFailException != null) {
            buildLoggerAdapter.error(osaBuildFailException.getMessage() + (osaBuildFailException.getCause() == null ? "" : osaBuildFailException.getCause().getMessage()));
        }

        String[] lines = res.toString().split("\\n");
        for (String s : lines) {
            buildLoggerAdapter.error(s);
            log.info(s);
        }
        buildLoggerAdapter.error("-----------------------------------------------------------------------------------------\n");
        buildLoggerAdapter.error("");
    }

    public static void printAgentConfigError(CxLoggerAdapter buildLoggerAdapter) {
        buildLoggerAdapter.error("");
        buildLoggerAdapter.error("*****************************************************************************************************************************************");
        buildLoggerAdapter.error("|  Please add the system property:                                                                                                      |");
        buildLoggerAdapter.error("|  -Datlassian.org.osgi.framework.bootdelegation=javax.servlet,javax.servlet.*,sun.*,com.sun.*,org.w3c.dom.*,org.apache.xerces.         |");
        buildLoggerAdapter.error("|  to the agent run command and restart it.                                                                                             |");
        buildLoggerAdapter.error("|                                                                                                                                       |");
        buildLoggerAdapter.error("|  if the agent is run via wrapper, add the property to the the wrapper.conf file:                                                      |");
        buildLoggerAdapter.error("|  wrapper.java.additional.3=-Datlassian.org.osgi.framework.bootdelegation=javax.servlet,javax.servlet.*,sun.*,com.sun.*,org.w3c.dom.*  |");
        buildLoggerAdapter.error("|  and restart it                                                                                                                       |");
        buildLoggerAdapter.error("|                                                                                                                                       |");
        buildLoggerAdapter.error("|  Please refer to the documentation: https://checkmarx.atlassian.net/wiki/spaces/KC/pages/127859917/Configuring+Remote+Agent+Support   |");
        buildLoggerAdapter.error("*****************************************************************************************************************************************");
        buildLoggerAdapter.error("");
    }

}
