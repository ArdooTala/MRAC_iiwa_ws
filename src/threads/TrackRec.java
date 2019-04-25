package threads;

import java.util.ArrayList;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.IApplicationData;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplicationData;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.Tool;

public class TrackRec extends Thread {
	private LBR theRobot;
	private Tool theTool;
	private ArrayList<Frame> theList;
	private RoboticsAPIApplicationData theData;
	
	public TrackRec (LBR _lbriiwa, Tool _handhold, ArrayList<Frame> _trackPoints, IApplicationData appData) {
		theRobot = _lbriiwa;
		theTool = _handhold;
		theList = _trackPoints;
		theData = (RoboticsAPIApplicationData) appData;
	}
	
	@Override
	public void run() {
		do {
			theList.add(theRobot.getCurrentCartesianPosition(theTool.getFrame("/TCP")));
			ThreadUtil.milliSleep(200);
		}while(theData.getProcessData("trackPath").getValue());
		
	}
	
	public ArrayList<Frame> getList() {
		return theList;
	}

} 
