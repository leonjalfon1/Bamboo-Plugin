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
import com.atlassian.util.concurrent.Nullable;
import com.checkmarx.v7.ArrayOfGroup;
import com.checkmarx.v7.Group;
import com.cx.client.CxClientService;
import com.cx.client.CxClientServiceImpl;
import com.cx.plugin.dto.CxParam;
import com.cx.plugin.dto.ValueComparator;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class AgentTaskConfigurator extends AbstractTaskConfigurator {
    private long DEFAULT_PRESET_ID = 17;//TODO

    private String DEFAULT_TEAM_ID = "1"; //TODO

    private static Map<String, String> presetList = new HashMap<String, String>();
    private static Map<String, String> teamPathList = new HashMap<String, String>();
    private CxClientService cxClientService = null;
    private final static String DEFAULT_SETTING_LABEL = "Use Default Setting";
    private final static String SPECIFIC_SETTING_LABEL = "Specific Task Setting";

    private static AdministrationConfiguration adminConfig;
    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_SERVER = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_SERVER, DEFAULT_SETTING_LABEL, CxParam.COSTUME_CONFIGURATION_SERVER, SPECIFIC_SETTING_LABEL);
    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_CXSAST = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_CXSAST, DEFAULT_SETTING_LABEL, CxParam.COSTUME_CONFIGURATION_CXSAST, SPECIFIC_SETTING_LABEL);
    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_CONTROL = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_CONTROL, DEFAULT_SETTING_LABEL, CxParam.COSTUME_CONFIGURATION_CONTROL, SPECIFIC_SETTING_LABEL);


    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {

        Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        getAdminConfiguration();
        config = generateCredentialsFields(params, adminConfig, config);

        config.put(CxParam.PROJECT_NAME, params.getString(CxParam.PROJECT_NAME));
        config.put(CxParam.GENERATE_PDF_REPORT, params.getString(CxParam.GENERATE_PDF_REPORT));

        String cxPreset = params.getString(CxParam.PRESET_ID);
        if (cxPreset != null && !cxPreset.equals(CxParam.NO_PRESET) && presetList != null) {
            config.put(CxParam.PRESET_NAME, presetList.get(cxPreset));
            config.put(CxParam.PRESET_ID, cxPreset);
        }

        String cxTeam = params.getString(CxParam.TEAM_PATH_ID);
        if (cxTeam != null && !cxTeam.equals(CxParam.NO_TEAM_PATH) && teamPathList != null) {
            config.put(CxParam.TEAM_PATH_NAME, teamPathList.get(cxTeam));
            config.put(CxParam.TEAM_PATH_ID, cxTeam);
        }

        config.put(CxParam.OSA_ENABLED, params.getString(CxParam.OSA_ENABLED));
        config.put(CxParam.IS_INCREMENTAL_SCAN, params.getString(CxParam.IS_INCREMENTAL_SCAN));
        config.put(CxParam.IS_SYNCHRONOUS, params.getString(CxParam.IS_SYNCHRONOUS));

        //fill CxSAST Scan fields
        config = generateCxSASTFields(params, adminConfig, config);

        //fill Scan Control fields scan fields
        config = generateScanControlFields(params, adminConfig, config);

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        getAdminConfiguration();
        context.put("configurationModeTypesServer", CONFIGURATION_MODE_TYPES_MAP_SERVER);
        context.put("configurationModeTypesCxSAST", CONFIGURATION_MODE_TYPES_MAP_CXSAST);
        context.put("configurationModeTypesControl", CONFIGURATION_MODE_TYPES_MAP_CONTROL);
        String projectName = resolveProjectName(context);
        context.put(CxParam.PROJECT_NAME, projectName);
        String DEFAULT_URL = "http://localhost";
        context.put(CxParam.SERVER_URL, DEFAULT_URL);//TODO

        populateCredentialsFields(null, null, context, adminConfig, null, null);

        populateCxSASTFields(context, adminConfig, null, null);

        String OPTION_FALSE = "false";
        context.put(CxParam.IS_INCREMENTAL_SCAN, OPTION_FALSE);
        populateScanControlFields(context, adminConfig, null, null);

        String OPTION_TRUE = "true";
        context.put(CxParam.IS_SYNCHRONOUS, OPTION_TRUE);
        context.put(CxParam.GENERATE_PDF_REPORT, OPTION_FALSE);
        context.put(CxParam.OSA_ENABLED, OPTION_FALSE);
    }

    private String resolveProjectName(@NotNull Map<String, Object> context) {
        String projectName;
        try {
            Object plan = context.get("plan");
            Method getName = plan.getClass().getDeclaredMethod("getName", null);
            projectName = (String) getName.invoke(plan);
        } catch (Exception e) {
            projectName = "";
        }
        return projectName;
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {

        super.populateContextForEdit(context, taskDefinition);
        Map<String, String> configMap = taskDefinition.getConfiguration();
        getAdminConfiguration();

        context.put("configurationModeTypesServer", CONFIGURATION_MODE_TYPES_MAP_SERVER);
        context.put("configurationModeTypesCxSAST", CONFIGURATION_MODE_TYPES_MAP_CXSAST);
        context.put("configurationModeTypesControl", CONFIGURATION_MODE_TYPES_MAP_CONTROL);
        context.put(CxParam.PROJECT_NAME, configMap.get(CxParam.PROJECT_NAME));

        final String preset = configMap.get(CxParam.PRESET_ID);
        final String team = configMap.get(CxParam.TEAM_PATH_ID);
        populateCredentialsFields(preset, team, context, adminConfig, configMap, CxParam.DEFAULT_CREDENTIALS);

        populateCxSASTFields(context, adminConfig, configMap, CxParam.DEFAULT_CXSAST);
        context.put(CxParam.IS_INCREMENTAL_SCAN, configMap.get(CxParam.IS_INCREMENTAL_SCAN));
        context.put(CxParam.GENERATE_PDF_REPORT, configMap.get(CxParam.GENERATE_PDF_REPORT));
        context.put(CxParam.OSA_ENABLED, configMap.get(CxParam.OSA_ENABLED));

        populateScanControlFields(context, adminConfig, configMap, CxParam.DEFAULT_SCAN_CONTROL);
    }

    private void populateCredentialsFields(final String cxPreset, final String cxTeam, @NotNull final Map<String, Object> context,
                                           AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        String cxServerUrl;
        String cxUser;
        String cxPass;
        String configType = CxParam.GLOBAL_CONFIGURATION_SERVER;
        if (fieldsSection != null) {
            configType = configMap.get(fieldsSection);
        }
        if (configType.equals(CxParam.GLOBAL_CONFIGURATION_SERVER)) {
            cxServerUrl = adminConfig.getSystemProperty(CxParam.GLOBAL_SERVER_URL);
            cxUser = adminConfig.getSystemProperty(CxParam.GLOBAL_USER_NAME);
            cxPass = adminConfig.getSystemProperty(CxParam.GLOBAL_PASSWORD);

        } else {
            cxServerUrl = configMap.get(CxParam.SERVER_URL);
            cxUser = configMap.get(CxParam.USER_NAME);
            cxPass = configMap.get(CxParam.PASSWORD);
            context.put(CxParam.SERVER_URL, cxServerUrl);
            context.put(CxParam.USER_NAME, cxUser);
            context.put(CxParam.PASSWORD, cxPass);
        }

        context.put(CxParam.GLOBAL_SERVER_URL, adminConfig.getSystemProperty(CxParam.GLOBAL_SERVER_URL));
        context.put(CxParam.GLOBAL_USER_NAME, adminConfig.getSystemProperty(CxParam.GLOBAL_USER_NAME));
        context.put(CxParam.GLOBAL_PASSWORD, adminConfig.getSystemProperty(CxParam.GLOBAL_PASSWORD));
        context.put(CxParam.DEFAULT_CREDENTIALS, configType);

        populateTeamAndPresetFields(cxServerUrl, cxUser, cxPass, cxPreset, cxTeam, context);
    }

    private void populateCxSASTFields(@NotNull final Map<String, Object> context, AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        if (fieldsSection == null || configMap.get(fieldsSection).equals(CxParam.GLOBAL_CONFIGURATION_CXSAST)) {
            context.put(CxParam.DEFAULT_CXSAST, CxParam.GLOBAL_CONFIGURATION_CXSAST);
        } else {
            String filterPattern = adminConfig.getSystemProperty(CxParam.GLOBAL_FILTER_PATTERN);
            context.put(CxParam.DEFAULT_CXSAST, CxParam.COSTUME_CONFIGURATION_CXSAST);
            context.put(CxParam.FOLDER_EXCLUSION, configMap.get(CxParam.FOLDER_EXCLUSION));
            context.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, configMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES));
        }
        context.put(CxParam.FILTER_PATTERN, fieldsSection == null ? CxParam.DEFAULT_FILTER_PATTERNS : configMap.get(CxParam.FILTER_PATTERN));
        context.put(CxParam.GLOBAL_FOLDER_EXCLUSION, adminConfig.getSystemProperty(CxParam.GLOBAL_FOLDER_EXCLUSION));
        context.put(CxParam.GLOBAL_FILTER_PATTERN, adminConfig.getSystemProperty(CxParam.GLOBAL_FILTER_PATTERN));
        context.put(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES, adminConfig.getSystemProperty(CxParam.GLOBAL_SCAN_TIMEOUT_IN_MINUTES));
    }


    private void populateScanControlFields(@NotNull final Map<String, Object> context, AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        if (fieldsSection == null || configMap.get(fieldsSection).equals(CxParam.GLOBAL_CONFIGURATION_CONTROL)) {
            context.put(CxParam.DEFAULT_SCAN_CONTROL, CxParam.GLOBAL_CONFIGURATION_CONTROL);
        } else {
            context.put(CxParam.DEFAULT_SCAN_CONTROL, CxParam.COSTUME_CONFIGURATION_CONTROL);
            context.put(CxParam.IS_SYNCHRONOUS, configMap.get(CxParam.IS_SYNCHRONOUS));
            context.put(CxParam.THRESHOLDS_ENABLED, configMap.get(CxParam.THRESHOLDS_ENABLED));
            context.put(CxParam.HIGH_THRESHOLD, configMap.get(CxParam.HIGH_THRESHOLD));
            context.put(CxParam.MEDIUM_THRESHOLD, configMap.get(CxParam.MEDIUM_THRESHOLD));
            context.put(CxParam.LOW_THRESHOLD, configMap.get(CxParam.LOW_THRESHOLD));
            context.put(CxParam.OSA_THRESHOLDS_ENABLED, configMap.get(CxParam.OSA_THRESHOLDS_ENABLED));
            context.put(CxParam.OSA_HIGH_THRESHOLD, configMap.get(CxParam.OSA_HIGH_THRESHOLD));
            context.put(CxParam.OSA_MEDIUM_THRESHOLD, configMap.get(CxParam.OSA_MEDIUM_THRESHOLD));
            context.put(CxParam.OSA_LOW_THRESHOLD, configMap.get(CxParam.OSA_LOW_THRESHOLD));
        }

        context.put(CxParam.GLOBAL_IS_SYNCHRONOUS, adminConfig.getSystemProperty(CxParam.GLOBAL_IS_SYNCHRONOUS));
        context.put(CxParam.GLOBAL_THRESHOLDS_ENABLED, adminConfig.getSystemProperty(CxParam.GLOBAL_THRESHOLDS_ENABLED));
        context.put(CxParam.GLOBAL_HIGH_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_HIGH_THRESHOLD));
        context.put(CxParam.GLOBAL_MEDIUM_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_MEDIUM_THRESHOLD));
        context.put(CxParam.GLOBAL_LOW_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_LOW_THRESHOLD));
        context.put(CxParam.GLOBAL_OSA_THRESHOLDS_ENABLED, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_THRESHOLDS_ENABLED));
        context.put(CxParam.GLOBAL_OSA_HIGH_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_HIGH_THRESHOLD));
        context.put(CxParam.GLOBAL_OSA_MEDIUM_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_MEDIUM_THRESHOLD));
        context.put(CxParam.GLOBAL_OSA_LOW_THRESHOLD, adminConfig.getSystemProperty(CxParam.GLOBAL_OSA_LOW_THRESHOLD));
    }

    private void populateTeamAndPresetFields(final String serverUrl, final String userName, final String password, String preset, String teamPath,      //TODO change the method to get the list only when created??
                                             @NotNull final Map<String, Object> context) {
        try {
            if (TryLogin(userName, password, serverUrl)) { //TODO handle Exceptions and handle url exceptiorn

                presetList = convertPresetType(cxClientService.getPresetList());
                context.put(CxParam.PRESET_LIST, presetList);
                if (preset != null && !StringUtils.isEmpty(preset)) {
                    context.put(CxParam.PRESET_ID, preset);
                } else if (!presetList.isEmpty()) {
                    context.put(CxParam.PRESET_ID, DEFAULT_PRESET_ID);
                }

                teamPathList = convertTeamPathType(cxClientService.getAssociatedGroupsList());
                context.put(CxParam.TEAM_PATH_LIST, teamPathList);
                if (teamPath != null && !StringUtils.isEmpty(teamPath)) {
                    context.put(CxParam.TEAM_PATH_ID, teamPath);
                } else if (!teamPathList.isEmpty())
                    context.put(CxParam.TEAM_PATH_ID, DEFAULT_TEAM_ID);

                return;
            }
        } catch (Exception e) {  //TODO handle the right exceptions
            System.out.println("Exception caught: '" + e.getMessage() + "'");
        }

        final Map<String, String> noPresets = new HashMap<String, String>();//TODO
        noPresets.put("noPreset", "Provide the correct Checkmarx server credentials to see presets list"); //TODO bind to properties
        context.put(CxParam.PRESET_LIST, noPresets);


        final Map<String, String> noTeams = new HashMap<String, String>();//TODO
        noTeams.put("noTeamPath", "Provide the correct Checkmarx server credentials to see teams list"); //TODO bind to properties
        context.put(CxParam.TEAM_PATH_LIST, noTeams);
    }

    private Map<String, String> generateCredentialsFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {
        final String useDefaultCredentials = params.getString(CxParam.DEFAULT_CREDENTIALS);
        config.put(CxParam.DEFAULT_CREDENTIALS, useDefaultCredentials);

        if (useDefaultCredentials != null && useDefaultCredentials.equals(CxParam.COSTUME_CONFIGURATION_SERVER)) {
            config.put(CxParam.SERVER_URL, params.getString(CxParam.SERVER_URL));
            config.put(CxParam.USER_NAME, params.getString(CxParam.USER_NAME));
            config.put(CxParam.PASSWORD, encrypt(params.getString(CxParam.PASSWORD)));
        }

        return config;
    }

    private Map<String, String> generateCxSASTFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {

        final String useDefaultCxSASTConfig = params.getString(CxParam.DEFAULT_CXSAST);
        config.put(CxParam.DEFAULT_CXSAST, useDefaultCxSASTConfig);

        if (useDefaultCxSASTConfig != null && useDefaultCxSASTConfig.equals(CxParam.COSTUME_CONFIGURATION_CXSAST)) {
            config.put(CxParam.FOLDER_EXCLUSION, params.getString(CxParam.FOLDER_EXCLUSION));
            config.put(CxParam.FILTER_PATTERN, params.getString(CxParam.FILTER_PATTERN));
            config.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, params.getString(CxParam.SCAN_TIMEOUT_IN_MINUTES));
        }
        return config;
    }

    private Map<String, String> generateScanControlFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {
        final String useDefaultScanControl = params.getString(CxParam.DEFAULT_SCAN_CONTROL);
        config.put(CxParam.DEFAULT_SCAN_CONTROL, useDefaultScanControl);

        if (useDefaultScanControl != null && useDefaultScanControl.equals(CxParam.COSTUME_CONFIGURATION_CONTROL)) {
            config.put(CxParam.THRESHOLDS_ENABLED, params.getString(CxParam.THRESHOLDS_ENABLED));
            config.put(CxParam.HIGH_THRESHOLD, params.getString(CxParam.HIGH_THRESHOLD));
            config.put(CxParam.MEDIUM_THRESHOLD, params.getString(CxParam.MEDIUM_THRESHOLD));
            config.put(CxParam.LOW_THRESHOLD, params.getString(CxParam.LOW_THRESHOLD));
            config.put(CxParam.OSA_THRESHOLDS_ENABLED, params.getString(CxParam.OSA_THRESHOLDS_ENABLED));
            config.put(CxParam.OSA_HIGH_THRESHOLD, params.getString(CxParam.OSA_HIGH_THRESHOLD));
            config.put(CxParam.OSA_MEDIUM_THRESHOLD, params.getString(CxParam.OSA_MEDIUM_THRESHOLD));
            config.put(CxParam.OSA_LOW_THRESHOLD, params.getString(CxParam.OSA_LOW_THRESHOLD));
        }

        return config;
    }

    private boolean TryLogin(String userName, String cxPass, String serverUrl) {
        boolean ret = false;
        String loginError;
        if (serverUrl != null && !StringUtils.isEmpty(serverUrl) && userName != null && !StringUtils.isEmpty(userName) && cxPass != null && !StringUtils.isEmpty(cxPass)) {
            try {
                URL cxUrl = new URL(serverUrl); //TODO handle exception if its empty
                cxClientService = new CxClientServiceImpl(cxUrl, userName, decrypt(cxPass), true);
                cxClientService.loginToServer();

                ret = true;

            } catch (Exception CxClientException) {
                loginError = CxClientException.getMessage();

                System.out.println("Exception caught: " + loginError + "'");//TODO
                if (loginError.startsWith("HTTP transport")) {
                    loginError = "Invalid URL";
                }
                cxClientService = null;
                ret = false;
            }
        }
        return ret;
    }

    private Map<String, String> convertPresetType(List<com.checkmarx.v7.Preset> oldType) {
        HashMap<String, String> newType = new HashMap<String, String>();
        for (com.checkmarx.v7.Preset preset : oldType) {
            newType.put(Long.toString(preset.getID()), preset.getPresetName());
            String DEFAULT_PRESET = "Checkmarx Default";
            if (preset.getPresetName().equals(DEFAULT_PRESET)) {
                DEFAULT_PRESET_ID = preset.getID();
            }
        }

        return sortMap(newType);
    }

    private Map<String, String> convertTeamPathType(ArrayOfGroup oldType) {
        String DEFAULT_TEAM = "CxServer"; //TODO-DOR is that the right place?
        HashMap<String, String> newType = new HashMap<String, String>();
        for (Group group : oldType.getGroup()) {
            newType.put(group.getID(), group.getGroupName());
            if (group.getGroupName().equals(DEFAULT_TEAM)) {
                DEFAULT_TEAM_ID = group.getID();
            }
        }
        return sortMap(newType);
    }

    private Map<String, String> sortMap(HashMap<String, String> unsortedMap) {
        Comparator<String> comparator2 = new ValueComparator<String, String>(unsortedMap);
        TreeMap<String, String> sortedMap = new TreeMap<String, String>(comparator2);
        sortedMap.putAll(unsortedMap);

        return sortedMap;
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

    //Validation methods
    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        String useGlobal = params.getString(CxParam.DEFAULT_CREDENTIALS);
        if (useGlobal != null && useGlobal.equals(CxParam.COSTUME_CONFIGURATION_SERVER)) {
            validateNotEmpty(params, errorCollection, CxParam.USER_NAME);
            validateNotEmpty(params, errorCollection, CxParam.PASSWORD);
            validateNotEmpty(params, errorCollection, CxParam.SERVER_URL);
        } else {
            validateGlobalNotEmpty(errorCollection, CxParam.GLOBAL_SERVER_URL);
            validateGlobalNotEmpty(errorCollection, CxParam.GLOBAL_USER_NAME);
            validateGlobalNotEmpty(errorCollection, CxParam.GLOBAL_PASSWORD);
        }

        validateNotEmpty(params, errorCollection, CxParam.PROJECT_NAME);
        useGlobal = params.getString(CxParam.DEFAULT_CXSAST);
        if (useGlobal != null && useGlobal.equals(CxParam.COSTUME_CONFIGURATION_CXSAST)) {
            validateNotNegative(params, errorCollection, CxParam.SCAN_TIMEOUT_IN_MINUTES);
        }


        useGlobal = params.getString(CxParam.DEFAULT_SCAN_CONTROL);
        if (useGlobal != null && useGlobal.equals(CxParam.COSTUME_CONFIGURATION_CONTROL)) {
            validateNotNegative(params, errorCollection, CxParam.HIGH_THRESHOLD);
            validateNotNegative(params, errorCollection, CxParam.MEDIUM_THRESHOLD);
            validateNotNegative(params, errorCollection, CxParam.LOW_THRESHOLD);
            validateNotNegative(params, errorCollection, CxParam.OSA_HIGH_THRESHOLD);
            validateNotNegative(params, errorCollection, CxParam.OSA_MEDIUM_THRESHOLD);
            validateNotNegative(params, errorCollection, CxParam.OSA_LOW_THRESHOLD);
        }
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (value == null || StringUtils.isEmpty(value)) {
            errorCollection.addError(key, ((ConfigureBuildTasks) errorCollection).getText(key + ".error"));
        }
    }

    private void validateGlobalNotEmpty(@NotNull final ErrorCollection errorCollection, @NotNull String key) {
        getAdminConfiguration();
        final String value = adminConfig.getSystemProperty(key);
        if (value == null || StringUtils.isEmpty(value)) {
            errorCollection.addError(key, ((ConfigureBuildTasks) errorCollection).getText(key + ".error"));
        }
    }

    private void validateNotNegative(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (value != null && !StringUtils.isEmpty(value)) {
            try {
                int num = Integer.parseInt(value);
                if (num > 0) {
                    return;
                }
                errorCollection.addError(key, ((ConfigureBuildTasks) errorCollection).getText(key + ".notPositive"));
            } catch (Exception e) {
                errorCollection.addError(key, ((ConfigureBuildTasks) errorCollection).getText(key + ".notPositive"));
            }
        }
    }

    private void getAdminConfiguration(){
        if (adminConfig == null) {
            adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION);
        }
    }



}
