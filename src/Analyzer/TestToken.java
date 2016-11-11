package Analyzer;

import java.io.IOException;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;


public class TestToken {

    /**
     *
     * Description:      �鿴�ִ���Ϣ
     * @param str        ���ִʵ��ַ���
     * @param analyzer    �ִ���
     *
     */
    public static void displayToken(String str,Analyzer analyzer){
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Analyzer aly1 = new StandardAnalyzer(Version.LUCENE_44);
        Analyzer aly2 = new StopAnalyzer(Version.LUCENE_44);
        Analyzer aly3 = new SimpleAnalyzer(Version.LUCENE_44);
        Analyzer aly4 = new MyAnalyzer(Version.LUCENE_44);
        
        String str = "";
        
        System.out.println(str); 
        System.out.print("Stad:	"); 
        TestToken.displayToken(str, aly1);
        System.out.print("Stop:	");
        TestToken.displayToken(str, aly2);
        System.out.print("Simple:	");
        TestToken.displayToken(str, aly3);
        System.out.print("cyb:	");
        TestToken.displayToken(str, aly4);
    }
}