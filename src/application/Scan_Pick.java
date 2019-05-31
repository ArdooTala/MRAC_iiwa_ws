package application;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.kuka.connectivity.motionModel.smartServoLIN.ISmartServoLINRuntime;
import com.kuka.connectivity.motionModel.smartServoLIN.SmartServoLIN;
import com.kuka.generated.ioAccess.BeckhoffIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Frame;
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
public class Scan_Pick extends RoboticsAPIApplication {
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
	
    private DatagramSocket socket;
    public boolean running;
    private byte[] buf = new byte[65508];
    
    ISmartServoLINRuntime _smartServoLINRuntime = null;
    
    BeckhoffIOGroup io;
	
	@Override
	public void initialize() {
		// initialize your application here
		try {
			socket = new DatagramSocket(30000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		// initialize your application here
		kuka_Sunrise_Cabinet_1 = (Controller) getContext().getControllers().toArray()[0];
		iiwa_14 = (LBR) kuka_Sunrise_Cabinet_1.getDevices().toArray()[0];
		
		io = new BeckhoffIOGroup(kuka_Sunrise_Cabinet_1);
		
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
		force.parametrize(CartDOF.Z).setStiffness(3000);
		force.parametrize(CartDOF.ROT).setStiffness(300);
		
		//break action on force
		forceDetected = ForceCondition.createNormalForceCondition(actTCP, CoordinateAxis.Z, 30);
	}

	@Override
	public void run() {
		// your application execution starts here
//		iiwa_14.move(ptpHome());
		actTCP.moveAsync(ptp(getApplicationData().getFrame("/Scan_Base")).setJointVelocityRel(.2));
		running = true;
		// your application execution starts here
		while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
				socket.receive(packet);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
             
            String received = new String(packet.getData(), 0, packet.getLength());
             
            if (received.length() > 3)
            {
            	parse_command(received);
            }
        }
        socket.close();
	}
	
	private void parse_command(String received) {
		getLogger().info(received);
		String [] elements = received.split(","); 
		if (elements.length > 1)
		{
			if (elements[0].equals("Seek")){
				seek();
			}
			else if (elements[0].equals("Locate")) {
				locate(elements);
			}
			else if (elements[0].equals("Pick")){
				pick();
			}
		}
		buf = new byte[65508];
	}
	
	public void seek() {
		IMotionContainer scan;
		
		for (int i = 1; i <= 2; i++) {
			ObjectFrame f = getApplicationData().getFrame("/Scan_Pose/P"+Integer.toString(i));
			scan = actTCP.moveAsync(lin(f).setCartVelocity(100));
			while (!scan.isFinished()){
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	           
				String received = new String(packet.getData(), 0, packet.getLength());
				String [] rest = received.split(",");
				if (rest[0].equals("Locate"))
				{
					scan.cancel();		}
			}
		}
	}
	
	private void locate(String[] loc) {	
		Frame frm;
		double dX, dY;
		dX = Double.parseDouble(loc[1]);
		dY = Double.parseDouble(loc[2]);
//		frm = new Frame(
//				Double.parseDouble(loc[1]),
//				Double.parseDouble(loc[2]),
//				Double.parseDouble(loc[3]),
//				Math.toRadians(Double.parseDouble(loc[4])),
//				Math.toRadians(Double.parseDouble(loc[5])),
//				Math.toRadians(Double.parseDouble(loc[6]))
//				);
		
		boolean initialized = false;
		AbstractFrame initialPosition = iiwa_14.getCurrentCartesianPosition(actTCP);
		SmartServoLIN aSmartServoLINMotion = new SmartServoLIN(initialPosition);
        aSmartServoLINMotion.setMinimumTrajectoryExecutionTime(20e-3);
        
        while (true) {
        	DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
				socket.receive(packet);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
             
            String received = new String(packet.getData(), 0, packet.getLength());
            if (received.length() > 3)
            {	
            	String [] center = received.split(","); 
        		if (center.length > 1)
        		{
        			if (center[0].equals("Locate")) {
        				dX = Double.parseDouble(center[1]);
        				dY = Double.parseDouble(center[2]);
        				getLogger().info("Update Recieved.");
    				}
        			else if (center[0].equals("Pick")){
        				break;
        			}
        			else {
        				dX = 0;
        				dY = 0;
        			}
        		}
        		else {
        			dX = 0;
    				dY = 0;
        		}
            }
            else {
            	dX = 0;
				dY = 0;
            }
        	
	        if (!initialized){
				//init smartservo
		        rlsTCP.moveAsync(aSmartServoLINMotion);
	
		        _smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
		        initialized = true;
			}
	        _smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
	        frm = _smartServoLINRuntime.getCurrentCartesianPosition(actTCP);
	        double[] vel = {30, 30, 30};
	        frm.setX(frm.getX() + dX);
	        frm.setY(frm.getY() + dY);
	        frm.setZ(frm.getZ() + 88);
			_smartServoLINRuntime.setMaxTranslationVelocity(vel);
			_smartServoLINRuntime.setDestination(frm);
        }
	}
	
	private void pick() {
		Frame frm = iiwa_14.getCurrentCartesianPosition(actTCP);
		frm.setZ(-100);
		frm.setY(frm.getY()+30);
		actTCP.move(lin(frm).setCartVelocity(100).breakWhen(forceDetected));
		
		frm.setZ(-300);
//		actTCP.move(lin(frm).setCartVelocity(10));
//		
		actTCP.move(lin(frm).setCartVelocity(10).breakWhen(forceDetected));
		
		io.setOut1(true);
		
		iiwa_14.move(positionHold(force , 3 , TimeUnit. SECONDS ));
		
		frm.setZ(200);
		actTCP.move(lin(frm).setCartVelocity(10));
	}
	
    public void dispose(){
    	io.setOut1(false);
    	running=false;
    	socket.close();
    }
}
