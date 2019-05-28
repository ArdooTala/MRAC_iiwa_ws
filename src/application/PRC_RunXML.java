package application;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import prc_classes.PRC_XMLOUT;
import prc_core.PRC_CORE;

import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
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
public class PRC_RunXML extends RoboticsAPIApplication {
	private Controller kuka_Sunrise_Cabinet_1;
	private LBR lbr_iiwa;


	@Override
	public void initialize() {
		kuka_Sunrise_Cabinet_1 = (Controller) getContext().getControllers().toArray()[0];
		lbr_iiwa = (LBR) kuka_Sunrise_Cabinet_1.getDevices().toArray()[0];
		// miiwa = (MobilePlatform) kuka_Sunrise_Cabinet_1.getDevice("KMP_omniMove_400_1");
	}

	@Override
	public void run() {
		LBR robot = lbr_iiwa; //SET ROBOT
		String toolname = "IAACGripper"; //SET TOOL NAME
		String tcpname = "TCP"; //SET TCP NAME
		ObjectFrame baseFrame = getApplicationData().getFrame("/BASE1"); //OPTIONAL: SET BASE COORDINATE SYSTEM
		boolean enablellogging = true; //OPTIONAL: ENABLE CONSOLE LOGGING
		MediaFlangeIOGroup iogrp = new MediaFlangeIOGroup(kuka_Sunrise_Cabinet_1);
		//AbstractIOGroup iogrp = iogrp;
		
		PRC_CORE prc_Core = new PRC_CORE();
		
		
		
		PRC_XMLOUT xmlout = prc_Core.CORE_ReadXML(prc_Core.CORE_ChooseXML(), getApplicationUI());
		
		prc_Core.CORE_RUN(xmlout, robot, kuka_Sunrise_Cabinet_1, getApplicationData().createFromTemplate(toolname), tcpname, baseFrame, enablellogging, getLogger(), null, getApplicationData(), iogrp);

	}


	/**
	 * Auto-generated method stub. Do not modify the contents of this method.
	 */
	public static void main(String[] args) {
		PRC_RunXML app = new PRC_RunXML();
		app.runApplication();
	}
}
