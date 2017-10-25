package com.cx.plugin.utils;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.spring.container.ContainerManager;
import com.cx.client.dto.ClientOrigin;
import com.cx.client.dto.LocalScanConfiguration;
import com.cx.client.dto.ScanResults;
import com.cx.client.rest.dto.OSASummaryResults;
import com.cx.plugin.dto.CxScanConfig;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.cx.plugin.dto.CxParam.*;
import static com.cx.plugin.dto.CxParam.GLOBAL_DENY_PROJECT;

/**
 * Created by Galn on 25/10/2017.
 */
public class CxConfigUtil {
    private static HashMap<String, String> configurationMap;
    private static AdministrationConfiguration adminConfig;

    public static HashMap<String, String> resolveConfigurationMap(ConfigurationMap configMap, CxLoggerAdapter log) throws TaskException {

        Object a = ContainerManager.getComponent("administrationConfigurationAccessor");
        try {
            Method getAdminConfig = a.getClass().getDeclaredMethod("getAdministrationConfiguration");
            adminConfig = (AdministrationConfiguration) getAdminConfig.invoke(a);
        } catch (Exception e) {
            throw new TaskException("Failed to resolve global configuration", e);
        }

        configurationMap = new HashMap<String, String>();

        if (CUSTOM_CONFIGURATION_SERVER.equals(configMap.get(SERVER_CREDENTIALS_SECTION))) {
            configurationMap.put(SERVER_URL, configMap.get(SERVER_URL));
            configurationMap.put(USER_NAME, configMap.get(USER_NAME));
            configurationMap.put(PASSWORD, configMap.get(PASSWORD));
        } else {
            configurationMap.put(SERVER_URL, getAdminConfig(GLOBAL_SERVER_URL));
            configurationMap.put(USER_NAME, getAdminConfig(GLOBAL_USER_NAME));
            configurationMap.put(PASSWORD, getAdminConfig(GLOBAL_PASSWORD));
        }

        configurationMap.put(PROJECT_NAME, configMap.get(PROJECT_NAME));

        String presetId = configMap.get(PRESET_ID);
        if (!StringUtils.isNumeric(presetId)) {
            throw new TaskException("Invalid preset Id");
        }

        String teamName = configMap.get(TEAM_PATH_NAME);
        if (StringUtils.isEmpty(teamName)) {
            throw new TaskException("Invalid team path");
        }

        configurationMap.put(PRESET_ID, presetId);
        configurationMap.put(PRESET_NAME, StringUtils.defaultString(configMap.get(PRESET_NAME)));
        configurationMap.put(TEAM_PATH_ID, StringUtils.defaultString(configMap.get(TEAM_PATH_ID)));
        configurationMap.put(TEAM_PATH_NAME, teamName);

        if (CUSTOM_CONFIGURATION_CXSAST.equals(configMap.get(CXSAST_SECTION))) {
            configurationMap.put(FOLDER_EXCLUSION, configMap.get(FOLDER_EXCLUSION));
            configurationMap.put(FILTER_PATTERN, configMap.get(FILTER_PATTERN));
            configurationMap.put(SCAN_TIMEOUT_IN_MINUTES, configMap.get(SCAN_TIMEOUT_IN_MINUTES));

        } else {
            configurationMap.put(FOLDER_EXCLUSION, getAdminConfig(GLOBAL_FOLDER_EXCLUSION));
            configurationMap.put(FILTER_PATTERN, getAdminConfig(GLOBAL_FILTER_PATTERN));
            configurationMap.put(SCAN_TIMEOUT_IN_MINUTES, getAdminConfig(GLOBAL_SCAN_TIMEOUT_IN_MINUTES));
        }

        configurationMap.put(COMMENT, configMap.get(COMMENT));
        String isIncremental = configMap.get(IS_INCREMENTAL);
        configurationMap.put(IS_INCREMENTAL, isIncremental);

        if (OPTION_FALSE.equals(isIncremental)) {
            configurationMap.put(IS_INTERVALS, OPTION_FALSE);
        } else {
            String isIntervals = configMap.get(IS_INTERVALS);
            configurationMap.put(IS_INTERVALS, isIntervals);
            if (OPTION_TRUE.equals(isIntervals)) {
                configurationMap = resolveIntervalFullScan(configMap.get(INTERVAL_BEGINS), configMap.get(INTERVAL_ENDS), configurationMap, log);
            }
        }

        configurationMap.put(GENERATE_PDF_REPORT, configMap.get(GENERATE_PDF_REPORT));
        configurationMap.put(OSA_ENABLED, configMap.get(OSA_ENABLED));

        if (CUSTOM_CONFIGURATION_CONTROL.equals(configMap.get(SCAN_CONTROL_SECTION))) {
            configurationMap.put(IS_SYNCHRONOUS, configMap.get(IS_SYNCHRONOUS));
            configurationMap.put(THRESHOLDS_ENABLED, configMap.get(THRESHOLDS_ENABLED));
            configurationMap.put(HIGH_THRESHOLD, configMap.get(HIGH_THRESHOLD));
            configurationMap.put(MEDIUM_THRESHOLD, configMap.get(MEDIUM_THRESHOLD));
            configurationMap.put(LOW_THRESHOLD, configMap.get(LOW_THRESHOLD));
            configurationMap.put(OSA_THRESHOLDS_ENABLED, configMap.get(OSA_THRESHOLDS_ENABLED));
            configurationMap.put(OSA_HIGH_THRESHOLD, configMap.get(OSA_HIGH_THRESHOLD));
            configurationMap.put(OSA_MEDIUM_THRESHOLD, configMap.get(OSA_MEDIUM_THRESHOLD));
            configurationMap.put(OSA_LOW_THRESHOLD, configMap.get(OSA_LOW_THRESHOLD));
        } else {
            configurationMap.put(IS_SYNCHRONOUS, getAdminConfig(GLOBAL_IS_SYNCHRONOUS));
            configurationMap.put(THRESHOLDS_ENABLED, getAdminConfig(GLOBAL_THRESHOLDS_ENABLED));
            configurationMap.put(HIGH_THRESHOLD, getAdminConfig(GLOBAL_HIGH_THRESHOLD));
            configurationMap.put(MEDIUM_THRESHOLD, getAdminConfig(GLOBAL_MEDIUM_THRESHOLD));
            configurationMap.put(LOW_THRESHOLD, getAdminConfig(GLOBAL_LOW_THRESHOLD));
            configurationMap.put(OSA_THRESHOLDS_ENABLED, getAdminConfig(GLOBAL_OSA_THRESHOLDS_ENABLED));
            configurationMap.put(OSA_HIGH_THRESHOLD, getAdminConfig(GLOBAL_OSA_HIGH_THRESHOLD));
            configurationMap.put(OSA_MEDIUM_THRESHOLD, getAdminConfig(GLOBAL_OSA_MEDIUM_THRESHOLD));
            configurationMap.put(OSA_LOW_THRESHOLD, getAdminConfig(GLOBAL_OSA_LOW_THRESHOLD));
        }

        configurationMap.put(GLOBAL_DENY_PROJECT, getAdminConfig(GLOBAL_DENY_PROJECT));

        return configurationMap;
    }

    private static HashMap<String, String> resolveIntervalFullScan(String intervalBegins, String intervalEnds, HashMap<String, String> configMap, CxLoggerAdapter log) {

        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            final Calendar calendarBeginsHourMinute = Calendar.getInstance();
            calendarBeginsHourMinute.setTime(dateFormat.parse(intervalBegins));
            final Calendar calendarBegins = Calendar.getInstance();
            calendarBegins.set(Calendar.HOUR_OF_DAY, calendarBeginsHourMinute.get(Calendar.HOUR_OF_DAY));
            calendarBegins.set(Calendar.MINUTE, calendarBeginsHourMinute.get(Calendar.MINUTE));
            calendarBegins.set(Calendar.SECOND, 0);
            Date dateBegins = calendarBegins.getTime();

            final Calendar calendarEndsHourMinute = Calendar.getInstance();
            calendarEndsHourMinute.setTime(dateFormat.parse(intervalEnds));
            final Calendar calendarEnds = Calendar.getInstance();
            calendarEnds.set(Calendar.HOUR_OF_DAY, calendarEndsHourMinute.get(Calendar.HOUR_OF_DAY));
            calendarEnds.set(Calendar.MINUTE, calendarEndsHourMinute.get(Calendar.MINUTE));
            calendarEnds.set(Calendar.SECOND, 0);
            Date dateEnds = calendarEnds.getTime();

            final Calendar calendarNow = Calendar.getInstance();
            final Date dateNow = calendarNow.getTime();

            if (dateBegins.after(dateEnds)) {
                if (dateBegins.after(dateNow)) {
                    calendarBegins.add(Calendar.DATE, -1);
                    dateBegins = calendarBegins.getTime();
                } else {
                    calendarEnds.add(Calendar.DATE, 1);
                    dateEnds = calendarEnds.getTime();
                }
            }

            configMap.put(INTERVAL_BEGINS, dateBegins.toString());
            configMap.put(INTERVAL_ENDS, dateEnds.toString());

            String forceFullScan = OPTION_FALSE;

            if (dateNow.after(dateBegins) && dateNow.before(dateEnds)) {
                forceFullScan = OPTION_TRUE;
            }
            configMap.put(FORCE_FULL_SCAN, forceFullScan);


        } catch (final ParseException e) {
            log.error("Full scan interval parse exception");
        }
        return configMap;

    }


    private static String getAdminConfig(String key) {
        return StringUtils.defaultString(adminConfig.getSystemProperty(key));
    }

    public static LocalScanConfiguration generateScanConfiguration(byte[] zippedSources, CxScanConfig config) {
        LocalScanConfiguration ret = new LocalScanConfiguration();
        ret.setProjectName(config.getProjectName());
        ret.setClientOrigin(ClientOrigin.BAMBOO);
        ret.setFolderExclusions(config.getFolderExclusions());
        ret.setFullTeamPath(config.getFullTeamPath());
        boolean isIncremental = config.isIncremental();
        if (isIncremental && config.isForceFullScan()) {
            isIncremental = false;
        }
        ret.setIncrementalScan(isIncremental);
        ret.setPresetId(config.getPresetId());
        ret.setZippedSources(zippedSources);
        ret.setFileName(config.getProjectName());
        ret.setComment(config.getComment());

        return ret;
    }

    public static boolean assertVulnerabilities(ScanResults scanResults, OSASummaryResults osaSummaryResults, StringBuilder res,CxScanConfig config ) throws TaskException {

        boolean failByThreshold = false;
        if (config.isSASTThresholdEnabled() && scanResults != null) {
            failByThreshold = isFail(scanResults.getHighSeverityResults(), config.getHighThreshold(), res, "high", "CxSAST ");
            failByThreshold |= isFail(scanResults.getMediumSeverityResults(), config.getMediumThreshold(), res, "medium", "CxSAST ");
            failByThreshold |= isFail(scanResults.getLowSeverityResults(), config.getLowThreshold(), res, "low", "CxSAST ");
        }
        if (config.isOSAThresholdEnabled() && osaSummaryResults != null) {
            failByThreshold |= isFail(osaSummaryResults.getTotalHighVulnerabilities(), config.getOsaHighThreshold(), res, "high", "CxOSA ");
            failByThreshold |= isFail(osaSummaryResults.getTotalMediumVulnerabilities(), config.getOsaMediumThreshold(), res, "medium", "CxOSA ");
            failByThreshold |= isFail(osaSummaryResults.getTotalLowVulnerabilities(), config.getOsaLowThreshold(), res, "low", "CxOSA ");
        }
        return failByThreshold;
    }

    public static boolean isFail(int result, Integer threshold, StringBuilder res, String severity, String severityType) {
        boolean fail = false;
        if (threshold != null && result > threshold) {
            res.append(severityType).append(severity).append(" severity results are above threshold. Results: ").append(result).append(". Threshold: ").append(threshold).append("\n");
            fail = true;
        }
        return fail;
    }

}
