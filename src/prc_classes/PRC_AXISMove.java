package prc_classes;
import com.kuka.roboticsAPI.deviceModel.JointPosition;


public class PRC_AXISMove {
	public JointPosition axispos = null;
	public double vel = 0;
	public String interpolation = "";
	
	public String ToString(){
		return new String("Axis Movement to A1 " + Double.toString(axispos.get(0)) + " A2 " + Double.toString(axispos.get(1))+ " A3 " + Double.toString(axispos.get(2))+ " A4 " + Double.toString(axispos.get(3))+ " A5 " + Double.toString(axispos.get(4))+ " A6 " + Double.toString(axispos.get(5))+ " A7 " + Double.toString(axispos.get(6)) + " with " + Double.toString(vel * 100) + "% speed");
	}
}
