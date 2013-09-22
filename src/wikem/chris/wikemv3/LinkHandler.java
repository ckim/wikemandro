package wikem.chris.wikemv3;

import java.net.URI;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;
//TODO maybe just use URI class which return the fragment and also 'decodes'
//also difference between Uri and URI classes
/*
 * messy handlling links within webword. also no need to have this loaded in most circumstances
 * need to parse relative links also. may need to package extra string in intent that launches new webwordactivity
 * if exists -> check if relative link (anchor) even exists -> if so, then go there
 * 
 *    #links, don't get triggered. i think webview automatically takes care of it?
 * 
 * 
 * design: probably static... no need to instantiate or keep in memory
 */
public class LinkHandler {
	public LinkHandler ( ){
 	//wth do i do here? nothing right... just a constructor?
	}
	
	//return a Uri ? but what about extras for relative links
public Intent handleLink(String link, Context c){
		
		link = parse(link);
		Intent wordIntent = new Intent(c, WebWordActivity.class);
		
		Uri uri = searchContent(link, c);
 		 
 		if(uri==null){
		 Log.e("linkhandler", "handlink has a null uri. bad link");
		//if bad link. put inan extra. will be string of at least the parsed link
			wordIntent.putExtra("badlink",link);
			return wordIntent; 
		}
		wordIntent.setData( uri );//set a uri
		return wordIntent;
}
	
	
	//return the URI but just do null if doesn't exist
private Uri searchContent(String url, Context c){
	try{
		CursorLoader cursorLoader = new CursorLoader(c, DictionaryProvider.ID_URI, null, null, new String[] {url},
    			null);
    		Cursor cursor = cursorLoader.loadInBackground();
    			
    //	Log.d("wwact" , "didn't throw error");
    	if (cursor!=null){//returns false if empty
    		try{
    			int i = cursor.getColumnIndexOrThrow(DictionaryDatabase.BASE_STRING);
    			String rowID = cursor.getString(i);
    			cursor.close();
    			Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
                        rowID);
		        Log.d("WEBWORD ACTIVIY","the full uri to go to is:" + data.toString()); 
		        	return data;
		        	 
    		}catch (Exception e){ Log.e("lh" , "wrong row?!");}
    			
    	}
    else{ //if cursor was empty
     		Log.e("lh" , "cursor is nil. link doesnt exist.");  
     		return null;
     	}    		
    		 	        			        		
  }catch(Exception e){Log.e("lh", "crap error thrown in parseabsolutelink"); return null;}
    
	return null;				 
}

	 
private String parse (String url){
		//link has the baseurl and comes out as file:///wiki/
		url=url.substring(13); 
        Log.d("linkhandler", "link is: " + url);
        
        
         url = url.replace('_', ' ').trim();
         //replace apostrophes and other escaped characters
         
         
         
         /*
          * 

//unescape these url encoded characters
- (NSString*) convertURLString: (NSString *) myString {
    NSMutableString * temp = [myString mutableCopy];
	//change underscores back to space...for whatever reason the renderer doesnt use %20 for space but _
    [temp replaceOccurrencesOfString:@"_"
                          withString:@" "
                             options:0
                               range:NSMakeRange(0, [temp length])];
	//change parentheses back... there seem to be some inconsistencies almost all unescaped
	//just incase
    [temp replaceOccurrencesOfString:@"%28"
                          withString:@"("
                             options:0
                               range:NSMakeRange(0, [temp length])];
    [temp replaceOccurrencesOfString:@"%29"
                          withString:@")"
                             options:0
                               range:NSMakeRange(0, [temp length])];
	//change quotes... (don't think used.. but just incase) "&quot;"
    [temp replaceOccurrencesOfString:@"%22"
                          withString:@"\""
                             options:0
                               range:NSMakeRange(0, [temp length])];
	//change apostrophes... @"&apos;"
    [temp replaceOccurrencesOfString:@"%27"
                          withString:@"'"
                             options:0
                               range:NSMakeRange(0, [temp length])];
	
    return [temp autorelease];
}

          */
        
		return url;
}
	
	 
	
	
	
	
	
	
	 
}
