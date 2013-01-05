package com.settademoniaco.robdacaravan;

import com.settademoniaco.robdacaravan.MainActivity;
import com.settademoniaco.robdacaravan.GameView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;

public class WorldActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new GameView(this));

	}

		
	public void toMainScreen(View view) {
		// Exiting the application from main menu
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		}

}
