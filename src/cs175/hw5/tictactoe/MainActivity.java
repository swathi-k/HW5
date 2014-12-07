package cs175.hw5.tictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button startbutton;
	Button scoresbutton;
	TextView mHighScoreView;
	final Context context = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_screen);
	addListenerOnButton();
	}

	public void addListenerOnButton() {

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

		mHighScoreView = (TextView) findViewById(R.id.HigestScore);
		mHighScoreView.setText(String.valueOf(getHighestScoreFromDB()));

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
    
	private int getHighestScoreFromDB() {
		initDB();
		MyDb snake = new MyDb(context);
		SQLiteDatabase db = snake.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from hw5 where name = 'player 1'", null);
		cursor.moveToFirst();
		int count = cursor.getColumnCount();
		int scoreInDb = 0;
		if (count > 0) {
			scoreInDb = cursor.getInt(cursor.getColumnIndex("HIGH_SCORE"));
		}

		cursor.close();
		db.close();
		return scoreInDb;
	}

	private void initDB() {
		MyDb snake = new MyDb(context);
		SQLiteDatabase db = snake.getWritableDatabase();
		// create table at first time
		String sql = "create table if not exists hw5 (name varchar(50), speed integer, HIGH_SCORE integer, score intger)";
		db.execSQL(sql);

		// db.execSQL("delete from hw4 where name = 'player 1'");
		Cursor rawQuery = db.rawQuery(
				"select * from hw5 where name = 'player 1'", null);

		if (rawQuery.getCount() == 0) {
			db.execSQL("insert into hw5 (name, HIGH_SCORE) values('player 1', 0)");
		}
		rawQuery.close();
		db.close();
	}

}
