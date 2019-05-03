package prc_classes;

public class PRC_AnOut {
	public int num = 0;
	public double state = 0.0;
	public boolean cont = false;
	
	public String ToString(){
		return new String("Analog Output " + Integer.toString(num) + " set to " + Double.toString(state));
	}
}
