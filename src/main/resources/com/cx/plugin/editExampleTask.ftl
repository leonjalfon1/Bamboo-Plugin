<style type="text/css" xmlns="http://www.w3.org/1999/html">
    .field-group {
        border-style: 1px solid;
        border-color: black;
    }

    .center {
        padding: 16px;
        border: double 1px;
        width: 69%;
    }

    .space {
        height: 10px;
    }

    .afterRadio {
        margin-top: -22px;
    }

    #radioGroupcostumeConfigurationServer, #radioGroupcostumeConfigurationControl, #radioGroupcostumeConfigurationCxSAST {
        left: 200px;
        top: -25px;
    }

    #radioGroupcostumeConfigurationServer + label, #radioGroupcostumeConfigurationControl + label, #radioGroupcostumeConfigurationCxSAST + label {
        position: inherit;
        top: -25px;
        left: 200px
    }

    form.aui.top-label .field-group > label {
        display: block;
        float: none;
        margin: 9px 0 5px 0;
        padding: 0;
        text-align: left;
        font-weight: bold;
    }

    form.aui .checkbox > label, form.aui .radio > label {
        color: #333333;
        font-weight: bold;
    }

    h3 {
        color: #3b73af;
        font-size: 17px;
        font-weight: bold;
        line-height: 1.5;
        text-transform: none;
        margin: 30px 0 0 0;
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

</style

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Checkmarx Server' ]
        [@ww.radio id = 'radioGroup' labelKey='' name='defaultCredentials' listKey='key' listValue='value' toggle='true' list=configurationModeTypesServer /]

        <div class="afterRadio">
            [@ui.bambooSection dependsOn='defaultCredentials' showOn='costumeConfigurationServer']
                [@ww.textfield labelKey="serverUrl.label" name="serverUrl" required='true'/]
                [@ww.textfield labelKey="userName.label" name="userName" required='true'/]
                [@ww.password labelKey="password.label" name="password" required='true' showPassword='true'/]
            [/@ui.bambooSection]
            [@ui.bambooSection dependsOn='defaultCredentials' showOn='globalConfigurationServer']
                [@ww.label labelKey="serverUrl.label" name="serverUrl" required='true'/]
                [@ww.label labelKey="userName.label" name="userName" required='true'/]
                [@ww.label type="password" labelKey="password.label" name="password" required='true' showPassword='false'/]
            [/@ui.bambooSection]
        </div>

        [@ww.textfield labelKey="projectName.label" name="projectName" required='true'/]
        [@ww.select labelKey="preset.label" name="presetId" list="presetList" listKey="key" listValue="value" multiple="false" required="true" cssClass="long-field" descriptionKey="preset.description"/]
        [@ww.select labelKey="teamPath.label" name="teamPathId" list="teamPathList" listKey="key" listValue="value" multiple="false" required="true" cssClass="long-field" descriptionKey="teamPath.description"/]
    [/@ui.bambooSection]
    </div>
</div>


<div class="space"></div>

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Checkmarx Scan CxSAST']
        [@ww.radio id = 'radioGroup' labelKey='' name='defaultCxSast' listKey='key' listValue='value' toggle='true' list=configurationModeTypesCxSAST /]
        <div class="afterRadio">
            [@ui.bambooSection dependsOn='defaultCxSast' showOn='costumeConfigurationCxSAST']
                [@ww.textfield labelKey="folderExclusions.label" name="folderExclusions" descriptionKey="folderExclusions.description" cssClass="long-field"/]
                [@ww.textarea labelKey="filterPatterns.label" name="filterPatterns" rows="4" cssClass="long-field"/]
                [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="scanTimeoutInMinutes" required='false'/]
            [/@ui.bambooSection]

        [@ui.bambooSection dependsOn='defaultCxSast' showOn='globalConfigurationCxSAST']
            [@ww.label labelKey="folderExclusions.label" name="folderExclusions" cssClass="long-field"/]
            [@ww.label labelKey="filterPatterns.label" name="filterPatterns" rows="4" cssClass="long-field"/]
            [@ww.label labelKey="scanTimeoutInMinutes.label" name="scanTimeoutInMinutes" required='false'/]
        [/@ui.bambooSection]

        </div>
        [@ww.checkbox labelKey="isIncremental.label" name="isIncremental" descriptionKey="isIncremental.description" toggle='false' /]
        [@ww.checkbox labelKey="generatePDFReport.label" name="generatePDFReport" toggle='false' descriptionKey='generatePDFReport.description'/]
    [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Checkmarx Scan CxOSA']
        <p>
            <small>Open Source Analysis (OSA) helps you manage the security risk involved in using open
                source libraries in your applications
            </small>
        </p>
        [@ww.checkbox labelKey="osaEnabled.label" name="osaEnabled" toggle='true' /]
    [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Control Checkmarx Scan']
        <p>
            <small>Controls the scan mode (synchrnous or asynchronous) and the build results threshold.
                The thresholds will define the minimal criteria to fail the build.
            </small>
        </p>
        [@ww.radio id = 'radioGroup' labelKey='' name='defaultScanControl' listKey='key' listValue='value' toggle='true' list=configurationModeTypesControl /]
    <div class="afterRadio">
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
            [#--[#if buildConfiguration.getString('repository.git.ssh.key')?has_content]--]
            [#if context.get("thresholdsEnabled")?has_content]
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" checked='true' /]
            [#else]
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" /]
            [/#if]

            [@ww.label labelKey="highThreshold.label" name="highThreshold" required='false'/]
            [@ww.label labelKey="mediumThreshold.label" name="mediumThreshold" required='false'/]
            [@ww.label labelKey="lowThreshold.label" name="lowThreshold" required='false'/]

            [#if context.get("osaThresholdsEnabled")?has_content]
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' disabled="true" checked='true' /]
            [#else]
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' disabled="true" /]
            [/#if]

            [@ww.label labelKey="highThreshold.label" name="osaHighThreshold"required='false'/]
            [@ww.label labelKey="mediumThreshold.label" name="osaMediumThreshold" required='false'/]
            [@ww.label labelKey="lowThreshold.label" name="osaLowThreshold" required='false'/]


        [/@ui.bambooSection]

    <div>
    [/@ui.bambooSection]
    </div>
    </div>
    [#--

    [@ui.messageBox type="info" titleKey="org.whitesource.bamboo.plugins.projectType.detection.maven" /]
    [#else]
        [@ui.messageBox type="warning" titleKey="org.whitesource.bamboo.plugins.projectType.maven.error" /]
        [@ww.hidden name='projectName' value='false' /]
        [@ww.file labelKey='repository.git.ssh.key' name='projectName' /]--]
