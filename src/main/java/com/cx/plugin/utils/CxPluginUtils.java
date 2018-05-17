package com.cx.plugin.utils;

import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.cx.plugin.dto.ScanResults;
import com.cx.restclient.configuration.CxScanConfig;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Created by Galn on 24/10/2017.
 */

public abstract class CxPluginUtils {

    public static void printConfiguration(CxScanConfig config, CxConfigHelper configBFF, CxLoggerAdapter log) {
        log.info("---------------------------------------Configurations:------------------------------------");
        log.info("Bamboo plugin version: " + configBFF.getPluginVersion());
        log.info("Username: " + config.getUsername());
        log.info("URL: " + config.getUrl());
        log.info("Project name: " + config.getProjectName());
        log.info("Deny project creation: " + config.getDenyProject());
        log.info("Scan timeout in minutes: " + (config.getSastScanTimeoutInMinutes() <= 0 ? "" : config.getSastScanTimeoutInMinutes()));
        log.info("Full team path: " + config.getTeamPath());
        log.info("Preset: " + config.getPresetName());
        log.info("Is incremental scan: " + config.getIncremental());
        log.info("Is interval full scans enabled: " + configBFF.isIntervals());
        if (configBFF.isIntervals()) {
            log.info("Interval- begins: " + configBFF.getIntervalBegins());
            log.info("Interval- ends: " + configBFF.getIntervalEnds());
            String forceScan = config.getForceScan() ? "" : "NOT ";
            log.info("Override full scan: " + config.getForceScan() + " (Interval based full scan " + forceScan + "activated.)");
        }
        log.info("Folder exclusions: " + (config.getSastFolderExclusions()));
        log.info("Is synchronous scan: " + config.getSynchronous());
        log.info("Generate PDF report: " + config.getGeneratePDFReport());
        log.info("CxSAST thresholds enabled: " + config.getSastThresholdsEnabled());
        if (config.getSastThresholdsEnabled()) {
            log.info("CxSAST high threshold: " + (config.getSastHighThreshold() == null ? "[No Threshold]" : config.getSastHighThreshold()));
            log.info("CxSAST medium threshold: " + (config.getSastMediumThreshold() == null ? "[No Threshold]" : config.getSastMediumThreshold()));
            log.info("CxSAST low threshold: " + (config.getSastLowThreshold() == null ? "[No Threshold]" : config.getSastLowThreshold()));
        }
        log.info("CxOSA enabled: " + config.getOsaEnabled());
        if (config.getOsaEnabled()) {
            log.info("CxOSA filter patterns: " + config.getOsaFilterPattern());
            log.info("CxOSA archive extract patterns: " + config.getOsaArchiveIncludePatterns());
            log.info("CxOSA install NMP and Bower before scan: " + config.getOsaRunInstall());

            log.info("CxOSA thresholds enabled: " + config.getOsaThresholdsEnabled());
            if (config.getOsaThresholdsEnabled()) {
                log.info("CxOSA high threshold: " + (config.getOsaHighThreshold() == null ? "[No Threshold]" : config.getOsaHighThreshold()));
                log.info("CxOSA medium threshold: " + (config.getOsaMediumThreshold() == null ? "[No Threshold]" : config.getOsaMediumThreshold()));
                log.info("CxOSA low threshold: " + (config.getOsaLowThreshold() == null ? "[No Threshold]" : config.getOsaLowThreshold()));
            }
        }
        log.info("------------------------------------------------------------------------------------------");
    }

    public static void printBuildFailure(String thDescription, ScanResults ret, CxLoggerAdapter log) {
        log.error("********************************************");
        log.error(" The Build Failed for the Following Reasons: ");
        log.error("********************************************");

        if (ret.getSastCreateException() != null) {
            log.error(ret.getSastCreateException().getMessage() + (ret.getSastCreateException().getCause() == null ? "" : ret.getSastCreateException().getCause().getMessage()));
        }
        if (ret.getSastWaitException() != null) {
            log.error(ret.getSastWaitException().getMessage() + (ret.getSastWaitException().getCause() == null ? "" : ret.getSastWaitException().getCause().getMessage()));
        }
        if (ret.getOsaCreateException() != null) {
            log.error(ret.getOsaCreateException().getMessage() + (ret.getOsaCreateException().getCause() == null ? "" : ret.getOsaCreateException().getCause().getMessage()));
        }
        if (ret.getOsaWaitException() != null) {
            log.error(ret.getOsaWaitException().getMessage() + (ret.getOsaWaitException().getCause() == null ? "" : ret.getOsaWaitException().getCause().getMessage()));
        }

        String[] lines = thDescription.split("\\n");
        for (String s : lines) {
            log.error(s);
        }
        log.error("-----------------------------------------------------------------------------------------\n");
        log.error("");
    }

    public static void printAgentConfigError(CxLoggerAdapter log) {
        log.error("");
        log.error("*****************************************************************************************************************************************");
        log.error("|  Please add the system property:                                                                                                      |");
        log.error("|  -Datlassian.org.osgi.framework.bootdelegation=javax.servlet,javax.servlet.*,sun.*,com.sun.*,org.w3c.dom.*,org.apache.xerces.         |");
        log.error("|  to the agent run command and restart it.                                                                                             |");
        log.error("|                                                                                                                                       |");
        log.error("|  if the agent is run via wrapper, add the property to the the wrapper.conf file:                                                      |");
        log.error("|  wrapper.java.additional.3=-Datlassian.org.osgi.framework.bootdelegation=javax.servlet,javax.servlet.*,sun.*,com.sun.*,org.w3c.dom.*  |");
        log.error("|  and restart it                                                                                                                       |");
        log.error("|                                                                                                                                       |");
        log.error("|  Please refer to the documentation: https://checkmarx.atlassian.net/wiki/spaces/KC/pages/127859917/Configuring+Remote+Agent+Support   |");
        log.error("*****************************************************************************************************************************************");
        log.error("");
    }

    public static String decrypt(String str) {
        String encStr;
        if (isEncrypted(str)) {
            try {
                encStr = new EncryptionServiceImpl().decrypt(str);
            } catch (EncryptionException e) {
                encStr = "";
            }
            return encStr;
        } else {
            return str;
        }
    }

    public static String encrypt(String password) {
        String encPass;
        if (!isEncrypted(password)) {
            try {
                encPass = new EncryptionServiceImpl().encrypt(password);
            } catch (EncryptionException e) {
                encPass = "";
            }
            return encPass;
        } else {
            return password;
        }
    }

    public static boolean isEncrypted(String encryptStr) {
        try {
            new EncryptionServiceImpl().decrypt(encryptStr);
        } catch (EncryptionException e) {
            return false;
        }
        return true;
    }

    public static Integer resolveInt(String value, Logger log) {
        Integer inti = null;
        if (!StringUtils.isEmpty(value)) {
            try {
                inti = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                log.warn("failed to parse integer value: " + value);
            }
        }
        return inti;
    }
}
