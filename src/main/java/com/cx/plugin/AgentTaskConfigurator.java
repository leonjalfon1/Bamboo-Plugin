package com.cx.plugin;

/**
 * Created by galn on 20/12/2016.
 */

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.ww2.actions.build.admin.config.task.ConfigureBuildTasks;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import com.checkmarx.v7.ArrayOfGroup;
import com.checkmarx.v7.Group;
import com.cx.client.CxClientService;
import com.cx.client.CxClientServiceImpl;
import com.cx.plugin.dto.CxObject;
import com.cx.plugin.dto.CxParam;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentTaskConfigurator extends AbstractTaskConfigurator {
    protected static final String OPTION_TRUE = "true";
    protected static final String OPTION_FALSE = "false";
    public static final String DEFAULT_USER_NAME = "admin@cx";
    public static final String DEFAULT_PASSWORD = "Cx123456!"; //TODO- CHANGE THIS TO adminConfig.getSystemProperty(CxGlobals.CXPASS))
    public static final String DEFAULT_URL = "http://localhost";
    public static final String DEFAULT_CX_PROJECT_NAME = "myProject";
    public static final String DEFAULT_OUTPUT_DIRECTORY = "outi!";
    public static final String DEFAULT_IS_INCREMENTAL_SCAN = OPTION_FALSE;
    public static final String DEFAULT_IS_SYNCHRONOUS = OPTION_FALSE;
    public static final String DEFAULT_EMPTY = "";//TODO not everything need default
    public static final String DEFAULT_ENABLE_THRESHOLDS = OPTION_FALSE;//TODO not everything need default
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


    private static List<CxObject> presetList = new ArrayList<CxObject>();
    private static List<CxObject> teamPathList = new ArrayList<CxObject>();
    private CxClientService cxClientService = null;
    protected String url = null; //TODO delete it, probably not needed

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {

        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(CxParam.USER_NAME.value(), params.getString(CxParam.USER_NAME.value()));
        config.put(CxParam.PASSWORD.value(), params.getString(CxParam.PASSWORD.value()));
        config.put(CxParam.URL.value(), params.getString(CxParam.URL.value()));
        config.put(CxParam.CX_PROJECT_NAME.value(), params.getString(CxParam.CX_PROJECT_NAME.value()));

        String cxPreset = params.getString(CxParam.PRESET.value());
        if (cxPreset != null && !cxPreset.equals(CxParam.NO_SESSION)) {//TODO
            cxPreset = resolveNameById(presetList, cxPreset);
            config.put(CxParam.PRESET.value(), cxPreset);
        }

        String cxTeam = params.getString(CxParam.TEAM_PATH.value());
        if (cxTeam != null && !cxTeam.equals(CxParam.NO_SESSION)) {//TODO
            cxTeam = resolveNameById(teamPathList, cxTeam);
            config.put(CxParam.TEAM_PATH.value(), cxTeam);
        }
        config.put(CxParam.FILTER_PATTERN.value(), params.getString(CxParam.FILTER_PATTERN.value()));
        config.put(CxParam.FOLDER_EXCLUSION.value(), params.getString(CxParam.FOLDER_EXCLUSION.value()));
        config.put(CxParam.OUTPUT_DIRECTORY.value(), params.getString(CxParam.OUTPUT_DIRECTORY.value()));
        config.put(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), params.getString(CxParam.SCAN_TIMEOUT_IN_MINUTES.value()));
        config.put(CxParam.IS_INCREMENTAL_SCAN.value(), params.getString(CxParam.IS_INCREMENTAL_SCAN.value()));
        config.put(CxParam.OSA_ENABLED.value(), params.getString(CxParam.OSA_ENABLED.value()));
        config.put(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value(), params.getString(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value()));
        config.put(CxParam.IS_SYNCHRONOUS.value(), params.getString(CxParam.IS_SYNCHRONOUS.value()));
        config.put(CxParam.THRESHOLDS_ENABLED.value(), params.getString(CxParam.THRESHOLDS_ENABLED.value()));
        config.put(CxParam.HIGH_THRESHOLD.value(), params.getString(CxParam.HIGH_THRESHOLD.value()));
        config.put(CxParam.MEDIUM_THRESHOLD.value(), params.getString(CxParam.MEDIUM_THRESHOLD.value()));
        config.put(CxParam.LOW_THRESHOLD.value(), params.getString(CxParam.LOW_THRESHOLD.value()));
        config.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), params.getString(CxParam.OSA_THRESHOLDS_ENABLED.value()));
        config.put(CxParam.OSA_HIGH_THRESHOLD.value(), params.getString(CxParam.OSA_HIGH_THRESHOLD.value()));
        config.put(CxParam.OSA_MEDIUM_THRESHOLD.value(), params.getString(CxParam.OSA_MEDIUM_THRESHOLD.value()));
        config.put(CxParam.OSA_LOW_THRESHOLD.value(), params.getString(CxParam.OSA_LOW_THRESHOLD.value()));


        return config;
    }

    @Override //TODO- remove all the params without a default value
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {

        super.populateContextForCreate(context);
        context.put(CxParam.USER_NAME.value(), DEFAULT_USER_NAME);
        context.put(CxParam.PASSWORD.value(), DEFAULT_PASSWORD);  //TODO-change the Default password
        context.put(CxParam.URL.value(), DEFAULT_URL);
        context.put(CxParam.CX_PROJECT_NAME.value(), DEFAULT_CX_PROJECT_NAME);

        populateProjectSelectFields(DEFAULT_USER_NAME, DEFAULT_PASSWORD, null, null, DEFAULT_URL, context);//TODO chnage the params

        context.put(CxParam.FILTER_PATTERN.value(), DEFAULT_FILTER_PATTERN);
        context.put(CxParam.OUTPUT_DIRECTORY.value(), DEFAULT_OUTPUT_DIRECTORY);
        context.put(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), DEFAULT_EMPTY);
        context.put(CxParam.IS_INCREMENTAL_SCAN.value(), DEFAULT_IS_INCREMENTAL_SCAN);
        context.put(CxParam.IS_SYNCHRONOUS.value(), DEFAULT_IS_SYNCHRONOUS);
        context.put(CxParam.THRESHOLDS_ENABLED.value(), DEFAULT_ENABLE_THRESHOLDS);
        context.put(CxParam.OSA_ENABLED.value(), OPTION_FALSE);
        context.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), OPTION_FALSE);

      //  context.put("testConnection", new TestConnection());
    }

   @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        populateContextForEditOrView(context, taskDefinition);
    }

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

    public void populateContextForEditOrView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {

        final String cxServerUrl = taskDefinition.getConfiguration().get(CxParam.URL.value());
        final String cxUser = taskDefinition.getConfiguration().get(CxParam.USER_NAME.value());
        String cxPass = taskDefinition.getConfiguration().get(CxParam.PASSWORD.value()); //TODO - Encrypt and Decrypt password
        final String cxPreset = taskDefinition.getConfiguration().get(CxParam.PRESET.value());
        final String cxTeam = taskDefinition.getConfiguration().get(CxParam.TEAM_PATH.value());
        //context.put("testConnection", new TestConnection());
        context.put(CxParam.USER_NAME.value(), cxUser);
        context.put(CxParam.PASSWORD.value(), cxPass);
        context.put(CxParam.URL.value(), cxServerUrl);
        context.put(CxParam.CX_PROJECT_NAME.value(), taskDefinition.getConfiguration().get(CxParam.CX_PROJECT_NAME.value()));

        populateProjectSelectFields(cxUser, cxPass, cxPreset, cxTeam, cxServerUrl, context);

        context.put(CxParam.FILTER_PATTERN.value(), taskDefinition.getConfiguration().get(CxParam.FILTER_PATTERN.value()));
        context.put(CxParam.FOLDER_EXCLUSION.value(), taskDefinition.getConfiguration().get(CxParam.FOLDER_EXCLUSION.value()));
        context.put(CxParam.OUTPUT_DIRECTORY.value(), taskDefinition.getConfiguration().get(CxParam.OUTPUT_DIRECTORY.value()));
        context.put(CxParam.SCAN_TIMEOUT_IN_MINUTES.value(), taskDefinition.getConfiguration().get(CxParam.SCAN_TIMEOUT_IN_MINUTES.value()));
        context.put(CxParam.IS_INCREMENTAL_SCAN.value(), taskDefinition.getConfiguration().get(CxParam.IS_INCREMENTAL_SCAN.value()));
        context.put(CxParam.OSA_ENABLED.value(), taskDefinition.getConfiguration().get(CxParam.OSA_ENABLED.value()));
        context.put(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value(), taskDefinition.getConfiguration().get(CxParam.OSA_SCAN_TIMEOUT_IN_MINUTES.value()));
        context.put(CxParam.IS_SYNCHRONOUS.value(), taskDefinition.getConfiguration().get(CxParam.IS_SYNCHRONOUS.value()));
        context.put(CxParam.THRESHOLDS_ENABLED.value(), taskDefinition.getConfiguration().get(CxParam.THRESHOLDS_ENABLED.value()));
        context.put(CxParam.HIGH_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.HIGH_THRESHOLD.value()));
        context.put(CxParam.MEDIUM_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.MEDIUM_THRESHOLD.value()));
        context.put(CxParam.LOW_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.LOW_THRESHOLD.value()));
        context.put(CxParam.OSA_THRESHOLDS_ENABLED.value(), taskDefinition.getConfiguration().get(CxParam.OSA_THRESHOLDS_ENABLED.value()));
        context.put(CxParam.OSA_HIGH_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.OSA_HIGH_THRESHOLD.value()));
        context.put(CxParam.OSA_MEDIUM_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.OSA_MEDIUM_THRESHOLD.value()));
        context.put(CxParam.OSA_LOW_THRESHOLD.value(), taskDefinition.getConfiguration().get(CxParam.OSA_LOW_THRESHOLD.value()));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        validateNotEmpty(params, errorCollection, CxParam.USER_NAME.value());
        validateNotEmpty(params, errorCollection, CxParam.PASSWORD.value());
        validateNotEmpty(params, errorCollection, CxParam.URL.value());
        // validateNotEmpty(params, errorCollection, CxParam.CX_PROJECT_NAME.value());
        validateNotNegative(params, errorCollection, CxParam.SCAN_TIMEOUT_IN_MINUTES.value());//TODO add more validations

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

    public void validateConnection(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {

        final String value = params.getString(key);
    }

    private void populateProjectSelectFields(final String userName, final String cxPass, String preset, String teamPath,
                                             final String serverUrl, @NotNull final Map<String, Object> context) {

        if (serverUrl != null && !StringUtils.isEmpty(serverUrl) && userName != null && !StringUtils.isEmpty(userName) && cxPass != null && !StringUtils.isEmpty(cxPass)) {
            try {

                if (!TryLogin(userName, cxPass, serverUrl)) { //TODO handek Exceptions and handle url exceptiorn
                    return;
                }
                ;
                presetList = convertPresetType(cxClientService.getPresetList());
                context.put(CxParam.PRESET_LIST.value(), presetList);
                if (preset != null && !StringUtils.isEmpty(preset)) {
                    preset = resolveNameById(presetList, preset); //todo handle Exception
                    context.put(CxParam.PRESET.value(), preset);
                }

                teamPathList = convertTeamPathType(cxClientService.getAssociatedGroupsList());
                context.put(CxParam.TEAM_PATH_LIST.value(), teamPathList);
                if (teamPath != null && !StringUtils.isEmpty(teamPath)) {
                    teamPath = resolveNameById(teamPathList, teamPath);
                    context.put(CxParam.TEAM_PATH.value(), teamPath);
                }

                return;

            } catch (Exception e) {  //TODO handle the right exceptions
                System.out.println("Exception caught: '" + e.getMessage() + "'");
            }
        }

        final List<CxObject> noPresets = new ArrayList<CxObject>();
        // noPresets.add(new CxObject("noPreset", this.getI18nBean().getText(CxParam.PRESET.value() + "." + CxParam.NO_SESSION.value()))); //TODO
        context.put(CxParam.PRESET.value(), noPresets);

        final List<CxObject> noTeams = new ArrayList<CxObject>();
        noTeams.add(new CxObject("noTeam", this.getI18nBean().getText(CxParam.TEAM_PATH.value() + "." + CxParam.NO_SESSION.value()))); //TODO
        context.put(CxParam.TEAM_PATH_LIST.value(), noTeams);
    }

    private boolean TryLogin(String userName, String cxPass, String url) {

        try {
            if (url == null) {
                url = "";
            }
            URL cxUrl = new URL(url); //TODO handle exception if its empty
            cxClientService = new CxClientServiceImpl(cxUrl, userName, cxPass); //TODO ask dor when change the password
            cxClientService.loginToServer();
            return true;

        } catch (Exception CxClientException) {
            System.out.println("Exception caught: '" + CxClientException.getMessage() + "'");//TODO
            cxClientService = null;
            return false;
        }

    }

    private String resolveNameById(List<CxObject> list, String id) {
        for (CxObject item : list) {
            if (item.getId().equals(id)) {
                return item.getName();
            }
        }
        return "";
    }

    private List<CxObject> convertPresetType(List<com.checkmarx.v7.Preset> oldType) {
        List<CxObject> newType = new ArrayList<CxObject>();
        for (com.checkmarx.v7.Preset preset : oldType) {
            newType.add(new CxObject(Long.toString(preset.getID()), preset.getPresetName()));
        }

        return newType;
    }

    private List<CxObject> convertTeamPathType(ArrayOfGroup oldType) {
        List<CxObject> newType = new ArrayList<CxObject>();
        for (Group group : oldType.getGroup()) {
            newType.add(new CxObject(group.getID(), group.getGroupName()));
        }

        return newType;
    }

}