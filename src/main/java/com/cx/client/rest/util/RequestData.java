package com.cx.client.rest.util;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.util.List;
import java.util.Map;

/**
 * Created by: iland.
 * Date: 1/13/2016.
 */
public class RequestData {

    private String url;
    private String postData;
    private Map<String, String> postParameters;
    private Map<String, String> headers;
    private MediaType contentType;
    private List<NewCookie> cookies;

    public RequestData() {
    }

    public RequestData(String url, String postData, Map<String, String> postParameters,
                       Map<String, String> headers, MediaType contentType, List<NewCookie> cookies) {
        this.url = url;
        this.postData = postData;
        this.postParameters = postParameters;
        this.headers = headers;
        this.contentType = contentType;
        this.cookies = cookies;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public Map<String, String> getPostParameters() {
        return postParameters;
    }

    public void setPostParameters(Map<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public List<NewCookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<NewCookie> cookies) {
        this.cookies = cookies;
    }

}
