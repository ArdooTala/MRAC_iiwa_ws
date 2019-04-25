package prc_classes;


import com.kuka.roboticsAPI.motionModel.Spline;

public class PRC_SPLMove {
	public Spline spl;
	
	public String ToString(){
		return new String("Spline " + spl.toString());
	}
}
