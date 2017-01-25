package com.cx.plugin.dto;

import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import org.hsqldb.lib.StringUtil;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created by galn on 21/12/2016.
 */
public class ScanConfiguration {

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

    private String preset;

    private String fullTeamPath;

    private String[] folderExclusions = new String[0];

    /**
     * Define an output directory for the scan results.
     */
    private String outputDirectory; //TODO default directory
    /**
     * Define a timeout (in minutes) for the scan. If the specified time has passed, the build fails.
     * Set to 0 to run the scan with no time limit.
     */
    private Integer scanTimeoutInMinutes;
    private boolean isIncrementalScan = false;
    private boolean isSynchronous = false;
    //    private boolean generatePDFReport;

    private boolean thresholdsEnabled = false;

    @Nullable
    private Integer highThreshold;
    @Nullable
    private Integer mediumThreshold;
    @Nullable
    private Integer lowThreshold;

    private boolean generatePDFReport = false;

    private boolean osaEnabled = false;

    private Integer osaScanTimeoutInMinutes;

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


    //    private boolean osaGeneratePDFReport;
    //  private boolean osaGenerateHTMLReport;

    public ScanConfiguration(ConfigurationMap configurationMap, String projectName) {
        setUsername(configurationMap.get(CxParam.USER_NAME.value()));
        String cxPass = configurationMap.get(CxParam.PASSWORD.value());
        setPassword(cxPass);
        setUrl(configurationMap.get(CxParam.SERVER_URL.value()));
        //this.projectName = configurationMap.value(CxParam.CX_PROJECT_NAME);
        setProjectName(this.projectName = projectName);//TODO
        setPreset(configurationMap.get(CxParam.PRESET.value()));
        setFullTeamPath(configurationMap.get(CxParam.TEAM_PATH.value()));
        setFolderExclusions(StringUtil.split(configurationMap.get(CxParam.FOLDER_EXCLUSION.value()), ","));

        //this.fileExclusions = configurationMap.get(CxParam.FILE_EXCLUSION);
        setOutputDirectory(configurationMap.get(CxParam.OUTPUT_DIRECTORY.value()));
        setScanTimeoutInMinutes(configurationMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES.value()));
        setIncrementalScan(configurationMap.getAsBoolean(CxParam.IS_INCREMENTAL_SCAN.value()));
        setSynchronous(configurationMap.getAsBoolean(CxParam.IS_SYNCHRONOUS.value()));//TODO value as boolean/Int
        setThresholdsEnabled(configurationMap.getAsBoolean(CxParam.THRESHOLDS_ENABLED.value()));
        setHighThreshold(configurationMap.get(CxParam.HIGH_THRESHOLD.value()));
        setMediumThreshold(configurationMap.get(CxParam.MEDIUM_THRESHOLD.value()));
        setLowThreshold(configurationMap.get(CxParam.LOW_THRESHOLD.value()));
        setGeneratePDFReport(configurationMap.getAsBoolean(CxParam.GENERATE_PDF_REPORT.value()));

        setOsaEnabled(configurationMap.getAsBoolean(CxParam.OSA_ENABLED.value()));
        setOsaScanTimeoutInMinutes(configurationMap.get(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value()));
        setOsaThresholdsEnabled(configurationMap.getAsBoolean(CxParam.OSA_THRESHOLDS_ENABLED.value()));
        setOsaHighSeveritiesThreshold(configurationMap.get(CxParam.OSA_HIGH_THRESHOLD.value()));
        setOsaMediumSeveritiesThreshold(configurationMap.get(CxParam.OSA_MEDIUM_THRESHOLD.value()));
        setOsaLowSeveritiesThreshold(configurationMap.get(CxParam.OSA_LOW_THRESHOLD.value()));
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

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    private void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Integer getScanTimeoutInMinutes() {
        return scanTimeoutInMinutes;
    }

    public void setScanTimeoutInMinutes(String scanTimeoutInMinutes) {
        this.scanTimeoutInMinutes = setNumberFromString(scanTimeoutInMinutes);
    }

    public boolean isIncrementalScan() {
        return isIncrementalScan;
    }

    public void setIncrementalScan(boolean incrementalScan) {
        isIncrementalScan = incrementalScan;
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
        this.highThreshold = setNumberFromString(highSeveritiesThreshold);
    }

    public Integer getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(Integer mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    private void setMediumThreshold(String mediumSeveritiesThreshold) {
        this.mediumThreshold = setNumberFromString(mediumSeveritiesThreshold);
    }
    public Integer getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(Integer lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    private void setLowThreshold(String lowSeveritiesThreshold) {
        this.lowThreshold = setNumberFromString(lowSeveritiesThreshold);
    }

    public String getFullTeamPath() {
        return fullTeamPath;
    }

    private void setFullTeamPath(String fullTeamPath) {
        this.fullTeamPath = fullTeamPath;
    }

    public String[] getFolderExclusions() {
        return folderExclusions;
    }

    private void setFolderExclusions(String[] folderExclusions) {
        this.folderExclusions = folderExclusions;
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

    public Integer getOsaScanTimeoutInMinutes() {
        return osaScanTimeoutInMinutes;
    }

    public void setOsaScanTimeoutInMinutes(Integer osaScanTimeoutInMinutes) {
        this.osaScanTimeoutInMinutes = osaScanTimeoutInMinutes;
    }

    private void setOsaScanTimeoutInMinutes(String osaScanTimeoutInMinutes) {
        this.osaScanTimeoutInMinutes = setNumberFromString(osaScanTimeoutInMinutes);
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
        this.osaHighThreshold = setNumberFromString(osaHighSeveritiesThreshold);
    }
    public Integer getOsaMediumThreshold() {
        return osaMediumThreshold;
    }

    public void setOsaMediumThreshold(Integer osaMediumThreshold) {
        this.osaMediumThreshold = osaMediumThreshold;
    }

    private void setOsaMediumSeveritiesThreshold(String osaMediumSeveritiesThreshold) {
        this.osaMediumThreshold = setNumberFromString(osaMediumSeveritiesThreshold);
    }
    public Integer getOsaLowThreshold() {
        return osaLowThreshold;
    }

    public void setOsaLowThreshold(Integer osaLowThreshold) {
        this.osaLowThreshold = osaLowThreshold;
    }

    private void setOsaLowSeveritiesThreshold(String osaLowSeveritiesThreshold) {
        this.osaLowThreshold = setNumberFromString(osaLowSeveritiesThreshold);
    }

    private Integer setNumberFromString(String number) { //TODO change to the builtin method
        Integer inti = null;
        try {
            if (number != null && !StringUtil.isEmpty(number)) {
                inti = Integer.parseInt(number);
            }

        } catch (Exception e) {
            inti = null;
        }
        return inti;
    }

    public boolean isSASTThresholdEnabled() {
        return isThresholdsEnabled() && (getLowThreshold() != null || getMediumThreshold() != null || getHighThreshold() != null);
    }

    public boolean isOSAThresholdEnabled() {
        return isOsaEnabled() && (getOsaHighThreshold() != null || getOsaMediumThreshold() != null || getOsaLowThreshold() != null);
    }

}
