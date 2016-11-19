import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

	public static void main(String args[]) throws InterruptedException, IOException {


		String path = "/cal/homes/ebenoit/workspace/MASTER SHAVADOOP JAR/";
		String fileToWordCount = "forestier_mayotte.txt";
		String fileWithPotentialMachines = "addresses.txt";
		
		
		DecimalFormat df0 = new DecimalFormat("00");
		DecimalFormat df3 = new DecimalFormat("000");
		

		long startTimeT = System.currentTimeMillis();
		
	///////////////////////////////////////
	// Analyse des machines disponibles ///
	///////////////////////////////////////		
		
		System.out.println("++++++ Begin connection tests ++++++\n");
		long startTimeP = System.currentTimeMillis();
		
		// Teste des conections aux machines
    	List<String> machines;
    	ArrayList<SshConnectionManager> listeTests = new ArrayList<SshConnectionManager>();

    	Path filein = Paths.get(path + fileWithPotentialMachines);
    	
    	try {
    		machines = Files.readAllLines(filein, Charset.forName("UTF-8"));
	    	
    		for (String machine : machines) {
    			// On teste la connection SSH pendant 7 secondes maximum grâce à la class SshConnectionManager
    			SshConnectionManager test = new SshConnectionManager(machine, 7);
    			test.start();
    			listeTests.add(test);
    		}
    	} catch (IOException e1) {
    		e1.printStackTrace();
    	}

    	
    	// Récupération des machines où la connection a réussi
    	ArrayList<String> liste_machines_ok = new ArrayList<String>();

    	for (SshConnectionManager test : listeTests) {
    		try {
    			test.join();// on attend la fin du test
    			if (test.isConnectionOK()) {
    				liste_machines_ok.add(test.getMachine());
    			}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}

    	// Write list machines OK in a file
    	Path file = Paths.get(path + "liste_machines_OK.txt"); // Remarque: chemin peut être supprimé
		try {
			Files.write(file, liste_machines_ok, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
				
    	System.out.println("\nLes " + liste_machines_ok.size() + " machines pour lesquelles la connection a réussi sont:\n" + liste_machines_ok);
    	long endTimeP = System.currentTimeMillis();
    	System.out.println("\n++++++ All connection tests completed  and file with working machines list is available  ++++++\n\n");
	
    	
    	
   
		
    ///////////////////////////////////////
    ///////////// Dictionaries ////////////
    ///////////////////////////////////////
    	
    	// Filter
    	long startTimeF = System.currentTimeMillis();
    	//Path fileInputNotCleaned = Paths.get(path + fileToWordCount);
    	
    	File inputFile = new File(path + fileToWordCount);
    	File tempFile = new File(path + "inputCleaned.txt");

    	BufferedReader reader = new BufferedReader(new FileReader(inputFile));
    	BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

    	String lineToRemove = "";
    	List<String> itemToRemove = Arrays.asList("", "↬", "-", ":", "'", "@", "le", "la", "l'", "les", "on", "il", "elle", "de", "des", "que", "ou", "leur", "et", "qui");
    	String currentLine;

    	while((currentLine = reader.readLine()) != null) {
    		
    	    // trim newline when comparing with lineToRemove
    		String trimmedLine = currentLine.trim();
    		if(trimmedLine.equals(lineToRemove)) continue;
    		
    	    // clean trimmedLine when comparing items from trimmedLine to badItems from charToRemove
    	    String[] splitTrimmedLine = trimmedLine.toLowerCase().split(" ");
    	    final List<String> cleanedSplitTrimmedLine =  new ArrayList<String>();
    	    Collections.addAll(cleanedSplitTrimmedLine, splitTrimmedLine); 
    	    for (String badItem : itemToRemove){
    	    	cleanedSplitTrimmedLine.remove(badItem);
    	    }
    	    splitTrimmedLine = cleanedSplitTrimmedLine.toArray(new String[cleanedSplitTrimmedLine.size()]);
    	    String cleanedLine = "";
    	    for (String item : splitTrimmedLine){
    	    	cleanedLine = cleanedLine + " " + item.replace(".", "");
    	    }
    	    cleanedLine.trim();
    	    // Write cleanedLine in the cleaned input file
    	    writer.write(cleanedLine + System.getProperty("line.separator"));    
    	}
    	writer.close(); 
    	reader.close(); 
    	//boolean successful = tempFile.renameTo(inputFile);

    	long endTimeF = System.currentTimeMillis();
    	
    	
    	

    	// Split
    	long startTimeS = System.currentTimeMillis();
    	
		// Input hash into line blocks - Dictionary SPLIT Sx
		// Lire et afficher le fichier INPUT et créer les fichier Sx
        Path fileInputCleaned = Paths.get(path + "inputCleaned.txt");
        Hashtable<String, Integer> S = new Hashtable<String, Integer>();
        int Sx_Lim = 0;
        
        try {
        	System.out.println("++++++ Started launching split mapping threads computations ++++++\n");
        	
            List<String> listOfLines;
            listOfLines = Files.readAllLines(fileInputCleaned, Charset.forName("UTF-8"));
            
            // Crée un fichier par line
            for (int j = 0; j < listOfLines.size(); j += 1){
            	if (listOfLines.get(j) != null){
	                String Sx = "S" + j + ".txt";
	                S.put(Sx, j);
	                Sx_Lim ++;
	            	try (PrintWriter out = new PrintWriter(path + Sx);){ 
                    	out.println(listOfLines.get(j));
	                    out.close();
	                } catch (IOException e) {
	                        e.printStackTrace();
	                }
            	} else{	
                	j++;
                }
            }
        	} catch (IOException e1) {
				e1.printStackTrace();
			} 			

        long endTimeS = System.currentTimeMillis();
        
        ///////////////////////////////////////
        // Lancement des jobs sur les slaves //
        ///////////////////////////////////////
	

		
		
    	// MAP & Dictionary UMx - machine  
        // Functionning like compute Sx -> UMx 
		// args[0] == 'compute Sx -> UMx'
		String args01 = "map";
		
        Hashtable<String, String> UMmach = new Hashtable<String, String>();
        Hashtable<String, ArrayList<String>> keyUMs = new Hashtable<String, ArrayList<String>>();
		long startTimeM = System.currentTimeMillis();
        
    	// Launcher of jobs on machines among connection OK
		String machineM = null;
		int i = 0;
		ArrayList<slaveManager> List_slaves = new ArrayList<slaveManager>();
		

		while (i < Sx_Lim) {
			// Gestion du nb de jobs à lancer vs nb de machines disponibles
			int j = i  %  liste_machines_ok.size();
			machineM = liste_machines_ok.get(j);
			
			// Build dictionary UMx - machine
			String UMx = "UM" + i;
			UMmach.put(UMx, machineM);
			
			String command = "cd workspace;java -jar SLAVESHAVADOOP.jar " + args01 + " " + machineM + " S" + i + ".txt";
	        if (machineM != null) {
	        	slaveManager slave = new slaveManager(machineM, command, 10000);
				slave.start();
				List_slaves.add(slave);
	        }
	        i++;
		}
		
	    
		// Listener of working machines
		for (slaveManager slave : List_slaves){
			
			// We wait for all threads and when finished before timeout passed, then print job is finished
			//synchronized (slave) {
			
		    slave.join(); // wait() possible
		    		    
		    // Get back keys from Slaves and build key - UMx Dictionary
		    ArrayList<String> jobDone = slave.getSlaveAnswer();  
		    
			for (String res : jobDone){
				String mach = slave.getMachine();
				String val1 = new String();
				
				//
				Set<String> UMmach_keys = UMmach.keySet(); 
				for (String um : UMmach_keys){
					if (UMmach.get(um) == mach){
						val1 = um;
					}
				}
				
				if (keyUMs.containsKey(res)){
					if (!keyUMs.get(res).contains(val1)){
						keyUMs.get(res).add(val1);
						keyUMs.put(res, keyUMs.get(res));
					}
				} else{ 
					ArrayList<String> val2 = new ArrayList<String>();
					val2.add(val1);
					keyUMs.put(res, val2);
				}
			}
			
			System.out.println("[Machine " + slave.getMachine() + "] Thread completed");
		}

		// Print UMx - machine  & word - UMxs dictionaries
		System.out.println("\nUMx - machine tracker is:\n" + UMmach);
		System.out.println("\nkeys - UMx tracker is:\n" + keyUMs);
		
		
		// Print that all is finished
		long endTimeM = System.currentTimeMillis();	
		System.out.println("\n++++++ All mapping threads computations completed ++++++\n");
		
		
		
		
		
		// Print that reducing has begun
		System.out.println("\n++++++ Started launching shuffling and reducing threads computations++++++\n");
		long startTimeSh = System.currentTimeMillis();
		
		// SHUFFLING MAP & Dictionary SMx - machine  	
		// Functionning like compute Sx -> UMx 
		// args[0] == 'compute Sx -> UMx'
		String args02 = "reduce";
		

		
		
        Hashtable<String, String> RMmach = new Hashtable<String, String>();
        Hashtable<String, String> RMkey = new Hashtable<String, String>();

        //Hashtable<String, ArrayList<String>> keyUMs = new Hashtable<String, ArrayList<String>>();
        
    	// Launch jobs on machines among connection OK
		String machineR = null;
		int j = 0;
		Set<String> List_keys = keyUMs.keySet();
		ArrayList<slaveManager> List_slavesR = new ArrayList<slaveManager>();
		
		
		for (i=0; i < List_keys.size(); i++){

			Object key = List_keys.toArray()[i];
			
			// Gestion du nb de jobs à lancer vs nb de machines disponibles
			j = i %  liste_machines_ok.size();
			machineR = liste_machines_ok.get(j);
				
			// Build SMx parameter and RMx - machine dictionary and RMx - key
			String SMx = "SM" + i;
			String RMx = "RM" + i;
			RMmach.put(RMx, machineR);
			RMkey.put(RMx, key.toString());
				
			String command = "cd workspace;java -jar SLAVESHAVADOOP.jar " + args02 + " " + machineR + " " + key + " " + SMx + " " 
					+ keyUMs.get(key).toString().replace("[", "").replace("]", "").replaceAll(", ", "/");
			System.out.println(command);
				
	        if (machineR != null) {
	        	slaveManager slave = new slaveManager(machineR, command, 10000);
				slave.start();
				List_slavesR.add(slave);
	        } 
    
		} 

		// We built RMx - machine dictionary
		System.out.println("\nRMx - machine tracker is:\n" + RMmach);
		System.out.println("\nRMx - key tracker is:\n" + RMkey + "\n");
		
		long endTimeSh = System.currentTimeMillis();
		
		
		long startTimeR = System.currentTimeMillis();
		
		// Listener for answers
		Hashtable<String, Integer> RMcount = new Hashtable<String, Integer>();
		for (slaveManager slave : List_slavesR){
			
			// We wait for all threads and when finished before timeout passed, then print job is finished
			//synchronized (slave) {
			
		    slave.join(); // wait() possible
		    		    
		    // Get back counts from Slaves and build RMx - count Dictionary
		    ArrayList<String> jobDone = slave.getSlaveAnswer();  
		    
			for (String res : jobDone){
				if (res == null) continue;
				String mach = slave.getMachine();
				String retrievedKey = new String();
				Set<String> RMmach_keys = RMmach.keySet(); 
				// Retrieve RM number then key
				for (String rm : RMmach_keys){
					if (RMmach.get(rm) == mach){
						retrievedKey = RMkey.get(rm);
					}
				}
				
				if (!RMcount.containsKey(res)){
					RMcount.put(retrievedKey, Integer.parseInt(res));
				}
			}
		}
		
		// Print that all is finished
		long endTimeR = System.currentTimeMillis();	
		System.out.println("\n++++++ All threads reducing computations completed ++++++\n");
		
		
		
		
		
		// Sort results by counts
		//public static void sortValue(Hashtable<?, Integer> t) {
			ArrayList<Map.Entry<?, Integer>> sortedCounts = new ArrayList<Entry<?, Integer>>(RMcount.entrySet());
			Collections.sort(sortedCounts, new Comparator<Map.Entry<?, Integer>>(){

        	public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
        		return o1.getValue().compareTo(o2.getValue());
        }});

        System.out.println(sortedCounts);
		//}
		
		
		// Print result in a final file
		try (PrintWriter outputTowardsResult = new PrintWriter(path + "Result.txt");){
			outputTowardsResult.println(RMcount);
		}
		
		// Print final dictionary word - count
		System.out.println("Wordcount result is:\n" + RMcount);

		// SMx -> slave va lire les fichiers umx d'entrée et affiche ligne par ligne les occurences de ce mot dans SMx.txt
		// RMx va lire SMx et compte occurence du mot et renvoie au master le nb final dans un ArrayList<String>
	
		long endTimeT = System.currentTimeMillis();
		
		// Timing 
		System.out.println("\nTotal execution time Preprocessing: " + df0.format((endTimeP - startTimeP) / 1000) + " s " + df3.format((endTimeP - startTimeP) % 1000) + " ms");
		System.out.println("Total execution time Filtering: " + df0.format((endTimeF - startTimeF) / 1000) + " s " + df3.format((endTimeF - startTimeF) % 1000) + " ms");
		System.out.println("Total execution time Splitting: " + df0.format((endTimeS - startTimeS) / 1000) + " s " + df3.format((endTimeS - startTimeS) % 1000) + " ms");
		System.out.println("Total execution time Mapping: " + df0.format((endTimeM - startTimeM) / 1000) + " s " + df3.format((endTimeM - startTimeM) % 1000) + " ms");
		System.out.println("Total execution time Shuffling: " + df0.format((endTimeSh - startTimeSh) / 1000) + " s " + df3.format((endTimeSh - startTimeSh) % 1000) + " ms");
		System.out.println("Total execution time Reducing: " + df0.format((endTimeR - startTimeR) / 1000) + " s " + df3.format((endTimeR - startTimeR) % 1000) + " ms");
		System.out.println("Total execution time TOTAL: " + df0.format((endTimeT - startTimeT) / 1000) + " s " + df3.format((endTimeT - startTimeT) % 1000) + " ms");

	}
}
