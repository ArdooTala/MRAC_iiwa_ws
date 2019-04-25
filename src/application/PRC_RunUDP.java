package application;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.awt.List;
import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import prc_classes.PRC_XMLOUT;
import prc_core.PRC_CORE;
import prc_core.PRC_UDP;



import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.MobilePlatform;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.SpatialObject;
import com.kuka.roboticsAPI.ioModel.AbstractIO;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.motionModel.CIRC;
import com.kuka.roboticsAPI.motionModel.IMotion;
import com.kuka.roboticsAPI.motionModel.PTP;
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
public class PRC_RunUDP extends RoboticsAPIApplication {
	
	private Controller kuka_Sunrise_Cabinet_1;
	private LBR lbr_iiwa;
	private PRC_UDP prc_udp;

	@Override
	public void initialize() {
		kuka_Sunrise_Cabinet_1 = (Controller) getContext().getControllers().toArray()[0];
		lbr_iiwa = (LBR) kuka_Sunrise_Cabinet_1.getDevices().toArray()[0];
		// miiwa = (MobilePlatform) kuka_Sunrise_Cabinet_1.getDevice("KMP_omniMove_400_1");
		
		prc_udp = new PRC_UDP();
	}
	
	@Override
	public void run() {
		LBR robot = lbr_iiwa; //SET ROBOT
		String toolname = "PolierVib"; //SET TOOL NAME
		String tcpname = "TCP"; //SET TCP NAME
		ObjectFrame baseFrame = getApplicationData().getFrame("/HaringBase"); //OPTIONAL: SET BASE COORDINATE SYSTEM
		boolean enablellogging = true; //OPTIONAL: ENABLE CONSOLE LOGGING
		//Beckhoff_OUTIOGroup iogrp = new Beckhoff_OUTIOGroup(kuka_Sunrise_Cabinet_1);
		//AbstractIOGroup iogrp = iogrp;
		
		try {
			prc_udp.CORE_UDP(robot, kuka_Sunrise_Cabinet_1, getApplicationData().createFromTemplate(toolname), tcpname, baseFrame, enablellogging, getLogger(), getApplicationData(), null, "172.31.1.148", 49152);
		} catch (SocketException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}finally{
			prc_udp.dispose();
		}

	}
	//FIXME 1904.David.G.-That should kill the receiver UDP port in Sunrise part if something goes wrong.
	@Override
	public void dispose() {
		getLogger().warn("Something went wrong. Killing app.");
		prc_udp.dispose();
		super.dispose();
	}

	/**
	 * Auto-generated method stub. Do not modify the contents of this method.
	 */
	public static void main(String[] args) {
		PRC_RunUDP app = new PRC_RunUDP();
		app.runApplication();
	}
}
