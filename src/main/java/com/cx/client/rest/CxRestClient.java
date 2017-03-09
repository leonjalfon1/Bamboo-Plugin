package com.cx.client.rest;

import com.cx.client.exception.CxClientException;
import com.cx.client.rest.dto.CreateOSAScanResponse;
import com.cx.client.rest.dto.OSAScanStatus;
import com.cx.client.rest.dto.OSASummaryResults;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by: Dorg & Galn.
 * Date: 16/06/2016.
 */
public class CxRestClient {

    private final String username;
    private final String password;
    private HttpClient apacheClient;
    private  HttpResponse loginResponse;
    private Header[] setCookies;
    private CookieStore cookieStore;

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

        //create httpclient
        cookieStore = new BasicCookieStore();
        apacheClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    public void setLogger(Logger log) {
        CxRestClient.log = log;
    }

   public void destroy() {
       apacheClient.getConnectionManager().shutdown();
    }


    public void login() throws CxClientException, IOException {
        //create login request
        HttpPost loginPost = new HttpPost(hostName  + "/" + ROOT_PATH +"/"+ AUTHENTICATION_PATH);
        String json = LOGIN_CREDENTIALS.replace("{userName}", username).replace("{userPassword}", password);
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        loginPost.setEntity(requestEntity);
        //send login request
         loginResponse = apacheClient.execute(loginPost);

        //validate login response

        validateApacheHttpClientResponse(loginResponse, 200, "Fail to authenticate");
    }

/*
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
*/

    public CreateOSAScanResponse createScanLargeFileWorkaround(long projectId, File zipFile) throws IOException, CxClientException {




        HttpPost post = new HttpPost( hostName  + "/" + ROOT_PATH +"/"+   OSA_SCAN_PROJECT_PATH.replace("{projectId}", String.valueOf(projectId)));
        InputStreamBody streamBody = new InputStreamBody(new FileInputStream(zipFile) , ContentType.APPLICATION_OCTET_STREAM, OSA_ZIPPED_FILE_KEY_NAME);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(OSA_ZIPPED_FILE_KEY_NAME, streamBody);
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        //set csrf header and cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                post.addHeader(CSRF_TOKEN_HEADER, c.getValue());
            }
        }
        setCookies = loginResponse.getHeaders("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (Header h : setCookies) {
            cookies.append(h.getValue()).append(";");
        }
        post.addHeader("cookie", cookies.toString());

        //send scan request
        HttpResponse response = apacheClient.execute(post);

        //verify scan request
        String createScanResponseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        validateApacheHttpClientResponse(response, 202, "Fail to create OSA scan");

        //extract response as object and return the link
        ObjectMapper mapper = new ObjectMapper();
        CreateOSAScanResponse createScanResponse = mapper.readValue(createScanResponseBody,CreateOSAScanResponse.class);
        return createScanResponse;
    }


    public OSAScanStatus getOSAScanStatus(String scanId) throws CxClientException, IOException {
        String resolvedPath =  hostName  + "/" + ROOT_PATH +"/" +OSA_SCAN_STATUS_PATH.replace("{scanId}", String.valueOf(scanId));//TODO if  not int
        HttpGet getRequest = new HttpGet(resolvedPath);
        //set csrf header and cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                getRequest.addHeader(CSRF_TOKEN_HEADER, c.getValue());
            }
        }
        setCookies = loginResponse.getHeaders("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (Header h : setCookies) {
            cookies.append(h.getValue()).append(";");
        }
        getRequest.addHeader("cookie", cookies.toString());

        HttpResponse response = apacheClient.execute(getRequest);
        validateResponse(response, Response.Status.OK, "fail get OSA scan status");

        return convertToObject(response, OSAScanStatus.class);
    }

    public OSASummaryResults getOSAScanSummaryResults(long projectId) throws CxClientException, IOException {
        String resolvedPath = hostName  + "/" + ROOT_PATH +"/" + OSA_SCAN_SUMMARY_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int
        HttpGet getRequest = new HttpGet(resolvedPath);
        //set csrf header and cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                getRequest.addHeader(CSRF_TOKEN_HEADER, c.getValue());
            }
        }
        setCookies = loginResponse.getHeaders("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (Header h : setCookies) {
            cookies.append(h.getValue()).append(";");
        }
        getRequest.addHeader("cookie", cookies.toString());

        HttpResponse response = apacheClient.execute(getRequest);
        validateResponse(response, Response.Status.OK, "fail get OSA scan summary results");

        return convertToObject(response, OSASummaryResults.class);
    }

    public String getOSAScanHtmlResults(long projectId) throws CxClientException, IOException {
        String resolvedPath =  hostName  + "/" + ROOT_PATH +"/" +OSA_SCAN_HTML_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int
        HttpGet getRequest = new HttpGet(resolvedPath);
        //set csrf header and cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                getRequest.addHeader(CSRF_TOKEN_HEADER, c.getValue());
            }
        }
        setCookies = loginResponse.getHeaders("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (Header h : setCookies) {
            cookies.append(h.getValue()).append(";");
        }
        getRequest.addHeader("cookie", cookies.toString());

        HttpResponse response = apacheClient.execute(getRequest);
        validateResponse(response, Response.Status.OK, "fail get OSA scan html results");

        return response.getEntity().toString();//TODO
    }

    public byte[] getOSAScanPDFResults(long projectId) throws CxClientException, IOException {
        String resolvedPath =  hostName  + "/" + ROOT_PATH +"/" +OSA_SCAN_PDF_PATH.replace("{projectId}", String.valueOf(projectId));//TODO if  not int

        HttpGet getRequest = new HttpGet(resolvedPath);
        HttpResponse response = apacheClient.execute(getRequest);
        validateResponse(response, Response.Status.OK, "fail get OSA scan pdf results");
        return response.getEntity().toString().getBytes();//TODO EXC

    }
    private void validateApacheHttpClientResponse(HttpResponse response, int status, String message) throws CxClientException {
        if (response.getStatusLine().getStatusCode() != status) {
            throw new CxClientException(message + ": " + "status code: " + response.getStatusLine().getStatusCode() + ". reason phrase: " + response.getStatusLine().getReasonPhrase());
        }
    }

    private void validateResponse(HttpResponse response, Response.Status expectedStatus, String message) throws CxClientException {
        if (response.getStatusLine().getStatusCode() != expectedStatus.getStatusCode()) {
            throw new CxClientException(message + ": " + response.getStatusLine().getStatusCode());
        }
    }

    private <T> T convertToObject(HttpResponse response, Class<T> valueType) throws CxClientException {
        String json = response.getEntity().toString();//TODO check
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
