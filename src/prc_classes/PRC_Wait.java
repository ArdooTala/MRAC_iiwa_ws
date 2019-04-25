package prc_classes;

public class PRC_Wait {
	public double time = 0;
	
	public String ToString(){
		return new String("Waiting for " + Double.toString(time) + " seconds");
	}
}
