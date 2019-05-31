package prc_core;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import prc_classes.PRC_CommandData;
import prc_classes.PRC_Enums;
import prc_classes.PRC_IOGroupExtended;
import threads.UDPReceiver;
import threads.UDPSender;

import com.kuka.common.ThreadUtil;
import com.kuka.connectivity.motionModel.smartServoLIN.ISmartServoLINRuntime;
import com.kuka.connectivity.motionModel.smartServoLIN.SmartServoLIN;
import com.kuka.roboticsAPI.applicationModel.IApplicationData;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.SpatialObject;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.task.ITaskLogger;


public class PRC_SmartServo {
	
	UDPReceiver udprec;
	UDPSender udpsend;
	ObjectFrame actTCP;
	BlockingQueue<PRC_CommandData> UDPInput;
	PRC_IOGroupExtended digiogroup;
	PRC_IOGroupExtended aniogroup;
	ISmartServoLINRuntime _smartServoLINRuntime = null;
    
	
	public void CORE_SmartServo(LBR robot, Controller kuka_Sunrise_Cabinet_1, SpatialObject tool, String tcpname, ObjectFrame baseFrame, boolean enablelogging, ITaskLogger logger, IApplicationData AppData, AbstractIOGroup ioGroup, String ip, int port) throws SocketException, UnknownHostException {
	

		boolean initialized = false;
		
		UDPInput = new LinkedBlockingQueue<PRC_CommandData>();
		
		
		if (ioGroup != null){
		digiogroup = new PRC_IOGroupExtended(ioGroup, kuka_Sunrise_Cabinet_1, PRC_Enums.DIGOUT);
		aniogroup = new PRC_IOGroupExtended(ioGroup, kuka_Sunrise_Cabinet_1, PRC_Enums.ANOUT);
		}
		
		tool.attachTo(robot.getFlange());
		actTCP = tool.getFrame(tcpname);
		
		udprec = new UDPReceiver(UDPInput);
		udpsend = new UDPSender(InetAddress.getByName(ip), port, UDPInput);
		
		if (robot != null)
		{
			udpsend.robot = robot;
			udpsend.toolframe = actTCP;
		}
		
		udpsend.start();
		udprec.start();
		
		ArrayList<IMotionContainer> motionContainers = new ArrayList<IMotionContainer>();

		
		AbstractFrame initialPosition = robot.getCurrentCartesianPosition(actTCP);

		// Create a new smart servo linear motion
        SmartServoLIN aSmartServoLINMotion = new SmartServoLIN(initialPosition);

        aSmartServoLINMotion.setMinimumTrajectoryExecutionTime(20e-3);





		
		while (true) {
			ArrayList<PRC_CommandData> udpcmds = new ArrayList<PRC_CommandData>();
			
			if (UDPInput != null & UDPInput.size() > 0){
				while (UDPInput.size() > 0)
				{
					try {
						udpcmds.add(UDPInput.take());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				ThreadUtil.milliSleep(50);
			}
			
			
			for (PRC_CommandData cmd : udpcmds) {
				if (cmd.prccmdType.equals(PRC_Enums.LIN)){

					if (baseFrame != null)
					{
						cmd.linMove.frame.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.linMove.frame;
					
					frm = PRC_SetRedundancy(robot, cmd);
					double vel[] = {cmd.linMove.vel, cmd.linMove.vel, cmd.linMove.vel};
					
					if (!initialized){
						//init smartservo
				        logger.info("Starting the SmartServoLIN in position control mode");
				        actTCP.moveAsync(aSmartServoLINMotion);

				        logger.info("Get the runtime of the SmartServoLIN motion");
				        _smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
				        initialized = true;
					}
					_smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
					_smartServoLINRuntime.setMaxTranslationVelocity(vel);
					_smartServoLINRuntime.setDestination(frm);

					if (enablelogging){logger.info(cmd.linMove.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.LINCOMP)){
					if (baseFrame != null)
					{
						cmd.linCompMove.frame.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.linCompMove.frame;
					
					frm = PRC_SetRedundancy(robot, cmd);
					
					CartesianImpedanceControlMode ImpedanceControl = new CartesianImpedanceControlMode();
					ImpedanceControl.parametrize(CartDOF.ALL).setDamping(0.7);
					ImpedanceControl.parametrize(CartDOF.X).setStiffness(cmd.linCompMove.stiffX).setAdditionalControlForce(cmd.linCompMove.addFX);
					ImpedanceControl.parametrize(CartDOF.Y).setStiffness(cmd.linCompMove.stiffY).setAdditionalControlForce(cmd.linCompMove.addFY);
					ImpedanceControl.parametrize(CartDOF.Z).setStiffness(cmd.linCompMove.stiffZ).setAdditionalControlForce(cmd.linCompMove.addFZ);
					ImpedanceControl.parametrize(CartDOF.ROT).setStiffness(300);
					
					double vel[] = {cmd.linCompMove.vel, cmd.linCompMove.vel, cmd.linCompMove.vel};
					
					if (!initialized){
						//init smartservo
				        logger.info("Starting the SmartServoLIN in position control mode");
				        actTCP.moveAsync(aSmartServoLINMotion.setMode(ImpedanceControl));

				        logger.info("Get the runtime of the SmartServoLIN motion");
				        _smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
				        initialized = true;
					}
					_smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
					_smartServoLINRuntime.setMaxTranslationVelocity(vel);
					_smartServoLINRuntime.changeControlModeSettings(ImpedanceControl);
					_smartServoLINRuntime.setDestination(frm);					

					if (enablelogging){logger.info(cmd.linCompMove.ToString());}
				} 
				
			}
		}
		
		
	}
	
	public void dispose(){
		udprec.dispose();
		udpsend.dispose();
	}
	
private AbstractFrame PRC_SetRedundancy(LBR robot, PRC_CommandData cmd) {
		
		if (cmd.prccmdType == PRC_Enums.PTP)
		{
			AbstractFrame frm = cmd.ptpMove.frame;
			if (cmd.ptpMove.turn.length() > 0){
			LBRE1Redundancy red = new LBRE1Redundancy(cmd.ptpMove.e1val, Integer.parseInt(cmd.ptpMove.status, 2), Integer.parseInt(cmd.ptpMove.turn, 2));
			frm.setRedundancyInformation(robot, red);
			}
			else {
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.ptpMove.e1val);
				e1val.setStatus(Integer.parseInt(cmd.ptpMove.status, 2));
				frm.setRedundancyInformation(robot, e1val);
			}
			return frm;
		}
		else if (cmd.prccmdType == PRC_Enums.PTPCOMP)
		{
			AbstractFrame frm = cmd.ptpCompMove.frame;
			if (cmd.ptpCompMove.turn.length() > 0){
			LBRE1Redundancy red = new LBRE1Redundancy(cmd.ptpCompMove.e1val, Integer.parseInt(cmd.ptpCompMove.status, 2), Integer.parseInt(cmd.ptpCompMove.turn, 2));
			frm.setRedundancyInformation(robot, red);
			}
			else {
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.ptpCompMove.e1val);
				e1val.setStatus(Integer.parseInt(cmd.ptpCompMove.status, 2));
				frm.setRedundancyInformation(robot, e1val);
			}
			return frm;
		}
		else if (cmd.prccmdType == PRC_Enums.LIN)
		{
			AbstractFrame frm = cmd.linMove.frame;
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.linMove.e1val);
				frm.setRedundancyInformation(robot, e1val);
			return frm;
		}
		else if (cmd.prccmdType == PRC_Enums.LINCOMP)
		{
			AbstractFrame frm = cmd.linCompMove.frame;
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.linCompMove.e1val);
				frm.setRedundancyInformation(robot, e1val);
			return frm;
		}
		else if (cmd.prccmdType == PRC_Enums.CIR)
		{
			AbstractFrame frm = cmd.cirMove.frame;
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.cirMove.e1val);
				frm.setRedundancyInformation(robot, e1val);
			return frm;
		}
		else if (cmd.prccmdType == PRC_Enums.SPL)
		{
			AbstractFrame frm = cmd.splPart.frame;
				LBRE1Redundancy e1val = new LBRE1Redundancy();
				e1val.setE1(cmd.splPart.e1val);
				frm.setRedundancyInformation(robot, e1val);
			return frm;
		}
		else
		{
			return null;
		}
	}
}
