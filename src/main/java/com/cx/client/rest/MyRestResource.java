package com.cx.client.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.checkmarx.v7.Credentials;
import com.checkmarx.v7.CxSDKWebService;
import com.checkmarx.v7.CxSDKWebServiceSoap;
import com.checkmarx.v7.CxWSResponseLoginData;
import com.cx.client.CxClientServiceImpl;
import com.cx.client.exception.CxClientException;
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
import java.util.Map;

/**
 * A resource of message.
 */
@Path("/")
public class MyRestResource {


    private static URL WSDL_LOCATION = CxSDKWebService.class.getClassLoader().getResource("WEB-INF/CxSDKWebService.wsdl");
    private static final QName SERVICE_NAME = new QName("http://Checkmarx.com/v7", "CxSDKWebService");
    private static String SDK_PATH = "/cxwebinterface/sdk/CxSDKWebService.asmx";



    @POST
    @Path("test/connection")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public Response testConnestion( Map<Object, Object> key) {
        String result ="";
        URL url = null;
        String urli = key.get("url").toString();
        try {
            url = new URL(urli);
            HttpURLConnection urlConn;
            URL toCheck = new URL(url + SDK_PATH);
            urlConn = (HttpURLConnection) toCheck.openConnection();
            urlConn.connect();
        } catch (Exception e) {
            result = "Invalid URL";
            return Response.status(400).entity(result).build();
        }

        String username = key.get("username").toString();
        String password = key.get("password").toString();
        try {
            if (loginToServer(url, username, password)){
                result =  "Success!";
                return Response.status(200).entity(result).build();}
            else{
                result = "Wrong username or password";
                return Response.status(400).entity(result).build();}
        }

       catch (Exception CxClientException) {
           result = "Fail to login";
           return Response.status(400).entity(result).build();
       }

    }


    private Map<Object, Object>  getURLFromKey(Map<Object, Object> key) {
        // In reality, this data would come from a database or some component
        // within the hosting application, for demonstration purpopses I will
        // just return the key
        return key;
    }

    private String getMessageFromKey(String key) {
        // In reality, this data would come from a database or some component
        // within the hosting application, for demonstration purpopses I will
        // just return the key
        return key;
    }

    public boolean loginToServer(URL url, String username, String password)   {
        try {
            CxSDKWebService ss = new CxSDKWebService(WSDL_LOCATION, SERVICE_NAME);
            CxSDKWebServiceSoap client = ss.getCxSDKWebServiceSoap();
            BindingProvider bindingProvider = (BindingProvider) client;
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url + SDK_PATH);

            Credentials credentials = new Credentials();
            credentials.setUser(username);
            credentials.setPass(password);
            CxWSResponseLoginData res = client.login(credentials, 1099);
            String sessionId = res.getSessionId();

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



}
 
