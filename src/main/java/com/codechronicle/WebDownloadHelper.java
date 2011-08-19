package com.codechronicle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

public class WebDownloadHelper {

	private static DefaultHttpClient httpclient = new DefaultHttpClient();

	public WebDownloadHelper() {
		
		try {
			SSLSocketFactory socketFactory = new SSLSocketFactory(
					new TrustStrategy() {

						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {

							return (chain.length == 1);
						}
					});
			
			httpclient = new DefaultHttpClient();
			
			Scheme https = new Scheme("https", 443, socketFactory);
			httpclient.getConnectionManager().getSchemeRegistry().register(https);
			
			Properties props = new Properties();
			InputStream propStream = ClassLoader.getSystemClassLoader().getResourceAsStream("server.props");
			props.load(propStream);
			propStream.close();
			
			HttpHost targetHost = new HttpHost(props.getProperty("secureserver"), 443, "https");
			String username = props.getProperty("username");
			String password = props.getProperty("password");
			
			httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	                new UsernamePasswordCredentials(username, password));
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public HttpResponse getResponse(String url) {
		
		HttpGet httpget = new HttpGet(url);
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	public String getAsString(String url) throws Exception {
		
		HttpResponse response = getResponse(url);
		
		StringWriter sw = new StringWriter();
		InputStream is = response.getEntity().getContent();
		IOUtils.copy(is, sw);
		
		return sw.toString();
	}

	public HttpResponse executePost(String url, List<NameValuePair> formparams) {
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(entity);
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httppost);
			
			return response; 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
