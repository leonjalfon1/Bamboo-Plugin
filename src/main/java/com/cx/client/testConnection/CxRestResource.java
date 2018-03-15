package com.cx.client.testConnection;

import com.checkmarx.v7.*;
import com.cx.client.osa.dto.CxClass;
import com.cx.client.testConnection.dto.TestConnectionResponse;
import com.cx.plugin.utils.CxEncryption;
import org.codehaus.plexus.util.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cx.plugin.utils.CxEncryption.decrypt;
import static com.cx.plugin.utils.CxParam.*;

/**
 * A resource of message.
 */
@Path("/")
public class CxRestResource {


    private static URL WSDL_LOCATION = CxSDKWebService.class.getClassLoader().getResource("WEB-INF/CxSDKWebService.wsdl");
    private static final QName SERVICE_NAME = new QName("http://Checkmarx.com/v7", "CxSDKWebService");
    private static String SDK_PATH = "/cxwebinterface/sdk/CxSDKWebService.asmx";
    private List<CxClass> presets;
    private List<CxClass> teams;
    private CxSDKWebServiceSoap client;
    private String sessionId;
    private String result = "";


    @POST
    @Path("test/connection")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response testConnection(Map<Object, Object> credentials) {

        TestConnectionResponse tcResponse;
        result = "";
        URL url;
        String urlToCheck;
        int statusCode = 400;

        urlToCheck = StringUtils.defaultString(credentials.get("url"));

        try {
            url = new URL(urlToCheck);
            HttpURLConnection urlConn;
            URL toCheck = new URL(url + SDK_PATH);
            urlConn = (HttpURLConnection) toCheck.openConnection();

            urlConn.connect();
        } catch (Exception e) {
            result = "Invalid URL";
            tcResponse = new TestConnectionResponse(result, null, null);
            return Response.status(statusCode).entity(tcResponse).build();
        }

        String username = StringUtils.defaultString(credentials.get("username"));
        String pas = StringUtils.defaultString(credentials.get("pas"));

        try {
            if (loginToServer(url, username, decrypt(pas))) {
                presets = getPresets();
                teams = getTeamPath();
                if (presets == null || teams == null) {
                    throw new Exception("invalid preset teamPath");
                }
                result = "Connection successful";
                tcResponse = new TestConnectionResponse(result, presets, teams);
                statusCode = 200;

            } else {
                if (result.equals("")) {
                    result = "Login failed";
                }
                presets = new ArrayList<CxClass>() {{
                    add(new CxClass(NO_PRESET, NO_PRESET_MESSAGE));
                }};

                teams = new ArrayList<CxClass>() {{
                    add(new CxClass(NO_TEAM_PATH, NO_TEAM_MESSAGE));
                }};

                tcResponse = new TestConnectionResponse(result, presets, teams);
            }
        } catch (Exception e) {
            result = "Fail to login: " + e.getMessage();
            tcResponse = new TestConnectionResponse(result, presets, teams);
        }
        return Response.status(statusCode).entity(tcResponse).build();
    }

    public boolean loginToServer(URL url, String username, String password) {
        try {

            CxSDKWebService ss = new CxSDKWebService(WSDL_LOCATION, SERVICE_NAME);
            client = ss.getCxSDKWebServiceSoap();
            BindingProvider bindingProvider = (BindingProvider) client;
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url + SDK_PATH);

            Credentials credentials = new Credentials();
            credentials.setUser(username);
            credentials.setPass(password);
            CxWSResponseLoginData res = client.login(credentials, 1099);
            sessionId = res.getSessionId();

            if (sessionId == null) {
                result = res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception CxClientException) {
            result = CxClientException.getMessage();
            if (result.startsWith("HTTP transport")) {
                result = "Invalid URL";
            }
            return false;
        }
    }

    private List<CxClass> getPresets() {
        CxWSResponsePresetList presetList = client.getPresetList(sessionId);
        if (!presetList.isIsSuccesfull()) {
            return null;
        }
        return convertPresetType(presetList.getPresetList().getPreset());
    }

    private List<CxClass> getTeamPath() {
        CxWSResponseGroupList teamPathList = client.getAssociatedGroupsList(sessionId);
        if (!teamPathList.isIsSuccesfull()) {
            return null;
        }
        return convertTeamPathType(teamPathList.getGroupList());
    }

    private List<CxClass> convertPresetType(List<Preset> oldType) {
        List<CxClass> newType = new ArrayList<CxClass>();
        for (Preset preset : oldType) {
            newType.add(new CxClass(Long.toString(preset.getID()), preset.getPresetName()));
        }
        return newType;
    }

    private List<CxClass> convertTeamPathType(ArrayOfGroup oldType) {
        List<CxClass> newType = new ArrayList<CxClass>();
        for (Group group : oldType.getGroup()) {
            newType.add(new CxClass(group.getID(), group.getGroupName()));
        }
        return newType;
    }
}