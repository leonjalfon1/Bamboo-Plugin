<style type="text/css" xmlns="http://www.w3.org/1999/html">

    form.aui .description {
        width: 90%;
    }

    .cx.center {
        padding: 16px;
        border: solid 1px;
        width: 90%;
        margin-top: 10px;
        margin-bottom: 25px;
        min-width: 500px;
        max-width: 820px;
        box-sizing: border-box;
    }

    input#radioGroupcustomConfigurationServer, input#radioGroupglobalConfigurationServer, input#radioGroupglobalConfigurationCxSAST, input#radioGroupcustomConfigurationCxSAST, input#radioGroupglobalConfigurationControl, input#radioGroupcustomConfigurationControl {
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
        max-width: 600px;
        width: 90%;
        max-height: 374px;
        min-height: 12px;
    }

    .aui-button.test-connection {
        margin: 10px 0;
    }

    #spinner{
        display: none;
        position: absolute;
        left: 50%;
        top: 20%;
    }


</style>



[@ww.form action="checkmarxDefaultConfiguration!save.action" method="post" submitLabelKey="cxDefaultConfigSubmit.label" titleKey="cxDefaultConfigTitle.label" cssClass="top-label"]
    [@ui.bambooSection title='Checkmarx Server' cssClass="cx center"]
        [@ww.textfield labelKey="serverUrl.label" name="globalServerUrl"/]
            [@ww.textfield labelKey="username.label" name="globalUsername"/]
            [@ww.password labelKey="password.label" name="globalPassword" showPassword='true' /]
        [/@ui.bambooSection]

    [@ui.bambooSection title='Checkmarx Scan CxSAST' cssClass="cx center"]
        [@ww.textfield labelKey="folderExclusions.label" name="globalFolderExclusions" descriptionKey="folderExclusions.description"  cssClass="long-field"/]
        [@ww.textarea labelKey="filterPatterns.label" name="globalFilterPatterns" rows="4"  cssClass="long-field"/]
        [@ww.textfield labelKey="scanTimeoutInMinutes.label" name="globalScanTimeoutInMinutes" required='false'/]
    [/@ui.bambooSection]


    [@ui.bambooSection title='Control Checkmarx Scan' cssClass="cx center"]

            [@ww.checkbox labelKey="isSynchronous.label" name="globalIsSynchronous" descriptionKey="isSynchronous.description" toggle='true' /]

            [@ui.bambooSection dependsOn='globalIsSynchronous' showOn='true']
        [@ww.checkbox labelKey="thresholdsEnabled.label" name="globalThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' /]
        [@ui.bambooSection dependsOn='globalThresholdsEnabled' showOn='true']
            [@ww.textfield labelKey="highThreshold.label" name="globalHighThreshold" required='false'/]
            [@ww.textfield labelKey="mediumThreshold.label" name="globalMediumThreshold" required='false'/]
            [@ww.textfield labelKey="lowThreshold.label" name="globalLowThreshold" required='false'/]
        [/@ui.bambooSection]

        [@ww.checkbox labelKey="osaThresholdsEnabled.label" name="globalOsaThresholdsEnabled" descriptionKey="thresholdsEnabled.description" toggle='true' /]
        [@ui.bambooSection dependsOn='globalOsaThresholdsEnabled' showOn='true']
            [@ww.textfield labelKey="highThreshold.label" name="globalOsaHighThreshold" required='false'/]
            [@ww.textfield labelKey="mediumThreshold.label" name="globalOsaMediumThreshold" required='false'/]
            [@ww.textfield labelKey="lowThreshold.label" name="globalOsaLowThreshold" required='false'/]
        [/@ui.bambooSection]
    [/@ui.bambooSection]

        [/@ui.bambooSection]

[/@ww.form]

