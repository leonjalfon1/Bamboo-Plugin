package com.cx.plugin.dto;

import org.hsqldb.lib.StringUtil;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Created by galn on 21/12/2016.
 */
public class CxScanConfiguration {

    private String username;
    /**
     * The password of the user running the scan.
     */
    private String password;
    /**
     * Host name of the CheckmarxTask application.
     */
    private String url;
    /**
     * The name of the project being scanned.
     */
    private String projectName;
    private long presetId;
    private String presetName;
    private String fullTeamPath;
    private String folderExclusions;
    private String filterPattern;
    /**
     * Define a timeout (in minutes) for the scan. If the specified time has passed, the build fails.
     * Set to 0 to run the scan with no time limit.
     */
    private Integer scanTimeoutInMinutes;
    private boolean isIncremental = false;
    private boolean isSynchronous = false;
    private boolean thresholdsEnabled = false;
    @Nullable
    private Integer highThreshold;
    @Nullable
    private Integer mediumThreshold;
    @Nullable
    private Integer lowThreshold;
    private boolean generatePDFReport = false;
    private boolean osaEnabled = false;
    private boolean osaThresholdsEnabled = false;
    /**
     * Configure a threshold for the CxOSA High Severity Vulnerabilities.
     * The build will fail if the sum of High Severity Vulnerabilities is larger than the threshold.
     * Leave empty to ignore threshold.
     */
    private Integer osaHighThreshold;
    /**
     * Configure a threshold for the CxOSA Medium Severity Vulnerabilities.
     * The build will fail if the sum of Medium Severity Vulnerabilities is larger than the threshold.
     * Leave empty to ignore threshold.
     */
    private Integer osaMediumThreshold;
    /**
     * Configure a threshold for the CxOSA Low Severity Vulnerabilities.
     * The build will fail if the sum of Low Severity Vulnerabilities is larger than the threshold.
     * Leave empty to ignore threshold.
     */
    private Integer osaLowThreshold;


    /**********   C-tor   ***************/
    public CxScanConfiguration(HashMap<String, String> configurationMap) {
        setUsername(configurationMap.get(CxParam.USER_NAME));
        setPassword(configurationMap.get(CxParam.PASSWORD));
        setUrl(configurationMap.get(CxParam.SERVER_URL));
        setProjectName(configurationMap.get(CxParam.PROJECT_NAME));
        setPresetId(Long.parseLong(configurationMap.get(CxParam.PRESET_ID)));
        setPresetName(configurationMap.get(CxParam.PRESET_NAME));
        setFullTeamPath(configurationMap.get(CxParam.TEAM_PATH_NAME));
        setFolderExclusions(configurationMap.get(CxParam.FOLDER_EXCLUSION));
        setFilterPattern(configurationMap.get(CxParam.FILTER_PATTERN));

        setScanTimeoutInMinutes(configurationMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES));
        setIncremental(Boolean.parseBoolean(configurationMap.get(CxParam.IS_INCREMENTAL)));
        setSynchronous(Boolean.parseBoolean(configurationMap.get(CxParam.IS_SYNCHRONOUS)));
        setThresholdsEnabled(Boolean.parseBoolean(configurationMap.get(CxParam.THRESHOLDS_ENABLED)));
        setHighThreshold(configurationMap.get(CxParam.HIGH_THRESHOLD));
        setMediumThreshold(configurationMap.get(CxParam.MEDIUM_THRESHOLD));
        setLowThreshold(configurationMap.get(CxParam.LOW_THRESHOLD));
        setGeneratePDFReport(Boolean.parseBoolean(configurationMap.get(CxParam.GENERATE_PDF_REPORT)));

        setOsaEnabled(Boolean.parseBoolean(configurationMap.get(CxParam.OSA_ENABLED)));
        setOsaThresholdsEnabled(Boolean.parseBoolean(configurationMap.get(CxParam.OSA_THRESHOLDS_ENABLED)));
        setOsaHighSeveritiesThreshold(configurationMap.get(CxParam.OSA_HIGH_THRESHOLD));
        setOsaMediumSeveritiesThreshold(configurationMap.get(CxParam.OSA_MEDIUM_THRESHOLD));
        setOsaLowSeveritiesThreshold(configurationMap.get(CxParam.OSA_LOW_THRESHOLD));
    }

    /********   Setters & Getters ***********/

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getPresetId() {
        return presetId;
    }

    public void setPresetId(long presetId) {
        this.presetId = presetId;
    }

    public Integer getScanTimeoutInMinutes() {
        return scanTimeoutInMinutes;
    }

    public void setScanTimeoutInMinutes(String scanTimeoutInMinutes) {
        Integer i = getAsInteger(scanTimeoutInMinutes);
        if (i == null) {
            this.scanTimeoutInMinutes = 0;
        } else {
            this.scanTimeoutInMinutes = i;
        }
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean incremental) {
        isIncremental = incremental;
    }

    public boolean isSynchronous() {
        return isSynchronous;
    }

    public void setSynchronous(boolean synchronous) {
        isSynchronous = synchronous;
    }

    public boolean isThresholdsEnabled() {
        return thresholdsEnabled;
    }

    public void setThresholdsEnabled(boolean thresholdsEnabled) {
        this.thresholdsEnabled = thresholdsEnabled;
    }

    public Integer getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(Integer highThreshold) {
        this.highThreshold = highThreshold;
    }

    private void setHighThreshold(String highSeveritiesThreshold) {
        this.highThreshold = getAsInteger(highSeveritiesThreshold);
    }

    public Integer getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(Integer mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    private void setMediumThreshold(String mediumSeveritiesThreshold) {
        this.mediumThreshold = getAsInteger(mediumSeveritiesThreshold);
    }

    public Integer getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(Integer lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    private void setLowThreshold(String lowSeveritiesThreshold) {
        this.lowThreshold = getAsInteger(lowSeveritiesThreshold);
    }

    public String getFullTeamPath() {
        return fullTeamPath;
    }

    private void setFullTeamPath(String fullTeamPath) {
        this.fullTeamPath = fullTeamPath;
    }

    public String getFolderExclusions() {
        return folderExclusions;
    }

    public void setFolderExclusions(String folderExclusions) {
        this.folderExclusions = folderExclusions;
    }

    public String getFilterPattern() {
        return filterPattern;
    }

    public void setFilterPattern(String filterPattern) {
        this.filterPattern = filterPattern;
    }

    public boolean isGeneratePDFReport() {
        return generatePDFReport;
    }

    public void setGeneratePDFReport(boolean generatePDFReport) {
        this.generatePDFReport = generatePDFReport;
    }

    public boolean isOsaEnabled() {
        return osaEnabled;
    }

    public void setOsaEnabled(boolean osaEnabled) {
        this.osaEnabled = osaEnabled;
    }

    public boolean isOsaThresholdsEnabled() {
        return osaThresholdsEnabled;
    }

    public void setOsaThresholdsEnabled(boolean osaThresholdsEnabled) {
        this.osaThresholdsEnabled = osaThresholdsEnabled;
    }

    public Integer getOsaHighThreshold() {
        return osaHighThreshold;
    }

    public void setOsaHighThreshold(Integer osaHighThreshold) {
        this.osaHighThreshold = osaHighThreshold;
    }

    private void setOsaHighSeveritiesThreshold(String osaHighSeveritiesThreshold) {
        this.osaHighThreshold = getAsInteger(osaHighSeveritiesThreshold);
    }

    public Integer getOsaMediumThreshold() {
        return osaMediumThreshold;
    }

    public void setOsaMediumThreshold(Integer osaMediumThreshold) {
        this.osaMediumThreshold = osaMediumThreshold;
    }

    private void setOsaMediumSeveritiesThreshold(String osaMediumSeveritiesThreshold) {
        this.osaMediumThreshold = getAsInteger(osaMediumSeveritiesThreshold);
    }

    public Integer getOsaLowThreshold() {
        return osaLowThreshold;
    }

    public void setOsaLowThreshold(Integer osaLowThreshold) {
        this.osaLowThreshold = osaLowThreshold;
    }

    private void setOsaLowSeveritiesThreshold(String osaLowSeveritiesThreshold) {
        this.osaLowThreshold = getAsInteger(osaLowSeveritiesThreshold);
    }

    private Integer getAsInteger(String number) {
        Integer inti = null;
        try {
            if (!StringUtil.isEmpty(number)) {
                inti = Integer.parseInt(number);
            }
        } catch (NumberFormatException e) {
            inti = null;
        }
        return inti;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public boolean isSASTThresholdEnabled() {
        return isThresholdsEnabled() && (getLowThreshold() != null || getMediumThreshold() != null || getHighThreshold() != null);
    }

    public boolean isOSAThresholdEnabled() {
        return isOsaEnabled() && isOsaThresholdsEnabled() && (getOsaHighThreshold() != null || getOsaMediumThreshold() != null || getOsaLowThreshold() != null);
    }
}
