package Analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;

public final class MyAnalyzer extends Analyzer { 
	
	private static final String[] stopWords = {"and", "of", "the", "to", "is", "their", "can", "all", "��", "��", "��", "����", "����", "����","��"};
  
    private final Version matchVersion;
  
    public MyAnalyzer(Version matchVersion) {
    	this.matchVersion = matchVersion;
    }
  
    @Override
    protected TokenStreamComponents createComponents(final String fieldName,final Reader reader) { // ����д�õ�Tokenizer���Ѿ��е�filter����Analyzer
    	
    	Tokenizer tokenizer = new MyTokenizer(Version.LUCENE_44, reader);
        
        TokenFilter lowerCaseFilter = new LowerCaseFilter(Version.LUCENE_44, tokenizer);
        TokenFilter synonymFilter = new SynonymFilter(lowerCaseFilter, getSynonymMap(), true);
        TokenFilter stopFilter = new StopFilter(Version.LUCENE_44, synonymFilter, buildCharArraySetFromArry(stopWords));
        TokenFilter stemFilter = new PorterStemFilter(stopFilter);
    	
        return new TokenStreamComponents(tokenizer, stemFilter);
    }
    
    private CharArraySet buildCharArraySetFromArry(String[] array) {
        CharArraySet set = new CharArraySet(Version.LUCENE_44, array.length, true);
        for(String value : array) {
        	set.add(value);
        }
        return set;
    }
      
      // ����һ��ͬ��ʱ�
    private SynonymMap getSynonymMap() {
    	String base1 = "fast";
        String syn1 = "rapid";
        
        String base2 = "slow";
        String syn2 = "sluggish";
        
        SynonymMap.Builder sb = new SynonymMap.Builder(true);
        sb.add(new CharsRef(base1), new CharsRef(syn1), true);
        sb.add(new CharsRef(base2), new CharsRef(syn2), true);
        SynonymMap smap = null;
        try{
        	smap = sb.build();
        }catch(IOException e) {
        	e.printStackTrace();
        }
        return smap;
    }
  
  
    public static void displayToken(String str,Analyzer analyzer){  // displayToken��ӡ�ִʵĽ������ʾ������������Ե�ֵ����������������汾֮���кܴ�ı�
    	try {
    		//��һ���ַ���������Token��
    		TokenStream stream  = analyzer.tokenStream("", new StringReader(str));
    		//������Ӧ�ʻ�
    		CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
    		OffsetAttribute ota = stream.addAttribute(OffsetAttribute.class);
    		stream.reset();
    		while(stream.incrementToken()){
    			System.out.print("[" + cta + "]");
    			System.out.print("[" + ota.endOffset() + "," + ota.startOffset() + "]");
    		}
    		System.out.println();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
  
  
    public static void main(String[] args) throws Exception {
    	Analyzer aly1 = new MyAnalyzer(Version.LUCENE_44);
	  
    	String str = "";
      
    	displayToken(str, aly1);
    }
}
