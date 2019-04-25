package prc_classes;

public class PRC_DigOut {
	public int num = 0;
	public boolean state = false;
	public boolean cont = false;
	
	public String ToString(){
		return new String("Digital Output " + Integer.toString(num) + " set to " + Boolean.toString(state));
	}
}
