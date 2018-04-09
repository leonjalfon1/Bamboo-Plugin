package com.cx.client;

import com.checkmarx.v7.CliScanArgs;
import com.checkmarx.v7.CxClientType;
import com.checkmarx.v7.ProjectScannedDisplayData;
import com.checkmarx.v7.ProjectSettings;
import com.cx.client.dto.BaseScanConfiguration;
import com.cx.client.dto.ScanResults;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by: Dorg.
 * Date: 15/09/2016.
 */
public abstract class CxPluginHelper {

    private static final String[] SUPPORTED_EXTENSIONS = {"jar", "war", "ear", "aar", "dll", "exe", "msi", "nupkg", "egg", "whl", "tar.gz", "gem", "deb", "udeb",
            "dmg", "drpm", "rpm", "pkg.tar.xz", "swf", "swc", "air", "apk", "zip", "gzip", "tar.bz2", "tgz", "c", "cc", "cp", "cpp", "css", "c++", "h", "hh", "hpp",
            "hxx", "h++", "m", "mm", "pch", "java", "c#", "cs", "csharp", "go", "goc", "js", "plx", "pm", "ph", "cgi", "fcgi", "psgi", "al", "perl", "t", "p6m", "p6l", "nqp,6pl", "6pm",
            "p6", "php", "py", "rb", "swift", "clj", "cljx", "cljs", "cljc"};

    private static final String DEFAULT_ARCHIVE_INCLUDES = "**/.*jar,**/*.war,**/*.ear,**/*.sca,**/*.gem,**/*.whl,**/*.egg,**/*.tar,**/*.tar.gz,**/*.tgz,**/*.zip,**/*.rar";

    private static final String EXCLUSSION_CHECKMARX_REPORTS = "**/Checkmarx/Reports/**";

    public static ScanResults genScanResponse(ProjectScannedDisplayData scanDisplayData) {
        ScanResults ret = new ScanResults();
        ret.setProjectId(scanDisplayData.getProjectID());
        ret.setScanID(scanDisplayData.getLastScanID());
        ret.setHighSeverityResults(scanDisplayData.getHighVulnerabilities());
        ret.setMediumSeverityResults(scanDisplayData.getMediumVulnerabilities());
        ret.setLowSeverityResults(scanDisplayData.getLowVulnerabilities());
        ret.setInfoSeverityResults(scanDisplayData.getInfoVulnerabilities());
        ret.setRiskLevelScore(scanDisplayData.getRiskLevelScore());

        return ret;
    }

    public static CliScanArgs genCliScanArgs(BaseScanConfiguration conf) {
        CliScanArgs cliScanArgs = new CliScanArgs();

        CxClientType cxClientType = CxClientType.SDK;
        try {
            cxClientType = CxClientType.valueOf(conf.getClientOrigin().name());
        } catch (Exception e) {
        }
        cliScanArgs.setClientOrigin(cxClientType);
        cliScanArgs.setIsIncremental(conf.isIncrementalScan());
        cliScanArgs.setIsPrivateScan(conf.isPrivateScan());
        cliScanArgs.setIgnoreScanWithUnchangedCode(conf.isIgnoreScanWithUnchangedCode());
        cliScanArgs.setComment(conf.getComment());

        ProjectSettings prjSettings = new ProjectSettings();
        String projectName = StringUtils.isEmpty(conf.getFullTeamPath()) ? conf.getProjectName() : conf.getFullTeamPath() + "\\" + conf.getProjectName();
        prjSettings.setProjectName(projectName);
        prjSettings.setPresetID(conf.getPresetId());
        prjSettings.setAssociatedGroupID(conf.getGroupId());
        prjSettings.setDescription(conf.getDescription());
        prjSettings.setIsPublic(conf.isPublic());
        prjSettings.setOwner(conf.getOwner());
        prjSettings.setProjectID(conf.getProjectId());
        cliScanArgs.setPrjSettings(prjSettings);

        return cliScanArgs;
    }

    public static String composeScanLink(String url, ScanResults scanResults) {
        return String.format(url + "/CxWebClient/ViewerMain.aspx?scanId=%s&ProjectID=%s", scanResults.getScanID(), scanResults.getProjectId());
    }

    public static String composeProjectStateLink(String url, long projectId) {
        return String.format(url + "/CxWebClient/portal#/projectState/%s/Summary", projectId);
    }

    public static String composeProjectOSASummaryLink(String url, long projectId) {
        return String.format(url + "/CxWebClient/SPA/#/viewer/project/%s", projectId);
    }

    public static Properties generateOSAScanConfiguration(String filterPatterns, String archiveIncludes, String scanFolder, boolean installBeforeScan) {
        Properties ret = new Properties();
        filterPatterns = StringUtils.defaultString(filterPatterns);
        archiveIncludes = StringUtils.defaultString(archiveIncludes);

        List<String> inclusions = new ArrayList<String>();
        List<String> exclusions = new ArrayList<String>();
        exclusions.add(EXCLUSSION_CHECKMARX_REPORTS);
        String[] filters = filterPatterns.split("\\s*,\\s*"); //split by comma and trim (spaces + newline)
        for (String filter : filters) {
            if (StringUtils.isNotEmpty(filter)) {
                if (!filter.startsWith("!")) {
                    inclusions.add(filter);
                } else if (filter.length() > 1) {
                    filter = filter.substring(1); // Trim the "!"
                    exclusions.add(filter);
                }
            }
        }

        String includesString = StringUtils.join(inclusions, ",");
        String excludesString = StringUtils.join(exclusions, ",");

        if (StringUtils.isNotEmpty(includesString)) {
            ret.put("includes", includesString);
        }

        if (StringUtils.isNotEmpty(excludesString)) {
            ret.put("excludes", excludesString);
        }

        ret.put("acceptExtensionsList", SUPPORTED_EXTENSIONS);

        if (StringUtils.isNotEmpty(archiveIncludes)) {
            String[] archivePatterns = archiveIncludes.split("\\s*,\\s*"); //split by comma and trim (spaces + newline)
            for (int i = 0; i < archivePatterns.length; i++) {
                if (StringUtils.isNotEmpty(archivePatterns[i]) && archivePatterns[i].startsWith("*.")) {
                    archivePatterns[i] = "**/" + archivePatterns[i];
                }
            }
            archiveIncludes = StringUtils.join(archivePatterns, ",");
            ret.put("archiveIncludes", archiveIncludes);
        } else {
            ret.put("archiveIncludes", DEFAULT_ARCHIVE_INCLUDES);
        }

        ret.put("archiveExtractionDepth", "4");

        if (installBeforeScan) {
            ret.put("npm.runPreStep", "true");
            ret.put("bower.runPreStep", "true");
        }

        ret.put("d", scanFolder);

        return ret;
    }

    public String convertArrayToString(String[] array) {
        return StringUtils.join(array, ',');
    }

    public void createEmptyZip(File zipFile) throws IOException {

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(zipFile);
            byte[] ioe = new byte[22];
            ioe[0] = 80;
            ioe[1] = 75;
            ioe[2] = 5;
            ioe[3] = 6;
            os.write(ioe);
            os.close();
            os = null;
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

}
