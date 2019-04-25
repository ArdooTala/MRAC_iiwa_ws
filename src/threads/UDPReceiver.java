package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import com.kuka.roboticsAPI.deviceModel.JointPosition;
import com.kuka.roboticsAPI.geometricModel.Frame;

import prc_classes.PRC_AXISMove;
import prc_classes.PRC_AnOut;
import prc_classes.PRC_CommandData;
import prc_classes.PRC_DigOut;
import prc_classes.PRC_Enums;
import prc_classes.PRC_LINCompMove;
import prc_classes.PRC_LINMove;
import prc_classes.PRC_PTPCompMove;
import prc_classes.PRC_PTPMove;
import prc_classes.PRC_Wait;

public class UDPReceiver extends Thread {
	 
    private DatagramSocket socket;
    public boolean running;
    private byte[] buf = new byte[65508];
    private BlockingQueue<PRC_CommandData> UDPQueue;
 
    public UDPReceiver(BlockingQueue<PRC_CommandData> UDPInput) throws SocketException {
        socket = new DatagramSocket(30000);
        UDPQueue = UDPInput;
    }
 
    public void run() {
        running = true;
 
        while (running) {
            DatagramPacket packet 
              = new DatagramPacket(buf, buf.length);
            try {
				socket.receive(packet);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
             
            String received 
              = new String(packet.getData(), 0, packet.getLength());
             
            if (received.length() > 10)
            {
            	UDPQueue.add(StringtoCmd(received));
            }

        }
        socket.close();
    }
    
    public void dispose(){
    	socket.close();
    }

	private PRC_CommandData StringtoCmd(String received) {
		String [] elements = received.split(","); 
		if (elements.length > 1)
		{
			if (elements[0].equals("LIN")){
				PRC_LINMove linmove = new PRC_LINMove();
				linmove.frame = new Frame(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]), Math.toRadians(Double.parseDouble(elements[4])), Math.toRadians(Double.parseDouble(elements[5])), Math.toRadians(Double.parseDouble(elements[6])));
				linmove.vel = Double.parseDouble(elements[7]) * 1000;
				linmove.e1val = Math.toRadians(Double.parseDouble(elements[8]));
				linmove.interpolation = elements[9];
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.linMove = linmove;
				cmd.prccmdType = PRC_Enums.LIN;
				return cmd;
			}
			if (elements[0].equals("LINCOMP")){
				PRC_LINCompMove linmove = new PRC_LINCompMove();
				linmove.frame = new Frame(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]), Math.toRadians(Double.parseDouble(elements[4])), Math.toRadians(Double.parseDouble(elements[5])), Math.toRadians(Double.parseDouble(elements[6])));
				linmove.vel = Double.parseDouble(elements[7])  * 1000;
				linmove.e1val = Math.toRadians(Double.parseDouble(elements[8]));
				linmove.interpolation = elements[9];
				linmove.stiffX = Double.parseDouble(elements[10]);
				linmove.stiffY = Double.parseDouble(elements[11]);
				linmove.stiffZ = Double.parseDouble(elements[12]);
				linmove.addFX = Double.parseDouble(elements[13]);
				linmove.addFY = Double.parseDouble(elements[14]);
				linmove.addFZ = Double.parseDouble(elements[15]);
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.linCompMove = linmove;
				cmd.prccmdType = PRC_Enums.LINCOMP;
				return cmd;
			}
			else if (elements[0].equals("PTP")){
				PRC_PTPMove ptpmove = new PRC_PTPMove();
				ptpmove.frame = new Frame(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]), Math.toRadians(Double.parseDouble(elements[4])), Math.toRadians(Double.parseDouble(elements[5])), Math.toRadians(Double.parseDouble(elements[6])));
				ptpmove.vel = Double.parseDouble(elements[7]) / 100.0;
				ptpmove.e1val = Math.toRadians(Double.parseDouble(elements[8]));
				ptpmove.interpolation = elements[9];
				ptpmove.status = elements[10];
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.ptpMove = ptpmove;
				cmd.prccmdType = PRC_Enums.PTP;
				return cmd;
			}
			else if (elements[0].equals("PTPCOMP")){
				PRC_PTPCompMove ptpmove = new PRC_PTPCompMove();
				ptpmove.frame = new Frame(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]), Math.toRadians(Double.parseDouble(elements[4])), Math.toRadians(Double.parseDouble(elements[5])), Math.toRadians(Double.parseDouble(elements[6])));
				ptpmove.vel = Double.parseDouble(elements[7]) / 100.0;
				ptpmove.e1val = Math.toRadians(Double.parseDouble(elements[8]));
				ptpmove.interpolation = elements[9];
				ptpmove.status = elements[10];
				ptpmove.stiffX = Double.parseDouble(elements[11]);
				ptpmove.stiffY = Double.parseDouble(elements[12]);
				ptpmove.stiffZ = Double.parseDouble(elements[13]);
				ptpmove.addFX = Double.parseDouble(elements[14]);
				ptpmove.addFY = Double.parseDouble(elements[15]);
				ptpmove.addFZ = Double.parseDouble(elements[16]);
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.ptpCompMove = ptpmove;
				cmd.prccmdType = PRC_Enums.PTPCOMP;
				return cmd;
			}
			else if (elements[0].equals("AXIS")){
				PRC_AXISMove ptpmove = new PRC_AXISMove();
				ptpmove.axispos = new JointPosition(Math.toRadians(Double.parseDouble(elements[1])), Math.toRadians(Double.parseDouble(elements[2])), Math.toRadians(Double.parseDouble(elements[3])), Math.toRadians(Double.parseDouble(elements[4])), Math.toRadians(Double.parseDouble(elements[5])), Math.toRadians(Double.parseDouble(elements[6])), Math.toRadians(Double.parseDouble(elements[7])));
				ptpmove.vel = Double.parseDouble(elements[8]) / 100.0;
				ptpmove.interpolation = elements[9];
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.axisMove = ptpmove;
				cmd.prccmdType = PRC_Enums.AXIS;
				return cmd;
			}
			else if (elements[0].equals("ANOUT")){
				PRC_AnOut anout = new PRC_AnOut();
				anout.num = Integer.parseInt(elements[1]);
				anout.state = Double.parseDouble(elements[2]);
				anout.cont = Boolean.parseBoolean(elements[3]);
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.anOut = anout;
				cmd.prccmdType = PRC_Enums.ANOUT;
				return cmd;
			}
			else if (elements[0].equals("DIGOUT")){
				PRC_DigOut digout = new PRC_DigOut();
				digout.num = Integer.parseInt(elements[1]);
				digout.state = Boolean.parseBoolean(elements[2]);
				digout.cont = Boolean.parseBoolean(elements[3]);
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.digOut = digout;
				cmd.prccmdType = PRC_Enums.DIGOUT;
				return cmd;
			}
			else if (elements[0].equals("WAIT")){
				PRC_Wait wait = new PRC_Wait();
				wait.time = Double.parseDouble(elements[1]);
				PRC_CommandData cmd = new PRC_CommandData();
				cmd.wait = wait;
				cmd.prccmdType = PRC_Enums.WAIT;
				return cmd;
			}
			else{
				return null;
			}
				
		}
		else
		{
			return null;
		}
		
	}
}