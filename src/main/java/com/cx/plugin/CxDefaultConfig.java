package com.cx.plugin;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationPersister;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.NotNull;
import com.cx.plugin.dto.CxParam;
import com.google.common.collect.ImmutableMap;
import org.codehaus.plexus.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


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


    private String isIncremental;
    private String folderExclusions;
    private String filterPattern = "!**/_cvs/**/*, !**/.svn/**/*,   !**/.hg/**/*,   !**/.git/**/*,  !**/.bzr/**/*, !**/bin/**/*," +
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

    private String isSynchronous;
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

        serverUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL.value());
        if (serverUrl == null || StringUtils.isEmpty(serverUrl)) {
            this.serverUrl = CxParam.DEFAULT_URL.value();
        }
        userName = adminConfig.getSystemProperty(CxParam.USER_NAME.value());
        password = adminConfig.getSystemProperty(CxParam.PASSWORD.value());

        isIncremental = adminConfig.getSystemProperty(CxParam.IS_INCREMENTAL_SCAN.value());
        folderExclusions = adminConfig.getSystemProperty(CxParam.FOLDER_EXCLUSION.value());
        String filterProperty = adminConfig.getSystemProperty(CxParam.FILTER_PATTERN.value());
        if (filterProperty != null){
            filterPattern = filterProperty;
        }

        scanTimeout = adminConfig.getSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES.value());
        isSynchronous = adminConfig.getSystemProperty(CxParam.IS_SYNCHRONOUS.value());
        thresholdsEnabled = adminConfig.getSystemProperty(CxParam.THRESHOLDS_ENABLED.value());
        highThreshold = adminConfig.getSystemProperty(CxParam.HIGH_THRESHOLD.value());
        mediumThreshold = adminConfig.getSystemProperty(CxParam.MEDIUM_THRESHOLD.value());
        lowThreshold = adminConfig.getSystemProperty(CxParam.LOW_THRESHOLD.value());
        osaThresholdsEnabled = adminConfig.getSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED.value());
        osaHighThreshold = adminConfig.getSystemProperty(CxParam.OSA_HIGH_THRESHOLD.value());
        osaMediumThreshold = adminConfig.getSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD.value());
        osaLowThreshold = adminConfig.getSystemProperty(CxParam.OSA_LOW_THRESHOLD.value());
        return INPUT;
    }

    public String save() { //TODO add validations
        boolean error = false;

        error |= validateNotEmpty(this.serverUrl, CxParam.SERVER_URL.value());
        if (!error) {
            try {
                validateUrl(serverUrl);
            } catch (MalformedURLException e) {
                addFieldError(CxParam.SERVER_URL.value(), getText(CxParam.SERVER_URL.value() + "." + ERROR + ".malformed"));
                error = true;
            }
        }
        error |= validateNotEmpty(this.userName, CxParam.USER_NAME.value());
        error |= validateNotEmpty(this.password, CxParam.PASSWORD.value());

        if (error) {
            return ERROR;
        }
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent("administrationConfiguration");
        adminConfig.setSystemProperty(CxParam.SERVER_URL.value(), serverUrl);
        adminConfig.setSystemProperty(CxParam.USER_NAME.value(), userName);
        adminConfig.setSystemProperty(CxParam.PASSWORD.value(), encrypt(password));

        adminConfig.setSystemProperty(CxParam.IS_INCREMENTAL_SCAN.value(), isIncremental);
        adminConfig.setSystemProperty(CxParam.FOLDER_EXCLUSION.value(), folderExclusions);
        adminConfig.setSystemProperty(CxParam.FILTER_PATTERN.value(), filterPattern);
        adminConfig.setSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), scanTimeout);

        adminConfig.setSystemProperty(CxParam.IS_SYNCHRONOUS.value(), isSynchronous);
        adminConfig.setSystemProperty(CxParam.THRESHOLDS_ENABLED.value(), thresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.HIGH_THRESHOLD.value(), highThreshold);
        adminConfig.setSystemProperty(CxParam.MEDIUM_THRESHOLD.value(), mediumThreshold);
        adminConfig.setSystemProperty(CxParam.LOW_THRESHOLD.value(), lowThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED.value(), osaThresholdsEnabled);
        adminConfig.setSystemProperty(CxParam.OSA_HIGH_THRESHOLD.value(), osaHighThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD.value(), osaMediumThreshold);
        adminConfig.setSystemProperty(CxParam.OSA_LOW_THRESHOLD.value(), osaLowThreshold);
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

    public String getIsIncremental() {
        return isIncremental;
    }

    public void setIsIncremental(String isIncermntal) {
        this.isIncremental = isIncermntal;
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

    public String getScanTimeout() {
        return scanTimeout;
    }

    public void setScanTimeout(String scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    public String getIsSynchronous() {
        return isSynchronous;
    }

    public void setIsSynchronous(String isSynchronous) {
        this.isSynchronous = isSynchronous;
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
