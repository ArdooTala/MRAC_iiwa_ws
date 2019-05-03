package prc_classes;
import com.kuka.roboticsAPI.geometricModel.Frame;


public class PRC_LINCompMove {
	public Frame frame = null;
	public double vel = 0;
	public String interpolation = "";
	public double e1val = 0;
	public double stiffX = 0;
	public double stiffY = 0;
	public double stiffZ = 0;
	public double addFX = 0;
	public double addFY = 0;
	public double addFZ = 0;
	
	public String ToString(){
		return new String("LIN Movement to X " + Double.toString(frame.getX()) + "Y"  + Double.toString(frame.getY())  + "Z"  + Double.toString(frame.getZ()) + "A"  + Double.toString(frame.getAlphaRad()) + "B"  + Double.toString(frame.getBetaRad())+ "C"  + Double.toString(frame.getGammaRad()) + " with " + Double.toString(vel) + "mm/sec speed");
	}
}
