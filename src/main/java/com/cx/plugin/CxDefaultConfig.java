package com.cx.plugin;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationPersister;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.NotNull;
import com.cx.plugin.dto.CxParam;
import org.codehaus.plexus.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * CxDefaultConfig is a GlobalAdminAction that populates cxDefaultConfig.ftl and handles the incoming data from it
 * <p>
 * It is being activated through the Bamboo framework,
 * as configured under checkmarx-default-config-xwork in atlassian-plugin.xml
 */

public class CxDefaultConfig extends GlobalAdminAction {
    private String globalServerUrl;
    private String globalUserName;
    private String globalPassword;
    private String DEFAULT_URL = "http://localhost"; //TODO- need?


    private String globalFilterPatterns = CxParam.DEFAULT_FILTER_PATTERNS;
    private String globalFolderExclusions;
    private String globalIsSynchronous;
    private String globalScanTimeoutInMinutes;
    private String globalThresholdsEnabled;
    private String globalHighThreshold;
    private String globalMediumThreshold;
    private String globalLowThreshold;
    private String globalOsaThresholdsEnabled;
    private String globalOsaHighThreshold;
    private String globalOsaMediumThreshold;
    private String globalOsaLowThreshold;

    @Override
    public String execute() {
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent("administrationConfiguration");

        globalServerUrl = adminConfig.getSystemProperty(CxParam.GLOBAL_SERVER_URL);
        if (globalServerUrl == null || StringUtils.isEmpty(globalServerUrl)) {
            this.globalServerUrl = DEFAULT_URL;
        }
        globalUserName = adminConfig.getSystemProperty(CxParam.GLOBAL_USER_NAME);
        globalPassword = adminConfig.getSystemProperty(CxParam.GLOBAL_PASSWORD);

        globalFolderExclusions = adminConfig.getSystemProperty(CxParam.GLOBAL_FOLDER_EXCLUSION);
        String filterProperty = adminConfig.getSystemProperty(CxParam.GLOBAL_FILTER_PATTERN);
        if (filterProperty != null){
            globalFilterPatterns = filterProperty;
        }

        globalScanTimeoutInMinutes = adminConfig.getSystemProperty(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES);
        globalIsSynchronous = adminConfig.getSystemProperty(CxParam.GLOBAL_IS_SYNCHRONOUS);
        globalThresholdsEnabled = adminConfig.getSystemProperty(CxParam.GLOBAL_THRESHOLDS_ENABLED);
        globalHighThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_HIGH_THRESHOLD);
        globalMediumThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_MEDIUM_THRESHOLD);
        globalLowThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_LOW_THRESHOLD);
        globalOsaThresholdsEnabled = adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_THRESHOLDS_ENABLED);
        globalOsaHighThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_HIGH_THRESHOLD);
        globalOsaMediumThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_MEDIUM_THRESHOLD);
        globalOsaLowThreshold = adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_LOW_THRESHOLD);
        return INPUT;
    }

    public String save() { //TODO add validations
        boolean error = false;

        error |= validateNotEmpty(this.globalServerUrl, CxParam.GLOBAL_SERVER_URL);
        if (!error) {
            try {
                validateUrl(globalServerUrl);
            } catch (MalformedURLException e) {
                addFieldError(CxParam.GLOBAL_SERVER_URL, getText(CxParam.GLOBAL_SERVER_URL + "." + ERROR + ".malformed"));
                error = true;
            }
        }
        error |= validateNotEmpty(this.globalUserName, CxParam.GLOBAL_USER_NAME);
        error |= validateNotEmpty(this.globalPassword, CxParam.GLOBAL_PASSWORD);

        if (error) {
            return ERROR;
        }
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent("administrationConfiguration");
        adminConfig.setSystemProperty(CxParam.GLOBAL_SERVER_URL, globalServerUrl);
        adminConfig.setSystemProperty(CxParam.GLOBAL_USER_NAME, globalUserName);
        adminConfig.setSystemProperty(CxParam.GLOBAL_PASSWORD, encrypt(globalPassword));

        adminConfig.setSystemProperty(CxParam.GLOBAL_FOLDER_EXCLUSION, globalFolderExclusions);
        adminConfig.setSystemProperty(CxParam.GLOBAL_FILTER_PATTERN, globalFilterPatterns);
        adminConfig.setSystemProperty(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES, globalScanTimeoutInMinutes);

        adminConfig.setSystemProperty(CxParam.GLOBAL_IS_SYNCHRONOUS, globalIsSynchronous);
        if (globalIsSynchronous== null){
            globalThresholdsEnabled = null;
        }
        adminConfig.setSystemProperty(CxParam.GLOBAL_THRESHOLDS_ENABLED, globalThresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.GLOBAL_HIGH_THRESHOLD, globalHighThreshold);
        adminConfig.setSystemProperty(CxParam.GLOBAL_MEDIUM_THRESHOLD, globalMediumThreshold);
        adminConfig.setSystemProperty(CxParam.GLOBAL_LOW_THRESHOLD, globalLowThreshold);
        adminConfig.setSystemProperty(CxParam.GLOBAL_OSA_THRESHOLDS_ENABLED, globalOsaThresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.GLOBAL_OSA_HIGH_THRESHOLD, globalOsaHighThreshold);
        adminConfig.setSystemProperty(CxParam.GLOBAL_OSA_MEDIUM_THRESHOLD, globalOsaMediumThreshold);
        adminConfig.setSystemProperty(CxParam.GLOBAL_OSA_LOW_THRESHOLD, globalOsaLowThreshold);
        ((AdministrationConfigurationPersister) ContainerManager.getComponent("administrationConfigurationPersister")).saveAdministrationConfiguration(adminConfig);

        addActionMessage(getText("cxDefaultConfigSuccess.label"));
        return SUCCESS;
    }


    private boolean validateNotEmpty(@NotNull String value, @NotNull String key) { //TODO unite the validation to one class
        boolean ret = false;
        if (value == null || StringUtils.isEmpty(value)) {
            addFieldError(key, getText(key + "." + ERROR));
            ret = true;
        }
        return ret;
    }

    private void validateUrl(final String spec) throws MalformedURLException {
        URL url = new URL(spec);
        if (url.getPath().length() > 0) {
            throw new MalformedURLException("must not contain path");
        }
    }

    private String encrypt(String password) {
        String encPass;
        try {
            encPass = new EncryptionServiceImpl().encrypt(password);
        } catch (EncryptionException e) {
            encPass = "";
        }
        return encPass;
    }

    /*************** Setters & Getters  ****************************/
    public String getGlobalServerUrl() {
        return globalServerUrl;
    }

    public void setGlobalServerUrl(String globalServerUrl) {
        this.globalServerUrl = globalServerUrl;
    }

    public String getGlobalUserName() {
        return globalUserName;
    }

    public void setGlobalUserName(String globalUserName) {
        this.globalUserName = globalUserName;
    }

    public String getGlobalPassword() {
        return globalPassword;
    }

    public void setGlobalPassword(String globalPassword) {
        this.globalPassword = globalPassword;
    }

    public String getGlobalFilterPatterns() {
        return globalFilterPatterns;
    }

    public void setGlobalFilterPatterns(String globalFilterPatterns) {
        this.globalFilterPatterns = globalFilterPatterns;
    }

    public String getGlobalFolderExclusions() {
        return globalFolderExclusions;
    }

    public void setGlobalFolderExclusions(String globalFolderExclusions) {
        this.globalFolderExclusions = globalFolderExclusions;
    }

    public String getGlobalIsSynchronous() {
        return globalIsSynchronous;
    }

    public void setGlobalIsSynchronous(String globalIsSynchronous) {
        this.globalIsSynchronous = globalIsSynchronous;
    }

    public String getGlobalScanTimeoutInMinutes() {
        return globalScanTimeoutInMinutes;
    }

    public void setGlobalScanTimeoutInMinutes(String globalScanTimeoutInMinutes) {
        this.globalScanTimeoutInMinutes = globalScanTimeoutInMinutes;
    }

    public String getGlobalThresholdsEnabled() {
        return globalThresholdsEnabled;
    }

    public void setGlobalThresholdsEnabled(String globalThresholdsEnabled) {
        this.globalThresholdsEnabled = globalThresholdsEnabled;
    }

    public String getGlobalHighThreshold() {
        return globalHighThreshold;
    }

    public void setGlobalHighThreshold(String globalHighThreshold) {
        this.globalHighThreshold = globalHighThreshold;
    }

    public String getGlobalMediumThreshold() {
        return globalMediumThreshold;
    }

    public void setGlobalMediumThreshold(String globalMediumThreshold) {
        this.globalMediumThreshold = globalMediumThreshold;
    }

    public String getGlobalLowThreshold() {
        return globalLowThreshold;
    }

    public void setGlobalLowThreshold(String globalLowThreshold) {
        this.globalLowThreshold = globalLowThreshold;
    }

    public String getGlobalOsaThresholdsEnabled() {
        return globalOsaThresholdsEnabled;
    }

    public void setGlobalOsaThresholdsEnabled(String globalOsaThresholdsEnabled) {
        this.globalOsaThresholdsEnabled = globalOsaThresholdsEnabled;
    }

    public String getGlobalOsaHighThreshold() {
        return globalOsaHighThreshold;
    }

    public void setGlobalOsaHighThreshold(String globalOsaHighThreshold) {
        this.globalOsaHighThreshold = globalOsaHighThreshold;
    }

    public String getGlobalOsaMediumThreshold() {
        return globalOsaMediumThreshold;
    }

    public void setGlobalOsaMediumThreshold(String globalOsaMediumThreshold) {
        this.globalOsaMediumThreshold = globalOsaMediumThreshold;
    }

    public String getGlobalOsaLowThreshold() {
        return globalOsaLowThreshold;
    }

    public void setGlobalOsaLowThreshold(String globalOsaLowThreshold) {
        this.globalOsaLowThreshold = globalOsaLowThreshold;
    }
}
