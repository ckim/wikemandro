package wikem.chris.wikemv3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DisclaimerActivity extends Activity{
 TextView tv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
			setContentView(R.layout.disclaimer);
			tv = (TextView) findViewById(R.id.text);
		  Button closeButton = (Button) findViewById(R.id.button);
		  closeButton.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 		    	proceed();
		      finish();
		    }
		  });
		}
	protected void proceed() {
		startActivity(new Intent(this, DownloaderTest.class));		
	}
}
