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
    private String serverUrl;
    private String userName;
    private String password;
    private String DEFAULT_URL = "http://localhost";


    private String folderExclusions;
    private String filterPatterns = "!**/_cvs/**/*, !**/.svn/**/*,   !**/.hg/**/*,   !**/.git/**/*,  !**/.bzr/**/*, !**/bin/**/*," +
            "!**/obj/**/*,  !**/backup/**/*, !**/.idea/**/*, !**/*.DS_Store, !**/*.ipr,     !**/*.iws,   " +
            "!**/*.bak,     !**/*.tmp,       !**/*.aac,      !**/*.aif,      !**/*.iff,     !**/*.m3u,   !**/*.mid,   !**/*.mp3,  " +
            "!**/*.mpa,     !**/*.ra,        !**/*.wav,      !**/*.wma,      !**/*.3g2,     !**/*.3gp,   !**/*.asf,   !**/*.asx,  " +
            "!**/*.avi,     !**/*.flv,       !**/*.mov,      !**/*.mp4,      !**/*.mpg,     !**/*.rm,    !**/*.swf,   !**/*.vob,  " +
            "!**/*.wmv,     !**/*.bmp,       !**/*.gif,      !**/*.jpg,      !**/*.png,     !**/*.psd,   !**/*.tif,   !**/*.swf,  " +
            "!**/*.jar,     !**/*.zip,       !**/*.rar,      !**/*.exe,      !**/*.dll,     !**/*.pdb,   !**/*.7z,    !**/*.gz,   " +
            "!**/*.tar.gz,  !**/*.tar,       !**/*.gz,       !**/*.ahtm,     !**/*.ahtml,   !**/*.fhtml, !**/*.hdm,   " +
            "!**/*.hdml,    !**/*.hsql,      !**/*.ht,       !**/*.hta,      !**/*.htc,     !**/*.htd,   !**/*.war,   !**/*.ear,  " +
            "!**/*.htmls,   !**/*.ihtml,     !**/*.mht,      !**/*.mhtm,     !**/*.mhtml,   !**/*.ssi,   !**/*.stm,   " +
            "!**/*.stml,    !**/*.ttml,      !**/*.txn,      !**/*.xhtm,     !**/*.xhtml,   !**/*.class, !**/*.iml    ";

    private String scanTimeout;
    private String thresholdsEnabled;
    private String highThreshold;
    private String mediumThreshold;
    private String lowThreshold;
    private String osaThresholdsEnabled;
    private String osaHighThreshold;
    private String osaMediumThreshold;
    private String osaLowThreshold;

    @Override
    public String execute() {
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent("administrationConfiguration");

        serverUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL);
        if (serverUrl == null || StringUtils.isEmpty(serverUrl)) {
            this.serverUrl = DEFAULT_URL;
        }
        userName = adminConfig.getSystemProperty(CxParam.USER_NAME);
        password = adminConfig.getSystemProperty(CxParam.PASSWORD);

        folderExclusions = adminConfig.getSystemProperty(CxParam.FOLDER_EXCLUSION);
        String filterProperty = adminConfig.getSystemProperty(CxParam.FILTER_PATTERN);
        if (filterProperty != null){
            filterPatterns = filterProperty;
        }

        scanTimeout = adminConfig.getSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES);
        thresholdsEnabled = adminConfig.getSystemProperty(CxParam.THRESHOLDS_ENABLED);
        highThreshold = adminConfig.getSystemProperty(CxParam.HIGH_THRESHOLD);
        mediumThreshold = adminConfig.getSystemProperty(CxParam.MEDIUM_THRESHOLD);
        lowThreshold = adminConfig.getSystemProperty(CxParam.LOW_THRESHOLD);
        osaThresholdsEnabled = adminConfig.getSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED);
        osaHighThreshold = adminConfig.getSystemProperty(CxParam.OSA_HIGH_THRESHOLD);
        osaMediumThreshold = adminConfig.getSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD);
        osaLowThreshold = adminConfig.getSystemProperty(CxParam.OSA_LOW_THRESHOLD);
        return INPUT;
    }

    public String save() { //TODO add validations
        boolean error = false;

        error |= validateNotEmpty(this.serverUrl, CxParam.SERVER_URL);
        if (!error) {
            try {
                validateUrl(serverUrl);
            } catch (MalformedURLException e) {
                addFieldError(CxParam.SERVER_URL, getText(CxParam.SERVER_URL + "." + ERROR + ".malformed"));
                error = true;
            }
        }
        error |= validateNotEmpty(this.userName, CxParam.USER_NAME);
        error |= validateNotEmpty(this.password, CxParam.PASSWORD);

        if (error) {
            return ERROR;
        }
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent("administrationConfiguration");
        adminConfig.setSystemProperty(CxParam.SERVER_URL, serverUrl);
        adminConfig.setSystemProperty(CxParam.USER_NAME, userName);
        adminConfig.setSystemProperty(CxParam.PASSWORD, encrypt(password));

        adminConfig.setSystemProperty(CxParam.FOLDER_EXCLUSION, folderExclusions);
        adminConfig.setSystemProperty(CxParam.FILTER_PATTERN, filterPatterns);
        adminConfig.setSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES, scanTimeout);

        adminConfig.setSystemProperty(CxParam.THRESHOLDS_ENABLED, thresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.HIGH_THRESHOLD, highThreshold);
        adminConfig.setSystemProperty(CxParam.MEDIUM_THRESHOLD, mediumThreshold);
        adminConfig.setSystemProperty(CxParam.LOW_THRESHOLD, lowThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED, osaThresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.OSA_HIGH_THRESHOLD, osaHighThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD, osaMediumThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_LOW_THRESHOLD, osaLowThreshold);
        ((AdministrationConfigurationPersister) ContainerManager.getComponent("administrationConfigurationPersister")).saveAdministrationConfiguration(adminConfig);

        addActionMessage(getText("cxDefaultConfigSuccess.label"));
        return SUCCESS;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String cxPass) {
        this.password = cxPass;
    }

    public String getFolderExclusions() {
        return folderExclusions;
    }

    public void setFolderExclusions(String folderExclusions) {
        this.folderExclusions = folderExclusions;
    }

    public String getFilterPatterns() {
        return filterPatterns;
    }

    public void setFilterPatterns(String filterPatterns) {
        this.filterPatterns = filterPatterns;
    }

    public String getScanTimeout() {
        return scanTimeout;
    }

    public void setScanTimeout(String scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    public String getThresholdsEnabled() {
        return thresholdsEnabled;
    }

    public void setThresholdsEnabled(String thresholdsEnabled) {
        this.thresholdsEnabled = thresholdsEnabled;
    }

    public String getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(String highThreshold) {
        this.highThreshold = highThreshold;
    }

    public String getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(String mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    public String getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(String lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    public String getOsaThresholdsEnabled() {
        return osaThresholdsEnabled;
    }

    public void setOsaThresholdsEnabled(String osaThresholdsEnabled) {
        this.osaThresholdsEnabled = osaThresholdsEnabled;
    }

    public String getOsaHighThreshold() {
        return osaHighThreshold;
    }

    public void setOsaHighThreshold(String osaHighThreshold) {
        this.osaHighThreshold = osaHighThreshold;
    }

    public String getOsaMediumThreshold() {
        return osaMediumThreshold;
    }

    public void setOsaMediumThreshold(String osaMediumThreshold) {
        this.osaMediumThreshold = osaMediumThreshold;
    }

    public String getOsaLowThreshold() {
        return osaLowThreshold;
    }

    public void setOsaLowThreshold(String osaLowThreshold) {
        this.osaLowThreshold = osaLowThreshold;
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

}
