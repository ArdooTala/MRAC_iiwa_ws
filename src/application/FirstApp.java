package application;


import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.kuka.generated.ioAccess.BeckhoffIOIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.conditionModel.ICallbackAction;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.executionModel.IFiredTriggerInfo;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.CoordinateAxis;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;

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
public class FirstApp extends RoboticsAPIApplication {
	@Inject
	private LBR lBR_iiwa_14_R820_1;
	private Tool tool;
	private ObjectFrame actTCP;
	private CartesianImpedanceControlMode soft;
	private BeckhoffIOIOGroup ios;


	@Override
	public void initialize() {
		// initialize your application here
	}

	@Override
	public void run() {
		
		int sel = getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION, "What to do?", "Do Something", "Do Something Else", "Do Something Different");

				switch (sel) {
				case 0:
				//do
				case 1:
				//do
				case 2:
				//do
				default:
				//do default
				}
		
		ios = new BeckhoffIOIOGroup((Controller) getContext().getControllers().toArray()[0]);
		
		ICallbackAction getPositionAction = new ICallbackAction() {
			@Override
			public void onTriggerFired(IFiredTriggerInfo triggerinformation) {
				ios.setOut1(true);
				ios.setOut2(false);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ios.setOut1(false);
				ios.setOut2(true);
				
			}
			} ;


		
		soft = new CartesianImpedanceControlMode();
		soft.parametrize(CartDOF.ALL).setDamping(.7);
		soft.parametrize(CartDOF.ROT).setStiffness(100);
		soft.parametrize(CartDOF.TRANSL).setStiffness(600);

		
		// your application execution starts here
		lBR_iiwa_14_R820_1.move(ptp(Math.toRadians(10),0,0,0,0,0,0));
		
		tool = createFromTemplate("IAACGripper");
		tool.attachTo(lBR_iiwa_14_R820_1.getFlange());
		actTCP = tool.getFrame("/TCP");
		
		actTCP.move(ptp(getApplicationData().getFrame("/P1")).setJointVelocityRel(0.33));
		
		IMotionContainer handle = actTCP.moveAsync(lin(getApplicationData().getFrame("/P2")).setCartVelocity(100).setBlendingCart(4));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		handle.cancel();
		
		actTCP.move(ptp(getApplicationData().getFrame("/P4")).setJointVelocityRel(0.33));
		
		ForceCondition forceDetected = ForceCondition.createNormalForceCondition(actTCP, CoordinateAxis.Z, 5);

		//actTCP.move(lin(getApplicationData().getFrame("/P5")).setCartVelocity(6).breakWhen(forceDetected));
		actTCP.move(lin(getApplicationData().getFrame("/P5")).setCartVelocity(6).triggerWhen(forceDetected, getPositionAction));

		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		actTCP.move(lin(getApplicationData().getFrame("/P4")).setCartVelocity(6));
		
		double stiffness = getApplicationData().getProcessData("stiffness").getValue();

		
		//actTCP.moveAsync(lin(getApplicationData().getFrame("/P3")).setCartVelocity(10).setBlendingCart(4).setMode(soft));
	
		lBR_iiwa_14_R820_1.move(ptp(Math.toRadians(10),0,0,0,0,0,0).setJointVelocityRel(0.8).setJointAccelerationRel(0.15));

		
	}
	
}