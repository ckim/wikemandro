package wikem.chris.wikemv3;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class DownloaderActivity extends Activity {

    /**
     * Checks if data has been downloaded. If so, returns true. If not,
     * starts an activity to download the data and returns false. If this
     * function returns false the caller should immediately return from its
     * onCreate method. The calling activity will later be restarted
     * (using a copy of its original intent) once the data download completes.
     * @param activity The calling activity.
     * @param customText A text string that is displayed in the downloader UI.
     * @param fileConfigUrl The URL of the download configuration URL.
     * @param configVersion The version of the configuration file.
     * @param dataPath The directory on the device where we want to store the
     * data.
     * @param userAgent The user agent string to use when fetching URLs.
     * @return true if the data has already been downloaded successfully, or
     * false if the data needs to be downloaded.
     */
	public static int max;
	private void setPreferences(int i){
		//ugly hack. also use this to insert the custom message if there is one
		
	/*
	 * this ugly piece of code just puts the 'max', ie. the number of wikem entries, into sharedprefs
	 * 
	 * whereas, the epoch is set to sharedpreferences in the updateTimeStamp method
	 */
		//done bc configParser is a static class and i can't use the editor in it or get an error
		//Cannot make a static reference to the non-static method
		
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
	 	SharedPreferences.Editor editor = settings.edit(); 
	 	editor.putInt("max", i);
	 	editor.putBoolean("db_loaded", false); //set this to true on successful update
	 	
	 	if(containsMyCustomMessage && parsedMessageFromInfoXML!=null){	 	
	 		editor.putString("message", parsedMessageFromInfoXML);
	 		//reset the variables
	 		containsMyCustomMessage = false;
	 		parsedMessageFromInfoXML = null;
	 	}
          // Commit the edits!
          editor.commit();
	}

	 //this is called by outside dltest
    public static boolean ensureDownloaded(Activity activity,
            String customText, String fileConfigUrl,
            String configVersion, String dataPath,
            String userAgent, String lastUpdated, String dataSrc, String destinationFile) {
        File dest = new File(dataPath);
        if (dest.exists()) { //why necessary that the directory exists?
 
        	Log.i(LOG_TAG, "before checking epochMatches");

          /*  if(epochMatches(dest, lastUpdated)){
            	Log.i(LOG_TAG, "epochs match, no need to download.");
            	return true;
            }*/
        }
    /*otherwise if new program or sdcard and file doesnt exist
    //or if puts in old SD card with older or newer epoch
    //continue with program*/
        DATA_SRC = dataSrc; //previously required in config file online but now local
        DEST = destinationFile; //ditto
        Intent intent = PreconditionActivityHelper.createPreconditionIntent(
                activity, DownloaderActivity.class);
     //these extras are taken out at downloader class 
        intent.putExtra(EXTRA_CUSTOM_TEXT, customText);
        intent.putExtra(EXTRA_FILE_CONFIG_URL, fileConfigUrl);
    //this configVersion passed from downloaderTest gets passed again to downloader() and then becomes mconfigVersion
        intent.putExtra(EXTRA_CONFIG_VERSION, configVersion);
        intent.putExtra(EXTRA_DATA_PATH, dataPath);
        intent.putExtra(EXTRA_USER_AGENT, userAgent);
    //added this
        intent.putExtra(EXTRA_LAST_UPDATED, lastUpdated);
        PreconditionActivityHelper.startPreconditionActivityAndFinish(
                activity, intent);
        return false;
    }

    /**
     * Delete a directory and all its descendants.
     * @param directory The directory to delete
     * @return true if the directory was deleted successfully.
     */
    public static boolean deleteData(String directory) {
    	Log.d("dlactivity" ,"WHY DELETING the directory tree?");
        return deleteTree(new File(directory), true);
    }

    private static boolean deleteTree(File base, boolean deleteBase) {
        boolean result = true;
        if (base.isDirectory()) {
            for (File child : base.listFiles()) {
                result &= deleteTree(child, true);
            }
        }
        if (deleteBase) {
            result &= base.delete();
        }
        return result;
    }

   
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
         
        SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
	   	 String customText = "";
	/*   	if(!settings.getBoolean("db_loaded", false)){ //first time use, the default will trigger this
	   		 customText = getString(R.string.first_splash);
	   	 }
        
      */  
        if(settings.getBoolean("first_opened", true)) //added v2. default if first_opened is true...thus assume upgraded version
        {
        	customText = "Preparing WikEM for first use. ";
        }

        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.downloader);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.downloader_title);
        ((TextView) findViewById(R.id.customText)).setText(
                customText + " " + intent.getStringExtra(EXTRA_CUSTOM_TEXT));
        mProgress = (TextView) findViewById(R.id.progress);
        mTimeRemaining = (TextView) findViewById(R.id.time_remaining);
        Button button = (Button) findViewById(R.id.cancel);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (mDownloadThread != null) {
                    mSuppressErrorMessages = true;
                    mDownloadThread.interrupt();
                }
            }
        });
        startDownloadThread();
    }

    private void startDownloadThread() {
        mSuppressErrorMessages = false;
        mProgress.setText("");
        mTimeRemaining.setText("");
        mDownloadThread = new Thread(new Downloader(), "Downloader");
        mDownloadThread.setPriority(Thread.NORM_PRIORITY - 1);
        mDownloadThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuppressErrorMessages = true;
        mDownloadThread.interrupt();
        try {
            mDownloadThread.join();
        } catch (InterruptedException e) {
            // Don't care.
        }
    }

    private ProgressDialog dialog;
    private void onDownloadSucceeded() {
       /* Log.i(LOG_TAG, "Download succeeded!");
     	DictionaryDatabase.upgrade();
     	*/
 
     /*   //now dl success, just launch dbload without necessarily wiping old table and starting from scratch
        Intent intent = new Intent(getApplicationContext(), DbLoadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //error without
         getApplicationContext().startActivity(intent);
    	*/
    	
    	
    /*	//ck:  for wahtever reason, without a timer the old alert gets frozen. 
     * benefit of timertask is to 1)allow time for alert to dissapear
     * 	and 2) run the upgrade in a separate thread for more stability (a little slower)
     * 		
    	*/
    	
    	
    	   dialog = ProgressDialog.show(this, "Rebuilding WikEM", 
	                "This will take less than a minute. Please do not interrupt this proccess. Thank you for your patience.", true);
	        dialog.show();
	        
	        Timer myTimer = new Timer(true); //ie the task is a daemon in the background
			myTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					TimerMethod();
				}

			}, 4000); //wait 4s.?
  //  	finish();
        //PreconditionActivityHelper.startOriginalActivityAndFinish(this);
    }
	  
    private void TimerMethod()
    {
    	 
    	   
        DictionaryDatabase.upgrade();
        dialog.dismiss();
        SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
		 	SharedPreferences.Editor editor = settings.edit();
		 	 if (settings.getBoolean("first_opened", true)){
	            	editor.putBoolean("first_opened", false);
	            	Log.d(LOG_TAG, "now first opened is false");	            	
	            }
		 	 if (settings.getBoolean("force_rebuild", true)){
				 	editor.putBoolean("force_rebuild", false);
		 	 }
			editor.putBoolean("db_loaded", true);	//confirmation of complete db load...
			editor.putBoolean("successfully_updated", true); //this is for recent update so _> toast
			editor.commit();
        startActivity(new Intent(this, SearchableDictionary.class));  
        finish();
        //call finish on this context from outside
    	 
    }
    private void onDownloadFailed(String reason) {
    	/*
    	 * would like to clean up error message such that parses out after the colon
    	 * eg. chris.test.downloaderActiviy$DownloaderException: No update to wikem database available
    	 */
        Log.e(LOG_TAG, "Download stopped: " + reason);
        String shortReason;
        int index = reason.indexOf('\n');
  //new start index      
        int index2 = reason.indexOf(':'); index2++;
        if (index >= 0) {
            shortReason = reason.substring(index2, index);
        	//shortReason = reason.substring(0, index);
        } else {
            shortReason = reason;
        }
    //build the error alert
         final AlertDialog alert = new Builder(this).create();
        boolean contains_no_update_avail = shortReason.contains(NO_UPDATE_MESSAGE);
        
        alert.setTitle(R.string.download_activity_download_stopped);

        if (!mSuppressErrorMessages) { //?when is this craps turned true?
            alert.setMessage(shortReason);
        }
        
        if (!contains_no_update_avail){
			        alert.setButton(getString(R.string.download_activity_retry),
			                new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			                startDownloadThread();
			            }
			
			        });
        }
        else{
        /*
         * add third button to ask option for rebuild if no update avail
         */
		        alert.setButton3("Force database Rebuild?", 
		        		new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                //finish();
		            	/*
		            	 * do work here
		            	 */
		            	
		            	onDownloadSucceeded();
		            	alert.cancel();
		            	
 		           }
		
		        });
        }
        alert.setButton2(getString(R.string.download_activity_quit),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
             	startActivity(new Intent(getApplicationContext(), SearchableDictionary.class)); //add this... 6/2011
                finish();
            }

        });
        try {
            alert.show();
        } catch (WindowManager.BadTokenException e) {
            // Happens when the Back button is used to exit the activity.
            // ignore.
        }
    }

    private void onReportProgress(int progress) {
        mProgress.setText(mPercentFormat.format(progress / 10000.0));
        long now = SystemClock.elapsedRealtime();
        if (mStartTime == 0) {
            mStartTime = now;
        }
        long delta = now - mStartTime;
        String timeRemaining = getString(R.string.download_activity_time_remaining_unknown);
        if ((delta > 3 * MS_PER_SECOND) && (progress > 100)) {
            long totalTime = 10000 * delta / progress;
            long timeLeft = Math.max(0L, totalTime - delta);
            if (timeLeft > MS_PER_DAY) {
                timeRemaining = Long.toString(
                    (timeLeft + MS_PER_DAY - 1) / MS_PER_DAY)
                    + " "
                    + getString(R.string.download_activity_time_remaining_days);
            } else if (timeLeft > MS_PER_HOUR) {
                timeRemaining = Long.toString(
                        (timeLeft + MS_PER_HOUR - 1) / MS_PER_HOUR)
                        + " "
                        + getString(R.string.download_activity_time_remaining_hours);
            } else if (timeLeft > MS_PER_MINUTE) {
                timeRemaining = Long.toString(
                        (timeLeft + MS_PER_MINUTE - 1) / MS_PER_MINUTE)
                        + " "
                        + getString(R.string.download_activity_time_remaining_minutes);
            } else {
                timeRemaining = Long.toString(
                        (timeLeft + MS_PER_SECOND - 1) / MS_PER_SECOND)
                        + " "
                        + getString(R.string.download_activity_time_remaining_seconds);
            }
        }
        mTimeRemaining.setText(timeRemaining);
    }

    private void onReportVerifying() {
        mProgress.setText(getString(R.string.download_activity_verifying));
        mTimeRemaining.setText("");
    }

    private static void quietClose(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            // Don't care.
        }
    }

    private static void quietClose(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            // Don't care.
        }
    }

    
    
    
    
    
    private static class Config {
        long getSize() {
            long result = 0;
            for(File file : mFiles) {
                result += file.getSize();
            }
            return result;
        }
        static class File {
         	//eg. called here konfig.mFiles.add(new Config.File(DATA_SRC, DEST, null , size.longValue()));
            public File(String src, String dest, String md5, long size) {
                if (src != null) {
                    this.mParts.add(new Part(src, md5, size));
                }
                this.dest = dest;
            }
            static class Part {
                Part(String src, String md5, long size) {
                    this.src = src;
                    this.md5 = md5;
                    this.size = size;
                }
                
                String src;
                String md5;
                long size;
            }
            ArrayList<Part> mParts = new ArrayList<Part>();
            String dest;
            long getSize() {
                long result = 0;
                for(Part part : mParts) {
                    if (part.size > 0) {
                        result += part.size;
                    }
                }
                return result;
            }
        }
        @SuppressWarnings("unused")
		String version;
        ArrayList<File> mFiles = new ArrayList<File>();
        //add long   for epoch.... usefulness of timestamp object?
        long epoch;
    }

    
    
    
    /**
     * <config version="">
     *   <file src="http:..." dest ="b.x" />
     *   <file dest="b.x">
     *     <part src="http:..." />
     *     ...
     *   ...
     * </config>
     *
     */
    private static class ConfigParser {
    	
    	   public static Config parse(InputStream is) throws XmlPullParserException, IOException{

    	         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    	         factory.setNamespaceAware(true); //?
    	         XmlPullParser xpp = factory.newPullParser();

    	         xpp.setInput( is, "UTF-8");
    	         int eventType = xpp.getEventType();
    	         Config konfig = new Config();
    	         String tagNameTemp = new String();
    	         String tempEpoch = new String();
    	         String tempBytes = new String();
    	         @SuppressWarnings("unused")
				String numberWikemEntries = new String();
    	        // Long size;
    	         
           	  	
    	     try{    
    	         while (eventType != XmlPullParser.END_DOCUMENT) {
    	          if(eventType == XmlPullParser.START_DOCUMENT) {
    	             Log.d(LOG_TAG, "pull parse start DOC");
    	          } else if(eventType == XmlPullParser.END_DOCUMENT) {
    	              Log.d(LOG_TAG, "pull parse End document");
    	          } else if(eventType == XmlPullParser.START_TAG) {
    	               tagNameTemp = xpp.getName();
    	               if(tagNameTemp.matches("lastupdate")){
      	            	 tempEpoch = xpp.getAttributeValue(0); //works off of zero-based attribute
      	            	 Log.d(LOG_TAG, "ok epoch of info file online is " + tempEpoch);
      	            	 //will use the epoch as string as "version"
      	            	 konfig.version = tempEpoch;
      	         	     Long epoch = Long.valueOf(tempEpoch);
      	            	 konfig.epoch = epoch.longValue();
      	              }
      	              if(tagNameTemp.matches("size")){
      	            	  //attribute value is the "num"
      	            	  numberWikemEntries = xpp.getAttributeValue(0);
      	            	  tempBytes = xpp.getAttributeValue(1);
      	          	      Long size  = Long.valueOf(tempBytes);
//make md5 null for now
      	                    konfig.mFiles.add(new Config.File(DATA_SRC, DEST, null , size.longValue()));
       	            	 Log.d(LOG_TAG, "ok size of bytes is " + tempBytes);
      	            	  
      	              }
      	              if(tagNameTemp.matches(MESSAGE_INFO)){
      	            	  //future use to relay messages eg: <message> "plz upgrade" <message/>
      	            	  //TODO
      	            	  containsMyCustomMessage = true;
      	            	//  super.onReportNeedForUpgrade(); doesnt work. cant use in static class
      	            	parsedMessageFromInfoXML = xpp.nextText();
       	            	  
      	         	       }
      	               
    	          } else if(eventType == XmlPullParser.END_TAG) {
    	              Log.d(LOG_TAG, "End tag ");
    	          } else if(eventType == XmlPullParser.TEXT) {
    	             
    	          }    			
    	         eventType = xpp.next();
  
    	         }
    	     }catch (XmlPullParserException e) {
  				e.printStackTrace(); Log.e(LOG_TAG, "error loading database in XMLPullparser..");
 			   }
    	    
    	       Log.d(LOG_TAG, "returning konfiggggg");
    	       return konfig;  
    	     }
     }
    
    
    
     
    
    @SuppressWarnings("serial")
	private class DownloaderException extends Exception {
        public DownloaderException(String reason) {
            super(reason);
        }
    }

   
    
    private class Downloader implements Runnable {
    	
        public void run() {
            Intent intent = getIntent();
            mFileConfigUrl = intent.getStringExtra(EXTRA_FILE_CONFIG_URL);
            mConfigVersion = intent.getStringExtra(EXTRA_CONFIG_VERSION);
            mDataPath = intent.getStringExtra(EXTRA_DATA_PATH);
            mUserAgent = intent.getStringExtra(EXTRA_USER_AGENT);
                        //add mlastupdated
            mLastUpdated = intent.getStringExtra(EXTRA_LAST_UPDATED);
            
            
            
            
            
            //mDataDir = new File(mDataPath);
 
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
                // Do something for froyo and above versions
                mDataDir = getExternalFilesDir(null);

            } else {
                // do something for phones running an SDK before froyo
            	mDataDir = new File(mDataPath);
            }
            
            
            try {
                // Download files.
                mHttpClient = new DefaultHttpClient();
                Config config = getConfig();
 //327               filter(config);
                persistantDownload(config);
           //   downloadDB(config);
                
                
                ImageDownloader i = new ImageDownloader();//my custom subclass
                i.imageDownload(); //download the images to the sd card
                
                
                
               verify(config); //41211 ck:ok restored it now that the filesize matches
          //after verified... make sure return new EPOCH and update last_updated
                updateTimeStamp(config);
                cleanup();
                setPreferences(max);//put this here... instead of in ondownloadsuceed... which can be called 
                //even if max is zero i think in instances of jsut db rebuilding AND we don't care about max anymore in wikem v3
               // ImageDownloader i = new ImageDownloader();//my custom subclass
               // i.imageDownload(); //download the images to the sd card
                reportSuccess();
            } catch (Exception e) {
   /*
    * if catches an exception than passes the error to reportFailure which,  in case of download failure
    * goes to handler -> onDownloadFailed(String reason). and parse out the traceStack
    * 
    *     	
    */
                reportFailure(e.toString() + "\n" + Log.getStackTraceString(e));
            }
        }

     
		private void persistantDownload(Config config)
        throws ClientProtocolException, DownloaderException, IOException {
            while(true) {
                try {
                    download(config);
                    break;
                } catch(java.net.SocketException e) {
                    if (mSuppressErrorMessages) {
                        throw e;
                    }
                } catch(java.net.SocketTimeoutException e) {
                    if (mSuppressErrorMessages) {
                        throw e;
                    }
                }
                Log.i(LOG_TAG, "Network connectivity issue, retrying.");
            }
        }


        private void reportSuccess() {
            mHandler.sendMessage(
                    Message.obtain(mHandler, MSG_DOWNLOAD_SUCCEEDED));
        }

        private void reportFailure(String reason) {
            mHandler.sendMessage(
                    Message.obtain(mHandler, MSG_DOWNLOAD_FAILED, reason));
        }

       private void reportProgress(int progress) {
            mHandler.sendMessage(
                    Message.obtain(mHandler, MSG_REPORT_PROGRESS, progress, 0));
        }
      

        private void reportVerifying() { //not used anymore i think
            mHandler.sendMessage(
                    Message.obtain(mHandler, MSG_REPORT_VERIFYING));
        }
        //TODO if parse specific method will throw message to upgrade the app
        //for use for future versions.. to communicate with legacy users
        /*
         * i figure make more generic to just give a message to user.
         */
     //   private void reportNeedUpgradeApp(String message){
        //	mHandler.sendMessage(Message.obtain(mHandler, MSG_NEED_UPGRADE_APP, message));
      //  }

        //only called from Downloader class run()
       
        private Config getConfig() throws DownloaderException,
            ClientProtocolException, IOException {
            Config config = null;
            if (mDataDir.exists()) { 
            	//nothing but crap was here
            } else { // passed datadirector from downloadTest doesnt exist so need to make it
                Log.i(LOG_TAG, "Creating directory " + mDataPath);
                mDataDir.mkdirs();
                mDataDir.mkdir();
                if (!mDataDir.exists()) {
                    throw new DownloaderException(
                            "Could not create the directory " + mDataPath);
                }
            }
            //if config is null... ie. ? local_config_temp doesnt exist
            //if config isn't null (i htink only if config versions same) basically skips downloading new one obviously.
           
            if (config == null) { //ie. should be every instance
            	//downloads the config file into the local temp file (download(source, destinaion).
           File localConfig = download(mFileConfigUrl, LOCAL_CONFIG_FILE_TEMP);
            	
            //	File localConfig = connectToWikem(mFileConfigUrl, LOCAL_CONFIG_FILE_TEMP);
     Log.d("DOWNLOADACt", "where the f is config:" + localConfig.toString());
                InputStream is = new FileInputStream(localConfig);
                //try parsing the input stream.. 
              
                try {
                 	config = ConfigParser.parse(is); //pull parse this piece
                	} catch (XmlPullParserException e) {
                		e.printStackTrace(); Log.e(LOG_TAG, "error loading database in XMLPullparser..");}
						               
                finally {
                    quietClose(is);
                }
                
                /*
                 * this next section.. checks "config version" of online config file. will not proceed without matched version
                 * mconfigVersion is the version passed into this activity from DownloaderTest for example
                 * config.version in this instance is the online version found in the XML after parsing
                 * 
 
                 * 
                 */
       /*if the epoch of the downloaded config file is less than or equal.. dont download      :)  
        */
                
                Long lastUpdated = new Long(mLastUpdated);
                File olddb = new File( mDataDir, DownloaderTest.DESTINATION_FILE);
                if (config.epoch <= lastUpdated.longValue() && olddb.exists()){ //check md5 or size
                	throw new DownloaderException(
                            NO_UPDATE_MESSAGE);
                }
           
            }
            return config;
        }

     /*  deleted all this filtered nonsense a while ago..
      *  private void noisyDelete(File file) throws IOException {
            if (! file.delete() ) {
                throw new IOException("could not delete " + file);
            }
        }*/

        private void download(Config config) throws DownloaderException,
            ClientProtocolException, IOException {
            mDownloadedSize = 0;
            getSizes(config);
            Log.i(LOG_TAG, "Total bytes to download: "
                    + mTotalExpectedSize);
            for(Config.File file : config.mFiles) {
                downloadFile(file);
            }
        }

        private void downloadFile(Config.File file) throws DownloaderException,
                FileNotFoundException, IOException, ClientProtocolException {
            boolean append = false;
            File dest = new File(mDataDir, file.dest); 
            long bytesToSkip = 0;
            if (dest.exists() && dest.isFile()) {
            	//previously designed to append parts of files... no longer needed.
            	//out of laziness will keep code. but have file overwritten always
             	      /*    	
                append = true;
                bytesToSkip = dest.length();
                mDownloadedSize += bytesToSkip;
                Log.d("DLACTIVIY" , "DOWNLOADfILE IF LOOP....NEED TO GET RID OF ...IT IS SKIPPING IF EXISTS");*/
            	
            }
            FileOutputStream os = null;
            long offsetOfCurrentPart = 0;
            try {
                for(Config.File.Part part : file.mParts) {
                    // The part.size==0 check below allows us to download
                    // zero-length files.
                    if ((part.size > bytesToSkip) || (part.size == 0)) {
                        MessageDigest digest = null;
                        if (part.md5 != null) {
                            digest = createDigest();
                            if (bytesToSkip > 0) {
                                FileInputStream is = openInput(file.dest);
                                try {
                                    is.skip(offsetOfCurrentPart);
                                    readIntoDigest(is, bytesToSkip, digest);
                                } finally {
                                    quietClose(is);
                                }
                            }
                        }
                        if (os == null) {
                            os = openOutput(file.dest, append);
                        }
                        downloadPart(part.src, os, bytesToSkip,
                                part.size, digest);
                        if (digest != null) {
                            String hash = getHash(digest);
                            if (!hash.equalsIgnoreCase(part.md5)) {
                                Log.e(LOG_TAG, "web MD5 checksums don't match. "
                                        + part.src + "\nExpected "
                                        + part.md5 + "\n     got " + hash);
                                quietClose(os);
                                dest.delete();
                                throw new DownloaderException(
                                      "Received bad data from web server");
                            } else {
                               Log.i(LOG_TAG, "web MD5 checksum matches.");
                            }
                        }
                    }
                    bytesToSkip -= Math.min(bytesToSkip, part.size);
                    offsetOfCurrentPart += part.size;
                }
            } finally {
                quietClose(os);
            }
        }

        private void cleanup() throws IOException {
        // may 6th 2011, still problems with this. don't even understand what it is for
        //edit: whoops. deleted the whole filtered nonsense. now i understand
        	//       File filtered = new File(mDataDir, LOCAL_FILTERED_FILE);

            //noisyDelete(filtered); //er why can not delete the file?
            File tempConfig = new File(mDataDir, LOCAL_CONFIG_FILE_TEMP);
            File realConfig = new File(mDataDir, LOCAL_CONFIG_FILE);
            tempConfig.renameTo(realConfig);
        }
    /*
     * update the LAST_UPDATED here and also needs to communicate with original caller of this activity    
     */
        private void updateTimeStamp(Config config){
         	mLastUpdated = String.valueOf(config.epoch);
        	Log.d(LOG_TAG, "mLastUpdated value changed to" + String.valueOf(config.epoch));
     
        	// We need an Editor object to make preference changes.
            // All objects are from android.context.Context
            SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("epoch", config.epoch);

            // Commit the edits!
            editor.commit();
     
        }
        
        private void verify(Config config) throws DownloaderException,
        ClientProtocolException, IOException {
            Log.i(LOG_TAG, "Verifying...");
            String failFiles = null;
            for(Config.File file : config.mFiles) {
                if (! verifyFile(file, true) ) {
                    if (failFiles == null) {
                        failFiles = file.dest;
                    } else {
                        failFiles += " " + file.dest;
                    }
                }
            }
            if (failFiles != null) {
                throw new DownloaderException(
                        "Possible bad SD-Card. MD5 sum incorrect for file(s) "
                        + failFiles);
            }
        }

        private boolean verifyFile(Config.File file, boolean deleteInvalid)
                throws FileNotFoundException, DownloaderException, IOException {
            Log.i(LOG_TAG, "verifying " + file.dest);
            reportVerifying();
            File dest = new File(mDataDir, file.dest);
            if (! dest.exists()) {
                Log.e(LOG_TAG, "File does not exist: " + dest.toString());
                return false;
            }
            long fileSize = file.getSize();
            long destLength = dest.length();
            if (fileSize != destLength) {
                Log.e(LOG_TAG, "Length doesn't match. Expected " + fileSize
                        + " got " + destLength);
                if (deleteInvalid) {
                    dest.delete();
                    return false;
                }
            }
            FileInputStream is = new FileInputStream(dest);
            try {
                for(Config.File.Part part : file.mParts) {
                    if (part.md5 == null) {
                        continue;
                    }
                    MessageDigest digest = createDigest();
                    readIntoDigest(is, part.size, digest);
                    String hash = getHash(digest);
                    if (!hash.equalsIgnoreCase(part.md5)) {
                        Log.e(LOG_TAG, "MD5 checksums don't match. " +
                                part.src + " Expected "
                                + part.md5 + " got " + hash);
                        if (deleteInvalid) {
                            quietClose(is);
                            dest.delete();
                        }
                        return false;
                    }
                }
            } finally {
                quietClose(is);
            }
            return true;
        }

        private void readIntoDigest(FileInputStream is, long bytesToRead,
                MessageDigest digest) throws IOException {
            while(bytesToRead > 0) {
                int chunkSize = (int) Math.min(mFileIOBuffer.length,
                        bytesToRead);
                int bytesRead = is.read(mFileIOBuffer, 0, chunkSize);
                if (bytesRead < 0) {
                    break;
                }
                updateDigest(digest, bytesRead);
                bytesToRead -= bytesRead;
            }
        }

        private MessageDigest createDigest() throws DownloaderException {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new DownloaderException("Couldn't create MD5 digest");
            }
            return digest;
        }

        private void updateDigest(MessageDigest digest, int bytesRead) {
            if (bytesRead == mFileIOBuffer.length) {
                digest.update(mFileIOBuffer);
            } else {
                // Work around an awkward API: Create a
                // new buffer with just the valid bytes
                byte[] temp = new byte[bytesRead];
                System.arraycopy(mFileIOBuffer, 0,
                        temp, 0, bytesRead);
                digest.update(temp);
            }
        }

        private String getHash(MessageDigest digest) {
            StringBuilder builder = new StringBuilder();
            for(byte b : digest.digest()) {
                builder.append(Integer.toHexString((b >> 4) & 0xf));
                builder.append(Integer.toHexString(b & 0xf));
            }
            return builder.toString();
        }


        /**
         * Ensure we have sizes for all the items.
         * @param config
         * @throws ClientProtocolException
         * @throws IOException
         * @throws DownloaderException
         */
        private void getSizes(Config config)
            throws ClientProtocolException, IOException, DownloaderException {
            for (Config.File file : config.mFiles) {
                for(Config.File.Part part : file.mParts) {
                    if (part.size < 0) {
                        part.size = getSize(part.src);
                    }
                }
            }
            mTotalExpectedSize = config.getSize();
        }

        private long getSize(String url) throws ClientProtocolException,
            IOException {
            url = normalizeUrl(url);
            Log.i(LOG_TAG, "Head " + url);
            HttpHead httpGet = new HttpHead(url);
            HttpResponse response = mHttpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Unexpected Http status code "
                    + response.getStatusLine().getStatusCode());
            }
            Header[] clHeaders = response.getHeaders("Content-Length");
            if (clHeaders.length > 0) {
                Header header = clHeaders[0];
                return Long.parseLong(header.getValue());
            }
            return -1;
        }

        private String normalizeUrl(String url) throws MalformedURLException {
            return (new URL(new URL(mFileConfigUrl), url)).toString();
        }

        private InputStream get(String url, long startOffset,
                long expectedLength)
            throws ClientProtocolException, IOException {
            url = normalizeUrl(url);
            Log.i(LOG_TAG, "Get " + url);

            mHttpGet = new HttpGet(url);
            int expectedStatusCode = HttpStatus.SC_OK;
            if (startOffset > 0) {
                String range = "bytes=" + startOffset + "-";
                if (expectedLength >= 0) {
                    range += expectedLength-1;
                }
                Log.i(LOG_TAG, "requesting byte range " + range);
                mHttpGet.addHeader("Range", range);
                expectedStatusCode = HttpStatus.SC_PARTIAL_CONTENT;
            }
            HttpResponse response = mHttpClient.execute(mHttpGet);
            long bytesToSkip = 0;
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != expectedStatusCode) {
                if ((statusCode == HttpStatus.SC_OK)
                        && (expectedStatusCode
                                == HttpStatus.SC_PARTIAL_CONTENT)) {
                    Log.i(LOG_TAG, "Byte range request ignored");
                    bytesToSkip = startOffset;
                } else {
                    throw new IOException("Unexpected Http status code "
                            + statusCode + " expected "
                            + expectedStatusCode);
                }
            }
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            if (bytesToSkip > 0) {
                is.skip(bytesToSkip);
            }
            return is;
        }

        private File download(String src, String dest)
            throws DownloaderException, ClientProtocolException, IOException {
            File destFile = new File(mDataDir, dest);
            FileOutputStream os = openOutput(dest, false);
            try {
                downloadPart(src, os, 0, -1, null);
            } finally {
                os.close();
            }
            return destFile;
        }

        private void downloadPart(String src, FileOutputStream os,
                long startOffset, long expectedLength, MessageDigest digest)
            throws ClientProtocolException, IOException, DownloaderException {
            boolean lengthIsKnown = expectedLength >= 0;
            if (startOffset < 0) {
                throw new IllegalArgumentException("Negative startOffset:"
                        + startOffset);
            }
            if (lengthIsKnown && (startOffset > expectedLength)) {
                throw new IllegalArgumentException(
                        "startOffset > expectedLength" + startOffset + " "
                        + expectedLength);
            }
            InputStream is = get(src, startOffset, expectedLength);
            try {
                @SuppressWarnings("unused")
				long bytesRead = downloadStream(is, os, digest);
                if (lengthIsKnown) {
                	//ie not for the info file but for the database
                /*	
                    long expectedBytesRead = expectedLength - startOffset;
                    if (expectedBytesRead != bytesRead) {
                        Log.e(LOG_TAG, "Bad file transfer from server: " + src
                                + " Expected " + expectedBytesRead
                                + " Received " + bytesRead);
                        throw new DownloaderException(
                                "Incorrect number of bytes received from server");
                                
                    }*/
                }
            } finally {
                is.close();
                mHttpGet = null;
            }
        }

        private FileOutputStream openOutput(String dest, boolean append)
            throws FileNotFoundException, DownloaderException {
            File destFile = new File(mDataDir, dest);
            File parent = destFile.getParentFile();
            if (! parent.exists()) {
                parent.mkdirs();
                Log.d("DLACT", "wtf? why making directory?");
            }
            if (! parent.exists()) {
                throw new DownloaderException("Could not create directory "
                        + parent.toString());
            }
            FileOutputStream os = new FileOutputStream(destFile, append);
            return os;
        }

        private FileInputStream openInput(String src)
            throws FileNotFoundException, DownloaderException {
            File srcFile = new File(mDataDir, src);
            File parent = srcFile.getParentFile();
            if (! parent.exists()) {
                parent.mkdirs();
                Log.d("DLACT", "wtf? why making directory?");

            }
            if (! parent.exists()) {
                throw new DownloaderException("Could not create directory "
                        + parent.toString());
            }
            return new FileInputStream(srcFile);
        }

       private long downloadStream(InputStream is, FileOutputStream os,
                MessageDigest digest)
                throws DownloaderException, IOException {
            long totalBytesRead = 0;
            while(true){
                if (Thread.interrupted()) {
                    Log.i(LOG_TAG, "downloader thread interrupted.");
                    mHttpGet.abort();
                    throw new DownloaderException("Thread interrupted");
                }
                int bytesRead = is.read(mFileIOBuffer);
                if (bytesRead < 0) {
                    break;
                }
                if (digest != null) {
                    updateDigest(digest, bytesRead);
                }
                totalBytesRead += bytesRead;
                os.write(mFileIOBuffer, 0, bytesRead);
                mDownloadedSize += bytesRead;
                int progress = (int) (Math.min(mTotalExpectedSize,
                        mDownloadedSize * 10000 /
                        Math.max(1, mTotalExpectedSize)));
                if (progress != mReportedProgress) {
                    mReportedProgress = progress;
                    reportProgress(progress);
                }
            }
            return totalBytesRead;
        } 
//add mlastupdated
        private String mLastUpdated;
        private DefaultHttpClient mHttpClient;
        private HttpGet mHttpGet;
        private String mFileConfigUrl;
        @SuppressWarnings("unused")
		private String mConfigVersion;
        private String mDataPath;
        @SuppressWarnings("unused")
        private String mUserAgent; 
        private long mTotalExpectedSize;
         private long mDownloadedSize;
         private int mReportedProgress;
        private final static int CHUNK_SIZE = 32 * 1024;
        byte[] mFileIOBuffer = new byte[CHUNK_SIZE];
    }
    public static File mDataDir; //changed from private, so can use in xmlImageDownloader

    private final static String LOG_TAG = "Downloader";
    private TextView mProgress;
    private TextView mTimeRemaining;
    private final DecimalFormat mPercentFormat = new DecimalFormat("0.00 %");
    private long mStartTime;
    private Thread mDownloadThread;
    private boolean mSuppressErrorMessages;

    private final static long MS_PER_SECOND = 1000;
    private final static long MS_PER_MINUTE = 60 * 1000;
    private final static long MS_PER_HOUR = 60 * 60 * 1000;
    private final static long MS_PER_DAY = 24 * 60 * 60 * 1000;

    private final static String LOCAL_CONFIG_FILE = "info.xml";
    private final static String LOCAL_CONFIG_FILE_TEMP = "config.tmp";
    //private final static String LOCAL_FILTERED_FILE = "w.downloadConfig_filtered";
    private final static String EXTRA_CUSTOM_TEXT = "DownloaderActivity_custom_text";
    private final static String EXTRA_FILE_CONFIG_URL = "DownloaderActivity_config_url";
    private final static String EXTRA_CONFIG_VERSION = "DownloaderActivity_config_version";
    private final static String EXTRA_DATA_PATH = "DownloaderActivity_data_path";
    private final static String EXTRA_USER_AGENT = "DownloaderActivity_user_agent";
    //added extra long ... don't know what to initialize
    public static String EXTRA_LAST_UPDATED = "blalba_lastUpdated";
    public static String DATA_SRC = "blabla_SRC";
    public static String DEST ="blablaConfig_dest";
    public static String NO_UPDATE_MESSAGE = "No update to wikem database available";

    private final static int MSG_DOWNLOAD_SUCCEEDED = 0;
    private final static int MSG_DOWNLOAD_FAILED = 1;
    private final static int MSG_REPORT_PROGRESS = 2;
    private final static int MSG_REPORT_VERIFYING = 3;
    private final static int MSG_IMAGE_DOWNLOADED = 4;
    
    //message_info is the tag of potential message that i want to relay to users
    private final static String MESSAGE_INFO = "message";
    private static boolean containsMyCustomMessage = false;
    private static String parsedMessageFromInfoXML = null;
    
    private final Handler mHandler = new Handler() {
        @Override
        //Looper.prepare();
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_DOWNLOAD_SUCCEEDED:
                onDownloadSucceeded();
                break;
            case MSG_DOWNLOAD_FAILED:
                onDownloadFailed((String) msg.obj);
                break;
            case MSG_REPORT_PROGRESS:
                onReportProgress(msg.arg1);
                break;
            case MSG_REPORT_VERIFYING:
                onReportVerifying();
                break;
            case MSG_IMAGE_DOWNLOADED:
            	onImageDownloaded((String) msg.obj);
            	break;
           
            default:
                throw new IllegalArgumentException("Unknown message id "
                        + msg.what);
            }
        }

    };

    private class ImageDownloader{
    	public  void imageDownload(){
    		ArrayList<String> imagelist = null;
    		try {
    			 imagelist = parsingXML(new URL(DownloaderTest.IMAGELIST_URL));
			} catch (ClientProtocolException e) {e.printStackTrace();} catch (MalformedURLException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
			
			download(imagelist);
			
    	}
    	 private  ArrayList<String> parsingXML(URL url) 
		 	throws ClientProtocolException, IOException{
		         
		                 try {
		                          
		                         SAXParserFactory spf = SAXParserFactory.newInstance();
		                         SAXParser sp = spf.newSAXParser();
		  
		                         /* Get the XMLReader of the SAXParser we created. */
		                         XMLReader xr = sp.getXMLReader();
		                         /* Create a new ContentHandler and apply it to the XML-Reader*/
		                         XMLImageHandler myExampleHandler = new XMLImageHandler();
		                         xr.setContentHandler(myExampleHandler);
		                        
		                         /* Parse the xml-data from our URL. */
		                         xr.parse(new InputSource(url.openStream()));
		                         /* Parsing has finished. */
		  
		                         /* Our ExampleHandler now provides the parsed data to us. */
		                    //     ParsedExampleDataSet parsedExampleDataSet =
		                          return myExampleHandler.getParsedData();
		  
		                         /* Set the result to be displayed in our GUI. */
		                     //    tv.setText(parsedExampleDataSet.toString());
		                        
		                 } catch (Exception e) {
		                         /* Display any Error to the GUI. */
		                       //  tv.setText("Error: " + e.getMessage());
		                	 Log.e("webwordact parsingXML" , e.getMessage());
		                 }
		                return null;
		         }
     
    	//ck, my custom reporter for imagedownloaded subclass
        private void reportImageDownloaded(String filename) {
            mHandler.sendMessage(
                    Message.obtain(mHandler, MSG_IMAGE_DOWNLOADED , filename));
        }
    	private void download(ArrayList<String> imagelist){
    		Iterator<String> i = imagelist.iterator();
    		boolean didDL = false;
    		String filename = null;
    		try{
	    		while (i.hasNext()){
	    			if(didDL){
	    				reportImageDownloaded(filename);
	    				didDL = false; //reset the bool
	    			}
	    			String imageurl = i.next(); 
	        		filename = imageurl.substring(imageurl.lastIndexOf('/') + 1);
	        		File f = new File(mDataDir,filename);
	        		if (!f.exists()){ //only dl files that don't exist
	        			executeHttpGet(imageurl, f);
	        			didDL = true;
	        		}
	    		}
    		} catch (Exception e) {	e.printStackTrace();Log.e("da", "error downloading images");}
    	}
    	private  void executeHttpGet(String imageurl, File imagefile) throws Exception ,ClientProtocolException, IOException, FileNotFoundException{
//download to localfile with last path segment FILENAME in directory mdatadir, assuming mdatadir is already set to apprriate Android version earlier
            
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(imageurl));
                HttpResponse response = client.execute(request);
                InputStream in = response.getEntity().getContent();
               // File imagefile = new File(mDataDir, filename);
                try {
                    OutputStream output = new FileOutputStream (imagefile);
                    try {
                        byte[] buffer = new byte[1024];
 						int bytesRead = 0;
                        while ((bytesRead = in.read(buffer, 0, buffer.length)) >= 0) {
                            output.write(buffer, 0, bytesRead);
                        }
                    } finally { output.close(); }
                } finally { in.close(); }
                                
    	} 
    	    	    	 
    }//end of custom image dl class
//TODO make this more elegant ...
	private void onImageDownloaded(String f){
		((TextView) findViewById(R.id.customText)).setText("Downloading Images... " + f);
		mProgress.setText(" ");
			/*Toast toast = Toast.makeText(getApplicationContext(), f, Toast.LENGTH_SHORT);
	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	 		try{
	 			toast.show();
	 		}catch(Exception e){e.printStackTrace();}*/
	}
//TODO add error message to update this app.
	/*
	 * for use in future to notify users to get newer version of app 
	 * //Retrieve the values
Set<String> set = new HashSet<String>();
set = myScores.getStringSet("key", null);

//Set the values
Set<String> set = new HashSet<String>();
set.addAll(listOfExistingScores);
scoreEditor.putStringSet("key", set);
scoreEditor.commit();
	 */
	 
}
