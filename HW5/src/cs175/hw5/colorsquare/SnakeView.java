package cs175.hw5.colorsquare;

import java.util.ArrayList;
import java.util.Random;

import cs175.hw5.colorsquare.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * SnakeView: implementation of Snake game
 */
public class SnakeView extends TileView {
	// private SQLiteDatabase db;
	private static final String TAG = "SnakeView";

	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons.
	 */
	private int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;
	public static final int WIN = 4;
	public static final int GAMEOVER = 5;

	/**
	 * Current direction the snake is headed.
	 */
	private int mDirection = EAST;
	private int mNextDirection = EAST;
	private static final int NORTH = 1;
	private static final int SOUTH = 2;
	private static final int EAST = 3;
	private static final int WEST = 4;

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	private static final int RED_STAR = 1;
	private static final int YELLOW_STAR = 2;
	private static final int GREEN_STAR = 3;
	//private static final int BLUE_STAR = 4;
	/**
	 * mScore: Used to track the number of levels cleared mMoveDelay: number of
	 * milliseconds between snake movements. This will increase as levels are
	 * cleared.
	 */
	private long mScore = 0;
	private long mLives = 3;
	private int mCurrentLevel = 0;
	private int maxLevels = 3;
	private long mMoveDelay = 600;
	/**
	 * mLastMove: Tracks the absolute time when the snake last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 */
	private long mLastMove;

	/**
	 * mStatusText: Text shows to the user in some run states
	 */
	private TextView mStatusText;

	private TextView mscoreText;
	private TextView mliveText;
	private TextView mlevelText;

	/**
	 * mArrowsView: View which shows 4 arrows to signify 4 directions in which
	 * the snake can move
	 */
	private View mArrowsView;

	/**
	 * mBackgroundView: Background View
	 */
	private View mBackgroundView;

	/**
	 * mSnakeTrail: A list of Coordinates that make up the snake's body
	 * mAList: The location of the snake
	 */
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAList = new ArrayList<Coordinate>();

	private Walls mwall;

	/**
	 * Everyone needs a little randomness in their life
	 */
	private static final Random RNG = new Random();

	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date.
	 */

	private RefreshHandler mRedrawHandler = new RefreshHandler();

	private TextView mHighScoreView;
	
	private boolean created = false;

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			SnakeView.this.update();
			SnakeView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	/**
	 * Constructs a SnakeView based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSnakeView(context);
	}

	public SnakeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSnakeView(context);
	}

	private void initSnakeView(Context context) {

		setFocusable(true);

		Resources r = this.getContext().getResources();

		resetTiles(4);
		loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
		loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
		loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));
		//loadTile(BLUE_STAR, r.getDrawable(R.drawable.bluestar));

	}

	private void initNewGame() {
		mSnakeTrail.clear();
		mAList.clear();

		// For now we're just going to load up a short default eastbound snake
		// that's just turned north
		int midway = (mYTileCount / 2);

		mSnakeTrail.add(new Coordinate(5, midway));
		mSnakeTrail.add(new Coordinate(4, midway));
		mSnakeTrail.add(new Coordinate(3, midway));
		mSnakeTrail.add(new Coordinate(2, midway));
		mSnakeTrail.add(new Coordinate(1, midway));
		mSnakeTrail.add(new Coordinate(0, midway));
		mNextDirection = EAST;
		mMoveDelay = 600;
		if (mCurrentLevel == 1)
			mMoveDelay = 500;
		if (mCurrentLevel == 2)
			mMoveDelay = 400;

		mwall = new Walls(mXTileCount, mYTileCount);

	}

	/**
	 * Given a ArrayList of coordinates, we need to flatten them into an array
	 * of ints before we can stuff them into a map for flattening and storage.
	 * 
	 * @param cvec
	 *            : a ArrayList of Coordinate objects
	 * @return : a simple array containing the x/y values of the coordinates as
	 *         [x1,y1,x2,y2,x3,y3...]
	 */
	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
		int[] rawArray = new int[cvec.size() * 2];

		int i = 0;
		for (Coordinate c : cvec) {
			rawArray[i++] = c.x;
			rawArray[i++] = c.y;
		}

		return rawArray;
	}

	/**
	 * Save game state so that the user does not lose anything if the game
	 * process is killed while we are in the background.
	 * 
	 * @return a Bundle with this view's state
	 */
	public Bundle saveState() {
		Bundle map = new Bundle();

		map.putIntArray("mAList", coordArrayListToArray(mAList));
		map.putInt("mDirection", Integer.valueOf(mDirection));
		map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
		map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		map.putLong("mScore", Long.valueOf(mScore));
		map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

		return map;
	}

	/**
	 * Given a flattened array of ordinate pairs, we reconstitute them into a
	 * ArrayList of Coordinate objects
	 * 
	 * @param rawArray
	 *            : [x1,y1,x2,y2,...]
	 * @return a ArrayList of Coordinates
	 */
	private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

		int coordCount = rawArray.length;
		for (int index = 0; index < coordCount; index += 2) {
			Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(c);
		}
		return coordArrayList;
	}

	/**
	 * Restore game state if our process is being relaunched
	 * 
	 * @param icicle
	 *            a Bundle containing the game state
	 */
	public void restoreState(Bundle icicle) {
		setMode(PAUSE);

		mAList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
		mDirection = icicle.getInt("mDirection");
		mNextDirection = icicle.getInt("mNextDirection");
		mMoveDelay = icicle.getLong("mMoveDelay");
		mScore = icicle.getLong("mScore");
		mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
	}

	public void updateLabels() {

		mscoreText.setText("Sc: " + mScore);
		mscoreText.setVisibility(View.VISIBLE);

		mliveText.setText("Liv: " + mLives);
		mliveText.setVisibility(View.VISIBLE);

		mlevelText.setText("Lev: " + mCurrentLevel);
		mlevelText.setVisibility(View.VISIBLE);

	}

	public void moveSnake(int direction) {

		if (direction == Snake.MOVE_UP) {

			if (mMode == READY | mMode == LOSE) {
				/*
				 * At the beginning of the game, or the end of a previous one,
				 * we should start a new game if UP key is clicked.
				 */
				initNewGame();
				setMode(RUNNING);
				update();
				return;
			}

			if (mMode == PAUSE) {
				/*
				 * If the game is merely paused, we should just continue where
				 * we left off.
				 */
				setMode(RUNNING);
				update();
				return;
			}

			return;
		}

		
		if (direction == Snake.MOVE_LEFT) {

			Log.i("snakemoved1 left button pressed",
					"snakemoved1 left button pressed");
			if (mDirection == NORTH) {
				moveSnake1(WEST);
				return;
			} else if (mDirection == WEST) {
				moveSnake1(SOUTH);
				return;
			} else if (mDirection == SOUTH) {
				moveSnake1(EAST);
				return;
			} else {
				moveSnake1(NORTH);
				return;
			}
		} else {

			Log.i("snakemoved1 right button pressed",
					"snakemoved1 right button pressed");
			if (mDirection == NORTH) {
				moveSnake1(EAST);
				return;
			} else if (mDirection == EAST) {
				moveSnake1(SOUTH);
				return;
			} else if (mDirection == SOUTH) {
				moveSnake1(WEST);
				return;
			} else {
				moveSnake1(NORTH);
				return;
			}

		}

	}

	/**
	 * Handles snake movement triggers from Snake Activity and moves the snake
	 * accordingly. Ignore events that would cause the snake to immediately turn
	 * back on itself.
	 * 
	 * @param direction
	 *            The desired direction of movement
	 */
	private void moveSnake1(int direction) {

		if (direction == NORTH) {
			mNextDirection = NORTH;
			return;
		} else if (direction == SOUTH) {
			mNextDirection = SOUTH;
			return;
		} else if (direction == WEST) {
			mNextDirection = WEST;
			return;
		} else {
			mNextDirection = EAST;
			return;
		}

	}

	/**
	 * Sets the Dependent views that will be used to give information (such as
	 * "Game Over" to the user and also to handle touch events for making
	 * movements
	 * 
	 * @param newView
	 */
	public void setDependentViews(TextView scoreView, TextView liveView,
			TextView levelView, TextView msgView, View arrowView,
			View backgroundView, TextView highScoreView) {
		mscoreText = scoreView;
		mliveText = liveView;
		mlevelText = levelView;
		mStatusText = msgView;
		mArrowsView = arrowView;
		mBackgroundView = backgroundView;
		mHighScoreView = highScoreView;
	}

	/**
	 * Updates the current mode of the application (RUNNING or PAUSED or the
	 * like) as well as sets the visibility of textview for notification
	 * 
	 * @param newMode
	 */
	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;

		if (newMode == RUNNING && oldMode != RUNNING) {
			// hide the game instructions
			mStatusText.setVisibility(View.INVISIBLE);
			mBackgroundView.setVisibility(View.INVISIBLE);
			update();
			// make the background and arrows visible as soon the snake starts
			// moving
			mArrowsView.setVisibility(View.VISIBLE);
			return;
		}

		Resources res = getContext().getResources();
		CharSequence str = "";
		if (newMode == PAUSE) {
			mArrowsView.setVisibility(View.GONE);
			str = res.getText(R.string.mode_pause);

		}
		if (newMode == READY) {
			mArrowsView.setVisibility(View.GONE);
			str = res.getText(R.string.mode_ready);
			// TextView highScore = (TextView) findViewById(R.id.HigestScore);

		}
		// TODO modify WIN - show same level if lives exist
		if (newMode == LOSE) {
			mArrowsView.setVisibility(View.GONE);
			Log.i("current mscore when lose ", " mscore LOSE " + mScore);
			mSnakeTrail.clear();
			str = res.getString(R.string.mode_winsame, mLives);
			if (mCurrentLevel < 0) {

				newMode = GAMEOVER;
				mMode = GAMEOVER;
			}
			mBackgroundView.setVisibility(View.VISIBLE);
			mBackgroundView.bringToFront();
			mStatusText.bringToFront();

		}

		if (newMode == WIN) {
			mArrowsView.setVisibility(View.GONE);
			str = res.getString(R.string.mode_win, mLives);
			mCurrentLevel++;
			mBackgroundView.setVisibility(View.VISIBLE);
			mBackgroundView.bringToFront();
			mStatusText.bringToFront();
			mMode = READY;
		}

		if (newMode == GAMEOVER) {
			Log.i("current mscore when GAMEOVER ", "mscore GAMEOVER " + mScore);
			mCurrentLevel = 0;
			mLives = 3;
			setHighestScore(mScore);
			mScore = 0;
			str = res.getString(R.string.mode_lose, mScore);
			mArrowsView.setVisibility(View.GONE);
			mBackgroundView.setVisibility(View.VISIBLE);
			Log.i("current mscore when GAMEOVER ",
					"mscore GAMEOVER " + "Highest Score:"
							+ Integer.valueOf(getHighestScoreFromDB()));
			mHighScoreView.setText("Highest Score:"
					+ Integer.valueOf(getHighestScoreFromDB()));
			// mMode = READY;
			mStatusText.setText(str);
			mStatusText.setVisibility(View.VISIBLE);
			mBackgroundView.setVisibility(View.VISIBLE);
			mBackgroundView.bringToFront();
			mStatusText.bringToFront();
			mHighScoreView.bringToFront();
			mSnakeTrail.clear();
			// try { Thread.sleep(3000); Log.i("mscore Sleeping done",
			// "mscore Sleeping done");}
			// catch (InterruptedException ex) {
			// Log.i("","YourApplicationName.toString()"); }
			return;

		}

		mStatusText.setText(str);
		mStatusText.setVisibility(View.VISIBLE);

		mscoreText.setText("Sc: " + mScore);
		mscoreText.setVisibility(View.VISIBLE);

		mliveText.setText("Liv: " + mLives);
		mliveText.setVisibility(View.VISIBLE);
		setHighestScore(mScore);
		mlevelText.setText("Lev: " + mCurrentLevel);
		mlevelText.setVisibility(View.VISIBLE);
		mHighScoreView.setText(String.valueOf(getHighestScoreFromDB()));
	}

	private int getHighestScoreFromDB() {
		initDB();
		MyDb snake = new MyDb(getContext());
		SQLiteDatabase db = snake.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from hw4 where name = 'player 1'", null);
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
		MyDb snake = new MyDb(getContext());
		SQLiteDatabase db = snake.getWritableDatabase();
		// create table at first time
		String sql = "create table if not exists hw4 (name varchar(50), speed integer, HIGH_SCORE integer, score intger)";
		db.execSQL(sql);

		// db.execSQL("delete from hw4 where name = 'player 1'");
		Cursor rawQuery = db.rawQuery(
				"select * from hw4 where name = 'player 1'", null);

		if (rawQuery.getCount() == 0) {
			db.execSQL("insert into hw4 (name, HIGH_SCORE) values('player 1', 0)");
		}
		rawQuery.close();
		db.close();
	}

	private void setHighestScore(long score) {
		initDB();
		int scoreInDb = getHighestScoreFromDB();

		Log.i("Existing DB Score is :", " " + scoreInDb);
		if (scoreInDb < score) {

			Log.i("Current score is the highest score :", " " + score);
			MyDb snake = new MyDb(getContext());
			SQLiteDatabase db = snake.getWritableDatabase();

			db.execSQL("update hw4 set HIGH_SCORE =" + mScore
					+ " where name = 'player 1'");
			db.close();
		}

	}

	/**
	 * @return the Game state as Running, Ready, Paused, Lose
	 */
	public int getGameState() {
		return mMode;
	}

	/**
	 * Selects a random location within the garden that is not currently covered
	 * by the snake. Currently _could_ go into an infinite loop if the snake
	 * currently fills the garden, but we'll leave discovery of this prize to a
	 * truly excellent snake-player.
	 */
//	private void addRandomApple() {
//		Coordinate newCoord = null;
//		boolean found = false;
//		while (!found) {
//			// Choose a new location
//			int newX = 1 + RNG.nextInt(mXTileCount - 2);
//			int newY = 1 + RNG.nextInt(mYTileCount - 2);
//			newCoord = new Coordinate(newX, newY);
//
//			// Make sure it's not already under the snake
//			boolean collision = false;
//			int snakelength = mSnakeTrail.size();
//			for (int index = 0; index < snakelength; index++) {
//				if (mSnakeTrail.get(index).equals(newCoord)) {
//					collision = true;
//				}
//			}
//			// if we're here and there's been no collision
//			found = !collision;
//		}
//		if (newCoord == null) {
//			Log.e(TAG, "Somehow ended up with a null newCoord!");
//		}
//		mAList.add(newCoord);
//	}

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's
	 * location.
	 */
	public void update() {
		if (mMode == RUNNING) {
			long now = System.currentTimeMillis();

			if (now - mLastMove > mMoveDelay) {
				//clearTiles();
//				if(mwall != null)
//					mwall.reset();
				updateWalls();
				
				mLastMove = now;
			}
			mRedrawHandler.sleep(mMoveDelay);
		}

	}

	/**
	 * Draws some walls.
	 */
	private void updateWalls() {

		if(created)
			redrawWalls();
		else
			drawWallsLevel1();
			
	}

	private void redrawWalls() {
		
		for (int j = 3; j < mYTileCount - 2; j++) {
			for (int i = 2; i < mXTileCount - 2 ; i++) {
				
				int rand = mwall.getWall(i, j);
				if(rand == 1)
					setTile(GREEN_STAR, i, j);
				else if(rand == 2)
					setTile(RED_STAR, i, j);
				else if(rand == 3)
					setTile(YELLOW_STAR, i, j);
				else
			//		setTile(BLUE_STAR, i, j);
					setTile(GREEN_STAR, i, j);
			}
		}
		
	}

	private void drawWallsLevel1() {
		// Draw vertical line down the middle
		created = true;
		for (int j = 3; j < mYTileCount - 2; j++) {
			for (int i = 2; i < mXTileCount - 2 ; i++) {
				
				int rand = (int)(Math.random() * 3)+ 1;
				mwall.addWall(i, j, rand);
				if(rand == 1)
					setTile(GREEN_STAR, i, j);
				else if(rand == 2)
					setTile(RED_STAR, i, j);
				else if(rand == 3)
					setTile(YELLOW_STAR, i, j);
				else {
			//		setTile(BLUE_STAR, i, j);
					setTile(GREEN_STAR, i, j);}
				
			}
		}
	}

	private void checkWallsLevel1(Coordinate newHead) {
		return;
	}

	private void checkWallsLevel2(Coordinate newHead) {
		return;
	}

	/**
	 * Simple class containing two integer values and a comparison function.
	 * There's probably something I should use instead, but this was quick and
	 * easy to build.
	 */
	private class Coordinate {
		public int x;
		public int y;

		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}

		public boolean equals(Coordinate other) {
			if (x == other.x && y == other.y) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}

}