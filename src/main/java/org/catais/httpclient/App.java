package org.catais.httpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import com.google.gson.Gson;

public class App {

	public static void main(String[] args) throws ClientProtocolException, IOException {
			
		String protocol = "https";
		String host = "dev.geodienste.ch";
		int port = 443;
		String path = "/data_agg/interlis/import";
		
		String usr = "geodienste_so";
		String pwd = "ee4Aipai";
		
		String xtf = "/Users/stefan/Downloads/gewaesserschutz.xtf.zip";
		

		HttpHost targetHost = new HttpHost(host, port, protocol);
        HttpPost httpPost = new HttpPost(targetHost.toURI() + path);

		// Credentials
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(usr, pwd);
		provider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), credentials);
		HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

        // Add AuthCache to the execution context
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

        // Set parameters
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        
        FileBody fileBody = new FileBody(new File(xtf)); 
        StringBody topicBody = new StringBody("test_gws", ContentType.MULTIPART_FORM_DATA);
        StringBody publishBody = new StringBody("true", ContentType.MULTIPART_FORM_DATA);
        
        builder.addPart("lv03_file", fileBody);
        builder.addPart("topic", topicBody);
        builder.addPart("publish", publishBody);
        HttpEntity entity = builder.build();

        httpPost.setEntity(entity);
        
        // Do the request
		HttpResponse response = client.execute(httpPost, localContext);
		int statusCode = response.getStatusLine().getStatusCode();
		
		System.out.println(statusCode);
		
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = br.readLine()) != null) {
		    result.append(line);
		}

		System.out.println(result);

		Map jsonJavaRootObject = new Gson().fromJson(result.toString(), Map.class);

		System.out.println(jsonJavaRootObject);

		boolean success = false;
		success = (boolean) jsonJavaRootObject.get("success");
		
		if (success) {
			System.out.println(jsonJavaRootObject.get("url"));
		} else {
			System.out.println(jsonJavaRootObject.get("exceptions"));
		}
	
	}

}
