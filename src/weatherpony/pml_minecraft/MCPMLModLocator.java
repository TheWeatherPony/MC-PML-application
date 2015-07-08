package weatherpony.pml_minecraft;

import weatherpony.pml.implementorapi.IPMLLoadDirector;
import weatherpony.pml.implementorapi.StandardPMLModLocator;

public class MCPMLModLocator extends StandardPMLModLocator{
	@Override
	public void onLoad(IPMLLoadDirector loadEngine){
		super.onLoad(loadEngine);
		loadEngine.searchForLocalMod("mcpml.info");
	}
}
