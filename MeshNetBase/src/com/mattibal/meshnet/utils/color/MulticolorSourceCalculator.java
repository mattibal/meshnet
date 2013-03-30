package com.mattibal.meshnet.utils.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

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
	
	private final HashSet<LightSourcesTriangle> preferredTriangles = new HashSet<LightSourcesTriangle>();;
	private final HashSet<LightSourcesTriangle> otherTriangles = new HashSet<LightSourcesTriangle>();
	
	/**
	 * Generates a calculator based on the given light sources
	 * 
	 * @param preferredLightSource Usually the white LED
	 * @param otherLightSources Usually all LEDs except white
	 */
	public MulticolorSourceCalculator(LightSource preferredLightSource, 
			HashSet<LightSource> otherLightSources){
		
		// Generate triangles, one for every combination of light sources
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
		}
		
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
		
		double[] mix = null;
		LightSourcesTriangle okTriang = null;
		
		for(LightSourcesTriangle triang : preferredTriangles){
			mix = triang.calculateColorMix(color);
			if(mix != null){
				okTriang = triang;
				break;
			}
		}
		if(mix == null){
			for(LightSourcesTriangle triang : otherTriangles){
				mix = triang.calculateColorMix(color);
				if(mix != null){
					okTriang = triang;
					break;
				}
			}
		}
		if(mix == null){
			return null;
		} else {
			HashMap<LightSource, Double> resultMap = new HashMap<LightSource, Double>();
			for(int i=0; i<3; i++){
				resultMap.put(okTriang.getLightSources()[i], mix[i]);
			}
			return resultMap;
		}
	}

}
