package tw.idv.hippo.search;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.galagosearch.core.index.*;

public class Search1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start...");
		
		try {
			IndexReader ir = new IndexReader("D:\\hippo\\git\\Search\\galagosearch\\wiki-small.index");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
