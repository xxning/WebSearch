package Index;

import java.io.File;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.highlight.*;

import Analyzer.MyAnalyzer;

public final class SearchIndex {
	
	private static String IndexStorePath = "";
	private static Directory directory = null;
	private static Analyzer analyzer = null;
	////
	public String[] titles,results;
	public int nHits = 0;
	
	private static Properties props = new Properties();
	
	static	{
		try{			
			props.load(Index.class.getResourceAsStream("/db.properties"));
		}catch (IOException e){
			System.out.println("没找到配置文件");
			e.printStackTrace();   
		}
		IndexStorePath = props.getProperty("IndexPath");
		//System.out.println(IndexStorePath);  
	}
	////
	public ArrayList<Map<String,String>> search(String text){
		
		ArrayList<Map<String, String>> rs = new ArrayList<Map<String, String>>();
		DirectoryReader ireader;
		IndexSearcher isearcher;
		QueryParser parser;
		Query query;
		
        try{
            directory = FSDirectory.open(new File(IndexStorePath));
            analyzer = new MyAnalyzer(Version.LUCENE_44);
            ireader = DirectoryReader.open(directory);
            isearcher = new IndexSearcher(ireader);
    
            parser = new QueryParser(Version.LUCENE_44, "body", analyzer);
            query = parser.parse(text);
            
            ScoreDoc[] hits = isearcher.search(query, null, 10).scoreDocs;
            
            String body;
            String highLightText = "";
            
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>"); 
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));                        
                  
            //System.out.println(hits.length);
            
            //nHits = hits.length;
			//if(hits.length>1){
			//	titles = new String[hits.length];
			//	results = new String[hits.length];
			//}
            
            //ArrayList<Map<String, String>> rs = new ArrayList<Map<String, String>>();
        
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = isearcher.doc(hits[i].doc);
                
                Map<String,String> map = new HashMap<String,String>();
                map.put("url", hitDoc.get("url"));
                map.put("title", hitDoc.get("title"));
                map.put("subjectid", hitDoc.get("subjectid"));
                map.put("publishid", hitDoc.get("publishid"));
                map.put("description", hitDoc.get("description"));
                map.put("keywords", hitDoc.get("keywords"));
                //map.put("body", hitDoc.get("body"));
                
                body = hitDoc.get("body");
                if(body.length() > 200)
                	body = body.substring(0, 200) + "...";
                map.put("body", body);                
                
                
                //body = hitDoc.get("body");
                //body = body.replaceAll("■", "");
                                
                //System.out.println(body);
                //System.out.println(body.length());
                //System.out.println(body.lastIndexOf("祎"));
                
                highlighter.setTextFragmenter(new SimpleFragmenter(body.length()));      
                               
                if (text != null) {                  
                    TokenStream tokenStream = analyzer.tokenStream("body",new StringReader(body));   
                    highLightText = highlighter.getBestFragment(tokenStream, body);                 
                }
                map.put("body", highLightText);
                
                
                
                System.out.println("____________________________");
                System.out.println("url : " + hitDoc.get("url"));
                System.out.println("title : " + hitDoc.get("title"));
                System.out.println("subjectid : " + hitDoc.get("subjectid"));
                System.out.println("publishid : " + hitDoc.get("publishid"));
                System.out.println("description : " + hitDoc.get("description"));
                System.out.println("keywords : " + hitDoc.get("keywords"));
                System.out.println("body : " + hitDoc.get("body"));
                System.out.println("____________________________");
                
                //titles[i] = hitDoc.get("title");
				//results[i] = hitDoc.get("body");
                
                rs.add(map);
            }
            ireader.close();
            directory.close();
        }catch(Exception e){
            e.printStackTrace();
        }        
        return rs;
    }
	
	public static void setIndexPath(String path){
		IndexStorePath = path;
	}
	
	public static String getIndexPath(){
		return IndexStorePath;
	}
	
	public static void main(String[] args) throws IOException{
		SearchIndex search = new SearchIndex();
		search.search("朝鲜");
	}

}
