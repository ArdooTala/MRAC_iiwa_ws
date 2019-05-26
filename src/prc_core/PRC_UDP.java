package prc_core;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import prc_classes.PRC_CommandData;
import prc_classes.PRC_Enums;
import prc_classes.PRC_IOGroupExtended;

import sun.security.action.GetLongAction;
import threads.UDPReceiver;
import threads.UDPSender;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.IApplicationData;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.SpatialObject;
import com.kuka.roboticsAPI.geometricModel.math.XyzAbcTransformation;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.motionModel.CIRC;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.task.ITaskLogger;

public class PRC_UDP {
	
	UDPReceiver udprec;
	UDPSender udpsend;
	ObjectFrame actTCP;
	BlockingQueue<PRC_CommandData> UDPInput;
	PRC_IOGroupExtended digiogroup;
	PRC_IOGroupExtended aniogroup;
	
	public void CORE_UDP(LBR robot, Controller kuka_Sunrise_Cabinet_1, SpatialObject tool, String tcpname, ObjectFrame baseFrame, boolean enablelogging, ITaskLogger logger, IApplicationData AppData, AbstractIOGroup ioGroup, String ip, int port) throws SocketException, UnknownHostException {
	
		//movement parameters
		double ptpacc = 1.0;
		double ptpint = 4.0;
		double linacc = 1000;
		double linint = 4.0;
		
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
		int k = 0;
		
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
			
			for (PRC_CommandData cmd : udpcmds) {
				
				if (k>3)
				{
					while (!motionContainers.get(k-5).isFinished())
						ThreadUtil.milliSleep(50);
				}
				
				if (cmd.prccmdType.equals(PRC_Enums.ANOUT)){
					if (motionContainers.size() > 0){
						while (!motionContainers.get(motionContainers.size() - 1).isFinished())
							ThreadUtil.milliSleep(50);
						}
						String returnstr = aniogroup.prc_SetAnalogIO(cmd.anOut.num, cmd.anOut.state);

						if (enablelogging){logger.info(returnstr);}
					
				} else if (cmd.prccmdType.equals(PRC_Enums.AXIS)){
					
					if (cmd.axisMove.interpolation == ""){
						robot.move(ptp(cmd.axisMove.axispos).setJointVelocityRel(cmd.axisMove.vel).setJointAccelerationRel(ptpacc));
					}else{
						IMotionContainer mc = robot.moveAsync(ptp(cmd.axisMove.axispos).setJointVelocityRel(cmd.axisMove.vel).setJointAccelerationRel(ptpacc).setBlendingCart(ptpint));
						motionContainers.add(mc);
						k++;
					}
					
					
					
					if (enablelogging){logger.info(cmd.axisMove.ToString());}
					
				} else if (cmd.prccmdType.equals(PRC_Enums.CHANGETOOL)){
					
						if (motionContainers.size() > 0){
						while (!motionContainers.get(motionContainers.size() - 1).isFinished())
							ThreadUtil.milliSleep(50);
						}
					Double randomdblDouble = (Math.random() * 50 + 1);
					tool.addChildFrame("prctool" + randomdblDouble, XyzAbcTransformation.ofRad(cmd.changetool.toolframe.getX(), cmd.changetool.toolframe.getY(), cmd.changetool.toolframe.getZ(), cmd.changetool.toolframe.getAlphaRad(), cmd.changetool.toolframe.getBetaRad(), cmd.changetool.toolframe.getGammaRad()));		
					actTCP = tool.getFrame("prctool" + randomdblDouble);
					
				} else if (cmd.prccmdType.equals(PRC_Enums.DIGOUT)){
					
					if (motionContainers.size() > 0){
					while (!motionContainers.get(motionContainers.size() - 1).isFinished())
						ThreadUtil.milliSleep(50);
					}
					String returnstr = digiogroup.prc_SetDigIO(cmd.digOut.num, cmd.digOut.state);

					
					if (enablelogging){logger.info(returnstr);}
					
				} else if (cmd.prccmdType.equals(PRC_Enums.KMRMOVE)){
					
					if (motionContainers.size() > 0){
					while (!motionContainers.get(motionContainers.size() - 1).isFinished())
						ThreadUtil.milliSleep(50);
					}
					
					
					//miiwa.move(MobilePlatformMotions.relativeMotion(cmd.kmrMove.kmrx,cmd.kmrMove.kmry,cmd.kmrMove.kmrtheta).setVelocity(20.0, 0.1));
					
					if (enablelogging){logger.info(cmd.kmrMove.ToString());}
				
				} else if (cmd.prccmdType.equals(PRC_Enums.CIR)){
					if (baseFrame != null)
					{
						cmd.cirMove.frame.setParent(baseFrame);
						cmd.cirMove.auxframe.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.cirMove.frame;
					AbstractFrame auxfrm = cmd.cirMove.auxframe;
					
					frm = PRC_SetRedundancy(robot, cmd);

					if (cmd.cirMove.interpolation == "" || cmd.cirMove.interpolation == " ")
					{
						actTCP.move(new CIRC(auxfrm, frm).setCartVelocity(cmd.cirMove.vel).setCartAcceleration(linacc));
						
					}
					else
					{
						IMotionContainer mc = actTCP.moveAsync(new CIRC(auxfrm, frm).setCartVelocity(cmd.cirMove.vel).setCartAcceleration(linacc).setBlendingCart(linint));
						
						motionContainers.add(mc);
						k++;
					}

					if (enablelogging){logger.info(cmd.cirMove.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.LIN)){
					if (baseFrame != null)
					{
						cmd.linMove.frame.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.linMove.frame;
					
					frm = PRC_SetRedundancy(robot, cmd);
					
					if (cmd.linMove.interpolation == "" || cmd.linMove.interpolation == " ")
					{
						actTCP.move(lin(cmd.linMove.frame).setCartVelocity(cmd.linMove.vel).setCartAcceleration(linacc));
					}
					else
					{
						IMotionContainer mc = actTCP.moveAsync(lin(cmd.linMove.frame).setCartVelocity(cmd.linMove.vel).setBlendingCart(linint).setCartAcceleration(linacc));
						motionContainers.add(mc);
						k++;
					}

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
					
					
					if (cmd.linCompMove.interpolation == "" || cmd.linMove.interpolation == " ")
					{
						actTCP.move(lin(cmd.linCompMove.frame).setCartVelocity(cmd.linCompMove.vel).setCartAcceleration(linacc).setMode(ImpedanceControl));
					}
					else
					{
						IMotionContainer mc = actTCP.moveAsync(lin(cmd.linCompMove.frame).setCartVelocity(cmd.linCompMove.vel).setBlendingCart(linint).setCartAcceleration(linacc).setMode(ImpedanceControl));
						motionContainers.add(mc);
						k++;
					}

					if (enablelogging){logger.info(cmd.linCompMove.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.PTP)){
					if (baseFrame != null)
					{
						cmd.ptpMove.frame.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.ptpMove.frame;
					
					frm = PRC_SetRedundancy(robot, cmd);

					if (cmd.ptpMove.interpolation == "" || cmd.ptpMove.interpolation == " ")
					{
						actTCP.move(ptp(frm).setJointVelocityRel(cmd.ptpMove.vel).setJointAccelerationRel(ptpacc));
					}
					else
					{
						IMotionContainer mc = actTCP.moveAsync(ptp(frm).setJointVelocityRel(cmd.ptpMove.vel).setJointAccelerationRel(ptpacc).setBlendingCart(ptpint));
						motionContainers.add(mc);
						k++;
					}
					
					if (enablelogging){logger.info(cmd.ptpMove.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.PTPCOMP)){
					if (baseFrame != null)
					{
						cmd.ptpCompMove.frame.setParent(baseFrame);
					}
					AbstractFrame frm = cmd.ptpCompMove.frame;
					
					frm = PRC_SetRedundancy(robot, cmd);
					
					CartesianImpedanceControlMode ImpedanceControl = new CartesianImpedanceControlMode();
					ImpedanceControl.parametrize(CartDOF.ALL).setDamping(0.7);
					ImpedanceControl.parametrize(CartDOF.X).setStiffness(cmd.linCompMove.stiffX).setAdditionalControlForce(cmd.linCompMove.addFX);
					ImpedanceControl.parametrize(CartDOF.Y).setStiffness(cmd.linCompMove.stiffY).setAdditionalControlForce(cmd.linCompMove.addFY);
					ImpedanceControl.parametrize(CartDOF.Z).setStiffness(cmd.linCompMove.stiffZ).setAdditionalControlForce(cmd.linCompMove.addFZ);
					ImpedanceControl.parametrize(CartDOF.ROT).setStiffness(300);

					if (cmd.ptpCompMove.interpolation == "" || cmd.ptpCompMove.interpolation == " ")
					{
						actTCP.move(ptp(frm).setJointVelocityRel(cmd.ptpCompMove.vel).setJointAccelerationRel(ptpacc).setMode(ImpedanceControl));
					}
					else
					{
						IMotionContainer mc = actTCP.moveAsync(ptp(frm).setJointVelocityRel(cmd.ptpCompMove.vel).setJointAccelerationRel(ptpacc).setBlendingCart(ptpint).setMode(ImpedanceControl));
						motionContainers.add(mc);
						k++;
					}
					
					if (enablelogging){logger.info(cmd.ptpCompMove.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.WAIT)){
					
					long time = (long) (cmd.wait.time * 1000);

					if (motionContainers.size() > 0){
					while (!motionContainers.get(motionContainers.size() - 1).isFinished())
						ThreadUtil.milliSleep(50);
					}
					
					ThreadUtil.milliSleep(time);

					if (enablelogging){logger.info(cmd.wait.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.MBATCH)){
					
					if (cmd.motionBatch.isCartesian){
						IMotionContainer mc = actTCP.moveAsync(cmd.motionBatch.motionBatch);
						motionContainers.add(mc);
						k++;
					} else {
						IMotionContainer mc = robot.moveAsync(cmd.motionBatch.motionBatch);
						motionContainers.add(mc);
						k++;
					}
					
					if (enablelogging){logger.info(cmd.motionBatch.ToString());}
				} else if (cmd.prccmdType.equals(PRC_Enums.SPL)){
					
						IMotionContainer mc = actTCP.moveAsync(cmd.splMove.spl);
						motionContainers.add(mc);
						k++;

					if (enablelogging){logger.info(cmd.splMove.ToString());}
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
				if (cmd.ptpMove.status.length() == 1){
					e1val.setStatus(Integer.parseInt(cmd.ptpMove.status));
				}
				else
				{
					e1val.setStatus(Integer.parseInt(cmd.ptpMove.status, 2));
				}
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
				if (cmd.ptpMove.status.length() == 1){
					e1val.setStatus(Integer.parseInt(cmd.ptpMove.status));
				}
				else
				{
					e1val.setStatus(Integer.parseInt(cmd.ptpMove.status, 2));
				}
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
