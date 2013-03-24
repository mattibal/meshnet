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
	 * An ordered set of absolute colors (points in the xy chromaticity diagram)
	 * that identify the light sources.
	 * 
	 * The Y in these object is the maximum luminance the light source
	 * can output.
	 * 
	 * I used a TreeSet because they should be ordered, so I can associate an
	 * index needed for matrix calculations.
	 */
	private final TreeSet<AbsoluteColor> vertices;
	
	public ColorTriangle(TreeSet<AbsoluteColor> vertices){
		if(vertices.size() != 3){
			throw new IllegalArgumentException("a triangle has 3 vertices");
		}
		this.vertices = vertices;
	}
	
	
	/**
	 * Generates three AbsoluteColors that if they are mixed
	 * they will produce the AbsoluteColor specified as parameter.
	 * If the luminance of one of the three output AbsoluteColors is greater
	 * than the maximum allowed by the corresponding light source,
	 * the luminance of the resulting mixed color will be reduced, but
	 * the chrominance will be the same.
	 * @param mixColor The color I want to obtain
	 */
	public TreeSet<AbsoluteColor> calculateColorMix(AbsoluteColor mixColor){
		
		double[][] matrixVal = new double[3][3];
		
		// riempi valori
		
		Matrix matrix = new Matrix(matrixVal);
		Matrix invMatrix = matrix.inverse();
		
		
		
	}
	

}
