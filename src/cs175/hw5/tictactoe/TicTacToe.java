package cs175.hw5.tictactoe;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TicTacToe extends Activity {
	private Game game1;
	private SQLiteDatabase db;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game1 = new Game(this);
		setContentView(game1);
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
    
	private void loadDBData() {
		// TODO Auto-generated method stub
		int highscore = 0;
		String result;
		TextView displayScore = (TextView) findViewById(R.id.HigestScore);
		MyDb sankeScore = new MyDb(this);
		db = sankeScore.getReadableDatabase();

		/****** Debug line 163 *****/

		Cursor cursor = db.query("hw5", null, null, null, null, null, null);
		if (cursor != null) {
			highscore = cursor.getInt(cursor.getColumnIndex("HIGH_SCORE"));
			result = Integer.toString(highscore);
			displayScore.setText(result);

			Log.i("what is result", "" + result);

		} else {
			result = "0";
			displayScore.setText(result);
			Log.i("what is result", " is it here" + result);
		}

		cursor.close();

	}

}
