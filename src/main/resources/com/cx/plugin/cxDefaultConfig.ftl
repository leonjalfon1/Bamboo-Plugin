<style type="text/css" xmlns="http://www.w3.org/1999/html">
    .field-group {
        border-style: 1px solid;
        border-color: black;
    }

    .center {
        padding: 16px;
        border: solid 1px;
        width: 69%;
    }

    .space {
        height: 10px;
    }
</style>




[@ww.form action="checkmarxDefaultConfiguration!save.action" method="post" submitLabelKey="cxDefaultConfigSubmit.label" titleKey="cxDefaultConfigTitle.label"]
<div class="field-group">
    <div class="center">
        [@ui.bambooSection title='Checkmarx Server' ]
        [@ww.textfield labelKey="defaultServerUrl.label" name="serverUrl" required="true" cssClass="long-field"/]
            [@ww.textfield labelKey="defaultUserName.label" name="userName" required="true" cssClass="long-field"/]
            [@ww.password labelKey="defaultPassword.label" name="password" required="true" showPassword='true' cssClass="long-field"/]
        [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
        [@ui.bambooSection title='Checkmarx Scan CxSAST' ]
        [@ww.textfield labelKey="folderExclusions.label" name="folderExclusions" cssClass="long-field"/]
        [@ww.textarea labelKey="filterPatterns.label" name="filterPatterns" rows="4" cssClass="long-field"/]
        [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="scanTimeoutInMinutes" required='false'/]
    [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
        [@ui.bambooSection title='Control Checkmarx Scan']
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="thresholdsEnabled" toggle='true' /]
                [@ui.bambooSection dependsOn='thresholdsEnabled' showOn='true']
                    [@ww.textfield labelKey="highThreshold.label" name="highThreshold" required='false'/]
                    [@ww.textfield labelKey="mediumThreshold.label" name="mediumThreshold" required='false'/]
                    [@ww.textfield labelKey="lowThreshold.label" name="lowThreshold" required='false'/]
                [/@ui.bambooSection]
                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="osaThresholdsEnabled" toggle='true' /]
                [@ui.bambooSection dependsOn='osaThresholdsEnabled' showOn='true']
                    [@ww.textfield labelKey="highThreshold.label" name="osaHighThreshold" required='false'/]
                    [@ww.textfield labelKey="mediumThreshold.label" name="osaMediumThreshold" required='false'/]
                    [@ww.textfield labelKey="lowThreshold.label" name="osaLowThreshold" required='false'/]
                [/@ui.bambooSection]
        [/@ui.bambooSection]
    </div>
</div>

[/@ww.form]

