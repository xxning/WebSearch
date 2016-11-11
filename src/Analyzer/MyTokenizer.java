package Analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.analysis.util.CharacterUtils.CharacterBuffer;
import org.apache.lucene.util.Version;

public class MyTokenizer extends Tokenizer{  // 
	
	
	private int offset = 0, bufferIndex = -1, dataLen = 0, finalOffset = 0;
	private int current;
	private int index;
	private static final int MAX_WORD_LEN = 255;
	private static final int RE_BUFFER_SIZE = 4096;
	 
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class); // ��һ���ֵĴ�
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class); // ����ԭ���е�λ�ã���Ϊ�˸�����ʾ�õ�
	 
	//private final CharacterUtils charUtils;
	//private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
	
	private String result;
	private String sourceStr;
	
	private char[] buffer = null;
	private int flag = 0;
	//private NLPIRDelegate nlpir;
	
	
	private final void init(Version matchVersion,Reader input){  // ��ʼ�����в����ģ���ʵûɶ�ã����ûʵ������ߣ
			init();
	}
	
	private final void init(){ // ��ʼ��������������һƪ���ţ�����NLPIR���зִ�
		try{
			buffer = new char[1024];
			int cpos = 0;
			int len;
			while((len = input.read(buffer, cpos, 1024)) == 1024){
				char[] t = buffer;
				buffer = new char[buffer.length+1024];
				System.arraycopy(t, 0, buffer, 0, t.length);
				cpos += len;
			}
			cpos += len;
			
			if(cpos == -1){
				sourceStr = "";
				result = "";
				flag++;				
			}
			else{
				sourceStr = new String(buffer,0,cpos);
				//System.out.println(sourceStr);
				result = NLPIRDelegate.getDelegate().process(sourceStr);
				//System.out.println(result);
				flag ++;
			}
		}catch(IOException e){
			;
		}
	}

	public MyTokenizer(Version matchVersion, Reader input) { // yoooooo~~~~
	    super(input);
	    //charUtils = CharacterUtils.getInstance(matchVersion);
	    //init(matchVersion,input);
	}

	public MyTokenizer(Version matchVersion, AttributeFactory factory, Reader input){ // yoooooo~~~~
		super(factory, input);
		//charUtils = CharacterUtils.getInstance(matchVersion);
	    //init(matchVersion,input);
	}
	
	protected boolean isTokenChar(int c){ //����ǵ��ַ��Ĺ��ˣ����������ĸ���ߺ��֣��͹��˵�
		return Character.isLetter(c);
	}
	
	protected int normalize(int c) { // ������������е���ĸ���Сд
		return Character.toLowerCase(c);
	}
	

	@Override
	  public final boolean incrementToken() throws IOException { // �����������Ҫ���ο���CharTokenizerд��
	    clearAttributes();
	    //System.out.println("nba" + index + current + sourceStr);
	    int length = 0;
	    int start = -1; // this variable is always initialized
	    int end = -1;
	    char[] buffer = termAtt.buffer();
	    while (true) {
	    	
	    	if(flag == 0){
	    		init();
	    	}
	    	if(index >= result.length()){
	    		if(length > 0){
	    			break;
	    		}else{
	    			finalOffset = correctOffset(offset);
	    			return false;
	    		}
	    	}	    	
	    	final int c = result.charAt(index);
	    	index++;
	    	current++;
	    	
	    	if(current >= sourceStr.length()){
	    		;
	    	}	    	
	    	else if(Character.isWhitespace(c) && (sourceStr.charAt(current-1) != c))
	    		current--;
	    	
	    	if(isTokenChar(c)){
	    		if(length == 0){
	    			assert start == -1;
	    			start = offset + current - 1;
	    			end = start;
	    		}else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
	    	          buffer = termAtt.resizeBuffer(2+length); // make sure a supplementary fits in the buffer
	            }
	    		end++;
	    		buffer[length] = (char) normalize(c);
	    		length++;
	    		if(length >= MAX_WORD_LEN)
	    			break;
	    	}
	    	else if(length > 0)
	    		break;
	    }
	    
	    termAtt.setLength(length);
	    assert start != -1;
	    offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
	    return true;
	}

	
	@Override
	public final void end() {  // ����offset
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException { // �������������Ҫ�����µ�һ���ļ����зִ�֮ǰ�����������ļ���
		                                      //����Ҫ�����ʱ������flagΪ0��Ȼ������һ�ε�incrementToken��ʱ����init()���ķִʴ���
		current = 0;
		index= 0;
	    bufferIndex = 0;
	    offset = 0;
	    dataLen = 0;
	    finalOffset = 0;
	    buffer = null;
	    flag = 0;
	    //ioBuffer.reset(); // make sure to reset the IO buffer!!
	}
}
