import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class SshConnectionManager extends Thread {
	
	private String machine;
	private int timeout;
	private ArrayBlockingQueue<String> standard_output = new ArrayBlockingQueue<String>(1000);
	private ArrayBlockingQueue<String> error_output = new ArrayBlockingQueue<String>(1000);
	private boolean connectionOK = false;
	
	
	// Obtention du nom de la machine
	public SshConnectionManager(String machine, int timeout){
		this.machine = machine;
		this.timeout = timeout;
	}

	public String getMachine() {
		return machine;
	}

	
	// Création du message de sortie lorsque la connection a été établie avec succès
	public void setConnectionOK(boolean connectionOK) {
		this.connectionOK = connectionOK;
	}

	public boolean isConnectionOK() {
		return connectionOK;
	}
	
	public void display(String texte){
		System.out.println("[TestConnectionSSH " + machine + "] " + texte);
	}
	
	
	// Processus de test pour se connecter à une machine
	public void run() {

		try {
			String[] resource = {"ssh", "-o StrictHostKeyChecking=no", machine, "echo OK"};
			ProcessBuilder process = new ProcessBuilder(resource);
			Process p = process.start();
			
			// Appel de la class readOutputFlow qui bufferise le stream d'entrée et résulte en une queue de lecture
			readOutputFlow outputFlow = new readOutputFlow(p.getInputStream(), standard_output);
			readOutputFlow errorFlow = new readOutputFlow(p.getErrorStream(), error_output);
			
			new Thread(outputFlow).start();
            new Thread(errorFlow).start();
            
            
            
            // Gestion des sorties en cas de succès, on laisse maximum timeout=7s pour établir la connection
            String s = standard_output.poll(timeout, TimeUnit.SECONDS);
            
            while(s!=null && !s.equals("ENDOFTHREAD")){
            	display(s);
            	if(s.contains("OK")){
            		connectionOK = true;
            	}
            	s = standard_output.poll(timeout, TimeUnit.SECONDS);
            }
            
            
            
            // Gestion des sorties en cas d'erreur/ timeout dépassé !
            s = null;
            s = error_output.poll(timeout, TimeUnit.SECONDS);
            
            while(s!=null && !s.equals("ENDOFTHREAD")){
            	display(s);
            	s = error_output.poll(timeout, TimeUnit.SECONDS);
            }
		
        // Autres exceptions
		} catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
			

	}
}



