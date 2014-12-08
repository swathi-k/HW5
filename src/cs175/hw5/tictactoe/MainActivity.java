package cs175.hw5.tictactoe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
    public static int PORT = 7890;
    public static String IP_ADDRESS = "10.185.203.199"; // This can be changed to a specific ip address to connect to the server

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
		registerWithServer();
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


    /*
     * Registers a user name with the server and returns if the action was successfull done
     * @param returns true if the user name is accepted by the server. Otherwise, returns false
     *        because someone has registered with the same name
     */
    private boolean registerWithServer() {
	boolean success = false;
	Socket socket = null;
	BufferedReader reader = null;
	PrintWriter writer = null;
	try {
	    socket = new Socket(IP_ADDRESS, PORT);
	    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    writer = new PrintWriter(socket.getOutputStream(), true);

	    String input = reader.readLine();
	    String fname = "hello";
	    String lname = "world";
	    if (input.equals("Fingercise Server")) {
		String output = "register:" + fname + " " + lname;
		writer.println(output);
		// can make a toast to signal if the register is successfully done or not
		if ((input = reader.readLine()) != null) {
		    if (input.equals("Okay")) {
			success = true;
		    }
		}
	    }
	} catch (UnknownHostException e) {
	    Log.d(getLocalClassName(), "Unable to connect to " + IP_ADDRESS + ":" + PORT);
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
		if (writer != null) {
		    writer.close();
		}

		if (socket != null) {
		    socket.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }			
	}
	
	return success;
    }

	
}
