package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;

import prc_classes.PRC_CommandData;

import com.kuka.roboticsAPI.deviceModel.JointLimits;
import com.kuka.roboticsAPI.deviceModel.JointPosition;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Vector;
import com.kuka.roboticsAPI.sensorModel.ForceSensorData;

public class UDPSender extends Thread {
	 
    private DatagramSocket socket;
    public boolean running;
    public LBR robot;
    public ObjectFrame toolframe;
    private byte[] buf = new byte[65508];
    private InetAddress address;
    private int port;
    private int sleeptime =33;
    private BlockingQueue<PRC_CommandData> CmdQueue;
 
    public UDPSender(InetAddress externaladdress, int externalport, BlockingQueue<PRC_CommandData> UDPQueue) throws SocketException {
        socket = new DatagramSocket(30001);
        address = externaladdress;
        port = externalport;
        CmdQueue = UDPQueue;
    }
 
    public void run() {
        running = true;
 
        while (running) {
        	String data = CollectPayload(robot, toolframe);
        	buf = data.getBytes();
        	DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

            try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
            try {
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        socket.close();
    }

	private String CollectPayload(LBR robot2, ObjectFrame toolframe2) {
		JointPosition jointpos = robot2.getCurrentJointPosition();
		Frame cartpos = robot2.getCurrentCartesianPosition(toolframe2);
		Vector force = robot2.getExternalForceTorque(toolframe2).getForce();
		DecimalFormat df = new DecimalFormat("#.####"); 
		
		String posstr = df.format(Math.toDegrees(jointpos.get(0))) + "," + df.format(Math.toDegrees(jointpos.get(1))) + "," + df.format(Math.toDegrees(jointpos.get(2))) + "," + df.format(Math.toDegrees(jointpos.get(3))) + "," + df.format(Math.toDegrees(jointpos.get(4))) + "," + df.format(Math.toDegrees(jointpos.get(5))) + "," + df.format(Math.toDegrees(jointpos.get(6)));
		String cartstr = df.format(cartpos.getX()) + "," + df.format(cartpos.getY()) + "," + df.format(cartpos.getZ()) + "," + df.format(Math.toDegrees(cartpos.getAlphaRad())) + "," + df.format(Math.toDegrees(cartpos.getBetaRad())) + "," + df.format(Math.toDegrees(cartpos.getGammaRad()));
		String forcevector = df.format(force.getX()) + "," + df.format(force.getY())+ "," + df.format(force.getZ());
		
		return (new Integer(CmdQueue.size()).toString()) + "," + posstr + "," + cartstr + "," + forcevector;
	}
}