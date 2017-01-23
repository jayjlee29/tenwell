package org.tenwell.identity.core.saml2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.dto.SAMLSingleLogoutRequestVO;

/**
 * @author jglee
 *
 */
public class SAMLLogoutRequestSender {
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLLogoutRequestSender.class);
	
	final static int singleLogoutRetryCount = 3;
	final static int singleLogoutRetryInterval = 5000;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    private static SAMLLogoutRequestSender instance = new SAMLLogoutRequestSender();

    /**
     * A private constructor since we are implementing a singleton here
     */
    private SAMLLogoutRequestSender() {

    }

    /**
     * getInstance method of LogoutRequestSender, as it is a singleton
     *
     * @return LogoutRequestSender instance
     */
    public static SAMLLogoutRequestSender getInstance() {
        return instance;
    }

    /**
     * takes an array of SingleLogoutRequestDTO objects, creates and submits each of them as a task
     * to the thread pool
     *
     * @param singleLogoutRequestDTOs Array of SingleLogoutRequestDTO representing all the session participants
     */
    public void sendLogoutRequests(SAMLSingleLogoutRequestVO[] singleLogoutRequestDTOs) {
        if (singleLogoutRequestDTOs == null) {
            return;
        }
        // For each logoutReq, create a new task and submit it to the thread pool.
        for (SAMLSingleLogoutRequestVO reqDTO : singleLogoutRequestDTOs) {
            threadPool.submit(new LogoutReqSenderTask(reqDTO));
            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("A logoutReqSenderTask is assigned to the thread pool");

            }
        }
    }
    
    
    /**
     * This method is used to derive the port from the assertion consumer URL.
     *
     * @param assertionConsumerURL Assertion Consumer URL
     * @return Port, if mentioned in the URL, or else 443 as the default value
     * @throws MalformedURLException when the ACS is malformed.
     */
    private int derivePortFromAssertionConsumerURL(String assertionConsumerURL)
            throws URISyntaxException {
        int port = 443;    // use 443 as the default port
        try {
            URI uri = new URI(assertionConsumerURL);
            if (uri.getPort() != -1) {    // if the port is mentioned in the URL
                port = uri.getPort();
            } else if ("http".equals(uri.getScheme())) {  // if it is using http
                port = 80;
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Error deriving port from the assertion consumer url", e);
            throw e;
        }
        return port;
    }
    
    /**
     * This class is used to model a single logout request that is being sent to a session participant.
     * It will send the logout req. to the session participant in its 'run' method when this job is
     * submitted to the thread pool.
     */
    private class LogoutReqSenderTask implements Runnable {

        private SAMLSingleLogoutRequestVO logoutReqDTO;

        public LogoutReqSenderTask(SAMLSingleLogoutRequestVO logoutReqDTO) {
            this.logoutReqDTO = logoutReqDTO;
        }

        @Override
        public void run() {
            List<NameValuePair> logoutReqParams = new ArrayList<NameValuePair>();
            StringBuffer logoutRequestWithSoapBinding = new StringBuffer();
            String decodedSAMLRequest = null;

            decodedSAMLRequest = logoutReqDTO.getLogoutResponse();

            logoutReqParams.add(new BasicNameValuePair(SAMLSSOConstants.SAML_REQUEST_PARAM_KEY, SAMLSSOUtil.encode(logoutReqDTO.getLogoutResponse())));

            LOGGER.debug("LogoutReqSenderTask run : " + decodedSAMLRequest);

            try {
                int port = derivePortFromAssertionConsumerURL(logoutReqDTO.getAssertionConsumerURL());
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(logoutReqParams, SAMLSSOConstants.ENCODING_FORMAT);
                HttpPost httpPost = new HttpPost(logoutReqDTO.getAssertionConsumerURL());
                httpPost.setEntity(entity);
                httpPost.addHeader(SAMLSSOConstants.COOKIE_PARAM_KEY, SAMLSSOConstants.SESSION_ID_PARAM_KEY + logoutReqDTO.getRpSessionId());
                TrustManager easyTrustManager = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] x509Certificates,
                            String s)
                            throws java.security.cert.CertificateException {
                        //overridden method, no method body needed here
                    }

                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] x509Certificates,
                            String s)
                            throws java.security.cert.CertificateException {
                        //overridden method, no method body needed here
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                };

                SSLContext sslContext = SSLContext.getInstance(SAMLSSOConstants.CRYPTO_PROTOCOL);
                sslContext.init(null, new TrustManager[]{easyTrustManager}, null);
                SSLSocketFactory sf = new SSLSocketFactory(sslContext);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                Scheme httpsScheme = new Scheme(SAMLSSOConstants.COM_PROTOCOL, sf, port);

                HttpClient httpClient = new DefaultHttpClient();
                httpClient.getConnectionManager().getSchemeRegistry().register(httpsScheme);

                HttpResponse response = null;
                boolean isSuccessfullyLogout = false;
                for (int currentRetryCount = 0; currentRetryCount < singleLogoutRetryCount; currentRetryCount++) {
                    int statusCode = 0;

                    //Completely consume the previous response before retrying
                    if (response != null) {
                        HttpEntity httpEntity = response.getEntity();
                        if (httpEntity != null && httpEntity.isStreaming()) {
                            InputStream instream = httpEntity.getContent();
                            if (instream != null)
                                instream.close();
                        }
                    }

                    // send the logout request as a POST
                    try {
                        response = httpClient.execute(httpPost);
                        statusCode = response.getStatusLine().getStatusCode();
                    } catch (IOException e) {
                        if (LOGGER.isDebugEnabled()) {
                        	LOGGER.debug("Error while executing http request.", e);
                        }
                        // ignore this exception since retrying is enabled if response is null.
                    }
                    if (response != null && (isHttpSuccessStatusCode(statusCode) || isHttpRedirectStatusCode(statusCode))) {
                        if (LOGGER.isDebugEnabled()) {
                        	LOGGER.debug("single logout request is sent to : " + logoutReqDTO.getAssertionConsumerURL() +
                                    " is returned with " + HttpStatus.getStatusText(response.getStatusLine().getStatusCode()));
                        }
                        isSuccessfullyLogout = true;
                        break;
                    } else {
                        if (statusCode != 0) {
                            LOGGER.warn("Failed single logout response from " +
                                    logoutReqDTO.getAssertionConsumerURL() + " with status code " +
                                    HttpStatus.getStatusText(statusCode));
                        }
                        try {
                            synchronized (Thread.currentThread()) {
                                Thread.currentThread().wait(singleLogoutRetryInterval);
                            }
                            LOGGER.info("Sending single log out request again with retry count " +
                                    (currentRetryCount + 1) + " after waiting for " +
                                    singleLogoutRetryInterval + " milli seconds to " +
                                    logoutReqDTO.getAssertionConsumerURL());
                        } catch (InterruptedException e) {
                            //Todo: handle this in better way.
                        }
                    }

                }
                if (!isSuccessfullyLogout) {
                    LOGGER.error("Single logout failed after retrying " + singleLogoutRetryCount +
                            " times with time interval " + singleLogoutRetryInterval + " in milli seconds.");
                }

            } catch (IOException e) {
                LOGGER.error("Error sending logout requests to : " +
                        logoutReqDTO.getAssertionConsumerURL(), e);
            } catch (GeneralSecurityException e) {
            	LOGGER.error("Error registering the EasySSLProtocolSocketFactory", e);
            } catch (RuntimeException e) {
            	LOGGER.error("Runtime exception occurred.", e);
            } catch (URISyntaxException e) {
            	LOGGER.error("Error deriving port from the assertion consumer url", e);
            }
        }
        
        /**
         * This check if the status code is 2XX, check value between 200 and 300
         *
         * @param status
         * @return
         */
        private boolean isHttpSuccessStatusCode(int status) {
            return status >= 200 && status < 300;
        }
        
        private boolean isHttpRedirectStatusCode(int status) {
            return status == 302 || status == 303;
        }
    }
}
