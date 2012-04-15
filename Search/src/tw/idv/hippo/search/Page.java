package tw.idv.hippo.search;

import java.util.HashSet;

/**
 * 存分Page資訊
 * @author Hippo
 *
 */
public class Page {
	private String url = null;
	private HashSet<String> inlinks = new HashSet<String>();
	private HashSet<String> outlinks = new HashSet<String>();
	private float pagerank = 0;
	
	public Page(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HashSet<String> getInlinks() {
		return inlinks;
	}

	public void setInlinks(HashSet<String> inlinks) {
		this.inlinks = inlinks;
	}

	public HashSet<String> getOutlinks() {
		return outlinks;
	}

	public void setOutlinks(HashSet<String> outlinks) {
		this.outlinks = outlinks;
	}

	public void addInLink(String inlink) {
		inlinks.add(inlink);
	}
	
	public void addOutLink(String outlink) {
		outlinks.add(outlink);
	}

	public float getPagerank() {
		return pagerank;
	}

	public void setPagerank(float pagerank) {
		this.pagerank = pagerank;
	}
}
