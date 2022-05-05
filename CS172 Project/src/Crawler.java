import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Crawler {
		public static Queue<String> frontier = new LinkedList<>();
		public static HashSet<String> visited = new HashSet<>();
		
		public static int pageCounter = 0;
		public static int levelCounter = 0;
		public static int MAX_PAGE_COUNT = 20; //should download a total of 40 pages (that is, if we don't encounter any errors during crawling)
		public static int MAX_LEVEL_COUNT = 5;
		// public static int fileNo = 0;
		
	public static void main(String[] args) {
		// String url = "https://www.ucr.edu/";
		// crawl(url); 

		try {
			String fileName = "CS172 Project\\src\\seed_urls.txt";
			File file = new File(fileName);

			byte[] fileBytes = Files.readAllBytes(file.toPath());
			char singleChar;
			String url = "";
			for (byte b : fileBytes) {
				singleChar = (char) b; // convert from byte to string
				url += singleChar;
				if (singleChar == '\n') { // finished getting one url
					crawl(url);
					url = ""; // empty url string to get new url
				}
			}
		}
		catch (Exception e) {
			System.err.println("File " + e.getMessage() + " is not found."); // handle exception
		}
	}

	private static void crawl(String url) {
		//url: the url we want to visit
		//visited: keep track of the sites we already visited
		levelCounter = 0;

		frontier.add(url);
		visited.add(url);

		while(!frontier.isEmpty() && pageCounter <= MAX_PAGE_COUNT && levelCounter <= MAX_LEVEL_COUNT) {
			String crawlUrl = frontier.remove();
			Document doc = request(crawlUrl); 
			
			System.out.println("CRAWL URL: " + crawlUrl);
			pageCounter += 1;
			levelCounter += 1;
			
			if(doc != null) { //if doc is ok to visit, then...
				//System.out.println(doc.select("a[href]")); //doc.select("a[href]") => combines ALL hyperlinks associated with the current page we are visiting
				
				for(Element link : doc.select("a[href]")) { //..select every single hyperlink in the seed document and visit them if unvisited
					
					String next_link = link.absUrl("href");
					
					//TODO: call normalize(url) function to normalize the link first to see if it's actually a duplicate of a url that has already been visited
					next_link = normalize(next_link);
					boolean valid = isValidUrl(next_link);
					
					if(visited.contains(next_link) == false && valid) { //checking if link was already visited (to avoid duplicates)
						frontier.add(next_link);
						visited.add(next_link);
						System.out.println("NEXT: " + next_link);
					}
				}
			}
	}
}

	//helper function: requests access to the link 
	private static Document request(String url) {
		try { //catch error when it is unable to connect
			Connection con = Jsoup.connect(url); //connect to web page
			Document doc = con.get(); //retrieve page content; get() fetches and parses the HTML file
			
			if(con.response().statusCode() == 200) { //"statusCode()==200" verifies that the document that we requested for succeeded
				System.out.println("Link: " + url + "\n");
				System.out.println(doc.title() + "\n\n"); //web page's title
				
				visited.add(url); //add link to visited HashSet
				
				//TODO: call file writer function here after writing it
				DownloadHTML(url, pageCounter, levelCounter);
				// fileNo+=1;

				return doc;
			}
			return null; //unable to get document 
		}
		
		catch(IOException e) {
			return null; //unable to connect
		}
}
	
	public static void DownloadHTML(String u, int pageCount, int levelCount) {
		try {
			String html = Jsoup.connect(u).get().html();
			String pc = Integer.toString(pageCount);
			String lc = Integer.toString(levelCount);
			
			//String fname = "C:\\Users\\hanna\\git\\Web-Crawler-Project\\CS172 Project\\files\\File" + c + ".txt"; 
			String fname = "CS172 Project\\src\\html_files\\File_no" + pc + "_level" + lc + ".txt"; 
			
			File file = new File(fname);
			
			if(!file.exists()) {
			FileWriter writer = new FileWriter(file);
			
			writer.write("<!--" + u + "-->\n\n"); //add the corresponding url to the top of the downloaded file; might need to delete this later
			writer.write(html);
			
			writer.flush();
			writer.close();		
			
			return;
			}
			
		}
		catch (IOException e) {
				e.printStackTrace();
		}
		return;
	}


	public static boolean isValidUrl(String link) {
		boolean valid = true;
		
		if(!link.contains(".edu")) { //if link is not .edu, do not visit it.
			valid = false;
		}
		if(link.contains(".pdf") || link.contains(".png") || link.startsWith("mailto:")) { //unsure about .png condition
			valid = false;
		}
		
		return valid;
	}
	
	
	
	//TODO: NEED TO ADD MORE TO normalize() (does not completely clean the url yet)
	public static String normalize(String link) { //this function should return a normalized string of the url
		int lastPos = link.length();
		String newLink = link;
		
		//indexOf returns -1 if "?" does not exist in string; otherwise, it returns the index of the 1st occurrence of the char "?"
		if(link.indexOf("?") > 0) { 
			lastPos = link.indexOf("?");
		}
		if(link.indexOf("#") > 0) {
			lastPos = link.indexOf("#");
		}
	
		newLink = link.substring(0, lastPos);
		return newLink;
	}
	
}