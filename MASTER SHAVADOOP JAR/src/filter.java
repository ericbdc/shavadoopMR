import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;


public class filter implements Runnable{

	private final InputStream inputStream;
    ArrayBlockingQueue<String> output;

    // Mise dans le buffer du stream d'entrée (fichier txt)
    filter(InputStream inputStream, ArrayBlockingQueue<String> output) {
        this.inputStream = inputStream;
        this.output = output;
    }

    private BufferedReader getBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    // Test de lecture ligne par ligne du stream d'entrée bufferisé et création 
    // d'une queue contenant toutes les lignes d'entrée non vides
    @Override
    public void run() {
        BufferedReader br = getBufferedReader(inputStream);
        String ligne = "";
        try {
            while ((ligne = br.readLine()) != null) {
            	// Filtre appliqué
            	ligne.trim().toLowerCase();
                output.put(ligne);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
	
	
}
