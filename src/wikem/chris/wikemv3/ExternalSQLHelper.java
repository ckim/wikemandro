package wikem.chris.wikemv3;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ExternalSQLHelper extends ExternalStorageReadOnlyHelper{

	public ExternalSQLHelper(String dbFileName){
			//, CursorFactory factory
			super(dbFileName
				//, factory
				);
		// TODO Auto-generated constructor stub
	}
	public SQLiteDatabase getDatabase(){
		Log.d("external SQL helper", "getDatabase called :)");

		return super.getDatabase();
		
	}
	public void close(){
		Log.d("external SQL helper", "calling super.close");

		 super.close();
	}
	 
}
