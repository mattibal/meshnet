package com.mattibal.meshnet.utils.color;

import java.awt.Color;

import com.mattibal.meshnet.utils.color.ColourConverter.WhitePoint;

//*****************************************************************************************
/** Utilities for handling the CIELuv colour space. This space is approximately perceptually
*  uniform in that two colours a fixed distance apart in the colour space should be equally
*  distinguishable from each other regardless of their location. 
*  <br /><br />
*  For details see <a href="http://en.wikipedia.org/wiki/CIELUV_color_space" target="_blank">
*  en.wikipedia.org/wiki/CIELUV_color_space</a>. 
*  <br /><br />
*  Includes methods for creating colour tables from this space that maximise the use of the
*  colour space with similar appearance to Brewer colour schemes. This is based on the 
*  process described by <b>Wijffelaars, Vliegen, van Wijk and van der Linden</b> (2008)
*  Generating color palettes using intuitive parameters, <i>Computer Graphics Forum,27(3)</i>.
*  @author Jo Wood, giCentre, City University London.
*  @version 3.2, 1st August, 2011. 
*/ 
//*****************************************************************************************

/* This file is part of giCentre utilities library. gicentre.utils is free software: you can 
* redistribute it and/or modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation, either version 3 of the License, or (at your
* option) any later version.
* 
* gicentre.utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License along with this
* source code (see COPYING.LESSER included with this source code). If not, see 
* http://www.gnu.org/licenses/.
*/

public class CIELuv 
{
 // ---------------------------- Class and object Variables ----------------------------
     
 private WhitePoint wp;          // Whitepoint used for colour conversion.
 
 //private double xn,yn,zn;
 //private double un,vn; 
 
 // Lookup tables for 'most saturated colour' for any give hue.
 private static double[][] msc = {{1,0,0.431}, {1,0,0.415}, {1,0,0.397}, {1,0,0.379}, {1,0,0.359}, {1,0,0.338},  // 0 deg (red)
                                  {1,0,0.314}, {1,0,0.289}, {1,0,0.260}, {1,0,0.226}, {1,0,0.185}, {1,0,0.131},
                                  {1,0,0.029}, {1,0.108,0}, {1,0.169,0}, {1,0.213,0}, {1,0.248,0}, {1,0.278,0},
                                  {1,0.303,0}, {1,0.327,0}, {1,0.348,0}, {1,0.368,0}, {1,0.386,0}, {1,0.403,0},
                                  {1,0.419,0}, {1,0.434,0}, {1,0.449,0}, {1,0.462,0}, {1,0.475,0}, {1,0.489,0},
                                  {1,0.501,0}, {1,0.513,0}, {1,0.523,0}, {1,0.535,0}, {1,0.546,0}, {1,0.556,0},
                                  {1,0.566,0}, {1,0.576,0}, {1,0.586,0}, {1,0.596,0}, {1,0.605,0}, {1,0.614,0},
                                  {1,0.624,0}, {1,0.633,0}, {1,0.641,0}, {1,0.650,0}, {1,0.659,0}, {1,0.667,0},
                                  {1,0.675,0}, {1,0.683,0}, {1,0.691,0}, {1,0.699,0}, {1,0.708,0}, {1,0.716,0},
                                  {1,0.724,0}, {1,0.732,0}, {1,0.739,0}, {1,0.747,0}, {1,0.755,0}, {1,0.763,0},  // 60 deg (yellow)
                                  {1,0.771,0}, {1,0.779,0}, {1,0.787,0}, {1,0.795,0}, {1,0.803,0}, {1,0.811,0},
                                  {1,0.819,0}, {1,0.827,0}, {1,0.835,0}, {1,0.843,0}, {1,0.852,0}, {1,0.860,0},
                                  {1,0.868,0}, {1,0.877,0}, {1,0.885,0}, {1,0.894,0}, {1,0.903,0}, {1,0.913,0}, 
                                  {1,0.922,0}, {1,0.931,0}, {1,0.94,0},  {1,0.95,0},  {1,0.96,0},  {1,0.969,0},
                                  {1,0.98,0},  {1,0.99,0},  {0.998,1,0}, {0.987,1,0}, {0.976,1,0}, {0.965,1,0},
                                  {0.954,1,0}, {0.943,1,0}, {0.932,1,0}, {0.921,1,0}, {0.909,1,0}, {0.898,1,0},
                                  {0.886,1,0}, {0.874,1,0}, {0.862,1,0}, {0.85,1,0},  {0.838,1,0}, {0.825,1,0},
                                  {0.811,1,0}, {0.798,1,0}, {0.784,1,0}, {0.771,1,0}, {0.757,1,0}, {0.742,1,0},
                                  {0.727,1,0}, {0.712,1,0}, {0.695,1,0}, {0.678,1,0}, {0.661,1,0}, {0.644,1,0},
                                  {0.625,1,0}, {0.605,1,0}, {0.584,1,0}, {0.563,1,0}, {0.539,1,0}, {0.514,1,0},  // 120 deg (green)
                                  {0.487,1,0}, {0.459,1,0}, {0.426,1,0}, {0.391,1,0}, {0.35,1,0},  {0.302,1,0},
                                  {0.241,1,0}, {0.151,1,0}, {0,1,0.084}, {0,1,0.205}, {0,1,0.273}, {0,1,0.324},
                                  {0,1,0.366}, {0,1,0.401}, {0,1,0.431}, {0,1,0.459}, {0,1,0.484}, {0,1,0.507},
                                  {0,1,0.527}, {0,1,0.547}, {0,1,0.565}, {0,1,0.581}, {0,1,0.598}, {0,1,0.612},
                                  {0,1,0.627}, {0,1,0.641}, {0,1,0.653}, {0,1,0.666}, {0,1,0.677}, {0,1,0.689},
                                  {0,1,0.699}, {0,1,0.71},  {0,1,0.72},  {0,1,0.73},  {0,1,0.74},  {0,1,0.749},
                                  {0,1,0.758}, {0,1,0.767}, {0,1,0.775}, {0,1,0.784}, {0,1,0.792}, {0,1,0.8},
                                  {0,1,0.807}, {0,1,0.814}, {0,1,0.822}, {0,1,0.829}, {0,1,0.837}, {0,1,0.844},
                                  {0,1,0.851}, {0,1,0.857}, {0,1,0.864}, {0,1,0.871}, {0,1,0.877}, {0,1,0.884},
                                  {0,1,0.89},  {0,1,0.897}, {0,1,0.903}, {0,1,0.909}, {0,1,0.915}, {0,1,0.921},  // 180 deg (cyan)
                                  {0,1,0.928}, {0,1,0.933}, {0,1,0.939}, {0,1,0.945}, {0,1,0.951}, {0,1,0.958},
                                  {0,1,0.963}, {0,1,0.969}, {0,1,0.975}, {0,1,0.98},  {0,1,0.987}, {0,1,0.992},
                                  {0,1,0.999}, {0,0.994,1}, {0,0.989,1}, {0,0.984,1}, {0,0.977,1}, {0,0.971,1},
                                  {0,0.966,1}, {0,0.961,1}, {0,0.955,1}, {0,0.948,1}, {0,0.943,1}, {0,0.938,1},
                                  {0,0.932,1}, {0,0.927,1}, {0,0.921,1}, {0,0.916,1}, {0,0.909,1}, {0,0.904,1},
                                  {0,0.898,1}, {0,0.893,1}, {0,0.888,1}, {0,0.882,1}, {0,0.875,1}, {0,0.87,1},
                                  {0,0.865,1}, {0,0.858,1}, {0,0.852,1}, {0,0.847,1}, {0,0.84,1},  {0,0.835,1},
                                  {0,0.828,1}, {0,0.822,1}, {0,0.816,1}, {0,0.809,1}, {0,0.803,1}, {0,0.796,1},
                                  {0,0.789,1}, {0,0.782,1}, {0,0.776,1}, {0,0.769,1}, {0,0.762,1}, {0,0.754,1},
                                  {0,0.746,1}, {0,0.739,1}, {0,0.731,1}, {0,0.723,1}, {0,0.714,1}, {0,0.705,1},  // 240 deg (blue)
                                  {0,0.697,1}, {0,0.687,1}, {0,0.678,1}, {0,0.668,1}, {0,0.658,1}, {0,0.648,1},
                                  {0,0.637,1}, {0,0.625,1}, {0,0.613,1}, {0,0.601,1}, {0,0.588,1}, {0,0.574,1},
                                  {0,0.559,1}, {0,0.543,1}, {0,0.527,1}, {0,0.509,1}, {0,0.49,1},  {0,0.47,1},
                                  {0,0.448,1}, {0,0.423,1}, {0,0.397,1}, {0,0.366,1}, {0,0.33,1},  {0,0.288,1},
                                  {0.096,0.257,1}, {0.181,0.243,1}, {0.237,0.229,1}, {0.28,0.213,1},  {0.317,0.196,1}, {0.35,0.178,1},
                                  {0.379,0.157,1}, {0.406,0.132,1}, {0.431,0.101,1}, {0.454,0.058,1}, {0.477,0.007,1}, {0.503,0.007,1},
                                  {0.526,0.006,1}, {0.549,0.005,1}, {0.57,0.003,1}, {0.591,0.002,1}, {0.61,0.003,1},  {0.629,0,1},
                                  {0.646,0,1}, {0.664,0,1}, {0.681,0,1}, {0.697,0,1}, {0.712,0,1}, {0.728,0,1},
                                  {0.743,0,1}, {0.758,0,1}, {0.772,0,1}, {0.787,0,1}, {0.8,0,1},   {0.814,0,1},
                                  {0.827,0,1}, {0.841,0,1}, {0.854,0,1}, {0.867,0,1}, {0.88,0,1},  {0.892,0,1},  // 300 deg (magenta)
                                  {0.904,0,1}, {0.917,0,1}, {0.93,0,1},  {0.942,0,1}, {0.954,0,1}, {0.967,0,1},
                                  {0.979,0,1}, {0.991,0,1}, {1,0,0.997}, {1,0,0.985}, {1,0,0.972}, {1,0,0.961},
                                  {1,0,0.95},  {1,0,0.938}, {1,0,0.928}, {1,0,0.916}, {1,0,0.906}, {1,0,0.896},
                                  {1,0,0.885}, {1,0,0.875}, {1,0,0.865}, {1,0,0.855}, {1,0,0.845}, {1,0,0.836},
                                  {1,0,0.826}, {1,0,0.816}, {1,0,0.806}, {1,0,0.796}, {1,0,0.787}, {1,0,0.778},
                                  {1,0,0.768}, {1,0,0.759}, {1,0,0.749}, {1,0,0.74},  {1,0,0.73},  {1,0,0.721},
                                  {1,0,0.711}, {1,0,0.701}, {1,0,0.692}, {1,0,0.682}, {1,0,0.672}, {1,0,0.662},
                                  {1,0,0.652}, {1,0,0.642}, {1,0,0.631}, {1,0,0.621}, {1,0,0.61},  {1,0,0.599},
                                  {1,0,0.588}, {1,0,0.577}, {1,0,0.565}, {1,0,0.555}, {1,0,0.543}, {1,0,0.53},
                                  {1,0,0.518}, {1,0,0.505}, {1,0,0.491}, {1,0,0.477}, {1,0,0.462}, {1,0,0.447},
                                  {1,0,0.431}};                                                                  // 360 deg (red)

 // ---------------------------------- Constructors ------------------------------------
 
 /** Creates a CIELuv converter using the default D65 illuminant with 2 degree observer
  *  position.
  */
 public CIELuv()
 {
     this(WhitePoint.D65);
 }

 /** Creates a CIELuv converter using the given reference calibration settings. These 
  *  allow different whitepoints to be set.
  *  @param wp Colour calibration reference to use in the CIELuv conversion.
  */ 
 CIELuv(WhitePoint wp)
 {       
     this.wp = wp;
     
     // RESTORE IF WE CALCULATE MSC ANALYTICALLY
     //xn = wp.getTristimulus()[0];
     //yn = wp.getTristimulus()[1];
     //zn = wp.getTristimulus()[2];
     
     //un = (4*xn)/(xn + 15*yn + 3*zn);
     //vn = (9*yn)/(xn + 15*yn + 3*zn);
 }
     
 // ------------------------------------ Methods ---------------------------------------
 
 /** Finds the CIELuv triplet representing the given colour. CIELuv L value scaled between 0-100, 
  *  and u and v values scaled between -100 and 100.
  *  @param colour Colour to convert.
  *  @return Vector holding CIELuv values where the x and y components hold the <i>u</i>
  *          and <i>v</i> values respectively and the z values holds the <i>L</i> value.
  */
 public double[] getLuv(Color colour) 
 {
     double[] Luv = ColourConverter.getLuv(colour, wp);
     double[] ret = {Luv[1],Luv[2],Luv[0]};
     return ret;
 }
 
 /** Finds the CIELCh triplet representing the given colour. CIELCh L value scaled between 0-100, 
  *  and C between 0 and 100 and h the colour wheel between 0 and 360 degrees.
  *  @param colour Colour to convert.
  *  @return Vector holding CIELCh values where the x and y components hold the <i>C</i>
  *          and <i>h</i> values respectively and the z values holds the <i>L</i> value.
  */
 public double[] getLCh(Color colour) 
 {
     // TODO: What is C scaled between?
     
     double[] Luv = ColourConverter.getLuv(colour, wp);
     double[] LCh = ColourConverter.LuvToLCh(Luv);
     double[] ret = {LCh[1],LCh[2],LCh[0]};
     return ret;
 }
  
 /** Finds the colour corresponding to the given CIELuv triplet. Out-of-gamut colours  will be
  *  returned as null if <code>useNearest</code> is false, otherwise the approximate nearest
  *  visible colour will be returned. CIELuv L values should be scaled between 0-100, 
  *  and a and b values scaled between -100 and 100. 
  *  @param L CIELuv L value scaled between 0 and 100.
  *  @param u CIELuv u value scaled between -100 and 100.
  *  @param v CIELiv v value scaled between -100 and 100.
  *  @param useNearest If true, out-of-gamut colours will be converted to their nearest visible 
  *                    equivalent. If not, out-of-gamut colours returned as null.
  *  @return Colour representing the given Luv values.
  */
 public Color getColour(double L, double u, double v, boolean useNearest)
 {
     double[] rgb = ColourConverter.luvToRGB(L, u, v, wp);
     double[] Luv = new double[] {L,u,v};
     
     if (useNearest)
     {
         return findNearest(rgb, Luv);
     }
     else if ((rgb[0] < 0) || (rgb[1] < 0) || (rgb[2] < 0) || (rgb[0] > 1) || (rgb[1] > 1) || (rgb[2] >1))
     {
         return null;
     }
     return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);        
 }
 
 /** Finds the colour corresponding to the given CIELCh triplet. Out-of-gamut colours  will be
  *  returned as null. CIE LCh L values should be scaled between 0-100, C (Chroma) values scaled 
  *  between -100 and 100 and h between 0 and 360 degrees.
  *  @param L CIELuv Lightness value scaled between 0 and 100.
  *  @param C CIELuv Chroma value scaled between 0 and 100.
  *  @param h CIELiv Hue value scaled between 0 and 360 degrees.
  *  @return Colour representing the given LCh values or null if the colour is out-of-gamut.
  */
 public Color getColourFromLCh(double L, double C, double h)
 {
     // TODO: What is C scaled between?
     
     return getColourFromLCh(L,C,h,false);
 }
 
 /** Finds the colour corresponding to the given CIELCh triplet. If <code>findNearest</code> is 
  *  true, out-of-gamut colours will be clamped to their nearest visible colour, if not, out-of-gamut
  *  colours are returned as null. CIE LCh L values should be scaled between 0-100, C (Chroma) values
  *  scaled between 0 and 100 and h between 0 and 360 degrees.
  *  @param L CIELuv Lightness value scaled between 0 and 100.
  *  @param C CIELuv Chroma value scaled between 0 and 100.
  *  @param h CIELiv Hue value scaled between 0 and 360 degrees.
  *  @param findNearest If true, nearest visible colour will be found.
  *  @return Colour representing the given LCh values.
  */
 public Color getColourFromLCh(double L, double C, double h, boolean findNearest)
 {
     // TODO: What is C scaled between?
     
     if (L >=100)
     {
         return Color.WHITE;
     }
     
     double[] Luv = ColourConverter.LChToLuv(new double[] {L,C,h});
     double[] rgb = ColourConverter.luvToRGB(Luv[0],Luv[1],Luv[2], wp);

     if (findNearest)
     {
         // Clamp RGB values in case of rounding errors or approximations
         rgb[0] = Math.max(rgb[0],0);
         rgb[1] = Math.max(rgb[1],0);
         rgb[2] = Math.max(rgb[2],0);
         rgb[0] = Math.min(rgb[0],1);
         rgb[1] = Math.min(rgb[1],1);
         rgb[2] = Math.min(rgb[2],1);
     }
     else if ((rgb[0] < 0) || (rgb[1] < 0) || (rgb[2] < 0) || (rgb[0] > 1) || (rgb[1] > 1) || (rgb[2] >1))
     {
         return null;
     }
     
     return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);        
 }
 
 /** Provides a single hue sequential scheme using the given hue and having the given number of colours.
  *  This version uses a default saturation value of 0.6 and brightness of 0.75.
  *  @param hue Hue upon which to base the scheme. This should be a value from 0 to 360 degrees.
  *  @param numColours Number of colour values in the colour table, or 0 for continuous scheme.
  *  @return Colour table representing the sequential scheme.
  */
 /*public ColourTable getSequential(double hue, int numColours)
 {
     return getSequential(hue,0.6,0.75, numColours);
 }*/
    
 /** Provides a single hue sequential scheme using the given hue and having the given number of colours.
  *  This version allows the saturation and brightness of the scheme to be set.
  *  @param hue Hue upon which to base the scheme. This should be a value from 0 to 360 degrees.
  *  @param saturation Saturation of the scheme scaled between 0-1.
  *  @param brightness Brightness of the scheme scaled between 0-1.
  *  @param numColours Number of colour values in the colour table, or 0 for continuous scheme.
  *  @return Colour table representing the sequential scheme.
  */
 /*public ColourTable getSequential(double hue, double saturation, double brightness, int numColours)
 {
     return getSequential(hue,hue,saturation,brightness,numColours);
 }*/
 
 /** Provides a multi-hue sequential scheme using the given hues and having the given number of colours.
  *  This version allows the saturation and brightness of the scheme to be set.
  *  @param hue1 First hue upon which to base the scheme. This should be a value from 0 to 360 degrees.
  *  @param hue2 Second hue upon which to base the scheme. This should be a value from 0 to 360 degrees.
  *  @param saturation Saturation of the scheme scaled between 0-1.
  *  @param brightness Brightness of the scheme scaled between 0-1.
  *  @param numColours Number of colour values in the colour table, or 0 for continuous scheme.
  *  @return Colour table representing the sequential scheme.
  */
 /*public ColourTable getSequential(double hue1, double hue2, double saturation, double brightness, int numColours)
 {
     boolean isContinuous = false;
     int validNumColours = numColours;
     
     if (validNumColours <=0)
     {
         isContinuous = true;
         validNumColours = 12;
     }
     ColourTable cTable = new ColourTable();     
     double c = Math.min(0.88,0.34+0.06*validNumColours);
     
     // Define the surface by the three corner points in LCh space
     double[] p0 = new double[] {0,0,hue1};
     
     double[] rgbMSC = getMostSaturatedColour(hue1);
//System.err.println("MSC rgb is "+rgbMSC[0]+","+rgbMSC[1]+","+rgbMSC[2]);
     double[] Luv = ColourConverter.getLuv(new Color((float)rgbMSC[0],(float)rgbMSC[1],(float)rgbMSC[2]), wp);
//System.err.println("MSC Luv is "+Luv[0]+","+Luv[1]+","+Luv[2]);
     double[] p1 = ColourConverter.LuvToLCh(Luv);
             
     double[] p2;
//System.err.println("MSC hue from LCh is "+p1[2]);    
     if (Math.abs(hue1-hue2) < 1)
     {
         // Single hue scheme
         p2 = new double[] {100,0,hue1};
     }
     else
     {
         // Multi-hue scheme.
         
         double w = 0;
         double pL = 50; // TODO: Should this be user defined?
         double[] pb = ColourConverter.LuvToLCh(ColourConverter.getLuv(Color.yellow, wp));
// System.err.println("Pb (LCh): "+pb[0]+","+pb[1]+","+pb[2]);
         
         double[] pMid = ColourConverter.RGBToHSL(getMostSaturatedColour(hue2));
         // Scale HSL to 0-100
         pMid[0] *= 100;
         pMid[1] *= 100;
         pMid[2] *= 100;
         double[] pEnd;
         if (pL <= pMid[2])
         {
             pEnd = new double[]{0,0,hue2};
         }
         else
         {
             pEnd = new double[]{100,0,hue2};
         }
         double alpha = (pEnd[0]-pL)/(pEnd[0]+pMid[2]);
         double M = (180+ pb[2] - hue1)%360 - 180;
         
         double sMax = alpha*(pMid[1]-pEnd[1])+pEnd[1];
         
         p2 = new double[3];
         p2[0] = 100*(1-w) + w*pb[0];
         p2[1] = Math.min(sMax,w*saturation*pb[1]);
         p2[2] = ((hue2+alpha*M)%360);
//System.err.println("p2LSH: "+p2[0]+","+p2[1]+","+p2[2]+" when h1 is "+hue1+" and h2 is "+hue2+" and M is "+M);
     }
     
     //System.err.println("MSC: "+rgbMSC[0]+","+rgbMSC[1]+","+rgbMSC[2]+"      "+p1[0]+","+p1[1]+","+Math.round(p1[2]*180/Math.PI));
     
     // Control points of the Bezier curve through the triangular space defined by p0,p1 and p2.
     double[] q0 = new double[] {p0[0]*(1-saturation) + p1[0]*saturation, p0[1]*(1-saturation) + p1[1]*saturation, p0[2]*(1-saturation) + p1[2]*saturation};
     double[] q2 = new double[] {p2[0]*(1-saturation) + p1[0]*saturation, p2[1]*(1-saturation) + p1[1]*saturation, p2[2]*(1-saturation) + p1[2]*saturation};
     double[] q1 = new double[] {0.5*(q0[0]+q2[0]),     0.5*(q0[1]+q2[1]),     0.5*(q0[2]+q2[2])};
     
     for (int i=0; i<validNumColours; i++)
     {
         double t = (double)i/(validNumColours-1);

         double Lt = 125 - 125*Math.pow(0.2, (1-c)*brightness + t*c);
         double TLt = T(Lt, p0[0], p2[0], q0[0], q1[0], q2[0]);
     
         double[] colourLCh;
         if (TLt <=0.5)
         {
             colourLCh = B(p0,q0,q1,2*TLt);
         }
         else
         {
             colourLCh = B(q1,q2,p2,2*(TLt-0.5));
         }
         
//System.err.println(Math.round(t*10)/10.0+" Lt: "+Math.round(Lt)+", TLt: "+Math.round(TLt*100)/100.0+" LC: "+Math.round(colourLCh[0])+","+Math.round(colourLCh[1]));
         
         Color colour = getColourFromLCh(colourLCh[0],colourLCh[1],colourLCh[2],true);
//System.err.println(" "+colour);       
         if (colour == null)
         {   
             if (isContinuous)
             {
                 cTable.addContinuousColourRule((float)i/(validNumColours-1), 255,255,255);
             }
             else
             {
                 cTable.addDiscreteColourRule(i+1, 255,255,255);
             }
         }
         else
         {
             if (isContinuous)
             {
                 cTable.addContinuousColourRule((float)i/(validNumColours-1),colour.getRed(), colour.getGreen(), colour.getBlue());
             }
             else
             {
                 cTable.addDiscreteColourRule(i+1, colour.getRed(), colour.getGreen(), colour.getBlue());
             }
         }
     }
     return cTable;
 }*/
 
 private double[] B(double[]b0, double[]b1,double[]b2,double t)
 {
     return new double[] {(1-t)*(1-t)*b0[0] + 2*(1-t)*t*b1[0]+t*t*b2[0],
                          (1-t)*(1-t)*b0[1] + 2*(1-t)*t*b1[1]+t*t*b2[1],
                          (1-t)*(1-t)*b0[2] + 2*(1-t)*t*b1[2]+t*t*b2[2]};
 }
 
 private double Binv(double b0, double b1, double b2,double v)
 {
     return (b0-b1+Math.sqrt(b1*b1-b0*b2+(b0-2*b1+b2)*v))/(b0-2*b1+b2);      
 }
 
 private double T(double l, double p0L, double p2L, double q0L, double q1L, double q2L)
 {
     if (l <= q1L)
     {
         return 0.5*Binv(p0L,q0L,q1L,l);
     }
     return 0.5*Binv(q1L,q2L,p2L,l)+0.5;
 }
 
 
 
 // ---------------------------------- Private methods ------------------------------------------
 
 /** Finds the most saturated colour for the given hue.
  *  @param hue Hue whose most saturated colour is to be found. This should be a value scaled between 0-360 degrees.
  *  @return Colour expressed in RGB coordinates.
  */
 public double[] getMostSaturatedColour(double hue)
 {
     // This version just uses a lookup table rather than calculating intersection analytically.
     int hueIndex = (int)Math.round(hue%360);
     return msc[hueIndex];
     
     
     /* Code for building the lookup table
     for (double C=200; C>1; C-=0.01)
     {
         int numCols = 0;
         double[] total = {0,0,0};
         for (double L=40; L<100; L+=0.1)
         {
             double[] Luv = ColourConverter.LChToLuv(new double[] {L,C,hue});
             double[] rgb = ColourConverter.luvToRGB(Luv[0],Luv[1],Luv[2], wp);

             if ((rgb[0] >= 0) && (rgb[1] >= 0) && (rgb[2] >=0) && (rgb[0] <= 1) && (rgb[1] <= 1) && (rgb[2] <= 1))
             {
                 numCols++;
                 total[0] += rgb[0];
                 total[1] += rgb[1];
                 total[2] += rgb[2];
             }
         }
         if (numCols > 0)
         {
             total[0] /= numCols;
             total[1] /= numCols;
             total[2] /= numCols;
             return total;
         }
     }
             
     System.err.println("No saturated colour found for hue "+hue);
     return new double[] {0,0,0};
     */
 }
 
 /** Finds the nearest in-gamut colour to the given RGB triplet.
  *  @param rgb Colour to find. Each value can be out of range 0-1.
  *  @param Luv The Luv triplet that give rise to the given RGB triplet.
  *  @return A guaranteed in-gamut colour as close as possible in CIELuv space to the given colour. 
  */
 private Color findNearest(double[]rgb, double[] Luv)
 {
     // Constrain out of bounds colours to their nearest visible colour (not strictly CIELuv)
     // TODO: Improve iteration to do a few random near neighbours.
     if ((rgb[0] < 0) || (rgb[1] < 0) || (rgb[2] < 0) || (rgb[0] > 1) || (rgb[1] > 1) || (rgb[2] > 1))
     {
         double L = Luv[0];
         double u = Luv[1];
         double v = Luv[2];
         
         // Move point towards the whitepoint and until it is just inside the bounds.
         double theta = Math.atan2(u,v);
         double xInc = Math.sin(theta);
         double yInc = Math.cos(theta);
         double minDistSq = u*u + v*v;
         double mag = Math.sqrt(minDistSq)/2;
         
         double u2 = u,
                v2 = v;
         
         if (minDistSq > 1)
         {
             u2 -= xInc*mag;
         }
         
         if (minDistSq > 1)
         {
             v2 -= yInc*mag;
         }
         
         Color lastColour = null;
         boolean isOutside = true;
         while (Math.abs(mag) > 0.01)
         {
             Color newColour = getColour(L,u2,v2,false);
             if (newColour == null)
             {
                 if (isOutside)
                 {
                     // We are still outside so continue in same direction
                     mag /=2.0;
                 }
                 else
                 {
                     // We have crossed from inside to out, so reverse direction
                     mag /= -2.0;
                 }
                 isOutside = true;
             }
             else
             {
                 // We have found a valid colour, so store it if it is nearer than previous nearest point.
                 double distSq = (u2-u)*(u2-u) + (v2-v)*(v2-v);
                 if (distSq < minDistSq)
                 {
                     minDistSq = distSq;
                     lastColour = newColour;
                 }
                                         
                 if (isOutside)
                 {
                     // We have crossed from outside to inside, so reverse direction
                     mag /= -2;
                 }
                 else
                 {
                     // We are still inside the boundary, so continue in same direction.
                     mag /= 2;
                 }
                 isOutside = false;
             }
             
             if (u2*u2 > 1)
             {
                 u2 -= xInc*mag;
             }
             
             if (v2*v2 > 1)
             {
                 v2 -= yInc*mag;
             }
         }
         if (lastColour == null)
         {
             // We must be very close to the whitepoint, so just return it.
             return getColour(L,0,0,false);
         }
         return lastColour;
     }
     
     // Colour was within gamut, so just create a color object out of the given r,g,b values.
     return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);
 }
}
