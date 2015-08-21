package weatherpony.pml_minecraft.detail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import weatherpony.pml.implementorapi.IPMLPluginManagement;
import weatherpony.pml.implementorapi.PMLSetup;
import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.pml.launch.PMLRoot;
import weatherpony.pml.premain.PreMain;
import weatherpony.pml_minecraft.MinecraftPML;

public class DetailedMinecraftPML extends MinecraftPML<DetailedMinecraftPML>{
	String side;
	public DetailedMinecraftPML(){
		super(new DetailedMinecraftProgramInformation());
	}
	//I'll remove this and refactor as much as I need to in order to compensate, if I find that someone's accessing this 
	private IPMLPluginManagement manager;
	@Override
	public void givePluginManager(IPMLPluginManagement manager){
		PMLLoadFocuser.registerLoadStateListener(new MCLoadListener(manager));
	}

	@Override
	protected void recommendedLoadTime_notLaunchWrapper() {
		this.manager.applicationRecommendedLoadTime(Thread.currentThread().getContextClassLoader());
	}
}
