package com.mattibal.meshnet.utils.color;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import Jama.Matrix;

/**
 * This class represent a triangle in the CIE xy chromaticity diagram.
 * 
 * Every vertex of the triangle is a light source, for example a colored LED.
 * For every vertex, this object should contain the xy coordinates (the
 * chromaticity of the light source) and the maximum Y (the maximum luminance
 * measured in lumen that the light source can output).
 */
public class ColorTriangle {
	
	/**
	 * A set of absolute colors (points in the xy chromaticity diagram)
	 * that identify the light sources.
	 * 
	 * The Y in these object is the maximum luminance the light source
	 * can output.
	 */
	private final AbsoluteColor[] vertices;
	
	public ColorTriangle(AbsoluteColor[] vertices){
		if(vertices.length != 3){
			throw new IllegalArgumentException("a triangle has 3 vertices");
		}
		this.vertices = vertices;
	}
	
	
	/**
	 * Generates the luminance values of the light sources that if they are mixed
	 * they will produce the AbsoluteColor specified as parameter.
	 * If the luminance of one of the three output AbsoluteColors is greater
	 * than the maximum allowed by the corresponding light source,
	 * the luminance of the resulting mixed color will be reduced, but
	 * the chrominance will be the same.
	 * 
	 * @param mixColor The color I want to obtain
	 * @return null if the light sources can't produce the mix you want
	 * (it's out of gamut)
	 */
	public double[] calculateColorMix(AbsoluteColor mixColor){
		
		double[][] matrixVal = new double[3][3];
		
		for(int col = 0; col<3; col++){
			matrixVal[0][col] = (vertices[col].getx() - mixColor.getx()) / vertices[col].gety();
			matrixVal[1][col] = (vertices[col].gety() - mixColor.gety()) / vertices[col].gety();
			matrixVal[2][col] = 1;
		}
		
		Matrix matrix = new Matrix(matrixVal);
		Matrix invMatrix = matrix.inverse();
		
		// the needed luminances of the light sources to produce the color mix
		double[] outLumi = new double[3];
		
		for(int i=0; i<3; i++){
			outLumi[i] = invMatrix.get(i, 2) * mixColor.getYlumi();
			// check if the luminance is negative
			if(outLumi[i] < 0){
				return null;
			}
		}
		
		// If the luminance is greater than the max lumen output of the light
		// source, linearly scale down all the luminance values
		
		// Calculate the needed luminance scale factor
		double scaleFactor = 1;
		for(int i=0; i<3; i++){
			double lumi = outLumi[i];
			double maxLumi = vertices[i].getYlumi();
			if(lumi > maxLumi){
				double myScaleFactor = maxLumi / lumi;
				if(myScaleFactor < scaleFactor){
					scaleFactor = myScaleFactor;
				}
			}
		}
		
		// apply the scale factor to output luminance values
		if(scaleFactor != 1){
			for(int i=0; i<3; i++){
				outLumi[i] = outLumi[i] * scaleFactor;
			}
		}
		
		return outLumi;
	}
	

}
