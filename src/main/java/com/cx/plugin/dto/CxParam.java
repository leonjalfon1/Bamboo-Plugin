package com.cx.plugin.dto;/**
 * Created by galn on 22/09/2016.
 */

public enum CxParam {
    USER_NAME("userName"),
    PASSWORD("password"),
    URL("url"),
    PROJECT_NAME("projectName"),
    TEAM_PATH("teamPath"),
    PRESET("preset"),
    IS_INCREMENTAL_SCAN("isIncrementalScan"),
    FOLDER_EXCLUSION("folderExclusions"),
    FILE_EXCLUSION("fileExclusions"),
    IS_SYNCHRONOUS("isSynchronous"),
    GENERATE_PDF_REPORT("generatePDFReport"),
    THRESHOLDS_ENABLED("thresholdsEnabled"),
    HIGH_THRESHOLD("highThreshold"),
    MEDIUM_THRESHOLD("mediumThreshold"),
    LOW_THRESHOLD("lowThreshold"),
    SCAN_TIMEOUT_IN_MINUTES("scanTimeoutInMinutes"),
    OSA_ENABLED("osaEnabled"),
    OSA_SCAN_TIMEOUT_IN_MINUTES("osaScanTimeoutInMinutes"),
    OSA_THRESHOLDS_ENABLED("osaThresholdsEnabled"),
    OSA_EXCLUSIONS("osaExclusions"),
    OSA_HIGH_THRESHOLD("osaHighThreshold"),
    OSA_MEDIUM_THRESHOLD("osaMediumThreshold"),
    OSA_LOW_THRESHOLD("osaLowSThreshold"),
    OSA_GENERATE_PDF_REPORT("osaGeneratePDFReport"),
    OSA_GENERATE_HTML_REPORT("osaGenerateHTMLReport"),
    OUTPUT_DIRECTORY("outputDirectory"),
    CX_PROJECT_NAME("myProject"),
    NO_SESSION("noSession"),
    PRESET_LIST("presetList"),
    TEAM_PATH_LIST("teamPathList"),
    FILTER_PATTERN("filterPatterns");


    //TODO

    private String uiValue;

    CxParam(String uiValue) {
        this.uiValue = uiValue;
    }

    public String value() {
        return this.uiValue;
    }

}
