package cs175.hw5.tictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Button startbutton;
	Button scoresbutton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_screen);
	addListenerOnButton();
	}

	public void addListenerOnButton() {

		final Context context = this;

		startbutton = (Button) findViewById(R.id.startButton);

		startbutton.setOnClickListener(new OnClickListener() {

			//@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(context, TicTacToe.class);
				startActivity(intent);

			}

		});
		
		scoresbutton = (Button) findViewById(R.id.scoresButton);

		scoresbutton.setOnClickListener(new OnClickListener() {

			//@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(context, TicTacToe.class);
				startActivity(intent);

			}

		});

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
