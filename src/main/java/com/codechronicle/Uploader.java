package com.codechronicle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Uploader {
	
	private static Logger log = LoggerFactory.getLogger(Uploader.class);
	
	private WebDownloadHelper web = new WebDownloadHelper();
	
	public static void main(String[] args) throws Exception {
		
		Uploader uploader = new Uploader();
		
		String baseURL = "https://saptarshi.homedns.org/nas/media/Pictures/2009/255.Hawaii/";
		//String baseURL = "https://saptarshi.homedns.org/nas/media/Pictures/2009/255.Hawaii/Dad%20Hawaii%202/Kuaui-1/";
		uploader.processURL(baseURL);
	}

	private void processURL(String baseURL) throws Exception {
		String page = web.getAsString(baseURL);
		
		Document doc = Jsoup.parse(page, baseURL);
		
		Elements elements = doc.getElementsByTag("a");
		Iterator<Element> elemIter = elements.iterator();
		for (Element link : elements) {
			String absHref = link.attr("abs:href");
			
			if (isImage(absHref)) {
				System.out.println("IMAGE DETECTED : " + absHref);
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
	
	
	/*private static void processDirectory(File root) throws Exception {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				processDirectory(file);
			} else {
				processFile(file);
			}
		}
	}

	private static void processFile(File file) throws Exception {
		//System.out.println(file.getAbsolutePath());
		
		String urlBase = "https://saptarshi.homedns.org/nas/media/Pictures/2007";
		String localDirBase = "/tmp/nas";
		
		String urlPath = file.getAbsolutePath().substring(localDirBase.length()+1);
		
		String url = urlBase + "/" + urlPath;
		System.out.println(url);
		
		HttpRes
		
		System.exit(0);
	}*/
}
