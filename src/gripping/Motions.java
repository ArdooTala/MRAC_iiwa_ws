package gripping;


import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.CoordinateAxis;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

/**
 * Implementation of a robot application.
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a 
 * {@link RoboticsAPITask#run()} method, which will be called successively in 
 * the application lifecycle. The application will terminate automatically after 
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the 
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an 
 * exception is thrown during initialization or run. 
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the 
 * {@link RoboticsAPITask#dispose()} method.</b> 
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class Motions extends RoboticsAPIApplication {
	@Inject
	private LBR iiwa_14;
	
	//create robot and controller
	private Controller kuka_Sunrise_Cabinet_1;
	
	//add extra force
	private CartesianImpedanceControlMode force;
	
	//break action on force
	ForceCondition forceDetected;
	
	//create tool and TCP
	private Tool tool;
	private ObjectFrame actTCP, rlsTCP;

	
	@Override
	public void initialize() {
		// initialize your application here
		kuka_Sunrise_Cabinet_1 = (Controller) getContext().getControllers().toArray()[0];
		iiwa_14 = (LBR) kuka_Sunrise_Cabinet_1.getDevices().toArray()[0];
		
		//create tool and TCP
		tool = createFromTemplate("SCGripper");
		tool.attachTo(iiwa_14.getFlange());
		actTCP = tool.getFrame("/TCP");
		rlsTCP = tool.getFrame("/RealSense");
		
		//add extra force
		force = new CartesianImpedanceControlMode();
		force.parametrize(CartDOF.ALL).setDamping(.7);
		force.parametrize(CartDOF.X).setStiffness(3000).setAdditionalControlForce(10.0);
		force.parametrize(CartDOF.Y).setStiffness(3000);
		force.parametrize(CartDOF.Z).setStiffness(1000);
		force.parametrize(CartDOF.ROT).setStiffness(300);
		
		//break action on force
		forceDetected = ForceCondition.createNormalForceCondition(actTCP, CoordinateAxis.Z, 20);
		
	}

	@Override
	public void run() {
		// your application execution starts here
		iiwa_14.move(ptpHome());
		
		actTCP.move(lin(getApplicationData().getFrame("/Demo/startP")).breakWhen(forceDetected));
		
	}
	
	
	public void seek() {
		IMotionContainer scan;
		
		scan = actTCP.moveAsync(lin(getApplicationData().getFrame("/Scan_Pose/P1")));
		while (!scan.isFinished()){
			if (true) {
				scan.cancel();
			}
		}
		scan = actTCP.moveAsync(lin(getApplicationData().getFrame("/Scan_Pose/P2")));
		while (!scan.isFinished()){
			if (true) {
				scan.cancel();
			}
		}
	}
	
	
	
}