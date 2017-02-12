package com.cx.client.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.checkmarx.v7.*;
import com.cx.client.CxClientServiceImpl;
import com.cx.client.exception.CxClientException;
import com.cx.plugin.dto.CxClass;
import com.cx.plugin.dto.TestConnectionResponse;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A resource of message.
 */
@Path("/")
public class MyRestResource {


    private static URL WSDL_LOCATION = CxSDKWebService.class.getClassLoader().getResource("WEB-INF/CxSDKWebService.wsdl");
    private static final QName SERVICE_NAME = new QName("http://Checkmarx.com/v7", "CxSDKWebService");
    private static String SDK_PATH = "/cxwebinterface/sdk/CxSDKWebService.asmx";
    private static List<CxClass> presets;
    private static List<CxClass> teams;
    private static  CxSDKWebServiceSoap client;
    private static  String sessionId;


    @POST
    @Path("test/connection")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response testConnetion( Map<Object, Object> key) {
        String result ="";
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
            if (loginToServer(url, username, password)){
                presets = getPresets();
                teams = getTeamPath();
                result =  "Success!";
                tcResponse = new TestConnectionResponse(result, presets, teams);

                return Response.status(200).entity(tcResponse).build();}
            else{
                result = "Wrong username or password";
                tcResponse = new TestConnectionResponse(result, null, null);
                return Response.status(400).entity(tcResponse).build();}
        }

       catch (Exception e) {
           result = "Fail to login";
           tcResponse = new TestConnectionResponse(result, null, null);
           return Response.status(400).entity(tcResponse).build();
       }
    }

    public boolean loginToServer(URL url, String username, String password)   {
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
                return false;
            }

            return true;
        }
        catch (Exception CxClientException) {
            String loginError = CxClientException.getMessage();

            if(loginError.startsWith("HTTP transport")){
                loginError = "Invalid URL";
            }
            System.out.println("Exception caught: " + loginError + "'");//TODO
            return  false;
        }
    }

    private List<CxClass>  getPresets() {
        CxWSResponsePresetList presetList = client.getPresetList(sessionId);
        if (!presetList.isIsSuccesfull()) {
           // log.warn("fail to retrieve preset list: ", presetList.getErrorMessage());
            //return preset
        }
        return convertPresetType(presetList.getPresetList().getPreset());
    }

   private List<CxClass>  getTeamPath() {
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
            newType.add(new CxClass(Long.toString(preset.getID()), preset.getPresetName().toString()));
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
 
