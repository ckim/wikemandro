package wikem.chris.wikemv3;
 
 

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;


public class XMLImageHandler extends DefaultHandler{

	 
	// ===========================================================
	// Getter & Setter
	// ===========================================================
 	private ArrayList <String> imageList;
	
	public ArrayList<String> getParsedData(){
		return this.imageList;
	}
	 
	@Override
	public void startDocument() throws SAXException {
	//	this.myParsedExampleDataSet = new ParsedExampleDataSet();
		//Log.d(" xml handler", "start doc. initialize list"); 
		imageList = new ArrayList <String> ();
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
	 	/*
		 *  <api>
	<query>
	<allimages>
	<img name="1.png" timestamp="2011-07-06T13:56:54Z" url="http://www.wikem.org/w/images/4/4a/1.png" descriptionurl="http://www.wikem.org/wiki/File:1.png"/>

		 */
		//Log.d(" xml handler", "start tag");  

		if(localName.equals("img")){
		String attrValue = atts.getValue("url");
		//int i = Integer.parseInt(attrValue);	
		Log.d(" xml handler", attrValue); //likely null?
		this.imageList.add(attrValue); //just keep as string here and parse it out outside
	 
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