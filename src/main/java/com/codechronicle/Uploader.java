package com.codechronicle;

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Uploader {
	
	private static Logger log = LoggerFactory.getLogger(Uploader.class);
	
	private DefaultHttpClient client = new DefaultHttpClient();
	
	public static void main(String[] args) throws Exception {
		
		Uploader uploader = new Uploader();
		
		String baseURL = "https://saptarshi.homedns.org/nas/media/Pictures/2011/";
		//String baseURL = "https://saptarshi.homedns.org/nas/media/Pictures/2009/255.Hawaii/Dad%20Hawaii%202/Kuaui-1/";
		uploader.processURL(baseURL);
	}
	
	public Uploader() {
		
		// Configure http client
		HttpConnectionHelper.configureSecurityFromPropertyFile(client, "server.props");
		HttpConnectionHelper.setNoSSLCertVerification(client);
	}

	private void processURL(String baseURL) throws Exception {
		String page = HttpConnectionHelper.getResponseAsString(HttpConnectionHelper.executeGetRequest(client, baseURL));
		
		Document doc = Jsoup.parse(page, baseURL);
		
		Elements elements = doc.getElementsByTag("a");
		Iterator<Element> elemIter = elements.iterator();
		
		for (Element link : elements) {
			String absHref = link.attr("abs:href");
			
			if (isImage(absHref)) {
				processImage(absHref);
			} else {
				if (isSubDirectory(baseURL, absHref)) {
					System.out.println("Processing subdirectory : " + absHref);
					processURL(absHref);
				} else {
					System.out.println("IGNORING : " + absHref);
				}
			}
		}
	}

	private int testCounter = 0;
	
	private void processImage(String url) {
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("origUrl", url));
		params.add(new BasicNameValuePair("localFile", ""));
		
		String defaultTag = generateDefaultTag(url);
		params.add(new BasicNameValuePair("tags", defaultTag));
		
		HttpConnectionHelper.executePost(client, "http://localhost:8080/rest/image", params);
		System.out.println("Posting image : " + url + " Tag: [" + defaultTag + "]");
		
		// Just for testing
		/*
		if (testCounter++ > 0) {
			System.exit(0);
		}
		*/
	}

	private String generateDefaultTag(String url) {
		
		String tag = null;
		
		try {
			
			URL encodedURL = new URL(url);
			tag = URLDecoder.decode(encodedURL.getFile(), "utf-8");
			tag = tag.substring(0,tag.lastIndexOf("/"));
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		return tag;
	}

	private boolean isSubDirectory(String baseURL, String url) {
		
		int qmarkIndex = url.lastIndexOf('?');
		
		if (qmarkIndex != -1) {
			url = url.substring(0,qmarkIndex);
		}
		
		if (url.length() <= baseURL.length()) {
			return false;
		}
		
		if (!url.endsWith("/")) {
			return false;
		}
		
		return true;
	}

	private boolean isImage(String absHref) {
		return absHref.toLowerCase().endsWith(".jpg");
	}
}
