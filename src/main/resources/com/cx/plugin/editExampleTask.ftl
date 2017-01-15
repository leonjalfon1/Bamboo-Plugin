<style type="text/css" xmlns="http://www.w3.org/1999/html">
    .field-group {
        border-style: 1px solid;
        border-color: black;
    }

    .center {
        padding: 16px;
        border: solid 1px;
        width: 91%;
    }

    .space {
        height: 10px;
    }

</style>

<div class="field-group">
[@ui.bambooSection title='Checkmarx Server' ]
[/@ui.bambooSection]
    <div class="center">
    [@ww.checkbox labelKey="useDefaultCardentials.label" name="useDefaultCardentials" toggle='true' /]
        [@ui.bambooSection dependsOn='useDefaultCardentials' showOn='false']
        [@ww.textfield labelKey="url.label" name="url" required='true'/]
        [@ww.textfield labelKey="userName.label" name="userName" required='true'/]
        [@ww.password labelKey="password.label" name="password" required='true'/]
    [/@ui.bambooSection]

        [@ww.textfield labelKey="projectName.label" name="projectName" required='false'/]
        [@ww.select labelKey="preset.label" name="preset" list="presetList" listKey="id" listValue="name" multiple="false" required="true" cssClass="long-field"/]
        [@ww.select labelKey="teamPath.label" name="teamPath" list="teamPathList" listKey="id" listValue="name" multiple="false" required="true" cssClass="long-field"/]
    </div>
</div>


<div class="space"></div>

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Checkmarx Scan CxSAST']
        [@ww.checkbox labelKey="isIncremental.label" name="isIncremental" toggle='false' /]
        [@ww.textfield labelKey="folderExclusions.label" name="folderExclusions" cssClass="long-field"/]
        [@ww.textarea labelKey="filterPatterns.label" name="filterPatterns" rows="4" cssClass="long-field"/]
        [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="scanTimeoutInMinutes" required='false'/]
    [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
[@ui.bambooSection title='Checkmarx Scan CxOSA']
    <div class="center">
        <p><small>Open Source Analysis (OSA) helps you manage the security risk involved in using open
            source libraries in your applications</small></p>
        <div class="space"></div>
        [@ww.checkbox labelKey="osaEnabled.label" name="osaEnabled" toggle='true' /]
        [@ui.bambooSection dependsOn='osaEnabled' showOn='true']
            [@ww.textfield labelKey="osaExclusions.label" name="osaExclusions" cssClass="long-field"/]
            [@ww.textfield labelKey="osaScanTimeoutInMinutes.label" name="osaScanTimeoutInMinutes" required='false'/]
        [/@ui.bambooSection]
    </div>
[/@ui.bambooSection]
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
    [@ui.bambooSection title='Control Checkmarx Scan']
        [@ww.checkbox labelKey="isSynchronous.label" name="isSynchronous" toggle='true' /]
        [@ui.bambooSection dependsOn='isSynchronous' showOn='true']
            [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" toggle='true' /]
            [@ui.bambooSection dependsOn='thresholdsEnabled' showOn='true']
                [@ww.textfield labelKey="highThreshold.label" name="highThreshold" required='false'/]
                [@ww.textfield labelKey="mediumThreshold.label" name="mediumThreshold" required='false'/]
                [@ww.textfield labelKey="lowThreshold.label" name="lowThreshold" required='false'/]
            [/@ui.bambooSection]
            [@ui.bambooSection dependsOn='osaEnabled' showOn='true']
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled" toggle='true' /]
                [@ui.bambooSection dependsOn='osaThresholdsEnabled' showOn='true']
                    [@ww.textfield labelKey="osaHighThreshold.label" name="osaHighThreshold" required='false'/]
                    [@ww.textfield labelKey="osaMediumThreshold.label" name="osaMediumThreshold" required='false'/]
                    [@ww.textfield labelKey="osaLowThreshold.label" name="osaLowThreshold" required='false'/]
                [/@ui.bambooSection]
            [/@ui.bambooSection]
        [/@ui.bambooSection]
    [/@ui.bambooSection]
    </div>
</div>

