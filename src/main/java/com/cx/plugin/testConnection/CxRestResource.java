package com.cx.plugin.testConnection;


import com.cx.plugin.testConnection.dto.TestConnectionResponse;
import com.cx.restclient.CxShragaClient;
import com.cx.restclient.dto.Team;
import com.cx.restclient.sast.dto.Preset;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cx.plugin.utils.CxParam.*;
import static com.cx.plugin.utils.CxPluginUtils.decrypt;

/**
 * A resource of message.
 */
@Path("/")
public class CxRestResource {

    private List<Preset> presets;
    private List<Team> teams;
    private CxShragaClient shraga;
    private String result = "";
    private Logger logger = LoggerFactory.getLogger(CxRestResource.class);


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
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

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
                presets = shraga.getPresetList();
                teams = shraga.getTeamList();
                if (presets == null || teams == null) {
                    throw new Exception("invalid preset teamPath");
                }
                result = "Connection successful";
                tcResponse = new TestConnectionResponse(result, presets, teams);
                statusCode = 200;

            } else {
                result = result.contains("Failed to authenticate")? "Failed to authenticate": result;
                result = result.startsWith("Login failed.")? result: "Login failed. " + result;

                presets = new ArrayList<Preset>() {{new Preset(NO_PRESET_ID, NO_PRESET_MESSAGE);}};
                teams = new ArrayList<Team>() {{new Team(NO_TEAM_PATH, NO_TEAM_MESSAGE);}};

                tcResponse = new TestConnectionResponse(result, presets, teams);
            }
        } catch (Exception e) {
            result = "Fail to login: " + e.getMessage();
            tcResponse = new TestConnectionResponse(result, presets, teams);
        }
        return Response.status(statusCode).entity(tcResponse).build();
    }

    private boolean loginToServer(URL url, String username, String password) {
        try {
            shraga = new CxShragaClient(url.toString().trim(), username, password, CX_ORIGIN, false, logger);
            shraga.login();

            return true;
        } catch (Exception CxClientException) {
            result = CxClientException.getMessage();
            return false;
        }
    }
}