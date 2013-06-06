package com.mattibal.meshnet.utils.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import Jama.Matrix;

/**
 * This is an utility to calculate the luminances that a set of different
 * colored light source must have to generate a given color when their light
 * is mixed.
 * 
 * A common use case is controlling a LED lamp made with a RGBW (W = white)
 * and/or RGBA (A = amber) high brightness LED.
 * In this case from the resulting luminance of each LED can be calculated
 * the PWM duty cycle of the LEDs.
 * 
 * It also can prioritize a certain light source, for example the white LED
 * in an RGBW array, that has a wider spectrum, to get a better color rendering.
 */
public class MulticolorSourceCalculator {
	
	//private final HashSet<LightSourcesTriangle> preferredTriangles = new HashSet<LightSourcesTriangle>();;
	//private final HashSet<LightSourcesTriangle> otherTriangles = new HashSet<LightSourcesTriangle>();

	private final LightSource[] sources;
	
	/**
	 * Generates a calculator based on the given light sources
	 * 
	 * @param preferredLightSource Usually the white LED
	 * @param otherLightSources Usually all LEDs except white
	 */
	public MulticolorSourceCalculator(LightSource[] sources){
		
		/*// Generate triangles, one for every combination of light sources
		HashSet<LightSource> allSources = (HashSet<LightSource>) otherLightSources.clone();
		allSources.add(preferredLightSource);
		ICombinatoricsVector<LightSource> initialVector =
				Factory.createVector(allSources);
		Generator<LightSource> gen = Factory.createSimpleCombinationGenerator(initialVector, 3);
		for(ICombinatoricsVector<LightSource> combination : gen){
			// This is executed at every combination
			
			// Create the triangle
			LightSourcesTriangle triang = 
					new LightSourcesTriangle(combination.getVector().toArray(new LightSource[0]));
			// add in the proper list
			if(combination.contains(preferredLightSource)){
				preferredTriangles.add(triang);
			} else {
				otherTriangles.add(triang);
			}
		}*/
		
		this.sources = sources;
		
	}
	
	/*
	 * TODO la distanza tra due punti nel piano xy mi sa che non serve mai
	 * calcolarla... quando uno chiede di calcolare il mix per un certo
	 * colore (punto xy) basta chiedere a tutti i triangoli possibili di 
	 * calcolare quel mix, quelli a cui il punto non appartiene ritorneranno
	 * null e verranno già esclusi, tra quelli in cui invece viene un
	 * risultato buono, bisognerebbe sceglierli (eventualmente "sommarli"??)
	 * in base ad una scala di priorità, che sarebbe quante lightsource
	 * "preferite" hanno dentro di loro (potrebbero esserci per esempio
	 * più bianchi, tutti preferiti)
	 */
	
	
	/**
	 * Calculates the luminances that the light sources must have to obtain
	 * the best possible rendering of the color specified as argument.
	 * 
	 * @return The luminance values of every light source
	 */
	public HashMap<LightSource, Double> getSourceLumiForColor(AbsoluteColor color){
		
		AbsoluteColor requestedColor = color;
		
		HashMap<LightSource, Double> residueSourceLumen = new HashMap<LightSource, Double>();
		for(LightSource source : sources){
			residueSourceLumen.put(source, source.getMaxLumi());
		}
		
		boolean solved = false;
		
		// Looping through triangles
		outerLoop:
		for(int a=0; a < sources.length; a++){
			for(int b=a+1; b < sources.length; b++){
				for(int c=b+1; c < sources.length; c++){
					// Calculate for this triangle the weights of light sources
					LightSource[] triangSources = {sources[a], sources[b], sources[c]};
					double[] triangResidueLumen = {residueSourceLumen.get(sources[a]),
						residueSourceLumen.get(sources[b]), residueSourceLumen.get(sources[c])
					};
					double[] triangLumens = calculateTriangleLumen(triangSources, triangResidueLumen, requestedColor);
					if(triangLumens != null){
						// Substract residue lumens of each source of the triangle
						residueSourceLumen.put(sources[a], residueSourceLumen.get(sources[a]) - triangLumens[0]);
						residueSourceLumen.put(sources[b], residueSourceLumen.get(sources[b]) - triangLumens[1]);
						residueSourceLumen.put(sources[c], residueSourceLumen.get(sources[c]) - triangLumens[2]);
						// Substract the sum of lumen of each source from requested lumens
						double triangleSum = triangLumens[0] + triangLumens[1] + triangLumens[2];
						requestedColor = new AbsoluteColor(requestedColor.getChromaticity(), requestedColor.getYlumi() - triangleSum);
						// Check if I satisfied all the requested lumens
						if(requestedColor.getYlumi()==0){
							solved = true;
							break outerLoop;
						}
					}
				}
			}
		}
		
		if(solved==false){
			return null;
		}
		HashMap<LightSource, Double> output = new HashMap<LightSource, Double>();
		for(LightSource source : sources){
			output.put(source, source.getMaxLumi() - residueSourceLumen.get(source));
		}
		return output;
		
	}
	
	
	private double[] calculateTriangleLumen(LightSource[] vertices, double[] verticesResidueLumen, AbsoluteColor mixColor){

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
			double maxLumi = verticesResidueLumen[i];
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
