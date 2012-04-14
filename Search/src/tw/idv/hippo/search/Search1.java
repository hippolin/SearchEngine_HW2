package tw.idv.hippo.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.DocumentIndexReader;
import org.galagosearch.core.parse.Tag;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.types.ExtractedLink;
public class Search1 {

	private boolean acceptLocalLinks = true;
    private boolean acceptNoFollowLinks = false;
	
    private TagTokenizer tagtokenizer = new TagTokenizer();
    
    private HashMap<String, Page> links = new HashMap<String, Page>();
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start...");
		
		Search1 s1 = new Search1();
		s1.run();
		
		
	}
	
	public void run() {
		try {
			DocumentIndexReader reader = new DocumentIndexReader("../galagosearch/wiki-small.corpus");
	        DocumentIndexReader.Iterator iterator = reader.getIterator();
	        while (!iterator.isDone()) {
	        	Document document = iterator.getDocument();
	            /*
	        	System.out.println("#IDENTIFIER: " + iterator.getKey());
	            Document document = iterator.getDocument();
	            System.out.println("#METADATA");
	            for (Entry<String, String> entry : document.metadata.entrySet()) {
	            	System.out.println(entry.getKey() + "," + entry.getValue());
	            }
	            */
	            
	        	//切tag
	            tagtokenizer.process(document);
	            
	            //處理link
	            processLink(document);
	            
	            //System.out.println("#TEXT");
	            //System.out.println(document.text);
	            iterator.nextDocument();
	        }
	        
	        
	        //依inlink數排序
	        LinkedList<Page> pagelist = new LinkedList<Page>(links.values()); 
	        
	        Collections.sort(pagelist, new Comparator<Page> () {

				@Override
				public int compare(Page o1, Page o2) {
					// TODO Auto-generated method stub
					return o2.getInlinks().size() - o1.getInlinks().size();
				}
		
			});
	        
	        //印出前10名
	        int i = 1;
	        
	        System.out.println("top:inlinkCount:url");
	        
	        for(Page p : pagelist) {
	        	System.out.println(i + ":" + p.getInlinks().size() + ":" + p.getUrl());
	        	if(++i > 10) break;
	        }
	        
	        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void processLink(Document document) throws IOException {
        String sourceUrl = document.metadata.get("url");

        if (sourceUrl == null) {
            return;
        }
        URL base = new URL(sourceUrl);

        for (Tag t : document.tags) {
            if (t.name.equals("base")) {
                try {
                    base = new URL(base, t.attributes.get("href"));
                } catch (Exception e) {
                    // this can happen when the link protocol is unknown
                    base = new URL(sourceUrl);
                    continue;
                }
            } else if (t.name.equals("a")) {
                String destSpec = t.attributes.get("href");
                URL destUrlObject = null;
                String destUrl = null;

                try {
                    destUrlObject = new URL(base, destSpec);
                    destUrl = destUrlObject.toString();
                } catch (Exception e) {
                    // this can happen when the link protocol is unknown
                    continue;
                }

                boolean linkIsLocal = destUrlObject.getHost().equals(base.getHost());

                
                if(linkIsLocal == false) continue;
                
                // if we're filtering out local links, there's no need to continue
                if (linkIsLocal && acceptLocalLinks == false) {
                    continue;
                }
                ExtractedLink link = new ExtractedLink();

                link.srcUrl = sourceUrl;
                link.destUrl = scrubUrl(destUrl);

                StringBuilder builder = new StringBuilder();

                for (int i = t.begin; i < t.end && i < document.terms.size(); i++) {
                    String term = document.terms.get(i);

                    if (term != null) {
                        builder.append(term);
                        builder.append(' ');
                    }
                }

                link.anchorText = builder.toString().trim();

                if (t.attributes.containsKey("rel") && t.attributes.get("rel").equals("nofollow")) {
                    link.noFollow = true;
                } else {
                    link.noFollow = false;
                }

                boolean acceptable = (acceptNoFollowLinks || link.noFollow == false) &&
                        (acceptLocalLinks || linkIsLocal == false);

                if (acceptable) {
                	
                	Page p = null;
                	
                	if(links.containsKey(link.destUrl)) {
                		p = links.get(link.destUrl);
                	} else {
                		p = new Page(link.destUrl);
                		links.put(link.destUrl, p);
                	}
            
                	p.addInLink(link.srcUrl);
                	
                	if(links.containsKey(link.srcUrl)) {
                		p = links.get(link.srcUrl);
                	} else {
                		p = new Page(link.srcUrl);
                		links.put(link.srcUrl, p);
                	}
                	
                	p.addOutLink(link.destUrl);

                }
            }
        }
    }

    public String scrubUrl(String url) {
        // remove a leading pound sign
        if (url.charAt(url.length() - 1) == '#') {
            url = url.substring(0, url.length() - 1);        // make it lowercase
        }
        url = url.toLowerCase();

        // remove a port number, if it's the default number
        url = url.replace(":80/", "/");
        if (url.endsWith(":80")) {
            url = url.replace(":80", "");
        }
        // remove trailing slashes
        while (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
	
    public void pageRank(HashMap<String,Page> pages) {
    	
    }
    
}
