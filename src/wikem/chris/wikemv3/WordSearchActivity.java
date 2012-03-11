package wikem.chris.wikemv3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import wikem.chris.R;


public class WordSearchActivity extends FragmentActivity implements
LoaderManager.LoaderCallbacks<Cursor>{
/*
 *  called from webword activity in shouldOverrideURLLoading
 * eg. click on link -> catches a null link in the try block -> calls this and just searches for it
 */
	private ListView lv;
	private TextView tv;
	private String query;
    private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cat);
 	        lv = (ListView) findViewById(R.id.list);
 	        tv = (TextView) findViewById(R.id.text);
 	        query = getIntent().getStringExtra("word");
 	        Log.d("SOWRDSEARCHACTIVITY", "WHERE IS ACTIVITY FOR:"+query);

 	        showResults(query);
   	       getSupportLoaderManager().initLoader(0, null, this);

	}
	
	

    private void showResults(String query) {
    	 // Specify the columns we want to display in the result
        String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
        		DictionaryDatabase.WIKEM_CATEGORY };

        // Specify the corresponding layout elements where we want the columns to go
        int[] to = new int[] { R.id.word, R.id.definition};
    	
    	adapter = new SimpleCursorAdapter(
		            this.getApplicationContext(),R.layout.result,
		            null,
		            from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    	    	 
    	 
    	if (adapter != null){
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
            }
            else{
            	Log.e("wordsearchac", " cursor is null");
            }
        }
    



	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		////Cursor cursor = managedQuery(DictionaryProvider.CONTENT_URI,
        //		null, null, new String[] {query}, null);  
		
		//public final Cursor managedQuery (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    	//public CursorLoader (Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    	
		  
		    CursorLoader cursorLoader = new CursorLoader(this,
		    		DictionaryProvider.CONTENT_URI, null, null, new String[] {query}, null);
		    return cursorLoader;	 
	}



	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (cursor !=null){
		   int count = cursor.getCount();
	     
	            String countString;
	            if (count == 1){
		              countString = Integer.toString(count) + " result for " + query;
	            }
	            else{
		              countString = Integer.toString(count) + " results for " + query;
	            }
	            tv.setText(countString);
		
		adapter.swapCursor(cursor);
		}
		else{
			tv.setText(" no results found for " + query);
		}
	}



	public void onLoaderReset(Loader<Cursor> arg0) {
 	 	adapter.swapCursor(null);
		
	}
		 
		 
}
