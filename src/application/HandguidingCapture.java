package application;


import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import prc_classes.PRC_CommandData;

import threads.UDPSender;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.JointImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;

import static com.kuka.roboticsAPI.motionModel.HRCMotions.*;

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
public class HandguidingCapture extends RoboticsAPIApplication {
	@Inject
	private LBR lBR_iiwa_14_R820_1;
	private Tool tool;
	private ObjectFrame actTCP;
	UDPSender udpsend;
	BlockingQueue<PRC_CommandData> UDPInput;

	@Override
	public void initialize() {
		// initialize your application here
		//lBR_iiwa_14_R820_1.setESMState("1");
	}

	@Override
	public void run() {
		
		//lBR_iiwa_14_R820_1.setESMState("1");
		lBR_iiwa_14_R820_1.move(ptpHome());
		
		tool = createFromTemplate("IAACGripper");
		tool.attachTo(lBR_iiwa_14_R820_1.getFlange());
		actTCP = tool.getFrame("/TCP");
		
		UDPInput = new LinkedBlockingQueue<PRC_CommandData>();
		
		try {
			udpsend = new UDPSender(InetAddress.getByName("171.31.1.149"), 49152, UDPInput);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (lBR_iiwa_14_R820_1 != null)
		{
			udpsend.robot = lBR_iiwa_14_R820_1;
			udpsend.toolframe = actTCP;
			
		}
		
		udpsend.start();
		
		//lBR_iiwa_14_R820_1.setESMState("2");
		//IMotionContainer handle = lBR_iiwa_14_R820_1.move(handGuiding());
		
		JointImpedanceControlMode impMode = new JointImpedanceControlMode(1.0,
				7.0, 7.0, 7.0, 7.0, 6.0, 1.0);
		IMotionContainer handle;
		handle = lBR_iiwa_14_R820_1.moveAsync(positionHold(impMode, -1,
				TimeUnit.SECONDS));
		
		
		while (true){
			int sel = getApplicationUI().displayModalDialog(
					ApplicationDialogType.QUESTION, "Stop Handguiding", "STOP", "CAPTURE");
	
			if (sel == 0)
			{
					handle.cancel();
					break;
			}
			else if (sel == 1)
			{
				UDPInput.add(new PRC_CommandData());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				UDPInput.clear();
			}
		}
		//lBR_iiwa_14_R820_1.setESMState("1");
		// your application execution starts here
		lBR_iiwa_14_R820_1.move(ptpHome());
	}
}