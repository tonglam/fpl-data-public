package com.tong.fpl.utils;

import okhttp3.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2021/8/30
 */
public class HttpUtils {

    private static final OkHttpClient client = new OkHttpClient();

    private static final CookieStore cookieStore = new BasicCookieStore();
    private static final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private static final CloseableHttpClient httpclient = createHttpClient();

    private static CloseableHttpClient createHttpClient() {
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(6000)
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();
    }

    public static Optional<String> httpGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                return Optional.of(result);
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            httpclient.close();
        }
        return Optional.empty();
    }

    public static Optional<String> httpPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return Optional.of(response.body().string());
        }
    }

}
