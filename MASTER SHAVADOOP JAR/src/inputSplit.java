import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;



public class inputSplit {

	private String file;
	private String registeringPath;
	
	public inputSplit(String file, String registeringPath){
		this.file = file;
		this.registeringPath = registeringPath;
		}
		
	public void run(){
        // Lire et afficher le fichier INPUT et créer les fichier Sx
        Path fileInput = Paths.get("/cal/homes/ebenoit/workspace/MASTER SHAVADOOP JAR/" + file);
        Hashtable<String, Integer> S = new Hashtable<String, Integer>();
        try {
            List<String> listOfLines;
            listOfLines = Files.readAllLines(fileInput, Charset.forName("UTF-8"));
            
            // Crée un fichier par line
            for (int i = 0; i < listOfLines.size(); i += 1){
                String Sx = "S" + i + ".txt";
                i ++;
                S.put(Sx, i);
            	try (PrintWriter out = new PrintWriter("/cal/homes/ebenoit/workspace/MASTER SHAVADOOP JAR/" + Sx);){
                    out.println(listOfLines.get(i));
                    out.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
            }
        	} catch (IOException e1) {
				e1.printStackTrace();
			} 			
	}
}
	        