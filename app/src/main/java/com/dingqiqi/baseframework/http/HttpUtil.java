package com.dingqiqi.baseframework.http;

import android.content.Context;

import com.dingqiqi.baseframework.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 只是放在这，暂时不用
 * Created by dingqiqi on 2016/12/28.
 */
public class HttpUtil {

    private final static int TIME_OUT = 10000;
    /**
     * HttpHelper对象
     */
    private static HttpUtil mHttpHelper;

    /**
     * 请求方式
     */
    public static String GET = "GET";
    public static String POST = "POST";
    public static String DELETE = "DELETE";
    public static String PUT = "PUT";


    /**
     * 获取HttpHelper对象
     *
     * @return
     */
    public static HttpUtil getInstance() {
        if (mHttpHelper == null) {
            mHttpHelper = new HttpUtil();
        }
        return mHttpHelper;
    }

    /**
     * 设置请求头
     *
     * @param param
     * @param mConnection
     */
    private void setParam(Map<String, String> param, HttpURLConnection mConnection) throws UnsupportedEncodingException {
        if (param == null) {
            return;
        }

        for (Map.Entry<String, String> map : param.entrySet()) {
            if (map.getKey() != null && map.getValue() != null) {
                mConnection.setRequestProperty(map.getKey(), map.getValue());
//                Log.i("HttpHelper", "请求头参数：" + map.getKey() + "=" + map.getValue());
            }
        }
    }

    /**
     * 获取请求参数
     *
     * @param param
     */
    private String getParam(Map<String, String> param) throws UnsupportedEncodingException {
        if (param == null) {
            return "";
        }
        String url = "";

        for (Map.Entry<String, String> map : param.entrySet()) {
            if (map.getKey() != null && map.getValue() != null) {
                url = url + map.getKey() + "=" + URLEncoder.encode(map.getValue(), "utf-8") + "&";
//                Log.i("HttpHelper", "参数：" + map.getKey() + "=" + URLEncoder.encode(map.getValue(), "utf-8"));
            }
        }

        if ("".equals(url)) {
            return "";
        }
        //取出最后一个&
        return url.substring(0, url.length() - 1);
    }

    /**
     * HttpUrlConnection 方式，支持指定load-der.crt证书验证，此种方式Android官方建议
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void initSSL(Context context) throws CertificateException, IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = context.getAssets().open("load-der.crt");
        Certificate ca = cf.generateCertificate(in);

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, null);
        keystore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keystore);

        // Create an SSLContext that uses our TrustManager
        SSLContext mSSLContext = SSLContext.getInstance("TLS");
        mSSLContext.init(null, tmf.getTrustManagers(), null);

        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(mSSLContext.getSocketFactory());
        InputStream input = urlConnection.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        LogUtil.e(result.toString());
    }

    /**
     * HttpUrlConnection支持所有Https免验证，不建议使用
     *
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void initSSLALL() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new TrustAllManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        connection.connect();
        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        StringBuffer result = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        LogUtil.e(result.toString());
    }

    public class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    //    /**
//     * HttpClient方式实现，支持所有Https免验证方式链接
//     *
//     * @throws ClientProtocolException
//     * @throws IOException
//     */
//    public void initSSLAllWithHttpClient() throws ClientProtocolException, IOException {
//        int timeOut = 30 * 1000;
//        HttpParams param = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(param, timeOut);
//        HttpConnectionParams.setSoTimeout(param, timeOut);
//        HttpConnectionParams.setTcpNoDelay(param, true);
//
//        SchemeRegistry registry = new SchemeRegistry();
//        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", TrustAllSSLSocketFactory.getDefault(), 443));
//        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
//        DefaultHttpClient client = new DefaultHttpClient(manager, param);
//
//        HttpGet request = new HttpGet("https://certs.cac.washington.edu/CAtest/");
//        // HttpGet request = new HttpGet("https://www.alipay.com/");
//        HttpResponse response = client.execute(request);
//        HttpEntity entity = response.getEntity();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
//        StringBuilder result = new StringBuilder();
//        String line = "";
//        while ((line = reader.readLine()) != null) {
//            result.append(line);
//        }
//        Log.e("HTTPS TEST", result.toString());
//    }
//
//    /**
//     * HttpClient方式实现，支持验证指定证书
//     *
//     * @throws ClientProtocolException
//     * @throws IOException
//     */
//    public void initSSLCertainWithHttpClient() throws ClientProtocolException, IOException {
//        int timeOut = 30 * 1000;
//        HttpParams param = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(param, timeOut);
//        HttpConnectionParams.setSoTimeout(param, timeOut);
//        HttpConnectionParams.setTcpNoDelay(param, true);
//
//        SchemeRegistry registry = new SchemeRegistry();
//        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", TrustCertainHostNameFactory.getDefault(this), 443));
//        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
//        DefaultHttpClient client = new DefaultHttpClient(manager, param);
//
//        // HttpGet request = new
//        // HttpGet("https://certs.cac.washington.edu/CAtest/");
//        HttpGet request = new HttpGet("https://www.alipay.com/");
//        HttpResponse response = client.execute(request);
//        HttpEntity entity = response.getEntity();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
//        StringBuilder result = new StringBuilder();
//        String line = "";
//        while ((line = reader.readLine()) != null) {
//            result.append(line);
//        }
//        Log.e("HTTPS TEST", result.toString());
//    }

    public interface RequestBack {
        /**
         * 成功
         */
        public void onSuccess(String result);

        /**
         * 失败
         */
        public void onFail(String result);
    }

}
