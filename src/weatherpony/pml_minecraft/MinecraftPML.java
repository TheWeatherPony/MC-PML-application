package weatherpony.pml_minecraft;

import java.lang.reflect.Field;

import net.minecraft.launchwrapper.LaunchClassLoader;
import weatherpony.pml.implementorapi.IEnviornmentASMSetup;
import weatherpony.pml.implementorapi.IPMLModLocator;
import weatherpony.pml.implementorapi.IPMLPluginManagement;
import weatherpony.pml.implementorapi.PMLSetup;
import weatherpony.pml.launch.ILoadStateListener;
import weatherpony.pml.launch.IPMLClassLoaderAncestrySupplier;
import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.util.misc.Tristate;

public abstract class MinecraftPML<self extends MinecraftPML<self>> extends PMLSetup<self, MCSide>{
	protected MinecraftPML(MinecraftProgramInformation info) {
		super(new IEnviornmentASMSetup(){
			@Override
			public Class[] enviornmentCullingAnnotations() {
				return new Class[]{MCSideOnly.class};
			}
			@Override
			public boolean shouldKeepCode(Class annotation, Enum applicationEnviornment, Enum annotationedEnviornment) {
				if(annotation.equals(MCSideOnly.class)){
					return applicationEnviornment == annotationedEnviornment;
				}else{
					throw new IllegalArgumentException();
				}
			}
		});
		MCInfo = info;
		
		PMLLoadFocuser.addLoadNote(this.getClass().getClassLoader(), "weatherpony.pml_minecraft");//this will let the application API be reached later on from another ClassLoader
		PMLLoadFocuser.addClassLoaderAncestrySupplier(new MinecraftClassLoaderAncestrySupplier());
	}
	static class MinecraftClassLoaderAncestrySupplier implements IPMLClassLoaderAncestrySupplier{
		@Override
		public Tristate isAncestorClassLoader(ClassLoader base, ClassLoader possibleAncestor, boolean extraDebug){
			Class clazz = base.getClass();
			while(!clazz.equals(Object.class)){
				if(extraDebug){
					System.err.println("looking at Class of type "+clazz.getName());
				}
				if(clazz.getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")){
					break;
				}
				clazz = clazz.getSuperclass();
			}
			if(clazz.equals(Object.class)){
				return Tristate.OTHER;
			}
			ClassLoader parent = null;
			try{
				Field parentfield = clazz.getDeclaredField("parent");
				parentfield.setAccessible(true);
				parent = (ClassLoader) parentfield.get(base);
			}catch(NoSuchFieldException e){
				System.err.println("Minecraft PML needs updating: parent field non-existant in LaunchClassLoader");
				e.printStackTrace(System.err);
				System.exit(25);
			}catch(Exception e){
				System.err.println("Minecraft PML had trouble reading info from LaunchClassLoader");
				e.printStackTrace(System.err);
				System.exit(26);
			}
			if(parent == null)
				return Tristate.OTHER;
			return PMLLoadFocuser.isAncestorClassLoader(parent, possibleAncestor, extraDebug);
		}
	}
	private final MinecraftProgramInformation MCInfo;
	@Override
	public MinecraftProgramInformation getPMLApplicationAPI() {
		return MCInfo;
	}
	@Override
	public IPMLModLocator getModLocator(){
		return new MCPMLModLocator();
	}
	//this is registered in the detailed section
	protected class MCLoadListener implements ILoadStateListener{
		public MCLoadListener(IPMLPluginManagement manager){
			this.manager = manager;
		}
		private final IPMLPluginManagement manager;
		@Override
		public boolean stateUpdate(LoadUpdate state){
			if(state instanceof ILoadStateListener.ThreadChangeContextClassLoaderUpdate){
				ILoadStateListener.ThreadChangeContextClassLoaderUpdate update = (ILoadStateListener.ThreadChangeContextClassLoaderUpdate)state;
				if(update.loader.getClass().equals(update.loader)){
					if("net.minecraft.launchwrapper.LaunchClassLoader".equals(update.loader.getClass().getName())){
						manager.applicationRecommendedLoadTime(update.loader);
						return false;//no longer listen - no longer any need
					}
				}
			}
			return true;
		}
		public boolean isLaunchClassLoader(ClassLoader loader){
			Class clazz = loader.getClass();
			while(!clazz.equals(Object.class)){
				if("net.minecraft.launchwrapper.LaunchClassLoader".equals(clazz.getName())){
					return true;
				}
				clazz = clazz.getSuperclass();
			}
			return false;
		}
	}
}
