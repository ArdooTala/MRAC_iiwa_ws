package prc_classes;
import com.kuka.roboticsAPI.geometricModel.Frame;


public class PRC_PTPMove {
	public Frame frame = null;
	public double vel = 0;
	public String interpolation = "";
	public String status = "";
	public String turn = "";
	public double e1val = 0;
	
	public String ToString(){
		return new String("PTP Movement to X " + Double.toString(frame.getX()) + "Y"  + Double.toString(frame.getY())  + "Z"  + Double.toString(frame.getZ()) + "A"  + Double.toString(frame.getAlphaRad()) + "B"  + Double.toString(frame.getBetaRad())+ "C"  + Double.toString(frame.getGammaRad()) + " with " + Double.toString(vel * 100) + "% speed");
	}
}
