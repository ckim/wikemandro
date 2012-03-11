package wikem.chris.wikemv3;

/*
 this class starts downloaderactivity and used to house static variables...now housed in singleton
 */
import java.io.File;
import wikem.chris.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

public class DownloaderTest extends Activity {	
	//useless constants... from original downloader program.
    private final static String CONFIG_VERSION = "4.0"; //i think just matching the config file
    private final static String USER_AGENT = "wikem Downloader Activity"; //user agent string when fetching urls
    private  static String LAST_UPDATED = ""; //jsut a default value... will be replaced
		    
    @Override public void onCreate(Bundle savedInstanceState) {
    	 
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
        LAST_UPDATED = Long.toString(settings.getLong("epoch", 1288369494)); //the 1238.. is default if epoch doesn't exist. jsut some old epoch so it will update on first run
     
        File appDbDir = new File(
            Environment.getExternalStorageDirectory(),Singleton.DATA_PATH); //2.0- and 2.1 compatible... in DLACTIVITY class, will detect FROYO and change implementation
        
        if (! DownloaderActivity.ensureDownloaded(this,
                getString(R.string.app_name), Singleton.FILE_CONFIG_URL,
                //CONFIG_VERSION, DATA_PATH, USER_AGENT, LAST_UPDATED, SRC_OF_DATA, DESTINATION_FILE)) {
                CONFIG_VERSION, appDbDir.toString(), USER_AGENT, LAST_UPDATED, Singleton.SRC_OF_DATA, Singleton.DESTINATION_FILE)) {
            return;
        }
        setContentView(R.layout.dmain);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
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
                Environment.getExternalStorageDirectory(),Singleton.DATA_PATH);
        DownloaderActivity.deleteData(appDbDir.toString());
        startActivity(getIntent());
        finish();
    }
    
}