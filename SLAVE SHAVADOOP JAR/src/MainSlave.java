import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;





public class MainSlave {
	
	/**
	 * 
	 * 
	 * @param args[0] mode "compute Sx -> UMx" or "compute UMx -> SMx"
	 * @param args[1] machine
	 * @param args[2] Sx.txt
	 * @throws InterruptedException
	 * @throws FileNotFoundException 
	 */
    
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		
		
		String path = "/cal/homes/ebenoit/workspace/MASTER SHAVADOOP JAR/";
		
		
		// Functionning like compute Sx -> UMx 
		// args[0] == 'compute Sx -> UMx'
		if (args[0].equals("map")){
			
			Path argPath = Paths.get(path + args[2]);
			try {
			    String[] listOfWords;
			    listOfWords = Files.readAllLines(argPath, Charset.forName("UTF-8"))
			    		.toString()
			    		.toLowerCase()
			    		.replace("[", "").replace("]", "")
			    		.trim()
			    		.split(" ");

			    // Crée un fichier UMx.txt par fichier d'entrée Sx.txt et construit le dictionnaire UMx - machine
			    int i = Integer.parseInt(args[2].replace("S", "").replace(".txt", ""));
			    String UMx = "UM" + i + ".txt"; 
			    
			    // Crée un fichier UMx-machine.txt contenant le résultat du map split
		    	try (PrintWriter outputTowardsUMx = new PrintWriter(path + UMx);){
		    		int j =0;
		    		// Build UMx files
					while (j  < listOfWords.length){
						outputTowardsUMx.println(listOfWords[j]);
			            // Print UMx distinct list of words
		            	System.out.println(listOfWords[j]);
			            j++;
			        } 
					outputTowardsUMx.close();
		        } catch (IOException e) {
	                e.printStackTrace();
			    }
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
		
		/**
		 * 
		 * 
		 * @param args[0] mode "reduce"
		 * @param args[1] machine
		 * @param args[2] Word
		 * @param args[3] SMx
		 * @param args[4] List of UMx sep="/"
		 * @throws InterruptedException
		 */
		
		// Functionning like compute UMx -> SMx
		// args[0] == 'compute UMx -> SMx'
		if (args[0].equals("reduce")){
			
			Hashtable<String, Integer> Final = new Hashtable<String, Integer>();
			String[] listOfUM = args[4].split("/");
			
			for (String UM : listOfUM){
				
				// Aller lire UM # x et compter les occurences de word
				Path argPath = Paths.get(path + UM + ".txt");
				try {
				    List<String> listOfWords = Files.readAllLines(argPath, Charset.forName("UTF-8"));
				    for (String word : listOfWords){
				    	word = word.toLowerCase();
				    	if (word.equalsIgnoreCase(args[2])){
				    		if (Final.keySet().contains(word) == true){
				    			int count = Final.get(word) + 1;
				    			Final.put(word, count);
				    		} else{
				    			int count = 1;
				    			Final.put(word, count);
				    		}
				    	}
				    }
   
				} catch (IOException e2) {
					e2.printStackTrace();
				} 
				}
			
			// Ecrire la réponse sur RMx et envoyer que job done au master
			System.out.println(Final.get(args[2].toLowerCase()));
			try (PrintWriter outputTowardsRMx = new PrintWriter(path + "RM" + args[3].toString().replace("SM", "") + ".txt");){
				outputTowardsRMx.println(args[2].toLowerCase() + " " + Final.get(args[2].toLowerCase()));
			}
		}
		
	// End Of Main and Class
	}

}
