package com.cx.client.rest;

import com.cx.client.exception.CxClientException;
import com.cx.client.rest.dto.CreateOSAScanResponse;
import com.cx.client.rest.dto.OSAScanStatus;
import com.cx.client.rest.dto.OSASummaryResults;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Created by: Dorg & Galn.
 * Date: 16/06/2016.
 */
public class CxRestClient {

    private final String username;
    private final String password;
    private Client client;
    private WebResource root;

    public static final String OSA_SCAN_PROJECT_PATH = "projects/{projectId}/scans";
    public static final String OSA_SCAN_STATUS_PATH = "scans/{scanId}";
    public static final String OSA_SCAN_SUMMARY_PATH = "projects/{projectId}/summaryresults";
    public static final String OSA_SCAN_HTML_PATH = "projects/{projectId}/opensourceanalysis/htmlresults";
    public static final String OSA_SCAN_PDF_PATH = "projects/{projectId}/opensourceanalysis/pdfresults";
    private static final String AUTHENTICATION_PATH = "auth/login";
    private static final String LOGIN_CREDENTIALS = "{\"username\": \"{userName}\", \"password\": \"{userPassword}\"}";
    public static final String OSA_ZIPPED_FILE_KEY_NAME = "OSAZippedSourceCode";
    private static final String ROOT_PATH = "CxRestAPI";
    public static final String CSRF_TOKEN_HEADER = "CXCSRFToken";
    private static ArrayList<Object> newCookies;
    private static ArrayList<Object> cookies;
    private static String csrfToken;
    private static String hostName;
    ObjectMapper mapper = new ObjectMapper();

    private static Logger log = LoggerFactory.getLogger(CxRestClient.class);


    private ClientFilter clientResponseFilter = new ClientFilter() {
        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
            if (newCookies != null) {
                cookies = convertToRequestCookie(newCookies);
                request.getHeaders().put("Cookie", cookies);
            }
            if (csrfToken != null) {
                request.getHeaders().putSingle(CSRF_TOKEN_HEADER, csrfToken);
            }

            ClientResponse response = getNext().handle(request);
            if (response.getCookies() != null) {
                if (newCookies == null) {
                    newCookies = new ArrayList<Object>();
                }
                // simple addAll just for illustration (should probably check for duplicates and expired newCookies)
                newCookies.addAll(response.getCookies());

                for (NewCookie cookie : response.getCookies()) {
                    if (cookie.getName().equals(CSRF_TOKEN_HEADER)) {
                        csrfToken = cookie.getValue();
                    }
                }
            }
            return response;
        }
    };

    private ArrayList<Object> convertToRequestCookie(ArrayList<Object> newCookies) {
        ArrayList<Object> cookies = new ArrayList<Object>();
        for (Object cookie : newCookies) {
            cookies.add(((NewCookie) cookie).toCookie());
        }
        return cookies;
    }

    public CxRestClient(String hostname, String username, String password) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;

        client = Client.create();
        root = client.resource(hostname + "/" + ROOT_PATH);
        client.addFilter(clientResponseFilter);
    }

    public void setLogger(Logger log) {
        CxRestClient.log = log;
    }

    public void destroy() {
        client.destroy();
    }


    public void login() throws CxClientException {
        newCookies = null;
        csrfToken = null;
        String credentials = LOGIN_CREDENTIALS.replace("{userName}", username).replace("{userPassword}", password);
        ClientResponse response = root.path(AUTHENTICATION_PATH).type("application/json").post(ClientResponse.class, credentials);//ClientResponse response = webResource.post(ClientResponse.class, "payload");
        validateResponse(response, Response.Status.OK, "fail to perform login");
    }

    public CreateOSAScanResponse createOSAScan(long projectId, File zipFile) throws CxClientException {

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(OSA_ZIPPED_FILE_KEY_NAME, zipFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        multiPart.bodyPart(fileDataBodyPart);

        String resolvedPath = OSA_SCAN_PROJECT_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int
        ClientResponse response = root.path(resolvedPath).type(multiPart.getMediaType()).post(ClientResponse.class, multiPart);
        validateResponse(response, Response.Status.ACCEPTED, "fail create OSA scan");

        return convertToObject(response, CreateOSAScanResponse.class);
    }

    public OSAScanStatus getOSAScanStatus(String scanId) throws CxClientException {
        String resolvedPath = OSA_SCAN_STATUS_PATH.replace("{scanId}", String.valueOf(scanId));//TODO if  not int
        ClientResponse response = root.path(resolvedPath).get(ClientResponse.class);
        validateResponse(response, Response.Status.OK, "fail get OSA scan status");

        return convertToObject(response, OSAScanStatus.class);
    }

    public OSASummaryResults getOSAScanSummaryResults(long projectId) throws CxClientException {
        String resolvedPath = OSA_SCAN_SUMMARY_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int
        ClientResponse response = root.path(resolvedPath).get(ClientResponse.class);
        validateResponse(response, Response.Status.OK, "fail get OSA scan summary results");

        return convertToObject(response, OSASummaryResults.class);
    }

    public String getOSAScanHtmlResults(long projectId) throws CxClientException {
        String resolvedPath = OSA_SCAN_HTML_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int
        ClientResponse response = root.path(resolvedPath).get(ClientResponse.class);
        validateResponse(response, Response.Status.OK, "fail get OSA scan html results");

        return response.getEntity(String.class);
    }

    public byte[] getOSAScanPDFResults(long projectId) throws CxClientException {
        String resolvedPath = OSA_SCAN_PDF_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int

        ClientResponse response = root.path(resolvedPath).get(ClientResponse.class);
        validateResponse(response, Response.Status.OK, "fail get OSA scan pdf results");
        return response.getEntity(byte[].class);

    }

    private void validateResponse(ClientResponse response, Response.Status expectedStatus, String message) throws CxClientException {
        if (response.getStatus() != expectedStatus.getStatusCode()) {
            throw new CxClientException(message + ": " + response.getStatus());
        }
    }

    private <T> T convertToObject(ClientResponse response, Class<T> valueType) throws CxClientException {
        String json = response.getEntity(String.class);
        T ret = null;
        try {
            ret = mapper.readValue(json, valueType);
        } catch (IOException e) {
            log.debug("fail to parse json response: [" + json + "]", e);
            throw new CxClientException("fail to parse json response: " + e.getMessage());
        }
        return ret;
    }
}
