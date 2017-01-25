package com.cx.plugin;

/**
 * Created by galn on 20/12/2016.
 */

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.ww2.actions.build.admin.config.task.ConfigureBuildTasks;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import com.checkmarx.v7.ArrayOfGroup;
import com.checkmarx.v7.Group;
import com.cx.client.CxClientService;
import com.cx.client.CxClientServiceImpl;
import com.cx.plugin.dto.CxObject;
import com.cx.plugin.dto.CxParam;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentTaskConfigurator extends AbstractTaskConfigurator {
    protected static final String OPTION_TRUE = "true";
    protected static final String OPTION_FALSE = "false";

    private static final String DEFAULT_FILTER_PATTERN = "!**/_cvs/**/*, !**/.svn/**/*,   !**/.hg/**/*,   !**/.git/**/*,  !**/.bzr/**/*, !**/bin/**/*," +
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


    private static Map<String, String> presetList;//= new HashMap<String, String>();
    private static Map<String, String> teamPathList;// = new ArrayList<CxObject>();
    private CxClientService cxClientService = null;
    public static final String GLOBAL_CONFIGURATION = "globalConfiguration";
    public static final String COSTUME_CONFIGURATION = "costumeConfiguration";
    private static final Map CONFIGURATION_MODE_TYPES_MAP = ImmutableMap
            .of(GLOBAL_CONFIGURATION, "Use Default Setting",
                    COSTUME_CONFIGURATION, "Specific Task Setting");

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {

        Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION.value());


        config = generateCredentialsFields(params, adminConfig, config);

        config.put(CxParam.CX_PROJECT_NAME.value(), params.getString(CxParam.CX_PROJECT_NAME.value()));
        config.put(CxParam.GENERATE_PDF_REPORT.value(), params.getString(CxParam.GENERATE_PDF_REPORT.value()));
        //config.put(CxParam.OUTPUT_DIRECTORY.value(), params.getString(CxParam.OUTPUT_DIRECTORY.value()));

        String cxPreset = params.getString(CxParam.PRESET.value());
        if (cxPreset != null && !cxPreset.equals(CxParam.NO_SESSION)) {//TODO
            config.put(CxParam.PRESET_NAME.value(), presetList.get(cxPreset));
            config.put(CxParam.PRESET.value(), cxPreset);
        }

        String cxTeam = params.getString(CxParam.TEAM_PATH.value());
        if (cxTeam != null && !cxTeam.equals(CxParam.NO_SESSION)) {//TODO
            config.put(CxParam.TEAM_PATH_NAME.value(), teamPathList.get(cxTeam));
            config.put(CxParam.TEAM_PATH.value(), cxTeam);
        }

        config.put(CxParam.OSA_ENABLED.value(), params.getString(CxParam.OSA_ENABLED.value()));
        config.put(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value(), params.getString(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value()));

        //fill CxSAST Scan fields
        final String useDefaultCxSASTConfig = params.getString(CxParam.DEFAULT_CXSAST.value());
        config = generateCxSASTFields(params, adminConfig, config, useDefaultCxSASTConfig);

        //fill Scan Control fields scan fields
        final String useDefaultScanControl = params.getString(CxParam.DEFAULT_SCAN_CONTROL.value());
        config = generateScanControlFields(params, adminConfig, config, useDefaultScanControl);

        return config;
    }

    @Override //TODO- remove all the params without a default value
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION.value());

        context.put(CxParam.DEFAULT_CREDENTIALS.value(), OPTION_TRUE);
        // context.put(CxParam.DEFAULT_CREDENTIALS.value(), GLOBAL_CONFIGURATION);
        context.put(CxParam.SERVER_URL.value(), CxParam.DEFAULT_URL.value());
        populateProjectSelectFields(null, null, context, adminConfig);

        context.put("configurationModeTypes", CONFIGURATION_MODE_TYPES_MAP);

        context.put(CxParam.DEFAULT_CXSAST.value(), GLOBAL_CONFIGURATION);
        context.put(CxParam.DEFAULT_CREDENTIALS.value(), OPTION_TRUE);
        context.put(CxParam.DEFAULT_SCAN_CONTROL.value(), GLOBAL_CONFIGURATION);
        context.put(CxParam.IS_INCREMENTAL_SCAN.value(), OPTION_FALSE);
        context.put(CxParam.FILTER_PATTERN.value(), DEFAULT_FILTER_PATTERN);

        context.put(CxParam.IS_SYNCHRONOUS.value(), OPTION_TRUE);
        context.put(CxParam.THRESHOLDS_ENABLED.value(), OPTION_FALSE);
        context.put(CxParam.GENERATE_PDF_REPORT.value(), OPTION_FALSE);
        context.put(CxParam.OSA_ENABLED.value(), OPTION_FALSE);
        context.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), OPTION_FALSE);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        Map<String, String> configMap = taskDefinition.getConfiguration();
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION.value());

        context.put("configurationModeTypes", CONFIGURATION_MODE_TYPES_MAP);

        final Boolean useDefaultCredentials = configMap.get(CxParam.DEFAULT_CREDENTIALS.value()).equals(OPTION_TRUE);

        context.put(CxParam.DEFAULT_CREDENTIALS.value(), useDefaultCredentials);
        context.put(CxParam.CX_PROJECT_NAME.value(), configMap.get(CxParam.CX_PROJECT_NAME.value()));

        final String preset = configMap.get(CxParam.PRESET.value());
        final String team = configMap.get(CxParam.TEAM_PATH.value());
        //context.put("testConnection", new TestConnection());

        if (useDefaultCredentials) {
            populateProjectSelectFields(preset, team, context, adminConfig);
        } else {
            final String serverUrl = configMap.get(CxParam.SERVER_URL.value());
            final String userName = configMap.get(CxParam.USER_NAME.value());
            String password = configMap.get(CxParam.PASSWORD.value());//TODO change to changePassword
            context.put(CxParam.SERVER_URL.value(), serverUrl);
            context.put(CxParam.USER_NAME.value(), userName);
            context.put(CxParam.PASSWORD.value(), password);
            populateProjectSelectFields(serverUrl, userName, password, preset, team, context);
        }

        final String useDefaultCxSASTConfig = configMap.get(CxParam.DEFAULT_CXSAST.value());
        context.put(CxParam.DEFAULT_CXSAST.value(), useDefaultCxSASTConfig);
        if (useDefaultCxSASTConfig.equals(COSTUME_CONFIGURATION)) {
            context.put(CxParam.IS_INCREMENTAL_SCAN.value(), configMap.get(CxParam.IS_INCREMENTAL_SCAN.value()));
            context.put(CxParam.FOLDER_EXCLUSION.value(), configMap.get(CxParam.FOLDER_EXCLUSION.value()));
            context.put(CxParam.FILTER_PATTERN.value(), configMap.get(CxParam.FILTER_PATTERN.value()));
            context.put(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), configMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES.value()));
        }
        context.put(CxParam.GENERATE_PDF_REPORT.value(), configMap.get(CxParam.GENERATE_PDF_REPORT.value()));
        context.put(CxParam.OSA_ENABLED.value(), configMap.get(CxParam.OSA_ENABLED.value()));
        context.put(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value(), configMap.get(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value()));

        final String useDefaultScanControl = configMap.get(CxParam.DEFAULT_SCAN_CONTROL.value());
        context.put(CxParam.DEFAULT_SCAN_CONTROL.value(), configMap.get(CxParam.DEFAULT_SCAN_CONTROL.value()));
        if (useDefaultScanControl.equals(COSTUME_CONFIGURATION)) {
            context.put(CxParam.IS_SYNCHRONOUS.value(), configMap.get(CxParam.IS_SYNCHRONOUS.value()));
            context.put(CxParam.THRESHOLDS_ENABLED.value(), configMap.get(CxParam.THRESHOLDS_ENABLED.value()));
            context.put(CxParam.HIGH_THRESHOLD.value(), configMap.get(CxParam.HIGH_THRESHOLD.value()));
            context.put(CxParam.MEDIUM_THRESHOLD.value(), configMap.get(CxParam.MEDIUM_THRESHOLD.value()));
            context.put(CxParam.LOW_THRESHOLD.value(), configMap.get(CxParam.LOW_THRESHOLD.value()));
            context.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), configMap.get(CxParam.OSA_THRESHOLDS_ENABLED.value()));
            context.put(CxParam.OSA_HIGH_THRESHOLD.value(), configMap.get(CxParam.OSA_HIGH_THRESHOLD.value()));
            context.put(CxParam.OSA_MEDIUM_THRESHOLD.value(), configMap.get(CxParam.OSA_MEDIUM_THRESHOLD.value()));
            context.put(CxParam.OSA_LOW_THRESHOLD.value(), configMap.get(CxParam.OSA_LOW_THRESHOLD.value()));
        }
    }

    private void populateProjectSelectFields(final String cxPreset, final String cxTeam, @NotNull final Map<String, Object> context, AdministrationConfiguration adminConfig) {
        final String cxServerUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL.value());
        final String cxUser = adminConfig.getSystemProperty(CxParam.USER_NAME.value());
        String cxPass = adminConfig.getSystemProperty(CxParam.PASSWORD.value());

        context.put(CxParam.SERVER_URL.value(), cxServerUrl);
        context.put(CxParam.USER_NAME.value(), cxUser);
        context.put(CxParam.PASSWORD.value(), cxPass);

        populateProjectSelectFields(cxServerUrl, cxUser, cxPass, cxPreset, cxTeam, context);
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {//TODO add more validations
        super.validate(params, errorCollection);
        validateNotEmpty(params, errorCollection, CxParam.USER_NAME.value());
        validateNotEmpty(params, errorCollection, CxParam.PASSWORD.value());
        validateNotEmpty(params, errorCollection, CxParam.SERVER_URL.value());
        // validateNotEmpty(params, errorCollection, CxParam.CX_PROJECT_NAME.value());
        validateNotNegative(params, errorCollection, CxParam.SCAN_TIMEOUT_IN_MINUTES.value());

       /* String nami = params.getString(CxParam.USER_NAME.value());
        String urlii = params.getString(CxParam.URL.value());
        String passi = params.getString(CxParam.PASSWORD.value());

        if (!TryLogin(nami, passi, urlii)) {
           // errorCollection.addError("login", ((ConfigureBuildTasks) errorCollection).getText("login" + ".error"));
        }
*/
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (value == null || StringUtils.isEmpty(value)) {
            errorCollection.addError(key, ((ConfigureBuildTasks) errorCollection).getText(key + ".error"));
        }
    }

    private void validateNotNegative(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (value != null && !StringUtils.isEmpty(value))
            try {
                int num = Integer.parseInt(value);
                if (num > 0) {
                    return;
                }
                errorCollection.addError("scanTimeoutInMinutes", "You did not provide a positive number of scanTimeoutInMinutes.");
            } catch (Exception e) {
                errorCollection.addError(key, "You did not provide a positive number");
            }
    }

    //TODO change the mthod to get the list only when created
    private void populateProjectSelectFields(final String serverUrl, final String userName, final String password, String preset, String teamPath,
                                             @NotNull final Map<String, Object> context) {

        if (serverUrl != null && !StringUtils.isEmpty(serverUrl) && userName != null && !StringUtils.isEmpty(userName) && password != null && !StringUtils.isEmpty(password)) {
            try {

                if (TryLogin(userName, password, serverUrl)) { //TODO handle Exceptions and handle url exceptiorn

                    presetList = convertPresetType(cxClientService.getPresetList());
                    context.put(CxParam.PRESET_LIST.value(), presetList);
                    //     presetName = presetList.get(preset);

                    if (preset != null && !StringUtils.isEmpty(preset)) {//TODO what if presetList not contain??
                        context.put(CxParam.PRESET.value(), preset);
                    }

                    teamPathList = convertTeamPathType(cxClientService.getAssociatedGroupsList());
                    context.put(CxParam.TEAM_PATH_LIST.value(), teamPathList);
                    //       teamPathName = teamPathList.get(teamPath);
                    if (teamPath != null && !StringUtils.isEmpty(teamPath)) {
                        context.put(CxParam.TEAM_PATH.value(), teamPath);
                    }
                }
            } catch (Exception e) {  //TODO handle the right exceptions
                System.out.println("Exception caught: '" + e.getMessage() + "'");
            }

            return;
        }

        final List<CxObject> noPresets = new ArrayList<CxObject>();//TODO
        // noPresets.add(new CxObject("noPreset", this.getI18nBean().getText(CxParam.PRESET.value() + "." + CxParam.NO_SESSION.value()))); //TODO
        context.put(CxParam.PRESET.value(), noPresets);

        final List<CxObject> noTeams = new ArrayList<CxObject>();//TODO
        //noTeams.add(new CxObject("noTeam", this.getI18nBean().getText(CxParam.TEAM_PATH.value() + "." + CxParam.NO_SESSION.value()))); //TODO
        context.put(CxParam.TEAM_PATH_LIST.value(), noTeams);
    }

    private Map<String, String> generateCredentialsFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {
        String cxServerUrl;
        String cxUser;
        String cxPass;
        if (!params.getBoolean(CxParam.DEFAULT_CREDENTIALS.value())) {
            cxServerUrl = params.getString(CxParam.SERVER_URL.value());
            cxUser = params.getString(CxParam.USER_NAME.value());
            cxPass = encrypt(params.getString(CxParam.PASSWORD.value()));
        } else {
            cxServerUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL.value());
            cxUser = adminConfig.getSystemProperty(CxParam.USER_NAME.value());
            cxPass = adminConfig.getSystemProperty(CxParam.PASSWORD.value());
        }

        config.put(CxParam.DEFAULT_CREDENTIALS.value(), params.getString(CxParam.DEFAULT_CREDENTIALS.value()));
        config.put(CxParam.SERVER_URL.value(), cxServerUrl);
        config.put(CxParam.USER_NAME.value(), cxUser);
        config.put(CxParam.PASSWORD.value(), cxPass);

        return config;
    }

    private Map<String, String> generateCxSASTFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config, String useDefaultCxSASTConfig) {
        String isIncremental;
        String folderExclusions;
        String filterPattern;
        String scanTimeout;

        if (useDefaultCxSASTConfig.equals(COSTUME_CONFIGURATION)) {
            isIncremental = params.getString(CxParam.IS_INCREMENTAL_SCAN.value());
            folderExclusions = params.getString(CxParam.FOLDER_EXCLUSION.value());
            filterPattern = params.getString(CxParam.FILTER_PATTERN.value());
            scanTimeout = params.getString(CxParam.SCAN_TIMEOUT_IN_MINUTES.value());
        } else {
            isIncremental = adminConfig.getSystemProperty(CxParam.IS_INCREMENTAL_SCAN.value());
            folderExclusions = adminConfig.getSystemProperty(CxParam.FOLDER_EXCLUSION.value());
            filterPattern = adminConfig.getSystemProperty(CxParam.FILTER_PATTERN.value());
            scanTimeout = adminConfig.getSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES.value());
        }
        config.put(CxParam.DEFAULT_CXSAST.value(), useDefaultCxSASTConfig);
        config.put(CxParam.IS_INCREMENTAL_SCAN.value(), isIncremental);
        config.put(CxParam.FOLDER_EXCLUSION.value(), folderExclusions);
        config.put(CxParam.FILTER_PATTERN.value(), filterPattern);
        config.put(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), scanTimeout);

        return config;
    }

    private Map<String, String> generateScanControlFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config, String useDefaultScanControl) {

        String isSynchronous;
        String thresholdsEnabled;
        String highThreshold;
        String mediumThreshold;
        String lowThreshold;
        String osaThresholdsEnabled;
        String osaHighThreshold;
        String osaMediumThreshold;
        String osaLowThreshold;
        if (useDefaultScanControl.equals(COSTUME_CONFIGURATION)) {
            isSynchronous = params.getString(CxParam.IS_SYNCHRONOUS.value());
            thresholdsEnabled = params.getString(CxParam.THRESHOLDS_ENABLED.value());
            highThreshold = params.getString(CxParam.HIGH_THRESHOLD.value());
            mediumThreshold = params.getString(CxParam.MEDIUM_THRESHOLD.value());
            lowThreshold = params.getString(CxParam.LOW_THRESHOLD.value());
            osaThresholdsEnabled = params.getString(CxParam.OSA_THRESHOLDS_ENABLED.value());
            osaHighThreshold = params.getString(CxParam.OSA_HIGH_THRESHOLD.value());
            osaMediumThreshold = params.getString(CxParam.OSA_MEDIUM_THRESHOLD.value());
            osaLowThreshold = params.getString(CxParam.OSA_LOW_THRESHOLD.value());
        } else {
            isSynchronous = adminConfig.getSystemProperty(CxParam.IS_SYNCHRONOUS.value());
            thresholdsEnabled = adminConfig.getSystemProperty(CxParam.THRESHOLDS_ENABLED.value());
            highThreshold = adminConfig.getSystemProperty(CxParam.HIGH_THRESHOLD.value());
            mediumThreshold = adminConfig.getSystemProperty(CxParam.MEDIUM_THRESHOLD.value());
            lowThreshold = adminConfig.getSystemProperty(CxParam.LOW_THRESHOLD.value());
            osaThresholdsEnabled = adminConfig.getSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED.value());
            osaHighThreshold = adminConfig.getSystemProperty(CxParam.OSA_HIGH_THRESHOLD.value());
            osaMediumThreshold = adminConfig.getSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD.value());
            osaLowThreshold = adminConfig.getSystemProperty(CxParam.OSA_LOW_THRESHOLD.value());
        }

        config.put(CxParam.DEFAULT_SCAN_CONTROL.value(), useDefaultScanControl);
        config.put(CxParam.IS_SYNCHRONOUS.value(), isSynchronous);
        config.put(CxParam.THRESHOLDS_ENABLED.value(), thresholdsEnabled);
        config.put(CxParam.HIGH_THRESHOLD.value(), highThreshold);
        config.put(CxParam.MEDIUM_THRESHOLD.value(), mediumThreshold);
        config.put(CxParam.LOW_THRESHOLD.value(), lowThreshold);
        config.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), osaThresholdsEnabled);
        config.put(CxParam.OSA_HIGH_THRESHOLD.value(), osaHighThreshold);
        config.put(CxParam.OSA_MEDIUM_THRESHOLD.value(), osaMediumThreshold);
        config.put(CxParam.OSA_LOW_THRESHOLD.value(), osaLowThreshold);

        return config;
    }

    private boolean TryLogin(String userName, String cxPass, String serverUrl) {
        cxPass = decrypt(cxPass);
        try {
            if (serverUrl == null) {
                serverUrl = "";
            }
            URL cxUrl = new URL(serverUrl); //TODO handle exception if its empty
            cxClientService = new CxClientServiceImpl(cxUrl, userName, cxPass); //TODO ask dor when change the password
            cxClientService.loginToServer();
            return true;

        } catch (Exception CxClientException) {
            System.out.println("Exception caught: '" + CxClientException.getMessage() + "'");//TODO
            cxClientService = null;
            return false;
        }
    }

    private Map<String, String> convertPresetType(List<com.checkmarx.v7.Preset> oldType) {
        Map<String, String> newType = new HashMap<String, String>();
        for (com.checkmarx.v7.Preset preset : oldType) {
            newType.put(Long.toString(preset.getID()), preset.getPresetName().toString());
        }
        return newType;
    }

    private Map<String, String> convertTeamPathType(ArrayOfGroup oldType) {
        Map<String, String> newType = new HashMap<String, String>();
        for (Group group : oldType.getGroup()) {
            newType.put(group.getID(), group.getGroupName());
        }
        return newType;
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

    private String decrypt(String password) {
        String encPass;
        try {
            encPass = new EncryptionServiceImpl().decrypt(password);
        } catch (EncryptionException e) {
            encPass = "";
        }

        return encPass;
    }
}





/*
    public void validateConnection(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {

        final String value = params.getString(key);
    }
*/


//  @BoundClass(bindingName="filterStationRelationships")
  /*  public class TestConnection implements TemplateMethodModelEx {

        public String exec(List args) throws TemplateModelException {
            boolean ret = false;
            try {
                if (args.size() != 3) {
                    throw new TemplateModelException("Wrong arguments");
                }

                ret = TryLogin(args.get(0).toString(), args.get(1).toString(), args.get(2).toString());
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            if (ret){
                return "Success";
            }
            else return "login Failed!";
        }

    }*/