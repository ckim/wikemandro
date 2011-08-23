package wikem.chris.wikemv3;

/*
 this class starts downloaderactivity and also contains some static contents for the app
 */
import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.AndroidRuntimeException;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/*datapath ignored in downloaderactivity is replaced by getexternalfilesdir()
 */

public class DownloaderTest extends Activity {
	
	public static final String IMAGELIST_URL = "http://www.wikem.org/w/api.php?action=query&prop=images&list=allimages&ailimit=500&format=xml";
  
	//private final static String FILE_CONFIG_URL = "http://christopherkim.bol.ucla.edu/info.xml";
    private final static String CONFIG_VERSION = "4.0"; //i think just matching the config file

    
    public final static String BACKUP_PATH = "/Android/data/wikem.chris.wikemv3/files/"; //different than normal datapath...
   
    private final static String USER_AGENT = "wikem Downloader Activity"; //user agent string when fetching urls
    public  static String LAST_UPDATED = ""; //jsut a default value... will be replaced
  /**
   * 
   * ckim... no longer going to use xml. too slow.   */
    //private final static String FILE_CONFIG_URL = "http://dl.wikem.org/info.xml";
    // public static final String SRC_OF_DATA = "http://dl.wikem.org/database.xml";
    // public static final String DESTINATION_FILE = "wikem_dest";
    //public final static String DATA_PATH = "/Android/data/wikem.chris/files/wikEM_data/"; //directory to store device
    				private final static String FILE_CONFIG_URL = "http://dl.wikem.org/files/info.xml";
				    public final static String DATA_PATH = "/Android/data/wikem.chris.wikemv3/files/"; //directory to store device..ck:0713 use back up path
				    public static final String SRC_OF_DATA = "http://dl.wikem.org/files/android_db" ;
				    public static final String DESTINATION_FILE = "junk.db";
				     
    
				    
				    //public final static String DATA_PATH = "/Android/data/wikem.chris.wikemv3/files/"; //directory to store device..ck:0713 use back up path

				    
				    
	 
				    
    @Override public void onCreate(Bundle savedInstanceState) {
    	 
        super.onCreate(savedInstanceState);
        //also pass along last_updated
        SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
         LAST_UPDATED = Long.toString(settings.getLong("epoch", 1288369494)); //the 1238.. is default if epoch doesn't exist. jsut some old epoch so it will update on first run
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new AndroidRuntimeException(
                "External storage (SD-Card) not mounted");
        } 
        File appDbDir = new File(
            Environment.getExternalStorageDirectory(),DATA_PATH); //2.0- and 2.1 compatible... in DLACTIVITY class, will detect FROYO and change implementation
        
    //    Log.d("DOWNLOADER TEST" , "THE FILE IS " + appDbDir.toString());
        if (! DownloaderActivity.ensureDownloaded(this,
                getString(R.string.app_name), FILE_CONFIG_URL,
                //CONFIG_VERSION, DATA_PATH, USER_AGENT, LAST_UPDATED, SRC_OF_DATA, DESTINATION_FILE)) {
                CONFIG_VERSION, appDbDir.toString(), USER_AGENT, LAST_UPDATED, SRC_OF_DATA, DESTINATION_FILE)) {
            return;
        }
        setContentView(R.layout.dmain);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;
        int id = item.getItemId();

        if (id == R.id.menu_main_download_again) {
            downloadAgain();
        } else {
            handled = false;
        }

        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }
        return handled;
    }

    private void downloadAgain() {
    	File appDbDir = new File(
                Environment.getExternalStorageDirectory(),DATA_PATH);
        DownloaderActivity.deleteData(appDbDir.toString());
        startActivity(getIntent());
        finish();
    }
    
}