//-------------------------- sast vars --------------------------------------

//thresholds
var thresholdsEnabled = true;
var highThreshold = 0;
var medThreshold = 0;
var lowThreshold = 1;

//counts
var highCount = 3;
var medCount = 3;
var lowCount = 3;


//-------------------------- osa vars --------------------------------------


var osaEnabled = true;

//libraries
var osaVulnerableAndOutdatedLibs = 8;
var okLibraries = 28;

//thresholds
var osaThresholdsEnabled = true;
var osaHighThreshold = 1;
var osaMedThreshold = 0;
var osaLowThreshold = 1;

//counts
var osaHighCount = 3;
var osaMedCount = 3;
var osaLowCount = 3;

//---------------------------------------------------------- other vars ---------------------------------------------------------------


var thresholdExceededHtml =
    '<div class="threshold-exceeded">' +
    '<div class="threshold-exceeded-icon">' +
    '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" id="SvgjsSvg1015" version="1.1" width="9.400000000000091" height="12.399999999999977" viewBox="0 0 9.400000000000091 12.399999999999977"><title>threshold ICON</title><desc>Created with Avocode.</desc><defs id="SvgjsDefs1016"/><path id="SvgjsPath1017" d="M1052 190L1056.29 190L1056.29 195.6L1052 195.6Z " fill="#da2945" fill-opacity="1" transform="matrix(1,0,0,1,-1049.3,-184.3)"/><path id="SvgjsPath1018" d="M1052.71 185.1L1055.57 185.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#da2945" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-1049.3,-184.3)"/><path id="SvgjsPath1019" d="M1052.71 188.1L1055.57 188.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#da2945" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-1049.3,-184.3)"/><path id="SvgjsPath1020" d="M1050.42 195.1L1057.64 195.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#da2945" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-1049.3,-184.3)"/></svg>' +
    '</div>' +
    '<div class="threshold-exceeded-text">' +
    'Threshold Exceeded' +
    '</div>' +
    '</div>';

var thresholdComplianceHtml =
    '<div class="threshold-compliance">' +
    '<div class="threshold-compliance-icon">' +
    '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" id="SvgjsSvg1050" version="1.1" width="13.99264158479491" height="13" viewBox="0 0 13.99264158479491 13"><title>Icon</title><desc>Created with Avocode.</desc><defs id="SvgjsDefs1051"><clipPath id="SvgjsClipPath1056"><path id="SvgjsPath1055" d="M1035.00736 793.9841L1035.00736 784.01589L1046.9926400000002 784.01589L1046.9926400000002 793.9841ZM1038.67 790.72L1036.68 788.72L1036 789.4L1038.67 792.0699999999999L1045.21 785.67L1044.54 785Z " fill="#ffffff"/></clipPath></defs><path id="SvgjsPath1052" d="M1033 789.5C1033 785.91015 1035.91015 783 1039.5 783C1043.08985 783 1046 785.91015 1046 789.5C1046 793.08985 1043.08985 796 1039.5 796C1035.91015 796 1033 793.08985 1033 789.5Z " fill="#21bf3f" fill-opacity="1" transform="matrix(1,0,0,1,-1033,-783)"/><path id="SvgjsPath1053" d="M1038.67 790.72L1036.68 788.72L1036 789.4L1038.67 792.0699999999999L1045.21 785.67L1044.54 785Z " fill="#ffffff" fill-opacity="1" transform="matrix(1,0,0,1,-1033,-783)"/><path id="SvgjsPath1054" d="M1038.67 790.72L1036.68 788.72L1036 789.4L1038.67 792.0699999999999L1045.21 785.67L1044.54 785Z " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="butt" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="1.4" clip-path="url(&quot;#SvgjsClipPath1056&quot;)" transform="matrix(1,0,0,1,-1033,-783)"/></svg>' +
    '</div>' +
    '<div class="threshold-compliance-text">' +
    'Threshold Compliance' +
    '</div>' +
    '</div>';

var SEVERITY = {
    HIGH: {value: 0, name: "high"},
    MED: {value: 1, name: "medium"},
    LOW: {value: 2, name: "low"},
    OSA_HIGH: {value: 3, name: "high"},
    OSA_MED: {value: 4, name: "medium"},
    OSA_LOW: {value: 5, name: "low"}
};

//---------------------------------------------------------- sast ---------------------------------------------------------------
//set bars height and count
document.getElementById("bar-count-high").innerHTML = highCount;
document.getElementById("bar-count-med").innerHTML = medCount;
document.getElementById("bar-count-low").innerHTML = lowCount;

document.getElementById("bar-high").setAttribute("style", "height:" + highCount * 100 / (highCount + medCount + lowCount) + "%");
document.getElementById("bar-med").setAttribute("style", "height:" + medCount * 100 / (highCount + medCount + lowCount) + "%");
document.getElementById("bar-low").setAttribute("style", "height:" + lowCount * 100 / (highCount + medCount + lowCount) + "%");


//if threshold is enabled
if (thresholdsEnabled) {
    var isThresholdExceeded = false;
    var thresholdExceededComplianceElement = document.getElementById("threshold-exceeded-compliance");


    if (highThreshold != null && highCount > highThreshold) {
        document.getElementById("tooltip-high").innerHTML = tooltipGenerator(SEVERITY.HIGH);
        isThresholdExceeded = true;
    }

    if (medThreshold != null && medCount > medThreshold) {
        document.getElementById("tooltip-med").innerHTML = tooltipGenerator(SEVERITY.MED);
        isThresholdExceeded = true;
    }

    if (lowThreshold != null && lowCount > lowThreshold) {
        document.getElementById("tooltip-low").innerHTML = tooltipGenerator(SEVERITY.LOW);
        isThresholdExceeded = true;
    }


    //if threshold exceeded
    if (isThresholdExceeded) {
        thresholdExceededComplianceElement.innerHTML = thresholdExceededHtml;
    }

    //else
    //show threshold compliance element
    else {
        thresholdExceededComplianceElement.innerHTML = thresholdComplianceHtml;
    }
}

//---------------------------------------------------------- osa ---------------------------------------------------------------
if (osaEnabled) {

    //set bars height and count
    document.getElementById("osa-bar-count-high").innerHTML = osaHighCount;
    document.getElementById("osa-bar-count-med").innerHTML = osaMedCount;
    document.getElementById("osa-bar-count-low").innerHTML = osaLowCount;

    document.getElementById("osa-bar-high").setAttribute("style", "height:" + osaHighCount * 100 / (osaHighCount + osaMedCount + osaLowCount) + "%");
    document.getElementById("osa-bar-med").setAttribute("style", "height:" + osaMedCount * 100 / (osaHighCount + osaMedCount + osaLowCount) + "%");
    document.getElementById("osa-bar-low").setAttribute("style", "height:" + osaLowCount * 100 / (osaHighCount + osaMedCount + osaLowCount) + "%");

    document.getElementById("vulnerable-libraries").innerHTML = osaVulnerableAndOutdatedLibs;
    document.getElementById("ok-libraries").innerHTML = okLibraries;


    //if threshold is enabled
    if (osaThresholdsEnabled) {
        var isOsaThresholdExceeded = false;
        var osaThresholdExceededComplianceElement = document.getElementById("osa-threshold-exceeded-compliance");


        if (osaHighThreshold != null && osaHighCount > osaHighThreshold) {
            document.getElementById("osa-tooltip-high").innerHTML = tooltipGenerator(SEVERITY.OSA_HIGH);
            isOsaThresholdExceeded = true;
        }

        if (osaMedThreshold != null && osaMedCount > osaMedThreshold) {
            document.getElementById("osa-tooltip-med").innerHTML = tooltipGenerator(SEVERITY.OSA_MED);
            isOsaThresholdExceeded = true;
        }

        if (osaLowThreshold != null && osaLowCount > osaLowThreshold) {
            document.getElementById("osa-tooltip-low").innerHTML = tooltipGenerator(SEVERITY.OSA_LOW);
            isOsaThresholdExceeded = true;
        }


        //if threshold exceeded
        if (isOsaThresholdExceeded) {
            osaThresholdExceededComplianceElement.innerHTML = thresholdExceededHtml;
        }

        //else
        //show threshold compliance element
        else {
            osaThresholdExceededComplianceElement.innerHTML = thresholdComplianceHtml;
        }

    }
}
else {
    document.getElementById("osa-info").setAttribute("style", "display:none");
}

function tooltipGenerator(severity) {
    var threshold = 0;
    var count = 0;
    var thresholdHeight = 0;

    switch (severity) {
        case SEVERITY.HIGH:
            threshold = highThreshold;
            count = highCount;
            break;
        case SEVERITY.MED:
            threshold = medThreshold;
            count = medCount;
            break;
        case SEVERITY.LOW:
            threshold = lowThreshold;
            count = lowCount;
            break;

        case SEVERITY.OSA_HIGH:
            threshold = osaHighThreshold;
            count = osaHighCount;
            break;
        case SEVERITY.OSA_MED:
            threshold = osaMedThreshold;
            count = osaMedCount;
            break;
        case SEVERITY.OSA_LOW:
            threshold = osaLowThreshold;
            count = osaLowCount;
            break;
    }

    //calculate visual height
    thresholdHeight = threshold * 100 / count;


    return '' +

        '<div class="tooltip-container" style="bottom:calc(' + thresholdHeight + '% - 1px)">' +
        '<div class="threshold-line"></div>' +
        '<div class="threshold-tooltip">' +
        '<div class="threshold-tooltip-background">' +
        '<div class="threshold-icon-white">' +
        '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" id="SvgjsSvg1044" version="1.1" width="9.400000000000091" height="12.399999999999977" viewBox="0 0 9.400000000000091 12.399999999999977"><title>threshold ICON</title><desc>Created with Avocode.</desc><defs id="SvgjsDefs1045"/><path id="SvgjsPath1046" d="M638 360L642.29 360L642.29 365.6L638 365.6Z " fill="#ffffff" fill-opacity="1" transform="matrix(1,0,0,1,-635.3,-354.3)"/><path id="SvgjsPath1047" d="M638.71 355.1L641.57 355.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-635.3,-354.3)"/><path id="SvgjsPath1048" d="M638.71 358.1L641.57 358.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-635.3,-354.3)"/><path id="SvgjsPath1049" d="M636.42 365.1L643.64 365.1 " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="miter" stroke-linecap="square" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="1.4" transform="matrix(1,0,0,1,-635.3,-354.3)"/></svg>' +
        '</div>' +
        '<div class="threshold-text">Threshold</div>' +
        '<div class="threshold-number">' + threshold + '</div>' +
        '</div>' +
        '</div>' +
        '</div>';

}