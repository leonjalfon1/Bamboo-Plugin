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
import com.cx.plugin.dto.CxParam;
import com.cx.plugin.dto.ValueComparator;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class AgentTaskConfigurator extends AbstractTaskConfigurator {
    protected final String OPTION_TRUE = "true";
    protected final String OPTION_FALSE = "false";
    protected final String DEFAULT_URL = "http://localhost"; //TODO-DOR is that the right place?
    protected final String DEFAULT_PRESET = "Checkmarx Default";
    protected long DEFAULT_PRESET_ID = 17;//TODO
    protected final String DEFAULT_TEAM = "CxServer"; //TODO-DOR is that the right place?
    protected String DEFAULT_TEAM_ID = "1"; //TODO
    String loginError = "login Error for Yair!";//TODO

    private static Map<String, String> presetList = new HashMap<String, String>();
    private static Map<String, String> teamPathList = new HashMap<String, String>();
    private CxClientService cxClientService = null;

    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_SERVER = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_SERVER, "Use Default Setting", CxParam.COSTUME_CONFIGURATION_SERVER , "Specific Task Setting");
    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_CXSAST = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_CXSAST, "Use Default Setting", CxParam.COSTUME_CONFIGURATION_CXSAST , "Specific Task Setting");
    private static Map<String, String> CONFIGURATION_MODE_TYPES_MAP_CONTROL = ImmutableMap.of(CxParam.GLOBAL_CONFIGURATION_CONTROL, "Use Default Setting", CxParam.COSTUME_CONFIGURATION_CONTROL, "Specific Task Setting");


    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {

        Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION);

        config = generateCredentialsFields(params, adminConfig, config);

        config.put(CxParam.PROJECT_NAME, params.getString(CxParam.PROJECT_NAME));
        config.put(CxParam.GENERATE_PDF_REPORT, params.getString(CxParam.GENERATE_PDF_REPORT));

        String cxPreset = params.getString(CxParam.PRESET_ID);
        if (cxPreset != null && !cxPreset.equals(CxParam.NO_PRESET) && presetList != null) {
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
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION);

        context.put("configurationModeTypesServer", CONFIGURATION_MODE_TYPES_MAP_SERVER);
        context.put("configurationModeTypesCxSAST", CONFIGURATION_MODE_TYPES_MAP_CXSAST);
        context.put("configurationModeTypesControl", CONFIGURATION_MODE_TYPES_MAP_CONTROL);
        String projectName = resolveProjectName(context);
        context.put(CxParam.PROJECT_NAME, projectName);
        context.put(CxParam.SERVER_URL, DEFAULT_URL);//TODO


        populateCredentialsFields(null, null, context, adminConfig, null, null);

        populateCxSASTFields(context, adminConfig, null, null);

        context.put(CxParam.IS_INCREMENTAL_SCAN, OPTION_FALSE);
        populateScanControlFields(context, adminConfig, null, null);

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
        final AdministrationConfiguration adminConfig = (AdministrationConfiguration) ContainerManager.getComponent(CxParam.ADMINISTRATION_CONFIGURATION);

        context.put("configurationModeTypesServer", CONFIGURATION_MODE_TYPES_MAP_SERVER);
        context.put("configurationModeTypesCxSAST", CONFIGURATION_MODE_TYPES_MAP_CXSAST);
        context.put("configurationModeTypesControl", CONFIGURATION_MODE_TYPES_MAP_CONTROL);
        context.put(CxParam.PROJECT_NAME, configMap.get(CxParam.PROJECT_NAME));

        final String preset = configMap.get(CxParam.PRESET_ID);
        final String team = configMap.get(CxParam.TEAM_PATH_ID);
        //context.put("testConnection", new TestConnectionAction());
        populateCredentialsFields(preset, team, context, adminConfig, configMap, CxParam.DEFAULT_CREDENTIALS);

        populateCxSASTFields(context, adminConfig, configMap, CxParam.DEFAULT_CXSAST);
        context.put(CxParam.IS_INCREMENTAL_SCAN, configMap.get(CxParam.IS_INCREMENTAL_SCAN));
        context.put(CxParam.GENERATE_PDF_REPORT, configMap.get(CxParam.GENERATE_PDF_REPORT));
        context.put(CxParam.OSA_ENABLED, configMap.get(CxParam.OSA_ENABLED));

        populateScanControlFields(context, adminConfig, configMap, CxParam.DEFAULT_CXSAST);
    }

    private void populateCredentialsFields(final String cxPreset, final String cxTeam, @NotNull final Map<String, Object> context,
                                           AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        String cxServerUrl;
        String cxUser;
        String cxPass;
        String configType;
        if (fieldsSection == null) {
            configType = CxParam.GLOBAL_CONFIGURATION_SERVER;
        } else{ configType = configMap.get(fieldsSection);}

        if (configType.equals(CxParam.GLOBAL_CONFIGURATION_SERVER)) {
            cxServerUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL);
            cxUser = adminConfig.getSystemProperty(CxParam.USER_NAME);
            cxPass = adminConfig.getSystemProperty(CxParam.PASSWORD);
        } else {
            cxServerUrl = configMap.get(CxParam.SERVER_URL);
            cxUser = configMap.get(CxParam.USER_NAME);
            cxPass = configMap.get(CxParam.PASSWORD);
        }

        context.put(CxParam.DEFAULT_CREDENTIALS, configType);
        context.put(CxParam.SERVER_URL, cxServerUrl);
        context.put(CxParam.USER_NAME, cxUser);
        context.put(CxParam.PASSWORD, cxPass);
        populateTeamAndPresetFields(cxServerUrl, cxUser, cxPass, cxPreset, cxTeam, context);
    }

    private void populateCxSASTFields(@NotNull final Map<String, Object> context, AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        String folderExclusion;
        String filterPattern;
        String scanTimeout;
        String configType;
        if (fieldsSection == null) {
            configType = CxParam.GLOBAL_CONFIGURATION_CXSAST;
        }
        else{ configType = configMap.get(fieldsSection);}

        if (configType.equals(CxParam.GLOBAL_CONFIGURATION_CXSAST)) {
            folderExclusion = adminConfig.getSystemProperty(CxParam.FOLDER_EXCLUSION);
            filterPattern = (adminConfig.getSystemProperty(CxParam.FILTER_PATTERN));
            scanTimeout = (adminConfig.getSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES));
        } else {
            folderExclusion = configMap.get(CxParam.FOLDER_EXCLUSION);
            filterPattern = configMap.get(CxParam.FILTER_PATTERN);
            scanTimeout = configMap.get(CxParam.SCAN_TIMEOUT_IN_MINUTES);
        }

        context.put(CxParam.DEFAULT_CXSAST, configType);
        context.put(CxParam.FOLDER_EXCLUSION, folderExclusion);
        context.put(CxParam.FILTER_PATTERN, filterPattern);
        context.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, scanTimeout);
    }

    private void populateScanControlFields(@NotNull final Map<String, Object> context, AdministrationConfiguration adminConfig, Map<String, String> configMap, String fieldsSection) {
        String isSynchronous;
        String thresholdEnabled;
        String highThreshold;
        String mediumThreshold;
        String lowThreshold;
        String osaThresholdEnabled;
        String osaHighThreshold;
        String osaMediumThreshold;
        String osaLowThreshold;
        String configType;
        if (fieldsSection == null) {
            configType = CxParam.GLOBAL_CONFIGURATION_CONTROL;
        }
        else{ configType = configMap.get(fieldsSection);}

        if (configType.equals(CxParam.GLOBAL_CONFIGURATION_CONTROL)) {
            isSynchronous = adminConfig.getSystemProperty(CxParam.IS_SYNCHRONOUS);
            thresholdEnabled = adminConfig.getSystemProperty(CxParam.THRESHOLDS_ENABLED);
            highThreshold = adminConfig.getSystemProperty(CxParam.HIGH_THRESHOLD);
            mediumThreshold = adminConfig.getSystemProperty(CxParam.MEDIUM_THRESHOLD);
            lowThreshold = adminConfig.getSystemProperty(CxParam.LOW_THRESHOLD);
            osaThresholdEnabled = adminConfig.getSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED);
            osaHighThreshold = adminConfig.getSystemProperty(CxParam.OSA_HIGH_THRESHOLD);
            osaMediumThreshold = adminConfig.getSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD);
            osaLowThreshold = adminConfig.getSystemProperty(CxParam.OSA_LOW_THRESHOLD);
        } else {
            isSynchronous = configMap.get(CxParam.IS_SYNCHRONOUS);
            thresholdEnabled = configMap.get(CxParam.THRESHOLDS_ENABLED);
            highThreshold = configMap.get(CxParam.HIGH_THRESHOLD);
            mediumThreshold = configMap.get(CxParam.MEDIUM_THRESHOLD);
            lowThreshold = configMap.get(CxParam.LOW_THRESHOLD);
            osaThresholdEnabled = configMap.get(CxParam.OSA_THRESHOLDS_ENABLED);
            osaHighThreshold = configMap.get(CxParam.OSA_HIGH_THRESHOLD);
            osaMediumThreshold = configMap.get(CxParam.OSA_MEDIUM_THRESHOLD);
            osaLowThreshold = configMap.get(CxParam.OSA_LOW_THRESHOLD);
        }

        context.put(CxParam.DEFAULT_SCAN_CONTROL, configType);
        context.put(CxParam.IS_SYNCHRONOUS, isSynchronous);
        context.put(CxParam.THRESHOLDS_ENABLED, thresholdEnabled);
        context.put(CxParam.HIGH_THRESHOLD, highThreshold);
        context.put(CxParam.MEDIUM_THRESHOLD, mediumThreshold);
        context.put(CxParam.LOW_THRESHOLD, lowThreshold);
        context.put(CxParam.OSA_THRESHOLDS_ENABLED, osaThresholdEnabled);
        context.put(CxParam.OSA_HIGH_THRESHOLD, osaHighThreshold);
        context.put(CxParam.OSA_MEDIUM_THRESHOLD, osaMediumThreshold);
        context.put(CxParam.OSA_LOW_THRESHOLD, osaLowThreshold);
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
        String cxServerUrl;
        String cxUser;
        String cxPass;

        if (useDefaultCredentials.equals(CxParam.GLOBAL_CONFIGURATION_SERVER)) {
            cxServerUrl = adminConfig.getSystemProperty(CxParam.SERVER_URL);
            cxUser = adminConfig.getSystemProperty(CxParam.USER_NAME);
            cxPass = adminConfig.getSystemProperty(CxParam.PASSWORD);
        } else {
            cxServerUrl = params.getString(CxParam.SERVER_URL);
            cxUser = params.getString(CxParam.USER_NAME);
            cxPass = encrypt(params.getString(CxParam.PASSWORD));
        }

        config.put(CxParam.DEFAULT_CREDENTIALS, params.getString(CxParam.DEFAULT_CREDENTIALS));
        config.put(CxParam.SERVER_URL, cxServerUrl);
        config.put(CxParam.USER_NAME, cxUser);
        config.put(CxParam.PASSWORD, cxPass);

        return config;
    }

    private Map<String, String> generateCxSASTFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {

        final String useDefaultCxSASTConfig = params.getString(CxParam.DEFAULT_CXSAST);
        String folderExclusions;
        String filterPattern;
        String scanTimeout;

        if (useDefaultCxSASTConfig.equals(CxParam.GLOBAL_CONFIGURATION_CXSAST)) {
            folderExclusions = adminConfig.getSystemProperty(CxParam.FOLDER_EXCLUSION);
            filterPattern = adminConfig.getSystemProperty(CxParam.FILTER_PATTERN);
            scanTimeout = adminConfig.getSystemProperty(CxParam.SCAN_TIMEOUT_IN_MINUTES);
        } else {
            folderExclusions = params.getString(CxParam.FOLDER_EXCLUSION);
            filterPattern = params.getString(CxParam.FILTER_PATTERN);
            scanTimeout = params.getString(CxParam.SCAN_TIMEOUT_IN_MINUTES);
        }
        config.put(CxParam.DEFAULT_CXSAST, useDefaultCxSASTConfig);
        config.put(CxParam.FOLDER_EXCLUSION, folderExclusions);
        config.put(CxParam.FILTER_PATTERN, filterPattern);
        config.put(CxParam.SCAN_TIMEOUT_IN_MINUTES, scanTimeout);

        return config;
    }

    private Map<String, String> generateScanControlFields(@NotNull final ActionParametersMap params, final AdministrationConfiguration adminConfig, Map<String, String> config) {
        final String useDefaultScanControl = params.getString(CxParam.DEFAULT_SCAN_CONTROL);
        String thresholdsEnabled;
        String highThreshold;
        String mediumThreshold;
        String lowThreshold;
        String osaThresholdsEnabled;
        String osaHighThreshold;
        String osaMediumThreshold;
        String osaLowThreshold;

        if (useDefaultScanControl.equals(CxParam.GLOBAL_CONFIGURATION_CONTROL)) {
            thresholdsEnabled = adminConfig.getSystemProperty(CxParam.THRESHOLDS_ENABLED);
            highThreshold = adminConfig.getSystemProperty(CxParam.HIGH_THRESHOLD);
            mediumThreshold = adminConfig.getSystemProperty(CxParam.MEDIUM_THRESHOLD);
            lowThreshold = adminConfig.getSystemProperty(CxParam.LOW_THRESHOLD);
            osaThresholdsEnabled = adminConfig.getSystemProperty(CxParam.OSA_THRESHOLDS_ENABLED);
            osaHighThreshold = adminConfig.getSystemProperty(CxParam.OSA_HIGH_THRESHOLD);
            osaMediumThreshold = adminConfig.getSystemProperty(CxParam.OSA_MEDIUM_THRESHOLD);
            osaLowThreshold = adminConfig.getSystemProperty(CxParam.OSA_LOW_THRESHOLD);
        } else {
            thresholdsEnabled = params.getString(CxParam.THRESHOLDS_ENABLED);
            highThreshold = params.getString(CxParam.HIGH_THRESHOLD);
            mediumThreshold = params.getString(CxParam.MEDIUM_THRESHOLD);
            lowThreshold = params.getString(CxParam.LOW_THRESHOLD);
            osaThresholdsEnabled = params.getString(CxParam.OSA_THRESHOLDS_ENABLED);
            osaHighThreshold = params.getString(CxParam.OSA_HIGH_THRESHOLD);
            osaMediumThreshold = params.getString(CxParam.OSA_MEDIUM_THRESHOLD);
            osaLowThreshold = params.getString(CxParam.OSA_LOW_THRESHOLD);
        }

        config.put(CxParam.DEFAULT_SCAN_CONTROL, useDefaultScanControl);
        config.put(CxParam.THRESHOLDS_ENABLED, thresholdsEnabled);
        config.put(CxParam.HIGH_THRESHOLD, highThreshold);
        config.put(CxParam.MEDIUM_THRESHOLD, mediumThreshold);
        config.put(CxParam.LOW_THRESHOLD, lowThreshold);
        config.put(CxParam.OSA_THRESHOLDS_ENABLED, osaThresholdsEnabled);
        config.put(CxParam.OSA_HIGH_THRESHOLD, osaHighThreshold);
        config.put(CxParam.OSA_MEDIUM_THRESHOLD, osaMediumThreshold);
        config.put(CxParam.OSA_LOW_THRESHOLD, osaLowThreshold);

        return config;
    }

    private boolean TryLogin(String userName, String cxPass, String serverUrl) {
        boolean ret = false;
        if (serverUrl != null && !StringUtils.isEmpty(serverUrl) && userName != null && !StringUtils.isEmpty(userName) && cxPass != null && !StringUtils.isEmpty(cxPass)) {
            try {
                if (serverUrl == null) {
                    serverUrl = "";
                }
                URL cxUrl = new URL(serverUrl); //TODO handle exception if its empty
                cxClientService = new CxClientServiceImpl(cxUrl, userName, decrypt(cxPass), true);
                cxClientService.loginToServer();

                ret = true;

            } catch (Exception CxClientException) {
                loginError = CxClientException.getMessage();

                System.out.println("Exception caught: " +loginError + "'");//TODO
                if(loginError.startsWith("HTTP transport")){
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
            newType.put(Long.toString(preset.getID()), preset.getPresetName().toString());
            if (preset.getPresetName().equals(DEFAULT_PRESET)) {
                DEFAULT_PRESET_ID = preset.getID();
            }
        }

        Map<String, String> sortedMap = sortMap(newType);
        return sortedMap;
    }

    private Map<String, String> convertTeamPathType(ArrayOfGroup oldType) {

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
        validateNotEmpty(params, errorCollection, CxParam.USER_NAME);
        validateNotEmpty(params, errorCollection, CxParam.PASSWORD);
        validateNotEmpty(params, errorCollection, CxParam.SERVER_URL);
        validateNotEmpty(params, errorCollection, CxParam.PROJECT_NAME);
        validateNotNegative(params, errorCollection, CxParam.SCAN_TIMEOUT_IN_MINUTES);

        String useglobal = params.getString(CxParam.DEFAULT_SCAN_CONTROL);
        if(useglobal.equals(CxParam.COSTUME_CONFIGURATION_CONTROL)) {
                validateNotNegative(params, errorCollection, CxParam.HIGH_THRESHOLD);
                validateNotNegative(params, errorCollection, CxParam.MEDIUM_THRESHOLD);
                validateNotNegative(params, errorCollection, CxParam.LOW_THRESHOLD);
                validateNotNegative(params, errorCollection, CxParam.OSA_HIGH_THRESHOLD);
                validateNotNegative(params, errorCollection, CxParam.OSA_MEDIUM_THRESHOLD);
                validateNotNegative(params, errorCollection, CxParam.OSA_LOW_THRESHOLD);

        }

        String nami = params.getString(CxParam.USER_NAME);
        String passi = params.getString(CxParam.PASSWORD);
        String urlii = params.getString(CxParam.SERVER_URL);

       if (!TryLogin(nami, encrypt(passi), urlii)) {
            errorCollection.addError(CxParam.DEFAULT_CREDENTIALS, loginError);
        }
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
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
                errorCollection.addError(key,  "You did not provide a positive number.");
            } catch (Exception e) {
                errorCollection.addError(key, "You did not provide a positive number");
            }
        }
    }

}





/*
    public void validateConnection(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {

        final String value = params.getString(key);
    }
*/


//  @BoundClass(bindingName="filterStationRelationships")
  /*  public class TestConnectionAction implements TemplateMethodModelEx {

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