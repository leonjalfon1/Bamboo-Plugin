<style type="text/css" xmlns="http://www.w3.org/1999/html">

    .form-content-container {
        /*padding: 16px;*/
        width: 90%;
        margin: 10px auto;
        min-width: 710px;
        max-width: 850px;
        box-sizing: border-box;
    }

    .cx-field-group {
        padding: 16px;
        border: solid 1px;
        /*width: 90%;*/
        margin: 10px auto;
        min-width: 500px;
        /*max-width: 650px;*/
        box-sizing: border-box;
    }

    form.aui .field-group > label {
        text-align: left;
        width: 175px;
    }

    form.aui .buttons-container {
        padding: 10px 0;
        margin: 10px auto;
        min-width: 500px;
        max-width: 850px;
        width: 90%;
        box-sizing: border-box;
    }

    form.aui .field-group {
        padding: 10px 0;
    }

    form.aui .field-group > label {
        text-align: left;
        width: 175px;
        margin-left: 0;
    }

    /*form.aui div.description::before {*/
        /*content: '';*/
        /*width: 175px;*/

    /*}*/

    div#checkmarxDefaultConfiguration_globalFolderExclusionsDesc {
        width: calc(100% - 175px);
        margin-left: auto;
    }

    form.aui.top-label .field-group > label {
        margin: 5px 0;
        font-weight: bold;
    }

    form.aui .checkbox > label {
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

</style>


[@ww.form action="checkmarxDefaultConfiguration!save.action" method="post" submitLabelKey="cxDefaultConfigSubmit.label" titleKey="cxDefaultConfigTitle.label"]
<div class="cx-field-group">
    [@ui.bambooSection title='Checkmarx Server' ]
        [@ww.textfield labelKey="serverUrl.label" name="globalServerUrl" cssClass="long-field"/]
            [@ww.textfield labelKey="userName.label" name="globalUserName"  cssClass="long-field"/]
            [@ww.password labelKey="password.label" name="globalPassword" showPassword='true' cssClass="long-field"/]
        [/@ui.bambooSection]
</div>

<div class="cx-field-group">
    [@ui.bambooSection title='Checkmarx Scan CxSAST' ]
        [@ww.textfield labelKey="folderExclusions.label" name="globalFolderExclusions" descriptionKey="folderExclusions.description" cssClass="long-field"/]
        [@ww.textarea labelKey="filterPatterns.label" name="globalFilterPatterns" rows="4" cssClass="long-field"/]
        [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="globalScanTimeoutInMinutes" required='false'/]
    [/@ui.bambooSection]
</div>


<div class="cx-field-group">
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

[/@ww.form]

