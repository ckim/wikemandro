package wikem.chris.wikemv3;
//TODO: implement this read only version before update?


/*
 * inspired by penguineman, a read only simplified DB helper on SD card
 * 
 - A concrete subclass of ExternalStorageReadOnlyOpenHelper was created to query the reference data via the 
 SQLiteDatabase object returned by the getReadableDatabase() method.
 - The databaseFileExists() method allowed the main Activity to check if the database file already existed 
 to decide whether to initiate download.

Conclusion
This final design had greatly improved the application in the following ways :
The user only had to download a small apk file initially, greatly reducing the barrier to installation.
The separate download step for the database file presented an opportunity to tell the user what was happening and to use Wi-Fi if available.
Most of the data resided on the SD-Card, where space was much more abundant.
There was ever only one copy of the reference data on the phone, no redundant duplication.
The main application code could be updated without having to download the reference data.
 */
import java.io.File;
 
import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.util.AndroidRuntimeException;
import android.util.Log;

public abstract class ExternalStorageReadOnlyHelper{
    private SQLiteDatabase database;
    private File dbFile;
  //  private SQLiteDatabase.CursorFactory factory;
    
   
    public ExternalStorageReadOnlyHelper(
        String dbFileName
        //,SQLiteDatabase.CursorFactory factory) 
        ){
      // this.factory = factory;
       
       if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
           throw new AndroidRuntimeException(
               "External storage (SD-Card) not mounted");
       } 
       File appDbDir = new File(
           Environment.getExternalStorageDirectory(),            
      //     "Android/data/com.mycompany.myapp/db");
           Singleton.BACKUP_PATH); //my datapath location
       if (!appDbDir.exists()) {
   		Log.d("external SQL helper", "having to make datapath dir");

           appDbDir.mkdirs();
       }
       this.dbFile = new File(appDbDir, dbFileName);
    }
    
    public boolean databaseFileExists() {
       return dbFile.exists();
    }
    
    private void open() {
        if (dbFile.exists()) {
    		Log.d("external SQL helper", "exists!!!!!!!");

            database = SQLiteDatabase.openDatabase(
                dbFile.getAbsolutePath(), 
               null,// factory, 
                SQLiteDatabase.OPEN_READONLY);     
        }
        else{		Log.d("external SQL helper", "tried to open but dbFile doesn't exist");
}
    }
       
    public synchronized void close() {
		Log.d("external SQL helper", "closing the backup DB!! ok?!?!");

        if (database != null ) {
           database.close();
           database = null;
       }
    }
   
    public synchronized SQLiteDatabase getReadableDatabase() {
        return getDatabase();
    }

    public boolean dbExists(){ //added this 6/2011 to check if exists, and if so not reinitialize everytime
    	if (database==null){
    	return true;}
    	else
    	return false;
    }
    
    protected SQLiteDatabase getDatabase() {
       if (database==null) {
   		Log.d("external SQL helper", "getdatabase is null will open first if possible");

           open();
       }
       return database;
    }    
}  
   