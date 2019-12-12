package com.ibm.websphere.samples.pbw.utils;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

public class BankPaymentProcessor {
    public static boolean pay(String cardNum, float subtotalCost, String id, String shippingMethodName) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String bank_uri = System.getProperty("BANK_URI", "http://localhost:8080/Bank-1.0-SNAPSHOT/rest/payment/do");


        URIBuilder builder = null;
        try {
            builder = new URIBuilder(bank_uri);
            builder.setParameter("amount", String.valueOf((int) (subtotalCost*100)));
            HttpPost post = new HttpPost(builder.build());
            post.setHeader("CardNr", cardNum);
            post.setEntity(new StringEntity("PlantsByWebsphere payment. Order id: " + id + ", shipping: " + shippingMethodName));
            CloseableHttpResponse execute = httpClient.execute(post);
            String response = EntityUtils.toString(execute.getEntity());

            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
