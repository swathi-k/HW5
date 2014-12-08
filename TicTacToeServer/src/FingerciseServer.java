import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FingerciseServer {
    private static JFrame frame;	// the server frame
    private static JTextArea messageArea;	// the server message area where messages are sent to the server
    private static final int PORT = 7890; // the port that the server listens on
    private static final String SUCCESS = "Okay";
    private static final String FAILURE = "Sorry";
    private static final String GREETINGS = "Fingercise Server";
    private static final String REGISTER = "register";
    private static final String RESULT = "result";
    private static final String STAT = "statistics";
    private static final String FILE_NAME = "database.dat";	// the file name to store data when the server shuts down
    private static ConcurrentHashMap<String, Integer> names = new ConcurrentHashMap<String, Integer>(); // a concurrent hash map that stores user names
    private static ConcurrentHashMap<String, ArrayList<ResultSet>> results = new ConcurrentHashMap<String, ArrayList<ResultSet>>();	// a concurrent hash map that stores users' information including score, high score, time and etc.

    /**
     * The main method of the server which just listens on a port and
     * creates handlers.
     */
    public static void main(String[] args) throws Exception {
	// set up the server frame and message area
	frame = new JFrame("Server");
	messageArea = new JTextArea(8, 40);
	frame.getContentPane().add(new JScrollPane(messageArea), "Center");
	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
	// set up actions when closing/shutting down the server frame
	frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // save data to FILE_NAME
        	saveData(names);
                saveData(results);
                frame.dispose();
                System.exit(0);
            }
            
            @SuppressWarnings("unchecked")
	    @Override
            public void windowOpened(WindowEvent e) {
        	// load data from FILE_NAME if FILE_NAME exists
        	File file = new File(FILE_NAME);
        	if (file.exists()) {
        	    names = (ConcurrentHashMap<String, Integer>) loadData();
        	    results = (ConcurrentHashMap<String, ArrayList<ResultSet>>) loadData();
        	}
               
            }
        });
	frame.setVisible(true);
	frame.pack();
	
        ServerSocket listener = new ServerSocket(PORT);
        
        messageArea.append("The fingercise server is running ...\n");
        
        // keep listening until there is a client
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
    
    /*
     * Saves serializable data to the FILE_NAME file
     * @param object - a serializable object
     */
    public static void saveData(Serializable object){
	try {
	    FileOutputStream saveFile = new FileOutputStream(FILE_NAME);
	    ObjectOutputStream out = new ObjectOutputStream(saveFile);
	    out.writeObject(object);
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /*
     * loads data from the FILE_NAME file
     * @return returns a serializable object
     */
    public static Object loadData(){
	Object result = null;
	try {
	    FileInputStream saveFile = new FileInputStream(FILE_NAME);
	    ObjectInputStream in = new ObjectInputStream(saveFile);
	    result = in.readObject();
	    in.close();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	return result;
    }

    /**
     * An inner handler class that extends thread. It is for dealing with 
     * an individual user and broadcasting messages.
     */
    private static class Handler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // GREETINGS
                out.println(GREETINGS);
                //writers.add(out);
                
                String input = in.readLine();
                String output = FAILURE;
                
                System.out.println(input);
                
                // if there's a request to process
                if (input != null) {
                    int delimiterIndex = input.indexOf(":");
                    String command = input.substring(0, delimiterIndex);
                    if (command.equals(REGISTER)) {	// register message
                	String player = input.substring(delimiterIndex + 1).toLowerCase();
                	if (!names.containsKey(player)) {
                	    names.put(player, 0);
                	    output = SUCCESS;
                	}
                    } else if (command.equals(RESULT)) {	// result message
                	String result = input.substring(delimiterIndex + 1).toLowerCase();
                	String[] rsArray = result.split("\\s+");
                	String name = rsArray[0] + " " + rsArray[1];
                	String game = rsArray[2];
                	Integer score = Integer.parseInt(rsArray[3]);
                	long time = System.currentTimeMillis();
                	TimedScore tmdScore = new TimedScore(score, time);
                	ResultSet set = new ResultSet(game);
                	// check if result is in the table. Take care of storing results
                	if (!results.containsKey(name)) {
                	    ArrayList<ResultSet> pResult = new ArrayList<ResultSet>();
                	    set.gameScores.add(tmdScore);
                	    pResult.add(set);
                	    results.put(name, pResult);
                	} else {
                	    ArrayList<ResultSet> playerResult = results.get(name);
                	    boolean found = false; // check if this is a result for oldGame
                	    int resultSetSize = playerResult.size();
                	    for (int i = 0; i < resultSetSize && !found; i++) {
                		ResultSet rs = playerResult.get(i);
                		if (rs.gameName.equals(game)) {
                		    found = true;
                		    // result for game that is already in the table
                		    // add new score to the gameScores
                		    rs.gameScores.add(tmdScore);
                		    output = SUCCESS; 
                		}
                	    }
                	    
                	    // result for new game that is not in the table
                	    if (!found) {
                		set.gameScores.add(tmdScore);
                		playerResult.add(set);
                		output = SUCCESS;
                	    }
                	}
                    } else if (command.equals(STAT)) {	// stat message
                	String playerName = input.substring(delimiterIndex + 1).toLowerCase().trim();
                	ArrayList<ResultSet> rsets = results.get(playerName);
                	long time = System.currentTimeMillis();
                	String statistics = "", gn = "";
                	Integer hs = -1, ohs = -1, oas = -1, ash = -1, asw = -1, asm = -1;
                	if (rsets != null) {
                	    for (ResultSet s : rsets) {
                		gn = s.gameName; // the game name
                		hs = Collections.max(s.gameScores).gameScore; // the highest score
                		ohs = overAllScore(s.gameName, "high"); // overall high score
                		oas = overAllScore(s.gameName, "average");
                		ash = userAverageScore(playerName, s.gameName, time, "hour");
                		asw = userAverageScore(playerName, s.gameName, time, "week");
                		asm = userAverageScore(playerName, s.gameName, time, "month");
                		statistics += gn + "\t" 
        		            	    + hs + "\t"
        		                    + ohs + "\t"
        		                    + oas + "\t"
        		                    + ash + "\t"
        		                    + asw + "\t"
        		                    + asm + "\t"
        		                    + "\n";
                	    }
                	} else {
                	    // no record in the table (first time player), but still displays overall records
                	    for (String key : results.keySet()) {
                		ArrayList<ResultSet> temp = results.get(key);
                		for (ResultSet t : temp) {
                		    gn = t.gameName;
                		    hs = 0;
                		    ohs = overAllScore(t.gameName, "high"); // overall high score
                		    oas = overAllScore(t.gameName, "average");
                		    ash = 0;
                		    asw = 0;
                		    asm = 0;
                		    statistics += gn + "\t" 
            		            	        + hs + "\t"
            		                        + ohs + "\t"
            		                        + oas + "\t"
            		                        + ash + "\t"
            		                        + asw + "\t"
            		                        + asm + "\t"
            		                        + "\n";
                		}
                		break; // print one set of records because don't want to create empty resultsets for new players
                	    }
                	}
                	
                	if (statistics.isEmpty()) // no record in the server
                	    statistics = "There's no record in the server yet because you are the first user of the app";
                	
                	output = statistics;
                    }
                    out.println(output);
                    messageArea.append(input + "\n");
                }
                
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        
        /*
         * Finds overall score based on category including overall high score and average high score
         * @param game - the game name
         * @param category - either high or average 
         * @return returns overall score
         */
        private int overAllScore(String game, String category) {
            int overAllHighest = Integer.MIN_VALUE;
            int sum = 0;
            for (String s : results.keySet()) {
        	ArrayList<ResultSet> rset = results.get(s);
        	for (ResultSet rs : rset) {
        	    if (rs.gameName.equals(game)) {
        		int userHighest = Collections.max(rs.gameScores).gameScore;
        		sum += userHighest;
        		if (overAllHighest < userHighest) 
        		    overAllHighest = userHighest;
        	    }
        	}
            }
            
            if (category.equals("high")) 
        	return overAllHighest;
            else // category.equals("average")
        	return sum / results.size();
        }
        
        /*
         * Finds the average score based on last hour, last week or last month
         * @param player - player name
         * @param game - game name
         * @param time - the time associated with the user score
         * @param within - either last hour, last week or last month
         * @return returns user average score
         */
        private int userAverageScore(String player, String game, long time, String within) {
            long hourInMillisecs = 0;
            
            if (within.equals("hour"))
        	hourInMillisecs = 3600000L; // 60*60*1000 = 1 hour
            else if (within.equals("week")) 
        	hourInMillisecs = 7 * 24 * 3600000L; // 1 week
            else // month
        	hourInMillisecs = 30 * 24 * 3600000L; // 1 month
            
            int sum = 0;
            int counter = 0;
            
            ArrayList<ResultSet> rset = results.get(player);
            for (ResultSet rs : rset) {
        	if (rs.gameName.equals(game)) {
        	    for (TimedScore ts : rs.gameScores) {
        		if ((time - ts.currentTime) <= hourInMillisecs) {
        		    sum += ts.gameScore;
        		    counter++;
        		}
        	    }
        	}
            }
            
            if (counter != 0)
        	return sum / counter;
            else 
        	return sum;
        }
    }
    
    /*
     * A private result set that implements serializable which 
     * holds game result.
     */
    private static class ResultSet implements Serializable {
	private static final long serialVersionUID = 1L;
	String gameName;
	ArrayList<TimedScore> gameScores;
	
	public ResultSet(String g) {
	    gameName = g;
	    gameScores = new ArrayList<TimedScore>();
	}
	
	
    }
    
    /*
     * A private timedscore that implements serializable which
     * holds score with time.
     */
    private static class TimedScore implements Serializable, Comparable<TimedScore> {
	private static final long serialVersionUID = 1L;
	Integer gameScore;
	Long currentTime;

	public TimedScore(Integer score, Long time) {
	    gameScore = score;
	    currentTime = time;
	}
	
	public int compareTo(TimedScore o) {
	    return gameScore - o.gameScore; // compare by game scores
	}
    }
}
