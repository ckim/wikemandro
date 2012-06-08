package wikem.chris.wikemv3;
 

public class Singleton {
/* simple singleton variable store*/
	 public static boolean verify = true;
	 
		public static final String IMAGELIST_URL = "http://www.wikem.org/w/api.php?action=query&prop=images&list=allimages&ailimit=500&format=xml";

		public final static String FILE_CONFIG_URL = "http://dl.wikem.org/files/info.xml";
	    public final static String BACKUP_PATH = "/Android/data/wikem.chris.wikemv3/files/"; //different than normal datapath...
	    

	    public final static String DATA_PATH = "/Android/data/wikem.chris.wikemv3/files/";
	    //directory to store device..ck:0713 use back up path
	    
	    
	    public static final String SRC_OF_DATA = "http://dl.wikem.org/files/android_db" ;
	    public static final String DESTINATION_FILE = "junk.db";
	    
	    public static final String SRC_OF_DATA_SLIM = "http://dl.wikem.org/files/android_dbslim" ; 
	  //public static final String SRC_OF_DATA_SLIM = "http://dl.wikem.org/files/testforchris.db" ; 

	    public static final String DESTINATION_FILE_SLIMDB = "slim.db";

		
}
