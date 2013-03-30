package com.mattibal.meshnet.utils.color;

/**
 * This represent a chromaticity value, a point in the CIE xy chromaticity
 * diagram
 */
public class Chromaticity {
	
	private final double x;
	private final double y;
	
	public Chromaticity(double x, double y){
		this.x = x;
		this.y = y;
	}

	public double getx(){
		return x;
	}
	public double gety(){
		return y;
	}
	
	public double getDistanceFrom(Chromaticity other){
		// Pythagorean theorem
		return Math.sqrt( Math.pow(getx()-other.getx(),2) + Math.pow(gety()-other.gety(),2) );
	}
}
