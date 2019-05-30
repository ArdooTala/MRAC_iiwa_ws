package prc_readxml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import prc_classes.PRC_AXISMove;
import prc_classes.PRC_AnOut;
import prc_classes.PRC_CIRMove;
import prc_classes.PRC_ChangeTool;
import prc_classes.PRC_CommandData;
import prc_classes.PRC_DigOut;
import prc_classes.PRC_Enums;
import prc_classes.PRC_KMRMove;
import prc_classes.PRC_LINCompMove;
import prc_classes.PRC_LINMove;
import prc_classes.PRC_PTPCompMove;
import prc_classes.PRC_PTPMove;
import prc_classes.PRC_SPLPart;
import prc_classes.PRC_SettingsData;
import prc_classes.PRC_Wait;

import com.kuka.roboticsAPI.deviceModel.JointPosition;
import com.kuka.roboticsAPI.geometricModel.Frame;


import java.util.*;

public class PRC_SaxHandler extends DefaultHandler {

    public List<PRC_CommandData> prccmds  = new ArrayList<PRC_CommandData>();
    public PRC_SettingsData prcsettings = new PRC_SettingsData();
    private PRC_CommandData currCMD = new PRC_CommandData();
    private Frame currFrame = new Frame(0,0,0,0,0,0);
    private Frame currAuxFrame = new Frame(0,0,0,0,0,0);
    private JointPosition currJointPosition = new JointPosition(7);
    private String currelement = "";
    private double currCartVEL = 0;
    private double currAxisVel = 0;
    private PRC_KMRMove kmrMove = new PRC_KMRMove();
    

    public void startElement(String uri, String localName,
        String qName, Attributes attributes) throws SAXException {

        if("PRC_CommandData".equals(qName)){
        	currCMD = new PRC_CommandData();
        } else if("FRAME".equals(qName)){
            currFrame = new Frame(0, 0, 0, 0, 0, 0);
        } else if("AUXFRAME".equals(qName)){
            currAuxFrame = new Frame(0, 0, 0, 0, 0, 0);
        } else if("AXIS".equals(qName)){
            currJointPosition = new JointPosition(7);
        }
	     if("PRC_Settings".equals(qName)){
	    	prcsettings = new PRC_SettingsData();
	    }
        currelement = qName;
        
    }

    public void endElement(String uri, String localName,
        String qName) throws SAXException {
    	
    	if("PRC_CMD".equals(qName)){
    		if (currCMD.prccmdType.equals(PRC_Enums.AXIS)){
    			currCMD.axisMove.axispos = currJointPosition;
    			currCMD.axisMove.vel = currAxisVel;
    		} else if (currCMD.prccmdType.equals(PRC_Enums.LIN)){
    			currCMD.linMove.frame = currFrame;
    			currCMD.linMove.vel = currCartVEL;
    		} else if (currCMD.prccmdType.equals(PRC_Enums.PTP)){
    			currCMD.ptpMove.frame = currFrame;
    			currCMD.ptpMove.vel = currAxisVel;
    		} else if (currCMD.prccmdType.equals(PRC_Enums.CHANGETOOL)){
    			currCMD.changetool.toolframe = currFrame;
    		} else if (currCMD.prccmdType.equals(PRC_Enums.CIR)){
    			currCMD.cirMove.frame = currFrame;
    			currCMD.cirMove.auxframe = currAuxFrame;
    			currCMD.cirMove.vel = currCartVEL;
    		} else if (currCMD.prccmdType.equals(PRC_Enums.SPL)){
    			currCMD.splPart.frame = currFrame;
    			currCMD.splPart.vel = currCartVEL;
    		} 
    		
    		prccmds.add(currCMD);
        	currCMD = new PRC_CommandData();
        } else if("BASE".equals(qName)){
        	prcsettings.base = currFrame;
        } else if("INITTOOL".equals(qName)){
        	prcsettings.inittool = currFrame;
        }
        
        
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {

        String value = new String(ch, start, length).trim();
        if(value.length() == 0) return; // ignore white space

        if("TYPE".equals(currelement)){
        	if ("LIN".equals(value)){
        		currCMD.prccmdType =PRC_Enums.LIN;
        		currCMD.linMove = new PRC_LINMove();
        	} else if ("LINCOMP".equals(value)){
        		currCMD.prccmdType =PRC_Enums.LINCOMP;
        		currCMD.linCompMove = new PRC_LINCompMove();
        	} else if ("PTP".equals(value)){
        		currCMD.prccmdType =PRC_Enums.PTP;
        		currCMD.ptpMove = new PRC_PTPMove();
        	} else if ("PTPCOMP".equals(value)){
        		currCMD.prccmdType =PRC_Enums.PTPCOMP;
        		currCMD.ptpCompMove = new PRC_PTPCompMove();
        	} else if ("AXIS".equals(value)){
        		currCMD.prccmdType =PRC_Enums.AXIS;
        		currCMD.axisMove = new PRC_AXISMove();
        	} else if ("CIR".equals(value)){
        		currCMD.prccmdType =PRC_Enums.CIR;
        		currCMD.cirMove = new PRC_CIRMove();
        	} else if ("SPL".equals(value)){
        		currCMD.prccmdType =PRC_Enums.SPL;
        		currCMD.splPart = new PRC_SPLPart();
        	 } else if ("WAIT".equals(value)){
        		currCMD.prccmdType =PRC_Enums.WAIT;
        		currCMD.wait = new PRC_Wait();
        	} else if ("ANIO".equals(value)){
        		currCMD.prccmdType =PRC_Enums.ANOUT;
        		currCMD.anOut = new PRC_AnOut();
        	} else if ("DIGIO".equals(value)){
        		currCMD.prccmdType =PRC_Enums.DIGOUT;
        		currCMD.digOut = new PRC_DigOut();
        	}else if ("CHANGETOOL".equals(value)){
        		currCMD.prccmdType =PRC_Enums.CHANGETOOL;
        		currCMD.changetool = new PRC_ChangeTool();
        	}
        } else if("A01".equals(currelement)){
        	currJointPosition.set(0, Double.parseDouble(value));
        } else if("A02".equals(currelement)){
        	currJointPosition.set(1, Double.parseDouble(value));
        } else if("A03".equals(currelement)){
        	currJointPosition.set(2, Double.parseDouble(value));
        } else if("A04".equals(currelement)){
        	currJointPosition.set(3, Double.parseDouble(value));
        } else if("A05".equals(currelement)){
        	currJointPosition.set(4, Double.parseDouble(value));
        } else if("A06".equals(currelement)){
        	currJointPosition.set(5, Double.parseDouble(value));
        } else if("A07".equals(currelement)){
        	currJointPosition.set(6, Double.parseDouble(value));
        } else if("X".equals(currelement)){
        	currFrame.setX(Double.parseDouble(value));
        } else if("Y".equals(currelement)){
        	currFrame.setY(Double.parseDouble(value));
        } else if("Z".equals(currelement)){
        	currFrame.setZ(Double.parseDouble(value));
        } else if("A".equals(currelement)){
        	currFrame.setAlphaRad(Double.parseDouble(value));
        } else if("B".equals(currelement)){
        	currFrame.setBetaRad(Double.parseDouble(value));
        } else if("C".equals(currelement)){
        	currFrame.setGammaRad(Double.parseDouble(value));
        } else if("Xa".equals(currelement)){
        	currAuxFrame.setX(Double.parseDouble(value));
        } else if("Ya".equals(currelement)){
        	currAuxFrame.setY(Double.parseDouble(value));
        } else if("Za".equals(currelement)){
        	currAuxFrame.setZ(Double.parseDouble(value));
        } else if("Aa".equals(currelement)){
        	currAuxFrame.setAlphaRad(Double.parseDouble(value));
        } else if("Ba".equals(currelement)){
        	currAuxFrame.setBetaRad(Double.parseDouble(value));
        } else if("Ca".equals(currelement)){
        	currAuxFrame.setGammaRad(Double.parseDouble(value));
        } else if("VEL".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LIN))
        	{
        		currCartVEL = Double.parseDouble(value);
        	}
        	else {
				currAxisVel = Double.parseDouble(value);
			}
        } else if("STATUS".equals(currelement)){
        	currCMD.ptpMove.status = value;
        } else if("TURN".equals(currelement)){
        	currCMD.ptpMove.turn = value;
        } else if("E1VAL".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LIN)){
        	currCMD.linMove.e1val = Double.parseDouble(value);
        	}
        	else if (currCMD.prccmdType.equals(PRC_Enums.PTP)){
            	currCMD.ptpMove.e1val = Double.parseDouble(value);
            } 
        	else if (currCMD.prccmdType.equals(PRC_Enums.CIR)){
            	currCMD.cirMove.e1val = Double.parseDouble(value);
            } 
        	else if (currCMD.prccmdType.equals(PRC_Enums.SPL)){
            	currCMD.splPart.e1val = Double.parseDouble(value);
            } 
        } else if("INT".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.AXIS)){
        		currCMD.axisMove.interpolation = value;
        	} else if (currCMD.prccmdType.equals(PRC_Enums.LIN)){
        		currCMD.linMove.interpolation = value;
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTP)){
        		currCMD.ptpMove.interpolation = value;
        	} else if (currCMD.prccmdType.equals(PRC_Enums.CIR)){
        		currCMD.cirMove.interpolation = value;
        	}
        } else if("STIFFX".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.stiffX = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.stiffX = Double.parseDouble(value);
        	}
        } else if("STIFFY".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.stiffY = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.stiffY = Double.parseDouble(value);
        	}
        } else if("STIFFZ".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.stiffZ = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.stiffZ = Double.parseDouble(value);
        	}
        } else if("ADDFX".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.addFX = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.addFX = Double.parseDouble(value);
        	}
        } else if("ADDFY".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.addFY = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.addFY = Double.parseDouble(value);
        	}
        } else if("ADDFZ".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.LINCOMP)){
        		currCMD.linCompMove.addFZ = Double.parseDouble(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.PTPCOMP)){
        		currCMD.ptpCompMove.addFZ = Double.parseDouble(value);
        	}
        } else if("KMRX".equals(currelement)){
        	kmrMove.kmrx = Double.parseDouble(value);
        }else if("KMRY".equals(currelement)){
        	kmrMove.kmry = Double.parseDouble(value);
        }else if("KMRTHETA".equals(currelement)){
        	kmrMove.kmrtheta = Double.parseDouble(value);
        	if ((Math.abs(kmrMove.kmrx) + Math.abs(kmrMove.kmry) + Math.abs(kmrMove.kmrtheta)) > 0.0 )
        	{
        		PRC_CommandData kmrcmd = new PRC_CommandData();
        		kmrcmd.kmrMove = kmrMove;
        		kmrcmd.prccmdType = PRC_Enums.KMRMOVE;
        	}
        	
        }else if("TIME".equals(currelement)){
        	currCMD.wait.time = (Double.parseDouble(value));
        }
        else if("IONUM".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.ANOUT)){
        		currCMD.anOut.num = Integer.parseInt(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.DIGOUT)){
        		currCMD.digOut.num = Integer.parseInt(value);
        	}
        } else if("IOCONT".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.ANOUT)){
        		currCMD.anOut.cont = Boolean.parseBoolean(value);
        	} else if (currCMD.prccmdType.equals(PRC_Enums.DIGOUT)){
        		currCMD.digOut.cont = Boolean.parseBoolean(value);
        	}
        } else if("IOSTATE".equals(currelement)){
        	if (currCMD.prccmdType.equals(PRC_Enums.ANOUT)){
        		currCMD.anOut.state = (Double.parseDouble(value));
        	} else if (currCMD.prccmdType.equals(PRC_Enums.DIGOUT)){
        		currCMD.digOut.state = Boolean.parseBoolean(value);
        	}
        } else if("PTPINT".equals(currelement)){
        	prcsettings.ptpint = (Double.parseDouble(value));
        } else if("LININT".equals(currelement)){
        	prcsettings.linint = (Double.parseDouble(value));
        }else if("PTPACC".equals(currelement)){
        	prcsettings.ptpacc = (Double.parseDouble(value));
        }else if("LINACC".equals(currelement)){
        	prcsettings.linacc = (Double.parseDouble(value));
        }
        
            
        	
        
        
    }

}  
        

