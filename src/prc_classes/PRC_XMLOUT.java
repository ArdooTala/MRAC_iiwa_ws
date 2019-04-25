package prc_classes;

import java.util.List;

public class PRC_XMLOUT {
	public List<PRC_CommandData> prccommands;
	public PRC_SettingsData prcsettings;
	
	public PRC_XMLOUT(List<PRC_CommandData> cmds, PRC_SettingsData settings)
	{
		prccommands = cmds;
		prcsettings = settings;
	}
}
