package cs175.hw5.colorsquare;

/**
 * Walls class keep track of walls when sanke is moving
 * 
 * **/
public class Walls {

	private int[][][] mywallArray;

	public Walls(int xmax, int ymax) {
		mywallArray = new int[xmax][ymax][1];
		reset();
	}

	public int getXMax() {

		return mywallArray.length;

	}

	public int getYMax() {
		return mywallArray[0].length;
	}

	public boolean addWall(int x, int y, int color) {
		mywallArray[x][y][0] = color;
		return true;
	}

	public int getWall(int x, int y) {
		if (x < 0 || y < 0)
			return -1;

		if (x >= mywallArray.length || y >= mywallArray[0].length)
			return -1;

		return mywallArray[x][y][0];
			
	}

	public void reset() {
		for (int i = 0; i < mywallArray.length; i++) {
			for (int j = 0; j < mywallArray[i].length; j++) {
				mywallArray[i][j][0] = -1;
			}
		}
	}

}
