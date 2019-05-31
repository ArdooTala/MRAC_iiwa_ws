package prc_classes;

import java.awt.Component;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;
import com.kuka.roboticsAPI.ioModel.Output;

public class PRC_IOGroupExtended extends AbstractIOGroup {
    public PRC_IOGroupExtended(AbstractIOGroup iogroup, Controller controller, PRC_Enums typein) {
		
    	super(controller, iogroup.getIOGroupName());

    	DigIOnames = new ArrayList<String>();
    	AnIOnames = new ArrayList<String>();
		for (Output oput : iogroup.getOutputs()) {
			if (oput.getDataType() == IOTypes.ANALOG & typein == PRC_Enums.ANOUT)
			{
				super.addAnalogOutput(oput.getIOName(), oput.getDataType(), oput.getBitSize(), 0.0, 1.0);
				AnIOnames.add(oput.getIOName());
				this.type = typein;
			}
			else if (oput.getDataType() == IOTypes.BOOLEAN & typein == PRC_Enums.DIGOUT)
			{
				super.addDigitalOutput(oput.getIOName(), oput.getDataType(), oput.getBitSize());
				DigIOnames.add(oput.getIOName());
				this.type = typein;
			}
		}
		Collections.sort(DigIOnames);
		Collections.sort(AnIOnames);
	}
    
    private PRC_Enums type;
    private List<String> DigIOnames;
    private List<String> AnIOnames;
    

	public String prc_SetDigIO (int num, Boolean value)
    {
		int ionum = num - 1;
    	if (type == PRC_Enums.DIGOUT){
    		if (num < 0 | ionum > DigIOnames.size()){
    			return "IO number out of bounds.";
    		}
    		else
    		{
    			super.setDigitalOutput(DigIOnames.get(ionum), value);
    			
    			int counter = 0;
    			
    			Boolean ioval = super.getBooleanIOValue(DigIOnames.get(ionum), true);
    			
    			while (counter < 50 & ioval != value)
    			{
    				ThreadUtil.milliSleep(3);
    				counter ++;
    				ioval = super.getBooleanIOValue(DigIOnames.get(ionum), true);
    			}
    			
    			ioval = super.getBooleanIOValue(DigIOnames.get(ionum), true);
    			//Boolean testbool = super.getBooleanIOValue(IOnames.get(ionum), true);
    			
    			if (counter >= 49)
    			{
    				return "IO not set.";
    			}
    			
    			return "Digital output " + num + " set to " + value;
    		}   		
    	}
    	else
    	{
    		return "No Digital IO found in this group";
    	}
    }
	
	public String prc_SetAnalogIO (int num, double value)
    {
		int ionum = num - 1;
    	if (type == PRC_Enums.ANOUT){
    		if (ionum < 0 | ionum > AnIOnames.size()){
    			return "IO number out of bounds.";
    		}
    		else
    		{
    			super.setAnalogOutput(AnIOnames.get(ionum), value);
    			
    			int counter = 0;
    			
    			while (counter < 50 & super.getAnalogIOValue(AnIOnames.get(ionum), true) != value)
    			{
    				ThreadUtil.milliSleep(3);
    				counter ++;
    			}
    			
    			if (counter >= 49)
    			{
    				return "IO not set.";
    			}
    			
    			return "Analog output " + num + " set to " + value;
    		}   		
    	}
    	else
    	{
    		return "No Digital IO found in this group";
    	}
    }
}
