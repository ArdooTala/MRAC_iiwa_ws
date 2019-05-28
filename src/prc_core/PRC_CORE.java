package prc_core;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.IApplicationData;
import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.deviceModel.MobilePlatform;
import com.kuka.roboticsAPI.deviceModel.StatusTurnRedundancy;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.SpatialObject;
import com.kuka.roboticsAPI.geometricModel.math.CoordinateAxis;
import com.kuka.roboticsAPI.geometricModel.math.XyzAbcTransformation;
import com.kuka.roboticsAPI.geometricModel.redundancy.AbstractRedundancyCollection;
import com.kuka.roboticsAPI.geometricModel.redundancy.IRedundancyCollection;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.Output;
import com.kuka.roboticsAPI.motionModel.CIRC;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.MotionBatch;
import com.kuka.roboticsAPI.motionModel.RobotMotion;
import com.kuka.roboticsAPI.motionModel.SPL;
import com.kuka.roboticsAPI.motionModel.Spline;
import com.kuka.roboticsAPI.motionModel.SplineMotionCP;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;
import com.kuka.roboticsAPI.uiModel.IApplicationUI;
import com.kuka.task.ITaskLogger;

import prc_classes.PRC_CommandData;
import prc_classes.PRC_Enums;
import prc_classes.PRC_FileChooser;
import prc_classes.PRC_IOGroupExtended;
import prc_classes.PRC_MotionBatch;
import prc_classes.PRC_SPLMove;
import prc_classes.PRC_XMLOUT;
import prc_readxml.PRC_SaxHandler;

public class PRC_CORE {

	public PRC_XMLOUT CORE_ReadXML (String xmlpath, IApplicationUI iApplicationUI) {
		if (xmlpath == "")
		{
			iApplicationUI.displayModalDialog(ApplicationDialogType.ERROR, "XML file not found", "OK");
			return null;
		}
		SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
        	InputStream    xmlInput  = new FileInputStream(xmlpath);
            SAXParser      saxParser = factory.newSAXParser();
            PRC_SaxHandler handler   = new PRC_SaxHandler();
            saxParser.parse(xmlInput, handler);
            return(new PRC_XMLOUT(handler.prccmds, handler.prcsettings));
            
        } catch (Throwable err) {
            err.printStackTrace ();
        }
		return null;

	}
	
	public String CORE_ChooseXML()
	{
		System.setProperty("java.awt.headless", "false");
		
		String returnString = "";
		

			PRC_FileChooser fileChooser = new PRC_FileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "XML Files generated by KUKA|prc", "xml");
			fileChooser.setFileFilter(filter);
	        int returnValue = fileChooser.showOpenDialog(null);
	        if (returnValue == JFileChooser.APPROVE_OPTION) {
	        	returnString = fileChooser.getSelectedFile().getAbsolutePath();
		        fileChooser = null;
		        return returnString;
	        }
	        else
	        {
	        	return "";
	        }
	        

		
        
        
        
	}

	private ObjectFrame actTCP;
	private double linint = 0.0;
	private double ptpint = 0.0;
	private double linacc = 1000;
	private double ptpacc = 0.20;
	PRC_IOGroupExtended digiogroup;
	PRC_IOGroupExtended aniogroup;
	
	
	
	public void CORE_RUN(PRC_XMLOUT xmlout, LBR robot, Controller kuka_Sunrise_Cabinet_1, SpatialObject tool, String tcpname, ObjectFrame baseFrame, boolean enablelogging, ITaskLogger logger, MobilePlatform miiwa, IApplicationData AppData, MediaFlangeIOGroup ioGroup) {
		
		if (ioGroup != null){
		digiogroup = new PRC_IOGroupExtended(ioGroup, kuka_Sunrise_Cabinet_1, PRC_Enums.DIGOUT);
		aniogroup = new PRC_IOGroupExtended(ioGroup, kuka_Sunrise_Cabinet_1, PRC_Enums.ANOUT);
		}
		
		
		
		//create tool
		tool.attachTo(robot.getFlange());
		actTCP = tool.getFrame(tcpname);
		
		if (enablelogging){
			logger.info("Tool Created"); 
		}
		
		//assign settings
		linint = xmlout.prcsettings.linint;
		ptpint = xmlout.prcsettings.linint;
		linacc = xmlout.prcsettings.linacc * 1000;
		ptpacc = xmlout.prcsettings.ptpacc / 100.0;
		
		ArrayList<PRC_CommandData> cmdsmod = new ArrayList<PRC_CommandData>();
		ArrayList<PRC_CommandData> motionbatchCMDs = new ArrayList<PRC_CommandData>();
		ArrayList<PRC_CommandData> SPLbatchCMDs = new ArrayList<PRC_CommandData>();
		PRC_CommandData lastcmd = xmlout.prccommands.get(xmlout.prccommands.size() - 1);
		

		
		int mbsizelimit = 100;
		//group in batches, without last one
		for (int i = 0; i < xmlout.prccommands.size(); i++) {
			if (xmlout.prccommands.get(i).prccmdType == PRC_Enums.LIN || xmlout.prccommands.get(i).prccmdType == PRC_Enums.PTP || xmlout.prccommands.get(i).prccmdType == PRC_Enums.LINCOMP || xmlout.prccommands.get(i).prccmdType == PRC_Enums.PTPCOMP)
			{
				if ((i + 1 < xmlout.prccommands.size()) && (xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.LIN || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.PTP || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.LINCOMP || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.PTPCOMP || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.CIR))
				{
					motionbatchCMDs.add(xmlout.prccommands.get(i));
					
				}
				else if (motionbatchCMDs.size() > 0){
						motionbatchCMDs.add(xmlout.prccommands.get(i));
						//add motionbatch to cmdsmod
						PRC_CommandData cmdout = new PRC_CommandData();
						cmdout.prccmdType = PRC_Enums.MBATCH;
						cmdout.motionBatch = new PRC_MotionBatch();
						
						//finish motionbatch
						
						cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, true);
						cmdsmod.add(cmdout);
						motionbatchCMDs.clear();
				}
				else
				{
					cmdsmod.add(xmlout.prccommands.get(i));
				}
				
				if (motionbatchCMDs.size() > mbsizelimit)
				{
					
					//add motionbatch to cmdsmod
					PRC_CommandData cmdout = new PRC_CommandData();
					cmdout.prccmdType = PRC_Enums.MBATCH;
					cmdout.motionBatch = new PRC_MotionBatch();
					
					//finish motionbatch
					
					cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, true);
					cmdsmod.add(cmdout);
					motionbatchCMDs.clear();
				}
				
			}
			else if (xmlout.prccommands.get(i).prccmdType == PRC_Enums.SPL)
			{
				if ((i + 1 < xmlout.prccommands.size()) && (xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.SPL))
				{
					SPLbatchCMDs.add(xmlout.prccommands.get(i));
				}
				else{
					SPLbatchCMDs.add(xmlout.prccommands.get(i));
						//add motionbatch to cmdsmod
						PRC_CommandData cmdout = new PRC_CommandData();
						cmdout.prccmdType = PRC_Enums.SPL;
						cmdout.splMove = new PRC_SPLMove();
						
						//finish motionbatch
						
						cmdout.splMove = CORE_SplineMotion(SPLbatchCMDs, baseFrame, robot);
						cmdsmod.add(cmdout);
						SPLbatchCMDs.clear();
				}
				
				if (SPLbatchCMDs.size() > 19)
				{
					
					//add motionbatch to cmdsmod
					PRC_CommandData cmdout = new PRC_CommandData();
					cmdout.prccmdType = PRC_Enums.SPL;
					cmdout.splMove = new PRC_SPLMove();
					
					//finish motionbatch
					
					cmdout.splMove = CORE_SplineMotion(SPLbatchCMDs, baseFrame, robot);
					cmdsmod.add(cmdout);
					SPLbatchCMDs.clear();
				}
				
			}
			else if (xmlout.prccommands.get(i).prccmdType == PRC_Enums.CIR)
			{
				if ((i + 1 < xmlout.prccommands.size()) && (xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.LIN || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.PTP || xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.CIR))
				{
					motionbatchCMDs.add(xmlout.prccommands.get(i));
					
				}
				else if (motionbatchCMDs.size() > 0){
						motionbatchCMDs.add(xmlout.prccommands.get(i));
						//add motionbatch to cmdsmod
						PRC_CommandData cmdout = new PRC_CommandData();
						cmdout.prccmdType = PRC_Enums.MBATCH;
						cmdout.motionBatch = new PRC_MotionBatch();
						
						//finish motionbatch
						
						cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, true);
						cmdsmod.add(cmdout);
						motionbatchCMDs.clear();
				}
				else
				{
					cmdsmod.add(xmlout.prccommands.get(i));
				}
				
				if (motionbatchCMDs.size() > mbsizelimit)
				{
					
					//add motionbatch to cmdsmod
					PRC_CommandData cmdout = new PRC_CommandData();
					cmdout.prccmdType = PRC_Enums.MBATCH;
					cmdout.motionBatch = new PRC_MotionBatch();
					
					//finish motionbatch
					
					cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, true);
					cmdsmod.add(cmdout);
					motionbatchCMDs.clear();
				}
				
			}
			else if (xmlout.prccommands.get(i).prccmdType == PRC_Enums.AXIS)
			{
				if ((i + 1 < xmlout.prccommands.size()) && (xmlout.prccommands.get(i + 1).prccmdType == PRC_Enums.AXIS))
				{
					motionbatchCMDs.add(xmlout.prccommands.get(i));
					
				}
				else if (motionbatchCMDs.size() > 0){
						motionbatchCMDs.add(xmlout.prccommands.get(i));
						//add motionbatch to cmdsmod
						PRC_CommandData cmdout = new PRC_CommandData();
						cmdout.prccmdType = PRC_Enums.MBATCH;
						cmdout.motionBatch = new PRC_MotionBatch();
						
						//finish motionbatch
						
						cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, false);
						cmdsmod.add(cmdout);
						motionbatchCMDs.clear();
				}
				else
				{
					cmdsmod.add(xmlout.prccommands.get(i));
				}
				
				if (motionbatchCMDs.size() > mbsizelimit)
				{
					
					//add motionbatch to cmdsmod
					PRC_CommandData cmdout = new PRC_CommandData();
					cmdout.prccmdType = PRC_Enums.MBATCH;
					cmdout.motionBatch = new PRC_MotionBatch();
					
					//finish motionbatch
					
					cmdout.motionBatch = CORE_RobotMotion(motionbatchCMDs, baseFrame, robot, false);
					cmdsmod.add(cmdout);
					motionbatchCMDs.clear();
				}
				
				
				
			}
			else
			{
				cmdsmod.add(xmlout.prccommands.get(i));
			}
			
			
			
		}
		
		
		ArrayList<IMotionContainer> motionContainers = new ArrayList<IMotionContainer>();
		int k = 0;
		
		for (PRC_CommandData cmd : cmdsmod) {
			
			if (k>3)
			{
				while (!motionContainers.get(k-3).isFinished())
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

				if (cmd.linMove.interpolation == "")
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
				
				if (cmd.linMove.interpolation == "")
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
				
				
				if (cmd.linCompMove.interpolation == "")
				{
					ForceCondition forceDetected = ForceCondition.createNormalForceCondition(actTCP, CoordinateAxis.X, 5);

					actTCP.move(lin(cmd.linCompMove.frame).setCartVelocity(cmd.linCompMove.vel).setCartAcceleration(linacc).setMode(ImpedanceControl).breakWhen(forceDetected));
					ioGroup.setOutput1(true);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ioGroup.setOutput1(false);
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

				if (cmd.ptpMove.interpolation == "")
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

				if (cmd.ptpCompMove.interpolation == "")
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
		
		if (lastcmd.prccmdType == PRC_Enums.AXIS)
		{
			robot.move(ptp(lastcmd.axisMove.axispos).setJointVelocityRel(lastcmd.axisMove.vel).setJointAccelerationRel(ptpacc));
		}
		else {
			if (motionContainers.size() > 0){
			while (!motionContainers.get(motionContainers.size() - 1).isFinished())
				ThreadUtil.milliSleep(5);
			}
		}
		
		
		
	}


	private PRC_MotionBatch CORE_RobotMotion(
			ArrayList<PRC_CommandData> motionbatchCMDs, ObjectFrame baseFrame, LBR robot, boolean isCartesian) {
		List<RobotMotion<?>> motionlist = new ArrayList<RobotMotion<?>>();
		
		
		for (PRC_CommandData cmd : motionbatchCMDs) {
			
			if (cmd.prccmdType == PRC_Enums.LIN){
				if (baseFrame != null)
				{
					cmd.linMove.frame.setParent(baseFrame);
				}
				AbstractFrame frm = cmd.linMove.frame;
				
				frm = PRC_SetRedundancy(robot, cmd);
				
				if (cmd.linMove.interpolation.equals("C_DIS")){
					RobotMotion<?> motion = (RobotMotion<?>) lin(frm).setCartVelocity(cmd.linMove.vel).setBlendingCart(linint).setCartAcceleration(linacc);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) lin(frm).setCartVelocity(cmd.linMove.vel).setBlendingCart(0).setCartAcceleration(linacc);
					motionlist.add(motion);
				}
			}
			else if (cmd.prccmdType == PRC_Enums.LINCOMP){
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

				
				if (cmd.linCompMove.interpolation.equals("C_DIS")){
					RobotMotion<?> motion = (RobotMotion<?>) lin(frm).setCartVelocity(cmd.linCompMove.vel).setBlendingCart(linint).setCartAcceleration(linacc).setMode(ImpedanceControl);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) lin(frm).setCartVelocity(cmd.linCompMove.vel).setBlendingCart(0).setCartAcceleration(linacc).setMode(ImpedanceControl);
					motionlist.add(motion);
				}
			}
			else if (cmd.prccmdType == PRC_Enums.CIR){
				if (baseFrame != null)
				{
					cmd.cirMove.frame.setParent(baseFrame);
					cmd.cirMove.auxframe.setParent(baseFrame);
				}
				AbstractFrame frm = cmd.cirMove.frame;
				AbstractFrame auxfrm = cmd.cirMove.auxframe;
				
				frm = PRC_SetRedundancy(robot, cmd);
				
				if (cmd.cirMove.interpolation.equals("C_DIS")){
					RobotMotion<?> motion = (RobotMotion<?>) new CIRC(frm, auxfrm).setCartVelocity(cmd.cirMove.vel).setBlendingCart(linint).setCartAcceleration(linacc);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) new CIRC(frm, auxfrm).setCartVelocity(cmd.cirMove.vel).setBlendingCart(0).setCartAcceleration(linacc);
					motionlist.add(motion);
				}
				

			}
			else if (cmd.prccmdType == PRC_Enums.PTP){
				if (baseFrame != null)
				{
					cmd.ptpMove.frame.setParent(baseFrame);
				}
				AbstractFrame frm = cmd.ptpMove.frame;
				
				frm = PRC_SetRedundancy(robot, cmd);
				
				
				if (cmd.ptpMove.interpolation.equals("C_PTP")){
					RobotMotion<?> motion = (RobotMotion<?>) ptp(frm).setJointVelocityRel(cmd.ptpMove.vel).setBlendingCart(ptpint).setJointAccelerationRel(ptpacc);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) ptp(frm).setJointVelocityRel(cmd.ptpMove.vel).setBlendingCart(0).setJointAccelerationRel(ptpacc);
					motionlist.add(motion);
				}
			}
			else if (cmd.prccmdType == PRC_Enums.PTPCOMP){
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
				
				if (cmd.ptpCompMove.interpolation.equals("C_PTP")){
					RobotMotion<?> motion = (RobotMotion<?>) ptp(frm).setJointVelocityRel(cmd.ptpCompMove.vel).setBlendingCart(ptpint).setJointAccelerationRel(ptpacc).setMode(ImpedanceControl);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) ptp(frm).setJointVelocityRel(cmd.ptpCompMove.vel).setBlendingCart(0).setJointAccelerationRel(ptpacc).setMode(ImpedanceControl);
					motionlist.add(motion);
				}
			}
			else if (cmd.prccmdType == PRC_Enums.AXIS){
				if (cmd.axisMove.interpolation.equals("C_PTP")){
					RobotMotion<?> motion = (RobotMotion<?>) ptp(cmd.axisMove.axispos).setJointVelocityRel(cmd.axisMove.vel).setBlendingCart(ptpint).setJointAccelerationRel(ptpacc);
					motionlist.add(motion);
				}
				else {
					RobotMotion<?> motion = (RobotMotion<?>) ptp(cmd.axisMove.axispos).setJointVelocityRel(cmd.axisMove.vel).setBlendingCart(0).setJointAccelerationRel(ptpacc);
					motionlist.add(motion);
				}
			}

		}
		
		PRC_CommandData cmdout = new PRC_CommandData();
		cmdout.prccmdType = PRC_Enums.MBATCH;
		cmdout.motionBatch = new PRC_MotionBatch();
		cmdout.motionBatch.isCartesian = isCartesian;

		//finish motionbatch
		
		cmdout.motionBatch.motionBatch = new MotionBatch(
				motionlist.toArray(new RobotMotion<?>[motionlist.size()]));
		
		return cmdout.motionBatch;
	}

	private PRC_SPLMove CORE_SplineMotion(
			ArrayList<PRC_CommandData> splCMDs, ObjectFrame baseFrame, LBR robot) {
		List<SplineMotionCP<?>> motionlist = new ArrayList<SplineMotionCP<?>>();

		for (PRC_CommandData cmd : splCMDs) {
			if (baseFrame != null)
			{
				cmd.splPart.frame.setParent(baseFrame);
			}
			AbstractFrame frm = cmd.splPart.frame;
			frm = PRC_SetRedundancy(robot, cmd);
			SplineMotionCP<?> motion = (SplineMotionCP<?>) new SPL(frm).setCartVelocity(cmd.splPart.vel);
			motionlist.add(motion);
		}
		
		PRC_CommandData cmdout = new PRC_CommandData();
		cmdout.prccmdType = PRC_Enums.SPL;
		cmdout.splMove = new PRC_SPLMove();

		//finish motionbatch
		
		cmdout.splMove.spl = new Spline(
				motionlist.toArray(new SplineMotionCP<?>[motionlist.size()])).setBlendingCart(linint);
		
		return cmdout.splMove;
		
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
