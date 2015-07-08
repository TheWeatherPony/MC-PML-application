package weatherpony.pml_minecraft;

import weatherpony.pml.implementorapi.IEnviornment;

public enum MCSide implements IEnviornment<MCSide> {
	CLIENT,
	SERVER;

	@Override
	public MCSide[] getAllValues() {
		return MCSide.values();
	}
}
