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

import static com.cx.plugin.dto.CxParam.*;


/**
 * CxGlobalConfig is a GlobalAdminAction that populates cxGlobalConfig.ftl and handles the incoming data from it
 * <p>
 * It is being activated through the Bamboo framework,
 * as configured under checkmarx-default-config-xwork in atlassian-plugin.xml
 */

public class CxGlobalConfig extends GlobalAdminAction {
    private String globalServerUrl;
    private String globalUserName;
    private String globalPassword;

    private String globalFilterPatterns = DEFAULT_FILTER_PATTERNS;
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

        globalServerUrl = adminConfig.getSystemProperty(GLOBAL_SERVER_URL);
        globalUserName = adminConfig.getSystemProperty(GLOBAL_USER_NAME);
        globalPassword = adminConfig.getSystemProperty(GLOBAL_PASSWORD);

        globalFolderExclusions = adminConfig.getSystemProperty(GLOBAL_FOLDER_EXCLUSION);
        String filterProperty = adminConfig.getSystemProperty(GLOBAL_FILTER_PATTERN);
        if (filterProperty != null) {
            globalFilterPatterns = filterProperty;
        }

        globalScanTimeoutInMinutes = adminConfig.getSystemProperty(GLOBAL_SCAN_TIMEOUT_IN_MINUTES);
        String isSyn = adminConfig.getSystemProperty(GLOBAL_IS_SYNCHRONOUS);
        globalIsSynchronous = isSyn == null ? "true" : isSyn;
        globalThresholdsEnabled = adminConfig.getSystemProperty(GLOBAL_THRESHOLDS_ENABLED);
        globalHighThreshold = adminConfig.getSystemProperty(GLOBAL_HIGH_THRESHOLD);
        globalMediumThreshold = adminConfig.getSystemProperty(GLOBAL_MEDIUM_THRESHOLD);
        globalLowThreshold = adminConfig.getSystemProperty(GLOBAL_LOW_THRESHOLD);
        globalOsaThresholdsEnabled = adminConfig.getSystemProperty(GLOBAL_OSA_THRESHOLDS_ENABLED);
        globalOsaHighThreshold = adminConfig.getSystemProperty(GLOBAL_OSA_HIGH_THRESHOLD);
        globalOsaMediumThreshold = adminConfig.getSystemProperty(GLOBAL_OSA_MEDIUM_THRESHOLD);
        globalOsaLowThreshold = adminConfig.getSystemProperty(GLOBAL_OSA_LOW_THRESHOLD);
        return INPUT;
    }

    public String save() {
        boolean error = isURLInvalid(globalServerUrl);
        error |= isScanTimeoutInvalid();

        if ("true".equals(globalIsSynchronous)) {
            if ("true".equals(globalThresholdsEnabled)) {
                error |= isNegative(getGlobalHighThreshold(), GLOBAL_HIGH_THRESHOLD);
                error |= isNegative(getGlobalMediumThreshold(), GLOBAL_MEDIUM_THRESHOLD);
                error |= isNegative(getGlobalLowThreshold(), GLOBAL_LOW_THRESHOLD);
            }
            if ("true".equals(globalOsaThresholdsEnabled)) {
                error |= isNegative(getGlobalOsaHighThreshold(), GLOBAL_OSA_HIGH_THRESHOLD);
                error |= isNegative(getGlobalOsaMediumThreshold(), GLOBAL_OSA_MEDIUM_THRESHOLD);
                error |= isNegative(getGlobalOsaLowThreshold(), GLOBAL_OSA_LOW_THRESHOLD);
            }
        }
        if (error) {
            return ERROR;
        }

        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(ADMINISTRATION_CONFIGURATION);
        adminConfig.setSystemProperty(GLOBAL_SERVER_URL, globalServerUrl);
        adminConfig.setSystemProperty(GLOBAL_USER_NAME, globalUserName);
        adminConfig.setSystemProperty(GLOBAL_PASSWORD, encrypt(globalPassword));

        adminConfig.setSystemProperty(GLOBAL_FOLDER_EXCLUSION, globalFolderExclusions);
        adminConfig.setSystemProperty(GLOBAL_FILTER_PATTERN, globalFilterPatterns);
        adminConfig.setSystemProperty(GLOBAL_SCAN_TIMEOUT_IN_MINUTES, globalScanTimeoutInMinutes);

        adminConfig.setSystemProperty(GLOBAL_IS_SYNCHRONOUS, globalIsSynchronous);
        if (globalIsSynchronous == null) {
            globalThresholdsEnabled = null;
            globalOsaThresholdsEnabled = null;
        }
        adminConfig.setSystemProperty(GLOBAL_THRESHOLDS_ENABLED, globalThresholdsEnabled);
        adminConfig.setSystemProperty(GLOBAL_HIGH_THRESHOLD, globalHighThreshold);
        adminConfig.setSystemProperty(GLOBAL_MEDIUM_THRESHOLD, globalMediumThreshold);
        adminConfig.setSystemProperty(GLOBAL_LOW_THRESHOLD, globalLowThreshold);
        adminConfig.setSystemProperty(GLOBAL_OSA_THRESHOLDS_ENABLED, globalOsaThresholdsEnabled);
        adminConfig.setSystemProperty(GLOBAL_OSA_HIGH_THRESHOLD, globalOsaHighThreshold);
        adminConfig.setSystemProperty(GLOBAL_OSA_MEDIUM_THRESHOLD, globalOsaMediumThreshold);
        adminConfig.setSystemProperty(GLOBAL_OSA_LOW_THRESHOLD, globalOsaLowThreshold);
        ((AdministrationConfigurationPersister) ContainerManager.getComponent("administrationConfigurationPersister")).saveAdministrationConfiguration(adminConfig);

        addActionMessage(getText("cxDefaultConfigSuccess.label"));
        return SUCCESS;
    }


    private boolean isURLInvalid(final String value) {
        boolean ret = false;
        if (!StringUtils.isEmpty(value)) {
            try {
                URL url = new URL(value);
                if (url.getPath().length() > 0) {
                    addFieldError(GLOBAL_SERVER_URL, ("URL must not contain path"));
                    ret = true;
                }
            } catch (MalformedURLException e) {
                addFieldError(GLOBAL_SERVER_URL, getText(GLOBAL_SERVER_URL + "." + ERROR + ".malformed"));
                ret = true;
            }
        }
        return ret;
    }


    private boolean isScanTimeoutInvalid() {
        boolean ret = false;
        String scanTimeout = getGlobalScanTimeoutInMinutes();
        if (!StringUtils.isEmpty(scanTimeout)) {
            try {
                int num = Integer.parseInt(scanTimeout);
                if (num <= 0) {
                    addFieldError(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES, getText(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES + ".notPositive"));
                    ret = true;
                }

            } catch (Exception e) {
                addFieldError(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES, getText(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES + ".notPositive"));
                ret = true;
            }
        }
        return ret;

    }

    private boolean isNegative(@NotNull String value, @NotNull String key) { //TODO unite the validation to one class
        boolean ret = false;
        if (!StringUtils.isEmpty(value)) {
            try {
                int num = Integer.parseInt(value);
                if (num < 0) {
                    addFieldError(key, getText(key + ".notPositive"));
                    ret = true;
                }

            } catch (Exception e) {
                addFieldError(key, getText(key + ".notPositive"));
                ret = true;
            }
        }
        return ret;
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
        this.globalScanTimeoutInMinutes = globalScanTimeoutInMinutes.trim();
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
