package com.cx.client.rest.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by: iland.
 * Date: 1/13/2016.
 */
public class RESTUtil {

    private static final Logger log = LoggerFactory.getLogger(RESTUtil.class);

    public static String appendVersion(String url, String version) {
        String versionStr;
        if (url.contains("?")) {
            versionStr = String.format("%s&v=%s", url, version);
        } else {
            versionStr = String.format("%s?v=%s", url, version);
        }
        log.debug("Version: " + versionStr);
        return versionStr;
    }

    public static String extractFromResponse(ClientResponse res, String xpathExpression) throws
            XPathExpressionException {
        String resType = res.getEntity(String.class);
        log.debug("Response type: " + resType);
        XPath xpath = XPathFactory.newInstance().newXPath();

        byte[] bytes = resType.getBytes();
        InputSource inputSource = new InputSource(new ByteArrayInputStream(bytes));

        return (String) xpath.evaluate(xpathExpression, inputSource, XPathConstants.STRING);
    }

    public static String[] extractFromResponse(ClientResponse res, String[] xpathExpressions) throws XPathExpressionException {
        String resType = res.getEntity(String.class);
        log.debug("Response type: " + resType);
        XPath xpath = XPathFactory.newInstance().newXPath();

        byte[] bytes = resType.getBytes();
        String[] values = new String[xpathExpressions.length];
        for (int i = 0; i < xpathExpressions.length; i++) {
            InputSource inputSource = new InputSource(new ByteArrayInputStream(bytes));
            values[i] = (String) xpath.evaluate(xpathExpressions[i], inputSource, XPathConstants.STRING);
        }
        return values;
    }

    public static String getResponseData(ClientResponse response) throws Exception {
        return response.getEntity(String.class);
    }

    public static JsonObject getResDataJsonObj(ClientResponse response) {
        String json = response.getEntity(String.class);
        JsonParser jsonParser = new JsonParser();
        return (JsonObject) jsonParser.parse(json);
    }

    public static JsonArray getResDataJsonArray(ClientResponse response) {
        String json = response.getEntity(String.class);
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(json).getAsJsonArray();
    }

    public static NewCookie findCookie(ClientResponse response, String cookieName) {
        List<NewCookie> cookies = response.getCookies();
        for (NewCookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    public static String findHeader(ClientResponse response, String headerName) {
        MultivaluedMap<String, String> headers = response.getHeaders();

        for (String header : headers.keySet()) {
            int i = 0;
            if (header.equalsIgnoreCase(headerName)) {
                return headers.get(header).get(i);
            }
        }
        return null;
    }

    public static String getPostBody(RequestData requestData) {
        if (requestData == null) {
            return "";
        }
        StringBuilder postBody = new StringBuilder();

        //Append post parameters to request body
        if (requestData.getPostParameters() != null) {
            for (String name : requestData.getPostParameters().keySet()) {
                postBody.append("&");
                postBody.append(name + "=" + requestData.getPostParameters().get(name));
            }
            //remove first '&' char
            if (postBody.length() > 0) {
                postBody.replace(0, 1, "");
            }

        }
        if (requestData.getPostData() != null) {
            //Append post body
            postBody.append(requestData.getPostData());
        }
        return postBody.toString();
    }

    /**
     * Can be overridden by a child class to specify a different content type
     *
     * @param contentType the content type you want.
     * @return signature method to use in Oauth request (Default to "application/xml")
     */
    public static MediaType getContentType(String contentType) {
        if (Pattern.compile(Pattern.quote("xml"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_XML_TYPE;
        } else if (Pattern.compile(Pattern.quote("/json"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_JSON_TYPE;
        } else if (Pattern.compile(Pattern.quote("atom"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        } else if (Pattern.compile(Pattern.quote("urlencoded"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_FORM_URLENCODED_TYPE;
        } else if (Pattern.compile(Pattern.quote("octet"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        } else if (Pattern.compile(Pattern.quote("svg"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_SVG_XML_TYPE;
        } else if (Pattern.compile(Pattern.quote("xhtml"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.APPLICATION_XHTML_XML_TYPE;
        } else if (Pattern.compile(Pattern.quote("data"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.MULTIPART_FORM_DATA_TYPE;
        } else if (Pattern.compile(Pattern.quote("html"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.TEXT_HTML_TYPE;
        } else if (Pattern.compile(Pattern.quote("plain"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.TEXT_PLAIN_TYPE;
        } else if (contentType.equalsIgnoreCase("") || contentType.equalsIgnoreCase("*/*") || Pattern.compile(Pattern.quote("wild"), Pattern.CASE_INSENSITIVE).matcher(contentType).find()) {
            return MediaType.WILDCARD_TYPE;
        }

        return MediaType.valueOf(contentType);
    }

}
