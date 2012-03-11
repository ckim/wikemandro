package wikem.chris.wikemv3;

import java.sql.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import wikem.chris.R;
import wikem.chris.wikemv3.CategoryActivity;
import wikem.chris.wikemv3.DictionaryDatabase;
import wikem.chris.wikemv3.DictionaryProvider;
import wikem.chris.wikemv3.DisclaimerActivity;
import wikem.chris.wikemv3.DownloaderTest;
import wikem.chris.wikemv3.FavoriteActivity;
import wikem.chris.wikemv3.ViewAllActivity;
import wikem.chris.wikemv3.WebWordActivity;

/**
 * The main activity for the dictionary.
 * Displays search results triggered by the search dialog and handles
 * actions from search suggestions.
 */
/*
 * 
<!--  Shared prefs:  -->
<!-- int max -> number of entries pulled from xml -->
<!--  boolean db_loaded -> after dl set to false. if not completed dictionaydatabsae upgrade stays false (otw true and no issue).either force reload or just toast and warn   -->
<!--  boolean sucessfully_updated  -> basically always false, only true to throw toast that says 'sucessfully updated'-->
<!--  long = epoch -> the time stamp of update pulled from xml -->
 <!-- boolean first_opened  -> after install true; henceforth after first successful dbload ,  false thereafter-->
 <!-- force_rebuild -> thrown if any sql db query (or upgrade) throws error, such as corrupted db
 
 <!-- String message = some sort of custom message... like 'plz goto market and upgrade your app'..-> 
 <!-- String dl-path -> the path that the downloader ended up using (eg will change if sd card pulled out). SET in downloaderActivity ->
 <!-- String default-path -> default path ... set up on first run with getFilesDir. -->
 
 <!-- boolean displayNotePref -> hide or display notes -->
 *
 */

 public class SearchableDictionary extends Activity {

    private TextView mTextView;
    private ListView mListView;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static String query;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        initializeApp(); 
   	 	checkIfNeedDBRebuild(); //does nothing now. was buggy.. 
   	 	toastIfJustSuccessfullyUdated();
        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        
 
        String []categories=  getResources().getStringArray(R.array.category_array);
        mListView.setAdapter(  new ArrayAdapter<String>(this, R.layout.list_item, categories));        
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {                  
                   String cat = (String) ((TextView) view).getText();
                   Intent catIntent = new Intent(getApplicationContext(), CategoryActivity.class);
                   catIntent.putExtra("category", cat);
                   startActivity(catIntent);
                }
              });
        
/*part of Activity class
 * Intent	 getIntent() -> Return the intent that started this activity.
 */    Intent intent = getIntent();
       setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); //call in onCreate method to enable type to search 
      
        /* ACTION VIEW:  * Display the data to the user.   * This is the most common action performed on data -- 
        * it is the generic action you can use on a piece of data to         * get the most reasonable thing to occur. 
        *    * */
        
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search SUGGESTION; launches activity to show word
            Intent wordIntent = new Intent(this, WebWordActivity.class);
            wordIntent.setData(intent.getData());
            //here too... start for result
           // startActivityForResult(wordIntent, 26); //again...arbitrary int 
           // finish(); //dunno why this is called here...
            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
             query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    
    
    private void initializeApp() {
		// will do some initialization maintenance stuff.. but only if there is an SD card.
    	//all the real work done on its own, as contentprovider is placed in the manifest file
    	
    	 SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         boolean firstOpened = settings.getBoolean("first_opened", true); //added v2. default if first_opened is true...thus assume upgraded version
         if (firstOpened){
        	editor.putBoolean("displayNotePref", false);
 	 		editor.putString("default-path", getFilesDir().toString());
 	 		editor.putLong("epoch",1288369494 ); //128 is just some random old epoch such that will download newer
 	 		editor.commit();
 	 		
 	 		 try { //pause for ~second and then launch the downloader
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
	        	 alertFirstOpened();
         }
         else if (settings.getString("message", null) != null){
        	 //if message exists, passed from the info.xml file relay that message to user
        	 String s = settings.getString("message", "thank you for using WikEM"); //stupid default
        	 if(s.trim().length()>2){ //dunno why used a char length of 2...just make sure not empty
        		 alertCustomMessage(s);
        	 }
         }
         else if(settings.getBoolean("force_rebuild", false)) {
        	 //ie. force a db rebuild due to error catch in dictdatabase
        	 alertForceDB(); //only option from there is go to donloader test       
         }         
         else { //ie. every other time wikem opens...just not the first installation
        	 //TODO put the async image dl service here no?
        	 
        	 //do nothing! everything else is done automatically no?
        	 }    
	}
    
	private void alertCustomMessage(String message) {
		// implementation of this is to be determined... 
		// rationale is to alert users to upgrade their app
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//String string = getString(R.string.info);

    	builder.setMessage(message)
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
     	        	   /*
     	        	    * i guess do nothing!! other than display the alert
     	        	    */
    	        	   dialog.cancel();
    	           }
    	       });    
    	 
    	AlertDialog alert = builder.create();      
    	alert.show();		
	}

	private void alertFirstOpened() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//String string = getString(R.string.info);

    	builder.setMessage( R.string.first_instructions)
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
     	        	  dialog.cancel();
     	        	  goToDisclaimer();
    	           }
    	       });	
    	AlertDialog alert = builder.create();      
    	alert.show();
	}

	protected void goToDisclaimer() {
		startActivity(new Intent(this, DisclaimerActivity.class));
		finish();		
	}

	private void alertForceDB() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//String string = getString(R.string.info);

    	builder.setMessage(" This app encountered an issue and we recommend rebuilding the database.")
    	       .setCancelable(true)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
     	        	   
    	        	   dialog.cancel();
    	        	 gotoDownloaderTest();
    	           }
    	       });    
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
	        	   dialog.cancel();
				  }
				});
    	AlertDialog alert = builder.create();      
    	alert.show();		
	}

	private void gotoDownloaderTest(){ //method here, just bc can't do this in an alert. context not same.
		startActivity(new Intent(this, DownloaderTest.class));
   	   finish(); //exit app
	}

	/*private void alertNoSD() {
		//   No SD card. alert.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//String string = getString(R.string.info);

    	builder.setMessage(" No writeable SD-Card or External Storage detected! Does not meet requirements for use. Please exit and reload SD-Card or other storage device. Thank You.")
    	       .setCancelable(false)
    	       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               ///// dunno what put here MyActivity.this.finish();
    	        	   
    	        	   dialog.cancel();
    	        	   finish(); //exit app
    	           }
    	       });    	       
    	AlertDialog alert = builder.create();      
    	alert.show();
		
	}*/

	private void toastIfJustSuccessfullyUdated() {		     	
    	SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
    	if (settings.getBoolean("successfully_updated", false)){ //default false
     		Toast toast = Toast.makeText(getApplicationContext(), "Successfully Updated WikEM", Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
    	}
    	SharedPreferences.Editor editor = settings.edit();
 			editor.putBoolean("successfully_updated", false);
			editor.commit();
			// always set back to false so only true when just succeeded
	}

	private void checkIfNeedDBRebuild() {
		/*
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0); 
		//db_loaded set and rest indownlaoder activity  
    	if (!settings.getBoolean("db_loaded", true)){ //ie. if not dbloaded correctly...just set default to true  so doesn't trigger on first run
    	 
    		Toast toast = Toast.makeText(getApplicationContext(), "Please press menu and re-update. Previous attempt interrupted", Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
			}*/
		}
	/**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
     */
    
    private void showResults(String query) {

        Cursor cursor = managedQuery(DictionaryProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
        //startManagingCursor(cursor);

        if (cursor == null) {
            // There are no results
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            // Display the number of results
            int count = cursor.getCount();
          //  String countString = getResources().getQuantityString(R.plurals.search_results,
                               //     count, new Object[] {count, query});
            String countString;
            if (count == 1){
	              countString = Integer.toString(count) + " result for " + query;
            }
            else{
	              countString = Integer.toString(count) + " results for " + query;
            }
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
            		DictionaryDatabase.WIKEM_CATEGORY };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.word, R.id.definition};

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor, from, to);
            mListView.setAdapter(words);

		            // Define the on-click listener for the list items
		            mListView.setOnItemClickListener(new OnItemClickListener() {
		                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		                    // Build the Intent used to open WordActivity with a specific word Uri
		                    Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
		                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
		                                                    String.valueOf(id));
		  //         Log.d("SEARCHABLE DICT", "uri data is.............. " + data.toString());
		                    wordIntent.setData(data);
		                   // startActivity(wordIntent);
//so text queries have the searched terms highlighted...let webwordactivity to highlight
		                    wordIntent.putExtra("is_query", true); 
		                  //  startActivityForResult(wordIntent, 26);//arbitrary int, just used for position <-??wth is this?
		                startActivity(wordIntent);
		                }
		            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.update: //menu item for updating
            	updateDb();
            	return true;
            case R.id.favorite:
            	displayFavs();
            	return true; 

 			case R.id.info:
 				displayInfo();
 				return true;
 			
 			case R.id.viewAll:
 				viewAll();
 				return true;
 
            default:
                return false;
        }
    }
    
    private void viewAll() { 
    	startActivity(new Intent(this, ViewAllActivity.class));
  	}

	//get string of date to display info
    public String getUpdateDate(){
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0); 
		long l = settings.getLong("epoch", 1288369494); //just some crud old value
    	//Date theLastUpdate = new Date(l * 1000);
    	Date d = new Date(l * 1000);
    	return d.toLocaleString();
    	//    	return theLastUpdate.toString();
    }
    
    private void displayInfo() {		
		String lastUpdated = "Last update was: " + getUpdateDate() + ". ";
		 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String string = getString(R.string.info);

    	builder.setMessage(lastUpdated + string)
    	       .setCancelable(false)
    	       .setPositiveButton("Return to WikEM", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               ///// dunno what put here MyActivity.this.finish();
    	        	   dialog.cancel();
    	           }
    	       });    	       
    	AlertDialog alert = builder.create();      
    	alert.show();		
	}

	private void displayFavs() {
     	startActivity(new Intent(this, FavoriteActivity.class));		
	}

	public void updateDb(){    	
    	startActivity(new Intent(this, DownloaderTest.class));
    	finish();
    }
    
    @Override
        protected void onStop(){
           super.onStop();         
        } 

}

