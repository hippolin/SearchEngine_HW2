package tw.idv.hippo.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.DocumentIndexReader;
import org.galagosearch.core.parse.Tag;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.types.ExtractedLink;

public class Search1 {

	private boolean acceptLocalLinks = true;
	private boolean acceptNoFollowLinks = false;

	private TagTokenizer tagtokenizer = new TagTokenizer();

	private HashMap<String, Page> pages = new HashMap<String, Page>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start...");

		Search1 s1 = new Search1();
		s1.run();

	}

	public void run() {
		int d = 0;
		try {
			DocumentIndexReader reader = new DocumentIndexReader(
					"../galagosearch/wiki-small.corpus");
			DocumentIndexReader.Iterator iterator = reader.getIterator();
			while (!iterator.isDone()) {
				Document document = iterator.getDocument();

				// System.out.println("#IDENTIFIER: " + iterator.getKey());
				// Document document = iterator.getDocument();
				// System.out.println("#METADATA");
				// for (Entry<String, String> entry :
				// document.metadata.entrySet()) {
				// System.out.println(entry.getKey() + "," + entry.getValue());
				// }

				String u = document.metadata.get("url");
				u = u.replaceAll("(.*/)", "");
				String u2 = "http://wiki-corpus/pl/articles/" + u.charAt(0)
						+ "/" + u.charAt(1) + "/" + u.charAt(2) + "/" + u;

				document.metadata.put("url", u2);

				// 切tag
				tagtokenizer.process(document);

				// 處理link
				processLink(document);

				// System.out.println("#TEXT");
				// System.out.println(document.text);
				iterator.nextDocument();
				d++;
			}

			proclink();
			// 依inlink數排序
			LinkedList<Page> pagelist = new LinkedList<Page>(pages.values());

			Collections.sort(pagelist, new Comparator<Page>() {

				@Override
				public int compare(Page o1, Page o2) {
					// TODO Auto-generated method stub
					return o2.getInlinks().size() - o1.getInlinks().size();
				}

			});

			// 印出前10名
			int i = 1;
			System.out.println("Top 10 Most Inlink");
			System.out.println("no:inlinkCount:url");

			for (Page p : pagelist) {
				System.out.println(i + ":" + p.getInlinks().size() + ":"
						+ p.getUrl());
				if (++i > 10)
					break;
			}

			
			pageRank(pages);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processLink(Document document) throws IOException {
		String sourceUrl = document.metadata.get("url").toLowerCase();

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

				boolean linkIsLocal = destUrlObject.getHost().equals(
						base.getHost());

				// if we're filtering out local links, there's no need to
				// continue
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

				if (t.attributes.containsKey("rel")
						&& t.attributes.get("rel").equals("nofollow")) {
					link.noFollow = true;
				} else {
					link.noFollow = false;
				}

				boolean acceptable = (acceptNoFollowLinks || link.noFollow == false)
						&& (acceptLocalLinks || linkIsLocal == false);

				if (acceptable) {

					Page p = null;

					if (linkIsLocal == true) {

						// if (pages.containsKey(link.destUrl)) {
						// p = pages.get(link.destUrl);
						// } else {
						// System.out.println(link.destUrl);
						// //System.out.println(sourceUrl);
						// //if(link.destUrl.indexOf("..") > 0) System.exit(0);
						// p = new Page(link.destUrl);
						// pages.put(link.destUrl, p);
						// }
						// p.addInLink(link.srcUrl);

						if (pages.containsKey(link.srcUrl)) {
							p = pages.get(link.srcUrl);
						} else {
							p = new Page(link.srcUrl);
							pages.put(link.srcUrl, p);
						}

						p.addOutLink(link.destUrl);
					}

				}
			}
		}
	}

	public void proclink() {
		for (Entry<String, Page> p : pages.entrySet()) {
			for (String olink : p.getValue().getOutlinks()) {
				if (pages.containsKey(olink)) {
					pages.get(olink).addInLink(p.getKey());
				} else {
					// pages.get(p.getKey()).getOutlinks().remove(olink);
				}
			}
		}
	}

	public String scrubUrl(String url) {
		// remove a leading pound sign
		if (url.charAt(url.length() - 1) == '#') {
			url = url.substring(0, url.length() - 1); // make it lowercase
		}

		url = url.replaceAll("#.+", "");

		// try {
		// url = URLDecoder.decode(url,"UTF-8");
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

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

	public void pageRank(HashMap<String, Page> pages) {
		float lambda = (float) 0.15;

		int pagelen = pages.size();

		HashMap<String, Float> mI = new HashMap<String, Float>(7000);
		HashMap<String, Float> mR = new HashMap<String, Float>(7000);

		float ii = (float) 1 / pagelen;

		for (String page : pages.keySet()) {
			mI.put(page, ii);
			mR.put(page, null);
		}
		//System.out.println(pagelen);
		//System.out.println(mR.size());

		float lambdaP = lambda / pagelen;

		// for(Entry<String, Float> e : mI.entrySet()) {
		// System.out.println(e.getValue());
		// }
		int cc = 0;
		while (cc++ < 1000) {
			for (String e : mR.keySet()) {
				mR.put(e, lambdaP);
			}

			int qlen;
			float ip, ipq, ipp;

			for (Entry<String, Page> p : pages.entrySet()) {
				HashSet<String> mQ = p.getValue().getOutlinks();
				qlen = mQ.size();
				ip = mI.get(p.getKey());

				if (qlen > 0) {
					ipq = (1 - lambda) * ip / qlen;
					for (String q : mQ) {
						if (mR.containsKey(q)) {
							mR.put(q, mR.get(q) + ipq);
						}
					}
				} else {
					ipp = (1 - lambda) * ip / pagelen;

					for (Entry<String, Float> q : mR.entrySet()) {
						mR.put(q.getKey(), q.getValue() + ipp);
					}
				}
				//System.out.println(qlen);
			}

			mI = (HashMap<String, Float>) mR.clone();
		}

		for (Entry<String, Float> p : mR.entrySet()) {
			pages.get(p.getKey()).setPagerank(p.getValue());
		}
		
		LinkedList<Page> pagelist = new LinkedList<Page>(pages.values());

		Collections.sort(pagelist, new Comparator<Page>() {

			@Override
			public int compare(Page o1, Page o2) {
				// TODO Auto-generated method stub
				return o1.getPagerank() > o2.getPagerank() ? 0 : 1;
			}

		});
		
		System.out.println("Top 20 PageRank");
		int i = 0;
		for (Page p : pagelist) {
			System.out.println(i + ":" + p.getPagerank() + ":"
					+ p.getUrl());
			if (++i > 20)
				break;
		}

	}

}
