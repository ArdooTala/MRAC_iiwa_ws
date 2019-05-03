package prc_classes;
import com.kuka.roboticsAPI.geometricModel.Frame;


public class PRC_SPLPart {
	public Frame frame = null;
	public double vel = 0;
	public double e1val = 0;
	
	public String ToString(){
		return new String("SPL Movement to X " + Double.toString(frame.getX()) + "Y"  + Double.toString(frame.getY())  + "Z"  + Double.toString(frame.getZ()) + "A"  + Double.toString(frame.getAlphaRad()) + "B"  + Double.toString(frame.getBetaRad())+ "C"  + Double.toString(frame.getGammaRad()) + " with " + Double.toString(vel) + "mm/sec speed");
	}
}
