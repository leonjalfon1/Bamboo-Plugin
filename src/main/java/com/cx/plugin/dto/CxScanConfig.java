package com.cx.plugin.dto;

import org.hsqldb.lib.StringUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import static com.cx.plugin.dto.CxParam.*;

/**
 * Created by galn on 21/12/2016.
 */
public class CxScanConfig {

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
    private String version;
    private boolean denyProject = false;
    /**
     * Define a timeout (in minutes) for the scan. If the specified time has passed, the build fails.
     * Set to 0 to run the scan with no time limit.
     */
    private Integer scanTimeoutInMinutes;
    private String comment;
    private boolean isIncremental = false;
    private boolean isIntervals = false;
    private boolean forceFullScan = false;
    private String intervalBegins;
    private String intervalEnds;

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
    private String osaFilterPattern;
    private String osaArchiveIncludePatterns;
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
    public CxScanConfig(HashMap<String, String> configurationMap) {
        setVersion();
        setUsername(configurationMap.get(USER_NAME));
        setPassword(configurationMap.get(PASSWORD));
        setUrl(configurationMap.get(SERVER_URL));
        setProjectName(configurationMap.get(PROJECT_NAME));
        setDenyProject(Boolean.parseBoolean(configurationMap.get(GLOBAL_DENY_PROJECT)));
        setPresetId(Long.parseLong(configurationMap.get(PRESET_ID)));
        setPresetName(configurationMap.get(PRESET_NAME));
        setFullTeamPath(configurationMap.get(TEAM_PATH_NAME));
        setFolderExclusions(configurationMap.get(FOLDER_EXCLUSION));
        setFilterPattern(configurationMap.get(FILTER_PATTERN));
        setScanTimeoutInMinutes(configurationMap.get(SCAN_TIMEOUT_IN_MINUTES));
        setComment(configurationMap.get(COMMENT));
        setIncremental(Boolean.parseBoolean(configurationMap.get(IS_INCREMENTAL)));
        setIntervals(Boolean.parseBoolean(configurationMap.get(IS_INTERVALS)));
        setIntervalBegins(configurationMap.get(INTERVAL_BEGINS));
        setIntervalEnds(configurationMap.get(INTERVAL_ENDS));
        setForceFullScan(Boolean.parseBoolean(configurationMap.get(FORCE_FULL_SCAN)));
        setSynchronous(Boolean.parseBoolean(configurationMap.get(IS_SYNCHRONOUS)));
        setThresholdsEnabled(Boolean.parseBoolean(configurationMap.get(THRESHOLDS_ENABLED)));
        setHighThreshold(configurationMap.get(HIGH_THRESHOLD));
        setMediumThreshold(configurationMap.get(MEDIUM_THRESHOLD));
        setLowThreshold(configurationMap.get(LOW_THRESHOLD));
        setGeneratePDFReport(Boolean.parseBoolean(configurationMap.get(GENERATE_PDF_REPORT)));

        setOsaEnabled(Boolean.parseBoolean(configurationMap.get(OSA_ENABLED)));
        setOsaFilterPattern(configurationMap.get(OSA_FILTER_PATTERNS));
        setOsaArchiveIncludePatterns(configurationMap.get(OSA_ARCHIVE_INCLUDE_PATTERNS));
        setOsaThresholdsEnabled(Boolean.parseBoolean(configurationMap.get(OSA_THRESHOLDS_ENABLED)));
        setOsaHighSeveritiesThreshold(configurationMap.get(OSA_HIGH_THRESHOLD));
        setOsaMediumSeveritiesThreshold(configurationMap.get(OSA_MEDIUM_THRESHOLD));
        setOsaLowSeveritiesThreshold(configurationMap.get(OSA_LOW_THRESHOLD));
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean incremental) {
        isIncremental = incremental;
    }

    public boolean isIntervals() {
        return isIntervals;
    }

    public boolean isForceFullScan() {
        return forceFullScan;
    }

    public void setForceFullScan(boolean forceFullScan) {
        this.forceFullScan = forceFullScan;
    }

    public void setIntervals(boolean intervals) {
        isIntervals = intervals;
    }

    public String getIntervalBegins() {
        return intervalBegins;
    }

    public void setIntervalBegins(String intervalBegins) {
        this.intervalBegins = intervalBegins;
    }

    public String getIntervalEnds() {
        return intervalEnds;
    }

    public void setIntervalEnds(String intervalEnds) {
        this.intervalEnds = intervalEnds;
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

    public String getOsaFilterPattern() {
        return osaFilterPattern;
    }

    public void setOsaFilterPattern(String osaFilterPattern) {
        this.osaFilterPattern = osaFilterPattern;
    }

    public String getOsaArchiveIncludePatterns() {
        return osaArchiveIncludePatterns;
    }

    public void setOsaArchiveIncludePatterns(String osaArchiveIncludePatterns) {
        this.osaArchiveIncludePatterns = osaArchiveIncludePatterns;
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

    public boolean isSASTThresholdEffectivelyEnabled() {
        return isThresholdsEnabled() && (getLowThreshold() != null || getMediumThreshold() != null || getHighThreshold() != null);
    }

    public boolean isOSAThresholdEffectivelyEnabled() {
        return isOsaEnabled() && isOsaThresholdsEnabled() && (getOsaHighThreshold() != null || getOsaMediumThreshold() != null || getOsaLowThreshold() != null);
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersion() {
        try {
            Properties properties = new Properties();
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("english.properties");
            if (is != null) {
                properties.load(is);
                this.version = properties.getProperty("version");
            }
        }catch (Exception e) {
            this.version = "";
        }
    }


    public boolean isDenyProject() {
        return denyProject;
    }

    public void setDenyProject(boolean denyProject) {
        this.denyProject = denyProject;
    }
}
