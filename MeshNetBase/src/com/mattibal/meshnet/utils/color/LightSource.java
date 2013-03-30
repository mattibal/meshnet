package com.mattibal.meshnet.utils.color;

/**
 * This represent a real physical light source (for example a LED) that can
 * emit light of a specific chrominance, and a luminance varying from
 * zero to a maximum luminance value.
 */
public class LightSource {
	
	private final Chrominance chroma;
	private final double maxLumi;
	
	public LightSource(Chrominance chroma, double maxLuminance){
		this.chroma = chroma;
		this.maxLumi = maxLuminance;
	}
	
	public LightSource(double x, double y, double maxLuminance){
		this.maxLumi = maxLuminance;
		this.chroma = new Chrominance(x, y);
	}

	public double getMaxLumi(){
		return maxLumi;
	}
	
	public double getx(){
		return chroma.getx();
	}
	public double gety(){
		return chroma.gety();
	}
	
	public int getPwmValue(double lumi, int maxPwmValue){
		return (int)((lumi*maxPwmValue)/maxLumi);
	}
	
}
