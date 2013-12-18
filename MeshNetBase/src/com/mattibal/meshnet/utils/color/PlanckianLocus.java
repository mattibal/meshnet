package com.mattibal.meshnet.utils.color;

/**
 * Calculates the xy coordinates of a point of the Planckian locus in the CIE 1931 chromaticity chart,
 * given the white temperature of the point in Kelvin
 *
 * The formulas are taken from here:
 * http://en.wikipedia.org/wiki/Planckian_locus
 */
public class PlanckianLocus {
	
	double x, y;
	
	
	public void calculate(double temp) throws IllegalArgumentException {
		
		if(temp<1667 || temp>25000){
			throw new IllegalArgumentException();
		}
		
		if(temp<4000){
			x = 0.17991 - (0.2661239*((1000000000)/(Math.pow(temp,3)))) - (0.2343580*((1000000)/(Math.pow(temp,2)))) + (0.8776956*((1000)/temp));
		} else {
			x = 0.24039 - (3.0258469*((1000000000)/(Math.pow(temp,3)))) + (2.1070379*((1000000)/(Math.pow(temp,2)))) + (0.2226347*((1000)/temp)); 
		}
		
		
		if(temp<2222){
			y = (2.18555832*x) - (1.1063814*Math.pow(x,3)) - (1.34811020*Math.pow(x,2)) - 0.20219683;
		} else if(temp<4000){
			y = (2.09137015*x) - (0.9549476*Math.pow(x,3)) - (1.37418593*Math.pow(x,2)) - 0.16748867;
		} else {
			y = (3.75112997*x) + (3.081758*Math.pow(x,3)) - (5.8733867*Math.pow(x,2)) - 0.37001483;
		}
	}
	
	
	public double getx(){
		return x;
	}
	
	public double gety(){
		return y;
	}
	

}
