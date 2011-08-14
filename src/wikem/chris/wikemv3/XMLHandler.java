package wikem.chris.wikemv3;
 
 

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class XMLHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================
	
	/*private boolean in_outertag = false;
	private boolean in_innertag = false;
	private boolean in_mytag = false;
	*/
//	private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	String parsedData;
	public String getParsedData(){
		return this.parsedData;
	}
	//public ParsedExampleDataSet getParsedData() {
	//	return this.myParsedExampleDataSet;
	//}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
	//	this.myParsedExampleDataSet = new ParsedExampleDataSet();
		//Log.d(" xml handler", "start doc"); 
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		//Log.d(" xml handler", "end doc"); 
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
	/*	if (localName.equals("outertag")) {
			this.in_outertag = true;
		}else if (localName.equals("innertag")) {
			this.in_innertag = true;
		}else if (localName.equals("mytag")) {
			this.in_mytag = true;
		}else if (localName.equals("tagwithnumber")) {
			// Extract an Attribute
			String attrValue = atts.getValue("thenumber");
			int i = Integer.parseInt(attrValue);
		//	myParsedExampleDataSet.setExtractedInt(i);
		}
		
*/		
		
		/*
		 * 
		 * eg.
		 * <revisions>
          <rev timestamp="2011-05-12T20:32:24Z" />
        </revisions>
        
           "yyyy-MM-dd'T'HH:mm:ss.SSSZ",

		 */
		//Log.d(" xml handler", "start tag");  

		if(localName.equals("rev")){
		String attrValue = atts.getValue("timestamp");
		//int i = Integer.parseInt(attrValue);	
		Log.d(" xml handler", attrValue); //likely null?
		this.parsedData =attrValue; //just keep as string here and parse it out outside
	 
		}
	
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
	 
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		//if(this.in_mytag){
    		//myParsedExampleDataSet.setExtractedString(new String(ch, start, length));
   // 	}
    }
}