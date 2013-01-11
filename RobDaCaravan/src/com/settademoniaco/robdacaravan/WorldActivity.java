package com.settademoniaco.robdacaravan;

import com.settademoniaco.robdacaravan.FieldActivity;
import com.settademoniaco.robdacaravan.WorldView;

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
		setContentView(new WorldView(this));

	}

	@Override
	public void onBackPressed() {
		WorldView.worldLoopThread.setRunning(false);
		super.finish();
	}
	
	
	public void toFieldScreen(View view) {
		// Moving into the fields
		Intent intent = new Intent(this, FieldActivity.class);
		startActivity(intent);
		}

	
}
