package prc_readxml;


import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import prc_classes.PRC_CommandData;

public class PRC_ReadXML {

	/**
	 * @param args
	 */
	
	public List<PRC_CommandData> PRCGetCMDs (String xmlpath) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            //InputStream    xmlInput  = PRC_ReadXML.class.getResourceAsStream("/xml/kukaprc.xml");
        	InputStream    xmlInput  = PRC_ReadXML.class.getResourceAsStream(xmlpath);
            SAXParser      saxParser = factory.newSAXParser();
            PRC_SaxHandler handler   = new PRC_SaxHandler();
            saxParser.parse(xmlInput, handler);
            return(handler.prccmds);
            
        } catch (Throwable err) {
            err.printStackTrace ();
        }
		return null;

	}

}


