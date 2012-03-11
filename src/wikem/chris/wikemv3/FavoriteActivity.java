package wikem.chris.wikemv3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import wikem.chris.R;

public class FavoriteActivity extends Activity{
	

	private ListView lv;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cat);
 	        lv = (ListView) findViewById(R.id.list);
 	        tv = (TextView) findViewById(R.id.text);
 	      //  String cat = getIntent().getStringExtra("category");
 	       // showCategories(cat);
 	        showFavorites();
	}
	
	
	
		 private void showFavorites() {
		// TODO Auto-generated method stub
			 Cursor cursor = managedQuery(DictionaryProvider.FAVORITES_URI, null, null,null, null);
			 if (cursor ==null){ //ie no results 
				 tv.setText(getString(R.string.no_favorites));//
			 }else{
				 
				 int count = cursor.getCount();
	       //      String countString = getResources().getQuantityString(R.plurals.search_results,
	         //                            count, new Object[] {count, "favorites"}); 
				   String countString;
		             if (count == 1){
			              countString = Integer.toString(count) + " results for favorite" ;
		             }
		             else{
			              countString = Integer.toString(count) + " results for favorites" ;
		             }
				 tv.setText(countString);

	             // Specify the columns we want to display in the result
	             String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
	             		DictionaryDatabase.WIKEM_CATEGORY };

	             // Specify the corresponding layout elements where we want the columns to go
	             int[] to = new int[] { R.id.word, R.id.definition};

	             // Create a simple cursor adapter for the definitions and apply them to the ListView
	             SimpleCursorAdapter words = new SimpleCursorAdapter(this,
	                                           R.layout.result, cursor, from, to);
	             lv.setAdapter(words);

	             // Define the on-click listener for the list items
	             lv.setOnItemClickListener(new OnItemClickListener() {
	                 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                     // Build the Intent used to open WordActivity with a specific word Uri
	                     Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
	                     Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
	                                                     String.valueOf(id));
	            Log.d("SEARCHABLE DICT", "uri data is.............. " + data.toString());
	                     wordIntent.setData(data);
	                     startActivity(wordIntent);
	                 }
	             });
	         }//end of else
		
	}


/*
 * public final Cursor managedQuery (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)

Since: API Level 1
Wrapper around query(android.net.Uri, String[], String, String[], String) that gives the resulting Cursor to call startManagingCursor(Cursor) so that the activity will manage its lifecycle for you.
Parameters
uri	The URI of the content provider to query.
projection	List of columns to return.
selection	SQL WHERE clause.
selectionArgs	The arguments to selection, if any ?s are pesent
sortOrder	SQL ORDER BY clause.
Returns
The Cursor that was returned by query().
 */
		
		 
		 @Override
		    public boolean onCreateOptionsMenu(Menu menu) {
		        MenuInflater inflater = getMenuInflater();
		        inflater.inflate(R.menu.fav_options_menu, menu);
		        return true;
		    }
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.search:
	                //startSearch();
	                return true;
	            case R.id.update: //menu item for updating
	            	startActivity(new Intent(this, DownloaderTest.class)); 
	            	finish(); //kill this activity
	            	return true;
	            case R.id.info:
	            	displayInfo();
	            
	            	
	            default:
	                return false;
	        }
	    } 
	    
	    private void displayInfo() {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String string = getString(R.string.info);
        	builder.setMessage(string )
        	       .setCancelable(false)
        	       .setPositiveButton("Return to WikEM", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	               ///// dunno what put here MyActivity.this.finish();
        	        	   dialog.cancel();
        	           }
        	       })
        	/*       .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       })*/
        	       ;
        	AlertDialog alert = builder.create();      
        	alert.show();
			
		}
}
