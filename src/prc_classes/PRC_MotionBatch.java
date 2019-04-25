package prc_classes;

import com.kuka.roboticsAPI.motionModel.MotionBatch;

public class PRC_MotionBatch {
	public MotionBatch motionBatch;
	public boolean isCartesian;
	
	public String ToString(){
		return new String("MotionBatch " + motionBatch.toString());
	}
}
