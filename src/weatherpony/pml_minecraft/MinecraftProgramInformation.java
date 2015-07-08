package weatherpony.pml_minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import weatherpony.pml.implementorapi.IProgramInformation;

public abstract class MinecraftProgramInformation implements IProgramInformation<MCSide> {
	public static final int MinecraftPMLAPIVersion = 1;
	private static final List<Integer> compatibleAPIVersions = Collections.unmodifiableList(Arrays.asList(1));
	
	public MinecraftProgramInformation(){
		
	}
	@Override
	public String programName() {
		return "Minecraft";
	}
	@Override
	public int PMLProgramAPIVersion() {
		return MinecraftPMLAPIVersion;
	}
	@Override
	public boolean isPMLProgramAPIVersionDirectlyCompatible(int version) {
		return compatibleAPIVersions.contains(version);
	}
	//Minecraft version information
	public abstract String minecraftVersionNumber();//the full version name (#.# or #.#.#)
	public abstract String minecraftCoreVersionNumber();//the first two parts of the version name (#.#). This may be the same thing as the full version
}
