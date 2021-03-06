import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class slaveManager extends Thread{
	
	private String machine;
	private String command;
	private int timeout;
	private ArrayBlockingQueue<String> standard_output = new ArrayBlockingQueue<String>(1000);
	private ArrayBlockingQueue<String> error_output = new ArrayBlockingQueue<String>(1000);
	private ArrayList<String> backOutput = new ArrayList<String>();

	
	public ArrayList<String> getSlaveAnswer(){
		return backOutput;
		}

	
	public String getMachine(){
		return machine;
	}
	

	public slaveManager(String machine, String command, int timeout){
		this.machine = machine;
		this.timeout = timeout;
		this.command = command;
	}
	
	public void affiche(String texte){
		System.out.println("[Machine " + machine + "] " + texte);
	}
	
	public void run(){
	 try {
         String[] commande = {"ssh","-o StrictHostKeyChecking=no", machine, command};
            ProcessBuilder pb = new ProcessBuilder(commande);
            Process p = pb.start();
            readOutputFlow fluxSortie = new readOutputFlow(p.getInputStream(), standard_output);
            readOutputFlow fluxErreur = new readOutputFlow(p.getErrorStream(), error_output);
            
            new Thread(fluxSortie).start();
            new Thread(fluxErreur).start();
 
            String s = standard_output.poll(timeout, TimeUnit.SECONDS);
            while(s!=null && !s.equals("ENDOFTHREAD")){
            	affiche(s);
            	backOutput.add(s);
            	s = standard_output.poll(timeout, TimeUnit.SECONDS);
            }
            
            s = null;
            s = error_output.poll(timeout, TimeUnit.SECONDS);
            while(s!=null && !s.equals("ENDOFTHREAD")){
            	affiche(s);
            	s = error_output.poll(timeout, TimeUnit.SECONDS);
            }
         
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	 
	 
	}

}