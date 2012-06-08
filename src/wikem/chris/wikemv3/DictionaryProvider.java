package wikem.chris.wikemv3;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Provides access to the dictionary database.
 */
public class DictionaryProvider extends ContentProvider {
    String TAG = "DictionaryProvider";

    public static String AUTHORITY = "wikem.chris.wikemv3.DictionaryProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/dictionary");
    public static final Uri BCONTENT_URI = Uri.parse("content://" + AUTHORITY + "/bdictionary");  
    public static final Uri CATEGORY_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/category");
    public static final Uri FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/favorites");
    public static final Uri LAST_UPDATE_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/last_update");
    public static final Uri ALL_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/all");
    public static final Uri BACKUP_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/backup"); 
    public static final Uri TITLE_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/title");
    public static final Uri ID_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/rowid"); //get EXACT match given a word
    public static final Uri ID_LIKE_URI = Uri.parse("content://" + AUTHORITY + "/dictionary/like");
    
    
     // MIME types used for searching words or looking up a single definition
    public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE ;
                                               // +  "/vnd.example.android.searchabledict";
    											
    public static final String DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE ;
                                                    //+   "/vnd.example.android.searchabledict";

    private DictionaryDatabase mDictionary;

    // UriMatcher stuff
    private static final int SEARCH_WORDS = 0;
    private static final int GET_WORD = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final int GET_ALL_CATEGORY =4;
    private static final int GET_TITLE =5;
    private static final int GET_FAVORITES =6;
    private static final int GET_EVERYTHING=7;
    private static final int GET_BACKUP=8;
    private static final int BSEARCH_WORDS=9;//get the definition from the backup
    private static final int GET_LAST_UPDATE = 10;
    private static final int GET_ID = 11;
    private static final int GET_ID_LIKE = 12;
    
    //more new uri matcher stuff so i can implement contentresolver ...
    
    

    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        //to get uri of the actual content??
        
        // to get definitions...
        matcher.addURI(AUTHORITY, "dictionary/all", GET_EVERYTHING);
        matcher.addURI(AUTHORITY, "dictionary/backup", GET_BACKUP);

        matcher.addURI(AUTHORITY, "dictionary/category", GET_ALL_CATEGORY);
        matcher.addURI(AUTHORITY, "dictionary/favorites", GET_FAVORITES);
        matcher.addURI(AUTHORITY, "dictionary/last_update", GET_LAST_UPDATE);
        matcher.addURI(AUTHORITY, "dictionary", SEARCH_WORDS);
        matcher.addURI(AUTHORITY, "bdictionary", BSEARCH_WORDS);
        matcher.addURI(AUTHORITY, "dictionary/#", GET_WORD);
        matcher.addURI(AUTHORITY, "dictionary/title", GET_TITLE);
        matcher.addURI(AUTHORITY, "dictionary/rowid", GET_ID);
        matcher.addURI(AUTHORITY, "dictionary/like", GET_ID_LIKE);

        
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
    	//getContext() is part of contentProvider: Retrieves the Context this provider is running in
                mDictionary = new DictionaryDatabase(getContext());
        return true;
    }


    /**
     * Handles all the dictionary searches and suggestion queries from the Search Manager.
     * When requesting a specific word, the uri alone is required.
     * When searching all of the dictionary for matches, the selectionArgs argument must carry
     * the search query as the first element.
     * All other arguments are ignored.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case SEARCH_WORDS:
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case BSEARCH_WORDS: //search the backup
                /*if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }*/
                return bsearch(selectionArgs[0]);//dunno if this is correct but whatever...
            case GET_WORD:
                return getWord(uri);
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);
            case GET_ALL_CATEGORY:
            	return getAllCategory(selectionArgs[0]);
            case GET_TITLE:
            	return searchTitle(selectionArgs[0]);
            case GET_FAVORITES:
            	return getFavorites();
            case GET_LAST_UPDATE:
            	return getLastUpdate(selectionArgs[0]);            	
            case GET_EVERYTHING:
            	return getEverything();
            case GET_BACKUP:
            	return getBackup();
            case GET_ID:
            	return getID(selectionArgs[0]);
            case GET_ID_LIKE:
            	return getIDLike(selectionArgs[0]);
            	
            	
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }
    private Cursor getIDLike(String query) {
   	 
        String[] columns = new String[] {
            BaseColumns._ID};

        return mDictionary.getWordLike(query, columns); //only search the titles
		
	}
    private Cursor getID(String query) {
	 
        String[] columns = new String[] {
            BaseColumns._ID};

        return mDictionary.getExactWordMatch(query, columns); //only search the titles
		
	}

	public  Cursor getEverything(){ //chnaged to public .. 6/2011
    	String[] columns = new String[] {
  	          BaseColumns._ID,
  	          DictionaryDatabase.KEY_WORD, 
  	          DictionaryDatabase.WIKEM_CATEGORY};
    			Log.d("DICT PROVIDER", "get everything");
  	
		return mDictionary.getEverything( columns);  
    }
    private Cursor getBackup(){
    	String[] columns = new String[] {
  	          BaseColumns._ID,
  	          DictionaryDatabase.KEY_WORD, 
  	          DictionaryDatabase.WIKEM_CATEGORY,
  	    //      DictionaryDatabase.KEY_DEFINITION}; //so cursor has the whole shebang to point to
  	          //only 1mb for cursur allowed. too big this way.
  	          
  	          DictionaryDatabase.WIKEM_URI}; //why is it using this?
    			Log.d("DICT PROVIDER", "getting backup");
  	
		return mDictionary.getBackup( columns);  
    }
    private Cursor getFavorites() {
    	 
    	String[] columns = new String[] {
    	          BaseColumns._ID,
    	          DictionaryDatabase.KEY_WORD, 
    	          DictionaryDatabase.WIKEM_CATEGORY};
    	
		return mDictionary.getFavorites( columns);  
	}
    private Cursor getLastUpdate(String query) {
   	 /*
   	  * only used to get   keywords with their last updated time
   	  */
    	String[] columns = new String[] {
    	          BaseColumns._ID,
    	            	          DictionaryDatabase.LAST_UPDATE};
    	
		return mDictionary.getLastUpdate(query, columns);  
	}
	private Cursor getAllCategory(String query){
    	query = query.toLowerCase();
    	String[] columns = new String[] {
    	          BaseColumns._ID,
    	          DictionaryDatabase.KEY_WORD, 
    	          DictionaryDatabase.WIKEM_CATEGORY};
    	return mDictionary.getAllCategory(query, columns);
    }
    private Cursor getSuggestions(String query) {
      query = query.toLowerCase();
      String[] columns = new String[] {
          BaseColumns._ID,
          DictionaryDatabase.KEY_WORD, 
          DictionaryDatabase.WIKEM_CATEGORY,
       //   DictionaryDatabase.KEY_DEFINITION,
     //   SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                      //  (only if you want to refresh shortcuts) 
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
         //got get access to saerchmanager use context.getSystemService(Context.SEARCH_SERVICE). 

          };

      return mDictionary.getWordMatches(query, columns);
    }

    private Cursor search(String query) {
      query = query.toLowerCase();
      String[] columns = new String[] {
          BaseColumns._ID,
          DictionaryDatabase.KEY_WORD,
          DictionaryDatabase.WIKEM_CATEGORY};

		return mDictionary.getWordMatchesFromTable(query, columns); //simple correction to change search query to fulltext
    }

    private Cursor bsearch(String title) { //for backup db.  ?try get definition from keyword
      //query = query.toLowerCase();
      String[] columns = new String[] {
          BaseColumns._ID,
        //  DictionaryDatabase.KEY_WORD,
          DictionaryDatabase.KEY_DEFINITION};

      return mDictionary.getBackupWord(title, columns);  
    }
    private Cursor searchTitle(String query) {
        query = query.toLowerCase();
        String[] columns = new String[] {
            BaseColumns._ID,
            DictionaryDatabase.KEY_WORD,
            DictionaryDatabase.WIKEM_CATEGORY};

        return mDictionary.getWordMatches(query, columns); //only search the titles
      }

    //this is whats called when the rowid is sent from searchmanger to webwordactivity. notice i added another column to get.
    private Cursor getWord(Uri uri) {
      String rowId = uri.getLastPathSegment();
      String[] columns = new String[] {
          DictionaryDatabase.KEY_WORD,
          DictionaryDatabase.KEY_DEFINITION,
          //add another column here?
          DictionaryDatabase.LAST_UPDATE
          	};

      return mDictionary.getWord(rowId, columns);
    }

    private Cursor refreshShortcut(Uri uri) {
      /* This won't be called with the current implementation, but if we include
       * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
       * could expect to receive refresh queries when a shortcutted suggestion is displayed in
       * Quick Search Box. In which case, this method will query the table for the specific
       * word, using the given item Uri and provide all the columns originally provided with the
       * suggestion query.
       */
      String rowId = uri.getLastPathSegment();
      String[] columns = new String[] {
          BaseColumns._ID,
          DictionaryDatabase.KEY_WORD,
          DictionaryDatabase.KEY_DEFINITION,
          SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

      return mDictionary.getWord(rowId, columns);
    }

    /**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_WORDS:
                return WORDS_MIME_TYPE;
            case GET_WORD:
                return DEFINITION_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...

   /* @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }*/
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
    	   if (sURIMatcher.match(uri) != SEARCH_WORDS) //i think the right uri for the content in general 
    	   { throw new IllegalArgumentException("Unknown URI " + uri); }

           @SuppressWarnings("unused")
		ContentValues values;
           if (initialValues != null) {
               values = new ContentValues(initialValues);
           } else {
               values = new ContentValues(); //?why'd i do this
           }
           
           long rowId = mDictionary.insert(initialValues);
           if (rowId > 0) {
               Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
               getContext().getContentResolver().notifyChange(noteUri, null);
               return noteUri;
           }
           else{
        	   return null;
           }
         
     }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       
         
        int count;
        switch (sURIMatcher.match(uri)) {
             case SEARCH_WORDS: //change row = selection (eg. KEY_WORD)  WHERE selectionargs[0] (eg. ACLS) 
            	 count = mDictionary.updateContent(values, selection ,selectionArgs[0]  );
                 break; 
            default: 
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
 
        getContext().getContentResolver().notifyChange(uri, null); 
        return count;
 
    }

       
 
}
