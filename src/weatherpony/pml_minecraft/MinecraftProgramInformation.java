package weatherpony.pml_minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import weatherpony.pml.implementorapi.IProgramInformation;

public abstract class MinecraftProgramInformation implements IProgramInformation<MCSide> {
	public static final int MinecraftPMLAPIVersion = 2;
	private static final List<Integer> compatibleAPIVersions = Collections.unmodifiableList(Arrays.asList(1,2));
	
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
	//API version 2: support for snapshots
	public boolean isFullRelease(){
		return !this.isSnapshot();
	}
	public abstract boolean isSnapshot();
	//if it's a snapshot, the name of the snapshot (<2 digit year>w<week number><release letter>) will be the returned value for both minecraftVersionNumber and minecraftCoreVersionNumber
	
	//version 1 compatibility
	//Minecraft version information
	public abstract String minecraftVersionNumber();//the full version name (#.# or #.#.#) //API version 2: this could be the name of a snapshot
	public abstract String minecraftCoreVersionNumber();//the first two parts of the version name (#.#). This may be the same thing as the full version
}
