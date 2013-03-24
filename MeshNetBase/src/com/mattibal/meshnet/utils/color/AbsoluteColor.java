package com.mattibal.meshnet.utils.color;

import java.util.Comparator;

public class AbsoluteColor {
	
	// CIE xyY color coordinates
	private final double x;
	private final double y;
	private final double Y;

	/**
	 * This creates an AbsoluteColor from sRGB values
	 */
	public AbsoluteColor(short r, short g, short b){
		float[] rgb = {r,g,b};
		float[] XYZ = SrgbConverter.RGBtoXYZ(rgb);
		
		// XYZ to xyY conversion
		this.x = XYZ[0]/(XYZ[0]+XYZ[1]+XYZ[2]);
		this.y = XYZ[1]/(XYZ[0]+XYZ[1]+XYZ[2]);
		this.Y = XYZ[1];
	}

	public AbsoluteColor(double x, double y, double Y){
		this.x=x;
		this.y=y;
		this.Y=Y;
	}
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AbsoluteColor){
			AbsoluteColor other = (AbsoluteColor) obj;
			return this.x == other.x && this.y == other.y && this.Y == other.Y;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		// TODO maybe the conversion from float to int here looses too much information
		double sum = x+y+Y;
		return (int) sum;
	}
	
	
	
	/**
	 * This comparator lets you order many AbsoluteColor object by how
	 * much they are distant from another AbsoluteColor in the
	 * xy chromaticity diagram, specified in the constructor of this class.
	 */
	protected class DistanceComparator implements Comparator<AbsoluteColor> {
		
		/**
		 * @param referenceColor The color on which are measured the distance
		 * between the two colors you want to compare
		 */
		public DistanceComparator(AbsoluteColor referenceColor) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public int compare(AbsoluteColor o1, AbsoluteColor o2) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}

}
