<style type="text/css" xmlns="http://www.w3.org/1999/html">

    form.aui .description {
        width: 90%;
    }

    .cx.center {
        padding: 16px;
        border: solid 1px;
        width: 90%;
        margin-top: 10px;
        margin-bottom: 10px;
        min-width: 500px;
        max-width: 650px;
        box-sizing: border-box;
    }

    input#radioGroupcostumeConfigurationServer, input#radioGroupglobalConfigurationServer, input#radioGroupglobalConfigurationCxSAST, input#radioGroupcostumeConfigurationCxSAST, input#radioGroupglobalConfigurationControl, input#radioGroupcostumeConfigurationControl {
        width: 14px;
    }

    form.aui.top-label .field-group > label {
        margin: 5px 0;
        font-weight: bold;
    }

    form.aui .radio {
        width: 43%;
        display: inline-block;
    }

    form.aui .checkbox > label, form.aui .radio > label {
        color: #333333;
        font-weight: bold;
    }


    .aui-page-focused .aui-page-panel-content h3:not(.collapsible-header) {
        margin-top: 0;
    }

    h3 {
        color: #3b73af;
        font-size: 17px;
    }

    form.aui .field-value {
        border-radius: 3.01px;
        max-width: 225px;
        max-height: 17px;
        width: 108%;
        border: 1px solid #cccccc;
        padding-bottom: 6px;
        display: inline-block;
        background-color: #f1f1f1;
        font-weight: bold;
        min-height: 13px;
        padding-top: 5px;
        padding-left: 10px;
    }

    #panel-editor-config form.aui .long-field {
        max-width: 500px;
        width: 90%;
        max-height: 374px;
        min-height: 12px;
    }

    .aui-button.test-connection {
        margin: 10px 0;
    }


</style

<div class="field-group">
    <div class="cx center">
    [@ui.bambooSection title='Checkmarx Server' ]
        [@ww.radio id = 'radioGroup' name='defaultCredentials' listKey='key' listValue='value' toggle='true' list=configurationModeTypesServer /]


            [@ui.bambooSection dependsOn='defaultCredentials' showOn='costumeConfigurationServer']
                [@ww.textfield labelKey="serverUrl.label" id="serverUrl" name="serverUrl" required='true'/]
                [@ww.textfield labelKey="userName.label"  id="userName" name="userName" required='true'/]
                [@ww.password labelKey="password.label"  id="password" name="password" required='true' showPassword='true'/]
              [#--  <button type="button" class="aui-button" id="test_connection">Test Connection</button>--]
                <button type="button" class="aui-button test-connection" id="test_connection">Test Connection</button>
            <div id="testConnectionMessage" class="test-connection-message"></div>
              [#--  <div id="testConnectionMessage"></div>--]

            [/@ui.bambooSection]
            [@ui.bambooSection dependsOn='defaultCredentials' showOn='globalConfigurationServer']
                [@ww.label labelKey="serverUrl.label" name="globalServerUrl" required='true'/]
                [@ww.label labelKey="userName.label" name="globalUserName" required='true'/]
                [@ww.label type="password" labelKey="password.label"/]
            [/@ui.bambooSection]

        [@ww.textfield labelKey="projectName.label" name="projectName" required='true'/]
        [@ww.select labelKey="preset.label" name="presetId" id="presetListId" list="presetList" listKey="key" listValue="value" multiple="false"  cssClass="long-field" descriptionKey="preset.description"/]
        [@ww.select labelKey="teamPath.label" name="teamPathId" id="teamPathListId" list="teamPathList" listKey="key" listValue="value" multiple="false"  cssClass="long-field" descriptionKey="teamPath.description"/]
    [/@ui.bambooSection]
    </div>
</div>



<div class="field-group">
    <div class="cx center">
    [@ui.bambooSection title='Checkmarx Scan CxSAST']
        [@ww.radio id = 'radioGroup' labelKey='' name='defaultCxSast' listKey='key' listValue='value' toggle='true' list=configurationModeTypesCxSAST /]

            [@ui.bambooSection dependsOn='defaultCxSast' showOn='costumeConfigurationCxSAST']
                [@ww.textfield labelKey="folderExclusions.label" name="folderExclusions" descriptionKey="folderExclusions.description" cssClass="long-field"/]
                [@ww.textarea labelKey="filterPatterns.label" name="filterPatterns" rows="4" cssClass="long-field"/]
                [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="scanTimeoutInMinutes" required='false'/]
            [/@ui.bambooSection]

        [@ui.bambooSection dependsOn='defaultCxSast' showOn='globalConfigurationCxSAST']
            [@ww.label labelKey="folderExclusions.label" name="globalFolderExclusions" descriptionKey="folderExclusions.description" cssClass="long-field"/]
            [@ww.label labelKey="filterPatterns.label" name="globalFilterPatterns" rows="4" cssClass="long-field"/]
            [@ww.label labelKey="scanTimeoutInMinutes.label" name="globalScanTimeoutInMinutes" required='false'/]
        [/@ui.bambooSection]


        [@ww.checkbox labelKey="isIncremental.label" name="isIncremental" descriptionKey="isIncremental.description" toggle='false' /]
        [@ww.checkbox labelKey="generatePDFReport.label" name="generatePDFReport" toggle='false' descriptionKey='generatePDFReport.description'/]
    [/@ui.bambooSection]
    </div>
</div>


<div class="field-group">
    <div class="cx center">
    [@ui.bambooSection title='Checkmarx Scan CxOSA']
        <p class="description">
            <small>
                Open Source Analysis (OSA) helps you manage the security risk involved in using open
                source libraries in your applications
            </small>
        </p>
        [@ww.checkbox labelKey="osaEnabled.label" name="osaEnabled" toggle='true' /]
    [/@ui.bambooSection]
    </div>
</div>


<div class="field-group">
    <div class="cx center">
    [@ui.bambooSection title='Control Checkmarx Scan']
        <p  class="description">
            <small>Controls the scan mode (synchrnous or asynchronous) and the build results threshold.
                The thresholds will define the minimal criteria to fail the build.
            </small>
        </p>
        [@ww.radio id = 'radioGroup' labelKey='' name='defaultScanControl' listKey='key' listValue='value' toggle='true' list=configurationModeTypesControl /]
        [@ui.bambooSection dependsOn='defaultScanControl' showOn='costumeConfigurationControl']
            [@ww.checkbox labelKey="isSynchronous.label" name="isSynchronous" descriptionKey="isSynchronous.description" toggle='true' /]


            [@ui.bambooSection dependsOn='isSynchronous' showOn='true']
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' /]
                [@ui.bambooSection dependsOn='thresholdsEnabled' showOn='true']

                    [@ww.textfield labelKey="highThreshold.label" name="highThreshold" required='false'/]
                    [@ww.textfield labelKey="mediumThreshold.label" name="mediumThreshold" required='false'/]
                    [@ww.textfield labelKey="lowThreshold.label" name="lowThreshold" required='false'/]
                [/@ui.bambooSection]

                [@ui.bambooSection dependsOn='thresholdsEnabled' showOn='false']
                    [@ww.label labelKey="highThreshold.label" required='false'/]
                    [@ww.label labelKey="mediumThreshold.label"  required='false'/]
                    [@ww.label labelKey="lowThreshold.label" required='false'/]
                [/@ui.bambooSection]

                [@ui.bambooSection dependsOn='osaEnabled' showOn='true']
                    [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' /]

                    [@ui.bambooSection dependsOn='osaThresholdsEnabled' showOn='true']
                        [@ww.textfield labelKey="highThreshold.label" name="osaHighThreshold"required='false'/]
                        [@ww.textfield labelKey="mediumThreshold.label" name="osaMediumThreshold" required='false'/]
                        [@ww.textfield labelKey="lowThreshold.label" name="osaLowThreshold" required='false'/]
                    [/@ui.bambooSection]

                    [@ui.bambooSection dependsOn='osaThresholdsEnabled' showOn='false']
                        [@ww.label labelKey="highThreshold.label" required='false'/]
                        [@ww.label labelKey="mediumThreshold.label"  required='false'/]
                        [@ww.label labelKey="lowThreshold.label"  required='false'/]
                    [/@ui.bambooSection]

                [/@ui.bambooSection]

                [@ui.bambooSection dependsOn='osaEnabled' showOn='false']
                    [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' /]

                    [@ww.label labelKey="highThreshold.label" required='false'/]
                    [@ww.label labelKey="mediumThreshold.label"  required='false'/]
                    [@ww.label labelKey="lowThreshold.label"  required='false'/]

                [/@ui.bambooSection]

            [/@ui.bambooSection]

            [@ui.bambooSection dependsOn='isSynchronous' showOn='false']
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' /]
                [@ww.label labelKey="highThreshold.label" required='false'/]
                [@ww.label labelKey="mediumThreshold.label"  required='false'/]
                [@ww.label labelKey="lowThreshold.label" required='false'/]
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' /]
                [@ww.label labelKey="highThreshold.label" required='false'/]
                [@ww.label labelKey="mediumThreshold.label"  required='false'/]
                [@ww.label labelKey="lowThreshold.label"  required='false'/]
            [/@ui.bambooSection]

        [/@ui.bambooSection]



        [@ui.bambooSection dependsOn='defaultScanControl' showOn='globalConfigurationControl']
            [#if context.get("globalIsSynchronous")?has_content]
                [@ww.checkbox labelKey="isSynchronous.label" name="globalIsSynchronous" descriptionKey="isSynchronous.description" toggle='true' disabled="true" checked='true' /]
                [#if context.get("globalThresholdsEnabled")?has_content]
                    [@ww.checkbox labelKey="thresholdsEnabled.label" name="globalThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" checked='true' /]
                [#else]
                    [@ww.checkbox labelKey="thresholdsEnabled.label" name="globalThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" /]
                [/#if]
            [#else]
                [@ww.checkbox labelKey="isSynchronous.label" name="globalIsSynchronous" descriptionKey="isSynchronous.description" toggle='true' disabled="true" checked='false'/]
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="globalThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" checked='false'/]
            [/#if]



            [@ww.label labelKey="highThreshold.label" name="globalHighThreshold" required='false'/]
            [@ww.label labelKey="mediumThreshold.label" name="globalMediumThreshold" required='false'/]
            [@ww.label labelKey="lowThreshold.label" name="globalLowThreshold" required='false'/]

            [#if context.get("osaThresholdsEnabled")?has_content]
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="globalOsaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' disabled="true" checked='true' /]
            [#else]
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="globalOsaThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" /]
            [/#if]

            [@ww.label labelKey="highThreshold.label" name="globalOsaHighThreshold"required='false'/]
            [@ww.label labelKey="mediumThreshold.label" name="globalOsaMediumThreshold" required='false'/]
            [@ww.label labelKey="lowThreshold.label" name="globalOsaLowThreshold" required='false'/]


        [/@ui.bambooSection]

    <div>
    [/@ui.bambooSection]
    </div>

