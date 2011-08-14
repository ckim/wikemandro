package wikem.chris.wikemv3;

 
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
//import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.FragmentActivity;
 

//these are compatability class stuff.
// also for loader manager use getSupportLoaderManager() not getloadermanager



public class ViewAllActivity extends FragmentActivity implements
LoaderManager.LoaderCallbacks<Cursor>{
	

	private ListView lv;
	private TextView tv;

	
 	    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	        // TBD DictionaryProvider.ALL_URI
		  String[] projection = null;
		  
		    CursorLoader cursorLoader = new CursorLoader(this,
		    		DictionaryProvider.ALL_URI, projection, null, null, null);
		    return cursorLoader;

	    }
/*
 * The new swapCursor() method, introduced in API Level 11 
 * and provided in the compatibility package, assigns the new Cursor but 
 * does not close the previous one. This allows 
 * the system to keep track of the Cursor and manage it for us, optimizing where appropriate.
 */
 	    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 	    	if (cursor != null){
 	    	int count = cursor.getCount();
            // String countString = getResources().getQuantityString(R.plurals.search_results,
                                     //count, new Object[] {count, "WikEM entries"}); 
			   String countString;
	           countString = Integer.toString(count) + " total WikEM entries ";
	             
             tv.setText(countString);
	        // finished retreiving cursur
	    	adapter.swapCursor(cursor);
 	    	}

	    }
	 /*
	  * The final callback method to implement is the onLoaderReset() method. 
	  * This method is triggered when the loader is being reset and the loader data 
	  * is no longer available. Our only use is within the adapter, so we’ll
	  *  simply clear the Cursor we were using with another call to the swapCursor() method:
	  * */
 	    public void onLoaderReset(Loader<Cursor> loader) {
	        // TBD
 	    	adapter.swapCursor(null);

 	    }
	    	
	    	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cat); //just use same layout as categories
 	        lv = (ListView) findViewById(R.id.list);
 	        tv = (TextView) findViewById(R.id.text);
 	      //  String cat = getIntent().getStringExtra("category");
 	       // showCategories(cat);
 	       getSupportLoaderManager().initLoader(0, null, this);
 	       
 	        showAll();
	}
	
	
	 private SimpleCursorAdapter adapter;
	 
		 private void showAll() {
		// TODO Auto-generated method stub
	/*		 Cursor cursor = managedQuery(DictionaryProvider.ALL_URI, null, null,null, null);
			 if (cursor ==null){ //ie no results 
				 tv.setText(getString(R.string.no_results));//
			 }else{
		*/		 
			 // Specify the columns we want to display in the result
             String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
             		DictionaryDatabase.WIKEM_CATEGORY };

             // Specify the corresponding layout elements where we want the columns to go
             int[] to = new int[] { R.id.word, R.id.definition};

			 adapter = new SimpleCursorAdapter(
			            this.getApplicationContext(),R.layout.result,
			            null,
			            from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		//for fragments i think	 setListAdapter(adapter);
			            
			            
			 /*           
 				int count = cursor.getCount();
	            // String countString = getResources().getQuantityString(R.plurals.search_results,
	                                     //count, new Object[] {count, "WikEM entries"}); 
				   String countString;
		           countString = Integer.toString(count) + " total WikEM entries ";
		             
	             tv.setText(countString);

	            
	             // Create a simple cursor adapter for the definitions and apply them to the ListView
	             SimpleCursorAdapter words = new SimpleCursorAdapter(this,
	                                           R.layout.result, cursor, from, to);
	           */
	             lv.setAdapter(adapter);

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
	      //   }//end of else
		
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
		        inflater.inflate(R.menu.options_menu, menu);
		        menu.removeItem(R.id.viewAll);
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
	 		//	case R.id.viewAll:
	 		//		viewAll();
	 
	            default:
	                return false;
	        }
	    } 
	    
	    private void displayFavs() {
	     	startActivity(new Intent(this, FavoriteActivity.class));			
		}

		public void updateDb(){	    	
	    	startActivity(new Intent(this, DownloaderTest.class));
	    	finish();
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
 