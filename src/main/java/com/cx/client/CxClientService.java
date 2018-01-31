package com.cx.client;

import com.checkmarx.v7.CxWSBasicRepsonse;
import com.checkmarx.v7.CxWSResponseScanStatus;
import com.checkmarx.v7.Group;
import com.checkmarx.v7.Preset;
import com.cx.client.dto.CreateScanResponse;
import com.cx.client.dto.LocalScanConfiguration;
import com.cx.client.dto.ReportType;
import com.cx.client.dto.ScanResults;
import com.cx.client.exception.CxClientException;
import com.cx.client.osa.dto.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by: Dorg.
 * Date: 18/09/2016.
 */
public interface CxClientService {

    void checkServerConnectivity() throws CxClientException;

    void loginToServer() throws CxClientException;

    CreateScanResponse createLocalScan(LocalScanConfiguration conf) throws CxClientException;

    CreateScanResponse createLocalScanResolveFields(LocalScanConfiguration conf) throws CxClientException;

    long resolvePresetIdFromName(String presetName);

    String resolvePresetNameFromId(String presetName);

    String resolveTeamNameFromTeamId(String teamId);

    void waitForScanToFinish(String runId, ScanWaitHandler<CxWSResponseScanStatus> waitHandler) throws CxClientException, InterruptedException;

    /**
     * @param runId
     * @param scanTimeoutInMin set scanTimeoutInMin to -1 for no timeout
     * @throws CxClientException
     */
    void waitForScanToFinish(String runId, long scanTimeoutInMin, ScanWaitHandler<CxWSResponseScanStatus> waitHandler) throws CxClientException, InterruptedException;

    ScanResults retrieveScanResults(long projectId) throws CxClientException;

    CreateOSAScanResponse createOSAScan(long projectId, List<OSAFile> osaFileList) throws CxClientException, IOException;

    OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler) throws CxClientException, InterruptedException, IOException;

    OSASummaryResults retrieveOSAScanSummaryResults(String scanId) throws CxClientException, IOException;

    List<Library> getOSALibraries(String scanId) throws CxClientException, IOException;

    List<CVE> getOSAVulnerabilities(String scanId) throws CxClientException, IOException;

    byte[] getScanReport(long scanId, ReportType reportType) throws CxClientException, InterruptedException;

    List<Group> getAssociatedGroupsList();

    List<Preset> getPresetList();

    void close();//todo implement

    void setLogger(Logger log);

    String getSessionId();

    CxWSBasicRepsonse cancelScan(String runId);

    boolean isNewProject(String projectName, String teamPath)throws CxClientException;

}

