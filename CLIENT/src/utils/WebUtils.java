package utils;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WebUtils {
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	public static void openWebpage(String urlString) {
		URL url;
		try {
			url = new URL(urlString);
			try {
		        openWebpage(url.toURI());
		    } catch (URISyntaxException e) {
		        e.printStackTrace();
		    }
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	}
}
