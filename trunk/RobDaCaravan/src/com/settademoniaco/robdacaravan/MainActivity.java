package com.settademoniaco.robdacaravan;

import com.settademoniaco.robdacaravan.R;
import com.settademoniaco.robdacaravan.WorldActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void toWorldScreen(View view) {
		// Moving on to the World View Scene
		Intent intent = new Intent(this, WorldActivity.class);
		startActivity(intent);
		}
	
	public void toSettingsScreen(View view) {
		// Moving on to the settings screen
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
		}
		
	public void destroyApp(View view) {
		// Exiting the application from main menu
		finish();
		}

}
