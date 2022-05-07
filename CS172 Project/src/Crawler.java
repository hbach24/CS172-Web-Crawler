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

class Link {
	public Link(String startUrl, int startLevel, String startParent) {
		url = startUrl;
		level = startLevel;
		parent = startParent;
	}
	public String url;
	public int level;
	public String parent;
}

public class Crawler {
		
		public static Queue<Link> frontier = new LinkedList<>();
		public static HashSet<String> visited = new HashSet<>();
		
		public static int pageCounter = 0;
		public static int levelCounter = 0;
		public static int MAX_PAGE_COUNT = 10000; //should download a total of 40 pages (that is, if we don't encounter any errors during crawling)
		public static int MAX_LEVEL_COUNT = 2;

		// public static int fileNo = 0;
		
	public static void main(String[] args) {
		// String url = "https://www.ucr.edu/";
		// crawl(url); 
		Link link;

		try {
			String fileName = "CS172 Project\\src\\seed.txt";
			File file = new File(fileName);

			byte[] fileBytes = Files.readAllBytes(file.toPath());
			char singleChar;
			String url = "";
			for (byte b : fileBytes) {
				singleChar = (char) b; // convert from byte to string
				if (singleChar == '\n') { // finished getting one url
					System.out.println("SEED: " + url);
					if (url != "") {
						link = new Link(url, 0, "seed");
						crawl(link);
						url = ""; // empty url string to get new url
						link = null; 
					}					
				} else {
					url += singleChar;
				}
			}
		}
		catch (Exception e) {
			System.err.println("File " + e.getMessage() + " is not found."); // handle exception
		}
	}

	private static void crawl(Link link) {
		//url: the url we want to visit
		//visited: keep track of the sites we already visited
		Link crawlUrl;
		Document doc; 

		frontier.add(link);
		visited.add(link.url);

		while(!frontier.isEmpty() && pageCounter <= MAX_PAGE_COUNT) {
			crawlUrl = frontier.remove();
			 
			System.out.println("CRAWL URL AT LEVEL " + crawlUrl.level + ": " + crawlUrl.url);
			doc = request(crawlUrl);

			pageCounter += 1;
			levelCounter = crawlUrl.level + 1;
			
			if(doc != null && levelCounter <= MAX_LEVEL_COUNT) { //if doc is ok to visit, then...
				//System.out.println(doc.select("a[href]")); //doc.select("a[href]") => combines ALL hyperlinks associated with the current page we are visiting
				
				for(Element l : doc.select("a[href]")) { //..select every single hyperlink in the seed document and visit them if unvisited
					
					String next_url = l.absUrl("href");
					
					//TODO: call normalize(url) function to normalize the link first to see if it's actually a duplicate of a url that has already been visited
					next_url = normalize(next_url);
					boolean valid = isValidUrl(next_url);
					
					if(visited.contains(next_url) == false && valid) { //checking if link was already visited (to avoid duplicates)
						Link next_link = new Link(next_url, levelCounter, crawlUrl.url); 
						frontier.add(next_link);
						visited.add(next_url);
						System.out.println("NEXT: " + next_url);
					}
				}
			}
	}
}

	//helper function: requests access to the link 
	private static Document request(Link link) {
		try { //catch error when it is unable to connect
			Connection con = Jsoup.connect(link.url); //connect to web page
			Document doc = con.get(); //retrieve page content; get() fetches and parses the HTML file
			
			if(con.response().statusCode() == 200) { //"statusCode()==200" verifies that the document that we requested for succeeded
				System.out.println("Link: " + link.url + "\n");
				System.out.println(doc.title() + "\n\n"); //web page's title
				
				// visited.add(link.url); //add link to visited HashSet (already added when crawled??)
				
				//TODO: call file writer function here after writing it
				DownloadHTML(link, pageCounter);
				// fileNo+=1;

				return doc;
			}
			return null; //unable to get document 
		}
		
		catch(IOException e) {
			return null; //unable to connect
		}
}
	
	public static void DownloadHTML(Link link, int pageCount) {
		try {
			String html = Jsoup.connect(link.url).get().html();
			String pc = Integer.toString(pageCount);
			String lc = Integer.toString(link.level);
			
			//String fname = "C:\\Users\\hanna\\git\\Web-Crawler-Project\\CS172 Project\\files\\File" + c + ".txt"; 
			String fname = "CS172 Project\\src\\html_files\\File_no" + pc + "_level" + lc + ".txt"; 
			
			File file = new File(fname);
			
			if(!file.exists()) {
			FileWriter writer = new FileWriter(file, true);
			
			writer.write("<!" + link.url + ">\n"); //add the corresponding url to the top of the downloaded file; might need to delete this later
			writer.write("<!Parent: " + link.parent + ">\n\n");
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