import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		
		public static int pageCounter =  0;
		public static int MAX_PAGE_COUNT = 40; //should download a total of 40 pages (that is, if we don't encounter any errors during crawling)
		public static int fileNo = 0;
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String url = "https://www.ucr.edu/";
		
		crawl(url); 

	}

	private static void crawl(String url) {
		//level: sets how many layers of websites we want to go into before we start going back up in the recursion
		//url: the url we want to visit
		//visited: keep track of the sites we already visited
		
		frontier.add(url);
		visited.add(url);
		
		while(!frontier.isEmpty() && pageCounter <= MAX_PAGE_COUNT) {
			String crawlUrl = frontier.remove();
			Document doc = request(crawlUrl); 
			pageCounter+=1; 
			
			if(doc != null) { //if doc is ok to visit, then..
				
				//TODO: Might need to add a depth 
				for(Element link : doc.select("a[href]")) { //..select all hyperlinks in HTML page from seed
					String next_link = link.absUrl("href");
					
					if(visited.contains(next_link) == false) { //checking if link was already visited (to avoid duplicates)
						frontier.add(next_link);
						visited.add(next_link);
					}
					
				}
			}
			
	}
}

	//helper function: requests access to the link 
	private static Document request(String url) {
		try { //catch error when it is unable to connect
			Connection con = Jsoup.connect(url); //connect to web page
			Document doc = con.get(); //retrieve page content
			
			if(con.response().statusCode() == 200) { //"statusCode()==200" checks if web page is connected (i think) 
				System.out.println("Link: " + url + "\n");
				System.out.println(doc.title() + "\n\n"); //web page's title
				visited.add(url); //add link to visited ArrayList
				
				//TODO: call file writer function here after writing it
				DownloadHTML(url, fileNo);
				fileNo+=1;

				return doc;
			}
			return null; //unable to get document 
		}
		
		catch(IOException e) {
			System.out.println("Unable to connect to " + url);
			return null; //unable to connect
			
		}
}
	
	public static void DownloadHTML(String u, int count) {
		try {
			String html = Jsoup.connect(u).get().html();
			String c = Integer.toString(count);
			String fname = "C:\\Users\\hanna\\eclipse-workspace\\CS172 Project\\files\\File" + c + ".txt"; //storing html files into my \files folder
			
			//String fname = "File" + c + ".txt"; //without a path (I just added my own path to an empty folder so I can keep things more organized)
			
			File file = new File(fname);
			
			if(!file.exists()) {
			FileWriter writer = new FileWriter(file);
			
			writer.write("<!--" + u + "-->\n\n"); //add the corresponding url to the top of the downloaded file
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
}