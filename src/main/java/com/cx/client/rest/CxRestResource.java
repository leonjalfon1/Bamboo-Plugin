package com.cx.client.rest;

import com.checkmarx.v7.*;
import com.cx.client.rest.dto.CxClass;
import com.cx.client.rest.dto.TestConnectionResponse;

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

/**
 * A resource of message.
 */
@Path("/")
public class CxRestResource {


    private static URL WSDL_LOCATION = CxSDKWebService.class.getClassLoader().getResource("WEB-INF/CxSDKWebService.wsdl");
    private static final QName SERVICE_NAME = new QName("http://Checkmarx.com/v7", "CxSDKWebService");
    private static String SDK_PATH = "/cxwebinterface/sdk/CxSDKWebService.asmx";
    private static List<CxClass> presets;
    private static List<CxClass> teams;
    private static CxSDKWebServiceSoap client;
    private static String sessionId;
    private static String result = "";
    ;


    @POST
    @Path("test/connection")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response testConnection(Map<Object, Object> key) {

        URL url = null;
        String urli = key.get("url").toString();
        TestConnectionResponse tcResponse;

        try {
            url = new URL(urli);
            HttpURLConnection urlConn;
            URL toCheck = new URL(url + SDK_PATH);
            urlConn = (HttpURLConnection) toCheck.openConnection();
            urlConn.connect();
        } catch (Exception e) {
            result = "Invalid URL";
            tcResponse = new TestConnectionResponse(result, null, null);
            return Response.status(400).entity(tcResponse).build();
        }

        String username = key.get("username").toString();
        String password = key.get("password").toString();
        try {
            if (loginToServer(url, username, password)) {
                presets = getPresets();
                teams = getTeamPath();
                result = "Success!";
                tcResponse = new TestConnectionResponse(result, presets, teams);

                return Response.status(200).entity(tcResponse).build();
            } else {
                if (result.equals("")) {
                    result = "Login failed";
                }
                tcResponse = new TestConnectionResponse(result, null, null);
                return Response.status(400).entity(tcResponse).build();
            }
        } catch (Exception e) {
            result = "Fail to login";
            tcResponse = new TestConnectionResponse(result, null, null);
            return Response.status(400).entity(tcResponse).build();
        }
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
            // log.warn("fail to retrieve preset list: ", presetList.getErrorMessage());//TODO
            //return preset
        }
        return convertPresetType(presetList.getPresetList().getPreset());
    }

    private List<CxClass> getTeamPath() {
        CxWSResponseGroupList teamPathList = client.getAssociatedGroupsList(sessionId);
        if (!teamPathList.isIsSuccesfull()) {
            //log.warn("Fail to retrieve group list: ", associatedGroupsList.getErrorMessage());
            //  return group;
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
 
