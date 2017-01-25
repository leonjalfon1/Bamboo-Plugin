package com.cx.client.rest.util;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by: iland.
 * Date: 1/13/2016.
 */
public class RESTHelper {

    private static final Logger log = LoggerFactory.getLogger(RESTHelper.class);

    public static ClientConfig configureClient() {
        TrustManager[] certs = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }
                }};
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (GeneralSecurityException e) {
            log.error("Fail to config REST client", e);
        }

        assert ctx != null;
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(MultiPartWriter.class);
        try {
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }, ctx));
        } catch (Exception e) {
            log.error("Fail to config REST client", e);
        }
        return config;
    }

}
