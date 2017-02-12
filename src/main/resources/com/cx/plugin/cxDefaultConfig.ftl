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
        [@ww.textfield labelKey="defaultServerUrl.label" name="globalServerUrl" required="true" cssClass="long-field"/]
            [@ww.textfield labelKey="defaultUserName.label" name="globalUserName" required="true" cssClass="long-field"/]
            [@ww.password labelKey="defaultPassword.label" name="globalPassword" required="true" showPassword='true' cssClass="long-field"/]
        [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
        [@ui.bambooSection title='Checkmarx Scan CxSAST' ]
        [@ww.textfield labelKey="folderExclusions.label" name="globalFolderExclusions" descriptionKey="folderExclusions.description" cssClass="long-field"/]
        [@ww.textarea labelKey="filterPatterns.label" name="globalFilterPatterns" rows="4" cssClass="long-field"/]
        [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="globalScanTimeoutInMinutes" required='false'/]
    [/@ui.bambooSection]
    </div>
</div>

<div class="space"></div>

<div class="field-group">
    <div class="center">
        [@ui.bambooSection title='Control Checkmarx Scan']

            [@ww.checkbox labelKey="isSynchronous.label" name="globalIsSynchronous" descriptionKey="isSynchronous.description" toggle='true' /]

            [@ui.bambooSection dependsOn='globalIsSynchronous' showOn='true']
                [@ww.checkbox labelKey="thresholdsEnabled.label" name="globalThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' /]
                [@ui.bambooSection dependsOn='globalThresholdsEnabled' showOn='true']
                    [@ww.textfield labelKey="highThreshold.label" name="globalHighThreshold" required='false'/]
                    [@ww.textfield labelKey="mediumThreshold.label" name="globalMediumThreshold" required='false'/]
                    [@ww.textfield labelKey="lowThreshold.label" name="globalLowThreshold" required='false'/]
                [/@ui.bambooSection]

                [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="globalOsaThresholdsEnabled"  descriptionKey="thresholdsEnabled.description"toggle='true' /]
                [@ui.bambooSection dependsOn='globalOsaThresholdsEnabled' showOn='true']
                    [@ww.textfield labelKey="highThreshold.label" name="globalOsaHighThreshold"required='false'/]
                    [@ww.textfield labelKey="mediumThreshold.label" name="globalOsaMediumThreshold" required='false'/]
                    [@ww.textfield labelKey="lowThreshold.label" name="globalOsaLowThreshold" required='false'/]
                [/@ui.bambooSection]
          [/@ui.bambooSection]

        [/@ui.bambooSection]
    </div>
</div>

[/@ww.form]

