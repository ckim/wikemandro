package wikem.chris.wikemv3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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


//TODO change queryto separate thread.
public class CategoryActivity extends FragmentActivity implements
LoaderManager.LoaderCallbacks<Cursor>{
	

	private ListView lv;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cat);
 	        lv = (ListView) findViewById(R.id.list);
 	        tv = (TextView) findViewById(R.id.text);
 	        String cat = getIntent().getStringExtra("category");
 	        showCategories(cat);
	}
	
	
	
		 private void showCategories(String cat){
	    	Cursor cursor = managedQuery(DictionaryProvider.CATEGORY_URI, null, null,
	    			new String[]{cat}, null);
	    	 if (cursor == null) {
	             // There are no results
	             tv.setText(getString(R.string.no_results, new Object[] {cat}));
	         } else {
	             // Display the number of results
	             int count = cursor.getCount();
	             //String countString = getResources().getQuantityString(R.plurals.search_results,
	               //                      count, new Object[] {count, cat});
	             String countString;
	             if (count == 1){
		              countString = Integer.toString(count) + " result for " + cat;
	             }
	             else{
		              countString = Integer.toString(count) + " results for " + cat;
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
	            	startActivity(new Intent(this, DownloaderTest.class)); finish();
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
	    private void displayFavs() {
 	    	startActivity(new Intent(this, FavoriteActivity.class));
			
		}



		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			return null;
		}



		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			// TODO Auto-generated method stub
			
		}



		public void onLoaderReset(Loader<Cursor> arg0) {
			// TODO Auto-generated method stub
			
		}
}
