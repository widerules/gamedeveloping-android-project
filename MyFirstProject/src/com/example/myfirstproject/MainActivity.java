package com.example.myfirstproject;

import ru.gopnikgame.ai.SimpleLogicStringAi;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstproject.MESSAGE";

	TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView exampleView, int actionId,
				KeyEvent event) {
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_DOWN) {

				EditText et = ((EditText) findViewById(R.id.input_text));
				String reply = SimpleLogicStringAi.getReply(et.getText()
						.toString());
				et.setText("");

				TextView tw = ((TextView) findViewById(R.id.output_text));
				tw.setText(reply);
			}
			return true;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		EditText e = ((EditText) findViewById(R.id.input_text));
		e.setOnEditorActionListener(exampleListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
