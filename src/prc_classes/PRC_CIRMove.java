package prc_classes;
import com.kuka.roboticsAPI.geometricModel.Frame;


public class PRC_CIRMove {
	public Frame frame = null;
	public Frame auxframe = null;
	public double vel = 0;
	public String interpolation = "";
	public double e1val = 0;
	
	public String ToString(){
		return new String("CIR Movement to X " + Double.toString(frame.getX()) + "Y"  + Double.toString(frame.getY())  + "Z"  + Double.toString(frame.getZ()) + "A"  + Double.toString(frame.getAlphaRad()) + "B"  + Double.toString(frame.getBetaRad())+ "C"  + Double.toString(frame.getGammaRad()) + " through X " + Double.toString(auxframe.getX()) + "Y"  + Double.toString(auxframe.getY())  + "Z"  + Double.toString(auxframe.getZ()) + "A"  + Double.toString(auxframe.getAlphaRad()) + "B"  + Double.toString(auxframe.getBetaRad())+ "C"  + Double.toString(auxframe.getGammaRad()) + " with " + Double.toString(vel) + "mm/sec speed");
	}
}
