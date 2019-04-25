package prc_classes;

public class PRC_KMRMove {
	public double kmrx = 0;
	public double kmry = 0;
	public double kmrtheta = 0;

	
	public String ToString(){
		return new String("KMR moving to X" + Double.toString(kmrx) + " Y" + Double.toString(kmry) + " Theta" + Double.toString(kmrtheta));
	}
}
