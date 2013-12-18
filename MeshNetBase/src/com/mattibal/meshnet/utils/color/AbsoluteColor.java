package com.mattibal.meshnet.utils.color;

/**
 * This is a device indipendent (absolute) representation of a color.
 */
public class AbsoluteColor {
	
	// we store internally color information as CIE xyY color coordinates
	private final Chromaticity chroma; // xy
	private final double Y;

	/**
	 * This creates an AbsoluteColor from sRGB values
	 */
	public AbsoluteColor(int r, int g, int b){
		float[] rgb = {r,g,b};
		float[] XYZ = SrgbConverter.RGBtoXYZ(rgb);
		
		// XYZ to xyY conversion
		double x = XYZ[0]/(XYZ[0]+XYZ[1]+XYZ[2]);
		double y = XYZ[1]/(XYZ[0]+XYZ[1]+XYZ[2]);
		this.Y = XYZ[1];
		
		chroma = new Chromaticity(x, y);
	}
	

	public AbsoluteColor(Chromaticity chroma, double Y){
		this.chroma = chroma;
		this.Y=Y;
	}
	
	
	public double getx(){
		return chroma.getx();
	}
	public double gety(){
		return chroma.gety();
	}
	public double getYlumi(){
		return Y;
	}
	
	public Chromaticity getChromaticity(){
		return chroma;
	}
	
}
