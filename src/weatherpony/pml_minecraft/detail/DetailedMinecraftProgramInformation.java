package weatherpony.pml_minecraft.detail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import weatherpony.pml.implementorapi.IEnviornment;
import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.pml_minecraft.MCSide;
import weatherpony.pml_minecraft.MinecraftProgramInformation;

public class DetailedMinecraftProgramInformation extends MinecraftProgramInformation{
	DetailedMinecraftProgramInformation(){
		super();
		String pattern = "[:\\s]";
		String[] args = PMLLoadFocuser.agentargs.split(pattern);
		final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
		final OptionSpec<String> profileName = parser.accepts("MCSide", "The game side we launched with").withRequiredArg();
		final OptionSet options = parser.parse(args);
		String side = options.valueOf(profileName);
		
		File override = new File("mcpml.sideoverride.ini");
		if(override.exists()){
			try{
				BufferedReader overridereader = new BufferedReader(new FileReader(override));
				side = overridereader.readLine();
				overridereader.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(side == null)
			throw new IllegalArgumentException("PML needs to know what side this is on. use the program argument (or the file override if that's not possible)");
		side = side.toUpperCase();
		this.side = MCSide.valueOf(side);
		
		versionHelper = new VersionHelper();
	}
	MCSide side;
	@Override
	public IEnviornment<MCSide> getApplicationEnviornment(){
		return this.side;
	}
	private final VersionHelper versionHelper;
	@Override
	public String minecraftVersionNumber(){
		return versionHelper.MCVersion;
	}
	@Override
	public String minecraftCoreVersionNumber(){
		return versionHelper.MCVersionRoot;
	}
	@Override
	public boolean isSnapshot(){
		return versionHelper.snapshot;
	}
}
