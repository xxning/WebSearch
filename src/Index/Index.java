package Index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import Analyzer.MyAnalyzer;

public class Index {
	
	private static String txtStorePath = ""; //数据文件的路径
	private static String afterParsePath = "";//数据文件处理完之后的存储路径
	private static String IndexStorePath = ""; //索引的位置
	
	private static String txtStoreDir = "";// 数据文件们的存储路径，处理完它们之后会在它们最后加_after
	
	private static Analyzer analyzer;
	private static Directory directory;
	private static IndexWriter indexWriter = null;	
	
	private static Properties props = new Properties();
	
	static	{
		try{			
			props.load(Index.class.getResourceAsStream("/db.properties"));
		}catch (IOException e){
			System.out.println("没找到配置文件");
			e.printStackTrace();   
		}
		txtStorePath = props.getProperty("txtStorePath");
		afterParsePath = props.getProperty("afterParsePath");
		IndexStorePath = props.getProperty("IndexPath");
		txtStoreDir = props.getProperty("txtStoreDir");
		//System.out.println(IndexStorePath);  
	}
	
	
	public static boolean createIndex() throws IOException{  // 建立索引
		
		analyzer = new MyAnalyzer(Version.LUCENE_44);
		directory = FSDirectory.open(new File(IndexStorePath));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		indexWriter = new IndexWriter(directory, config);
		
		return true;
	}
	
	public static List<File> getFileList(String dirPath) {  // 获取目录下的文件名列表
        File[] files = new File(dirPath).listFiles();
        List<File> fileList = new ArrayList<File>();
        for (File file : files) {
        	if (file.getName().lastIndexOf(".txt") > 0) {
                fileList.add(file);
            }
        }
        return fileList;
    }
	
	public static boolean writeIndexFromDirectory(String dir) throws IOException{  //从目录下将所有文件处理
		
		List<File> fileList = getFileList(dir);
		for (File file : fileList) {
			
			String type = file.getName().substring(file.getName().lastIndexOf(".")+1);
			if("txt".equalsIgnoreCase(type)){
				System.out.println("一个文件开始啦~~~");
				//System.out.println(file.getName());
				writeIndexFromFile(file);
			}
		}
		return true;
		
	}
	
	public static StringBuffer replaceAll(StringBuffer sb, String oldStr, String newStr) {
        int i = sb.indexOf(oldStr);
        int oldLen = oldStr.length();
        int newLen = newStr.length();
        while (i > -1) {
            sb.delete(i, i + oldLen);
            sb.insert(i, newStr);
            i = sb.indexOf(oldStr, i + newLen);
        }
        return sb;
    }
	
	public static boolean writeIndexFromFile(File file) throws IOException{ // 从文件写索引
		
		if(indexWriter == null){
			System.out.println("请先建立索引");
		}
		else{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));

			//System.out.println(file.getPath().substring(0, file.getPath().lastIndexOf(".")) + "_after.txt");
			
			File filew = new File(file.getPath().substring(0, file.getPath().lastIndexOf(".")) + "_after.txt");
			FileWriter fw = new FileWriter(filew.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			final Pattern pt_title = Pattern.compile("(<title>)(.*)(</title>)");
			final Pattern pt_url = Pattern.compile("(<url>)(.*)(</url>)");
			final Pattern pt_strong = Pattern.compile("(<strong>)(.*)(</strong>)");
			
			final Pattern pt_HTML = Pattern.compile("<([^>]*)>");

			String content;
			
			while((content = br.readLine()) != null){
				if(content.equals("<doc>")){
					
					org.apache.lucene.document.Document documentLucene = new org.apache.lucene.document.Document();
					
					StringBuffer body = new StringBuffer();
					
					while((content = br.readLine()) != null){

						if(content.length() == 0);
						else if(content.charAt(0) != '<'){
							body.append(content);
						}					
						else if(content.equals("</doc>")){
							//System.out.println("一篇搞完了");
							break;
						}
						else if(content.charAt(1) == 'm'){
							Document doc = Jsoup.parse(content);
							Elements elm = doc.select("meta");
							if(elm.size() != 0){
								if(elm.attr("name").equals("keywords") == true){									
									bw.write("keywords : " + elm.attr("content") + '\n');
									documentLucene.add(new StringField("keywords", elm.attr("content"), Store.YES));									
								}
								else if(elm.attr("name").equals("description")){
									bw.write("description : " + elm.attr("content") + '\n');
									documentLucene.add(new StringField("description", elm.attr("content"), Store.YES));
								}
								else if(elm.attr("name").equals("publishid")){
									bw.write("publishid : " + elm.attr("content") + '\n');
									documentLucene.add(new StringField("publishid", elm.attr("content"), Store.YES));
								}
								else if(elm.attr("name").equals("subjectid")){
									bw.write("subjectid : " + elm.attr("content") + '\n');
									documentLucene.add(new StringField("subjectid", elm.attr("content"), Store.YES));
								}
							}						
						}
						else{
							Matcher mc = pt_title.matcher(content);
							if(mc.find()){
								String strTitle = mc.group(2).trim();
								//System.out.println("Title : " + strTitle);
								bw.write("Title : " + strTitle + '\n');
								documentLucene.add(new StringField("title", strTitle, Store.YES));
							}
							mc = pt_url.matcher(content);
							if(mc.find()){
								String strUrl = mc.group(2).trim();
								//System.out.println("Url : " + strUrl);
								bw.write("Url : " + strUrl + '\n');
								documentLucene.add(new StringField("url", strUrl, Store.YES));
							}
							mc = pt_strong.matcher(content);
							if(mc.find()){
								String strStrong = mc.group(2).trim();
								body.append(strStrong);
							}						
						}					
					}
					
					//System.out.println("body : " + body);
					body = replaceAll(body, "<a>", "");
					body = replaceAll(body, "</a>", "");
					body = replaceAll(body, "<span>", "");
					body = replaceAll(body, "</span>", "");
					body = replaceAll(body, "<em>", "");
					body = replaceAll(body, "</em>", "");
					body = replaceAll(body, "<iframe>", "");
					body = replaceAll(body, "</iframe>", "");
					body = replaceAll(body, "&nbsp;", "");
					
					Matcher mc = pt_HTML.matcher(body);	
					StringBuffer sb = new StringBuffer();
					boolean result = mc.find();
					//System.out.println(result);
					while(result){
						mc.appendReplacement(sb, "");
						result = mc.find();
					}
					mc.appendTail(sb);
					
					body = sb;
					
					bw.write("body : " + body + '\n');
					documentLucene.add(new TextField("body", body.toString(), Store.YES));
					//System.out.println("一篇的完结是另一篇的开始");
					bw.write("一篇的完结是另一篇的开始" + '\n');				
					indexWriter.addDocument(documentLucene);
					indexWriter.commit();
				}
				
			}
			br.close();
			bw.close();
		}		
		return true;
		
	}
	
	public static boolean writeIndexFromFile(String path) throws IOException{  //从文件写索引，参数为String
		return writeIndexFromFile(new File(path));		
	}
	
	public static boolean writeIndexFromFile() throws IOException{ // 这个其实没啥用，测试用的
		return writeIndexFromFile(new File(txtStorePath));		
	}
	
	public static void setIndexPath(String path){  //设置索引路径
		IndexStorePath = path;
	}
	
	public static void setTxtStorePath(String path){ // 设置读入文件的路径
		txtStorePath = path;
	}
	
	public static void setAfterParsePath(String path){ // 设置处理完之后文件的路径
		afterParsePath = path;
	}
	
	public static String getIndexPath(){ // 获取索引路径
		return IndexStorePath;
	}
	
	public static String getTxtStorePath(){ // 获取读入文件的路径
		return txtStorePath;
	}
	
	public static String getAfterParsePath(){ // 获取处理完之后文件的路径
		return afterParsePath;
	}
	
	
	public static void main(String[] args) throws IOException{ // 测试用，现在是从一个目录下面将所有的文件处理建立索引
		createIndex();
		//writeIndexFromFile();
		//writeIndexFromFile("C:\\Users\\cybsb\\Desktop\\2012.q1.txt\\2012.q1.txt");
		writeIndexFromDirectory(txtStoreDir);
		System.out.println("完了");
	}
	

}
