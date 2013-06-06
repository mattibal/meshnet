package com.mattibal.meshnet.utils.color;

import java.awt.Color;

//  ****************************************************************************************
/** Utilities for converting between various colour spaces. Note that the default scaling 
 *  for RGB is 0-1 and hue is expressed as degrees (0-360). For other values, see 
 *  documentation for each conversion routine.
 *  @author Jo Wood,giCentre, City University London. Includes modified code from Duane 
 *  Schwartzwald and Harry Parker.
 *  @version 3.3, 1st August, 2011.
 */ 
// *****************************************************************************************

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

public class ColourConverter 
{
    // ---------------------------- Class Variables ----------------------------
        
    /** Whitepoint colour calibration settings. */     
    public enum WhitePoint
    {
        // Tristimulus values taken from Schanda, J. (19xx) Colorimetry, p.74
        
        /** The D50 illuminant with 2 degree observer position. */  D50(96.4,  100.0, 82.5),
        /** The D55 illuminant with 2 degree observer position. */  D55(95.68, 100.0, 92.14),
        /** The D65 illuminant with 2 degree observer position. */  D65(95.04, 100.0, 108.88),
        /** The D75 illuminant with 2 degree observer position. */  D75(94.97, 100.0, 122.61),
        /** The standard illuminant C. */                           C  (98.07, 100.0, 118.22);
        
        private double[] params;
        
        private WhitePoint(double X, double Y, double Z)
        {
            params = new double[] {X,Y,Z};
        }
        
        /** Reports the tristimulus reference coordinates of the whitepoint.
         *  @return Triplet of tristimulus coordinates of the whitepoint.
         */
        double[] getTristimulus()
        {
            return params;
        }
    }
    
    /** XYZ to sRGB conversion matrix (3x3) */
    static final double[][] M_INV  = {{ 3.2406, -1.5372, -0.4986},
                                      {-0.9689,  1.8758,  0.0415},
                                      { 0.0557, -0.2040,  1.0570}};
    
    /** sRGB to XYZ conversion matrix (3x3) */
    static final double[][] M   = {{0.4124, 0.3576,  0.1805},       // RX, GX, BX
                                   {0.2126, 0.7152,  0.0722},       // RY, GY, BY
                                   {0.0193, 0.1192,  0.9505}};      // RZ, GZ, BZ
    
    static final double DEG2RAD = 0.017453293;
    static final double RAD2DEG = 57.29577951;
            
    // ---------------------------------- Constructor ------------------------------------
    
    /** Prevents this class from being instantiated.
     */
    private ColourConverter()
    {
        // Do nothing. Since this constructor is private, it prevents the class from being
        // instantiated. Ensures singleton static behaviour.
    }
        
    // --------------------------------- Public methods ----------------------------------
    
    /** Finds the CIELab triplet representing the given colour. CIELab L value scaled between 0-100, 
     *  and a and b values scaled between -100 and 100. Based on the conversion code by Duane 
     *  Schwartzwald, 12th March, 2006 and Harry Parker, Feb 27th, 2007.
     *  See <a href="http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java" target="_blank">
     *  rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java</a>.
     *  @param colour Colour to convert.
     *  @param wp Whitepoint colour calibration value.
     *  @return Triplet of CIELab values in the order <i>L</i>, <i>a</i> and <i>b</i>.
     */
    public static double[] getLab(Color colour, WhitePoint wp) 
    {
        return XYZtoLAB(RGBtoXYZ(colour),wp);
    }
    
    /** Finds the CIELuv triplet representing the given colour. CIELuv L value scaled between 0-100, 
     *  and u and v values scaled between -100 and 100.
     *  @param colour Colour to convert.
     *  @param wp Whitepoint colour calibration value.
     *  @return Triplet of CIELuv values in the order <i>L</i>, <i>u</i> and <i>v</i>.
     */
    public static double[] getLuv(Color colour, WhitePoint wp) 
    {
        return XYZtoLuv(RGBtoXYZ(colour),wp);
    }
     
    /** Finds the colour corresponding to the given CIELab triplet. Out-of-gamut colours  will be
     *  returned as null if <code>useNearest</code> is false, otherwise the approximate nearest
     *  visible colour will be returned. CIELab L values should be scaled between 0-100, 
     *  and a and b values scaled between -100 and 100. Based on the conversion code by Duane 
     *  Schwartzwald, 12th March, 2006 and Harry Parker, Feb 27th, 2007.
     *  See <a href="http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java" target="_blank">
     *  rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java</a>.
     *  @param L CIELab L value scaled between 0 and 100.
     *  @param a CIELab a value scaled between -100 and 100.
     *  @param b CIELab b value scaled between -100 and 100.
     *  @param wp Whitepoint colour calibration value.
     *  @return RGB triplet representing the given Lab values. These are normally scaled between 0-1 for
     *          visible colours, but may be out of this range for out-of-gamut colours.
     */
    public static double[] labToRGB(double L, double a, double b, WhitePoint wp)
    {
        double[] Lab = new double[3];
        Lab[0] = L;
        Lab[1] = a;
        Lab[2] = b;
        return XYZtoRGB(LABtoXYZ(Lab,wp));
    }
    
    /** Finds the colour corresponding to the given CIELuv triplet. Out-of-gamut colours  will be
     *  returned as null if <code>useNearest</code> is false, otherwise the approximate nearest
     *  visible colour will be returned. CIELuv L values should be scaled between 0-100, 
     *  and u and v values scaled between -100 and 100.
     *  @param L CIELab L value scaled between 0 and 100.
     *  @param u CIELab u value scaled between -100 and 100.
     *  @param v CIELab v value scaled between -100 and 100.
     *  @param wp Whitepoint colour calibration value.
     *  @return RGB triplet representing the given Luv values. These are normally scaled between 0-1 for
     *          visible colours, but may be out of this range for out-of-gamut colours.
     */
    public static double[] luvToRGB(double L, double u, double v, WhitePoint wp)
    {
        double[] Luv = new double[3];
        Luv[0] = L;
        Luv[1] = u;
        Luv[2] = v;
        return XYZtoRGB(LuvToXYZ(Luv,wp));
    }
    
    // --------------------------------- Package methods ----------------------------------
    
    /** Converts the colour represented in XYZ space into an sRGB triplet. Note that the RGB values
     *  can be out of range depending on the colour conversion applied.
     *  @param XYZ Colour in XYZ space scaled between.
     *  @return RGB components of the colour represented by the coordinates in XYZ colour space. This is 
     *          a triplet of values scaled between 0-1 if visible, but can be out of range depending on
     *          the colour conversion.
     */
    static double[] XYZtoRGB(double[] XYZ) 
    {
        double x = XYZ[0]/100.0;
        double y = XYZ[1]/100.0;
        double z = XYZ[2]/100.0;

        // [r g b] = [X Y Z][M_INV]
        double r = (x * M_INV[0][0]) + (y * M_INV[0][1]) + (z * M_INV[0][2]);
        double g = (x * M_INV[1][0]) + (y * M_INV[1][1]) + (z * M_INV[1][2]);
        double b = (x * M_INV[2][0]) + (y * M_INV[2][1]) + (z * M_INV[2][2]);

        // Assume sRGB
        if (r > 0.0031308) 
        {
            r = (1.055*Math.pow(r,0.4166666667)) - 0.055;
        }
        else 
        {
            r *= 12.92;
        }
        if (g > 0.0031308) 
        {
            g = (1.055 * Math.pow(g,0.4166666667)) - 0.055;
        }
        else 
        {
            g *= 12.92;
        }
        if (b > 0.0031308) 
        {
            b = (1.055 * Math.pow(b,0.4166666667)) - 0.055;
        }
        else 
        {
            b *= 12.92;
        }
        
        return new double[] {r,g,b};
    }
    
    /** Converts colours in Lab space to XYZ space.
     *  @param Lab CIELab L, a and b values with L scaled between 0 and 100, and a and b scaled between -100 and 100.
     *  @param wp Whitepoint used to perform the transformation.
     *  @return Colour in XYZ space.
     */
    public static double[] LABtoXYZ(double[] Lab, WhitePoint wp) 
    {
        double y = (Lab[0] + 16.0) / 116.0;
        double y3 = Math.pow(y, 3.0);
        double x = (Lab[1] / 500.0) + y;
        double x3 = Math.pow(x, 3.0);
        double z = y - (Lab[2] / 200.0);
        double z3 = Math.pow(z, 3.0);

        if (y3 > 0.008856) 
        {
            y = y3;
        }
        else 
        {
            y = (y - (16.0 / 116.0)) / 7.787;
        }
        if (x3 > 0.008856) 
        {
            x = x3;
        }
        else 
        {
            x = (x - (16.0 / 116.0)) / 7.787;
        }
        if (z3 > 0.008856) 
        {
            z = z3;
        }
        else 
        {
            z = (z - (16.0 / 116.0)) / 7.787;
        }      
      
        double[] result = new double[3];
      
        result[0] = x*wp.getTristimulus()[0];
        result[1] = y*wp.getTristimulus()[1];
        result[2] = z*wp.getTristimulus()[2];
        return result;
    }
     
    /** Converts the given colour into XYZ colour space.
     *  @param colour Colour to convert. 
     *  @return Colour in XYZ space.
     */
    static double[] RGBtoXYZ(Color colour) 
    {
        double r = colour.getRed()/255.0;
        double g = colour.getGreen()/255.0;
        double b = colour.getBlue()/255.0;
        return RGBtoXYZ(new double[] {r,g,b});
    }
    
    /** Converts the given rgb triplet into XYZ colour space.
     *  @param rgb RGB triplet to convert. Values should be scaled between 0-1. 
     *  @return Colour in XYZ space.
     */
    static double[] RGBtoXYZ(double[] rgb) 
    {
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];

        // assume sRGB
        if (r <= 0.04045) 
        {
            r = r / 12.92;
        }
        else 
        {
            r = Math.pow(((r + 0.055) / 1.055), 2.4);
        }
        if (g <= 0.04045) 
        {
            g = g / 12.92;
        }
        else 
        {
            g = Math.pow(((g + 0.055) / 1.055), 2.4);
        }
        if (b <= 0.04045) 
        {
            b = b / 12.92;
        }
        else 
        {
            b = Math.pow(((b + 0.055) / 1.055), 2.4);
        }

        r *= 100.0;
        g *= 100.0;
        b *= 100.0;

        // [X Y Z] = [r g b][M]
        double[] result = new double[3];
        result[0] = (r * M[0][0]) + (g * M[0][1]) + (b * M[0][2]);
        result[1] = (r * M[1][0]) + (g * M[1][1]) + (b * M[1][2]);
        result[2] = (r * M[2][0]) + (g * M[2][1]) + (b * M[2][2]);

        return result;
    }

    
    /** Converts colours in XYZ space into a CIELuv triplet.
     *  @param XYZ Colour in XYZ space. 
     *  @param wp Whitepoint used for the transformation.
     *  @return Triplet of CIELuv values in the order <i>L</i>, <i>u</i> and <i>v</i>.
     */
    static double[] XYZtoLuv(double[] XYZ, WhitePoint wp)
    {
        double L,u,v;
        double uPrime, vPrime, unPrime, vnPrime;
        double xn = wp.getTristimulus()[0];
        double yn = wp.getTristimulus()[1];
        double zn = wp.getTristimulus()[2];
        
        //double x = XYZ[0] / xn;
        double y = XYZ[1] / yn;
        //double z = XYZ[2] / zn;
        
        unPrime = 4*xn / (xn + 15*yn + 3*zn);
        vnPrime = 9*yn / (xn + 15*yn + 3*zn);
        uPrime = 4*XYZ[0] / (XYZ[0] + 15*XYZ[1] + 3*XYZ[2]);
        vPrime = 9*XYZ[1] / (XYZ[0] + 15*XYZ[1] + 3*XYZ[2]);
        
        if (y > 0.008856452)    // (6/29)^3
        {
            L = 116*Math.pow(y,0.3333333333333) - 16;
        }
        else
        {
            L = 903.2962963*y;  // (29/3)^3
        }
        u = 13*L*(uPrime-unPrime);
        v = 13*L*(vPrime-vnPrime);
        
        return new double[] {L,u,v};
    }
    
    /** Converts colours in CIELuv space to an XYZ triplet.
     *  @param Luv Colour expressed in Luv space. 
     *  @param wp Whitepoint used for the transformation.
     *  @return Triplet of colour coordinates in XYZ space.
     */
    static double[] LuvToXYZ(double[] Luv, WhitePoint wp)
    {
        double X,Y,Z;
        double xn = wp.getTristimulus()[0];
        double yn = wp.getTristimulus()[1];
        double zn = wp.getTristimulus()[2];
        
        double unPrime = (4*xn)/(xn + 15*yn + 3*zn);
        double vnPrime = (9*yn)/(xn + 15*yn + 3*zn);

        double uPrime = Luv[1]/(13*Luv[0]) + unPrime;
        double vPrime = Luv[2]/(13*Luv[0]) + vnPrime;
        
        if (Luv[0] <= 8)
        {
            Y = yn*Luv[0]*0.001107056;      // (3/29)^3; 
        }
        else
        {
            Y = yn*((Luv[0]+16)/116)*((Luv[0]+16)/116)*((Luv[0]+16)/116);
        }
        X = Y*(9*uPrime/(4*vPrime));
        Z = Y*((12-3*uPrime - 20*vPrime)/(4*vPrime));
        
        return new double[] {X,Y,Z};
    }
    
    /** Converts colours in RGB space to an HSL (Hue, Saturation, Lightness) triplet.
     *  @param rgb RGB triplet each scaled between 0-1. 
     *  @return Triplet of colour coordinates in HSL space. Hue scaled between 0-360 degrees, S and L between 0-1.
     */
    static double[] RGBToHSL(double rgb[])
    {
        // See http://en.wikipedia.org/wiki/HSL_and_HSV for translation formulae
        double min,max,range;
        double h=0,s,l;
        
        if (rgb[0] > rgb[1])
        {
            min = rgb[1];
            max = rgb[0]; 
        }
        else 
        { 
            min = rgb[0]; 
            max = rgb[1]; 
        }

        if (rgb[2] > max) 
        {
            max = rgb[2];
        }
        if (rgb[2]< min)
        {
            min = rgb[2];
        }
        
        range = max-min; 
        l = (max+min)/2;
        
        if (range == 0)     // Grey.
        {
            h = 0;
            s = 0;
        }
        else
        {
            if (l <0.5)
            {
                s = range/(2*l);
            }
            else
            {
                s = range/(2-2*l);
            }
            
            double delR = (((max - rgb[0])/6) + (range/2)) /range;
            double delG = (((max - rgb[1])/6) + (range/2)) /range;
            double delB = (((max - rgb[2])/6) + (range/2)) /range;
    
            if ( rgb[0] == max) 
            {
                h = delB - delG;
            }
            else if (rgb[1] == max)
            {
                h = (1/3.0) + delR - delB;
            }
            else if (rgb[2] == max) 
            {
                h = (2/3.0) + delG - delR;
            }
            if (h<0) 
            {
                h += 1;
            }
            if (h>1)
            {
                h -= 1;
            }
        }
        return new double[] {h*360,s,l};
    }
    
    /** Converts colours in HSL (Hue, Saturation, Lightness) space to an RGB triplet. 
     *  @param hsl Triplet of colour coordinates in HSL space. Hue scaled between 0-360 degrees, S and L between 0-1.
     *  @return RGB triplet each scaled between 0-1.
     */
    static double[] HSLToRGB(double hsl[])
    {
        // See http://en.wikipedia.org/wiki/HSL_and_HSV for translation formulae
        double p,q,hk,tR,tG,tB;
        double[] rgb = new double[3];
        
        if (hsl[2] < 0.5)
        {
            q = hsl[2]*(1+hsl[1]);
        }
        else
        {
            q = hsl[2]+hsl[1]-(hsl[2]*hsl[1]);
        }
        p  = 2*hsl[2]*q;
        hk = hsl[0]/360;
        
        tR = hk + 1/3.0;
        tG = hk;
        tB = hk - 1/3.0;
        
        // Red component.
        if (tR < 0)
        {
            tR+=1;
        }
        else if (tR > 1)
        {
            tR -=1;
        }
        
        if (tR < 1/6.0)
        {
            rgb[0] = p + ((q-p)*6*tR);
        }
        else if (tR < 0.5)
        {
            rgb[0] = q;
        }
        else if (tR < 2/3.0)
        {
            rgb[0] = p + ((q-p)*6*(2/3.0-tR));
        }
        else 
        {
            rgb[0] = p;
        }
        
        // Green component.
        if (tG < 0)
        {
            tG+=1;
        }
        else if (tG > 1)
        {
            tG -=1;
        }
        
        if (tG < 1/6.0)
        {
            rgb[1] = p + ((q-p)*6*tG);
        }
        else if (tG < 0.5)
        {
            rgb[1] = q;
        }
        else if (tG < 2/3.0)
        {
            rgb[1] = p + ((q-p)*6*(2/3.0-tG));
        }
        else 
        {
            rgb[1] = p;
        }
        
        // Blue component.
        if (tB < 0)
        {
            tB+=1;
        }
        else if (tB > 1)
        {
            tB -=1;
        }
        
        if (tB < 1/6.0)
        {
            rgb[2] = p + ((q-p)*6*tB);
        }
        else if (tB < 0.5)
        {
            rgb[2] = q;
        }
        else if (tB < 2/3.0)
        {
            rgb[2] = p + ((q-p)*6*(2/3.0-tB));
        }
        else 
        {
            rgb[2] = p;
        }
        return rgb;
    }
    
    /** Converts a CIELuv triplet into CIE LCh triplet. This gives a more intuitive
     *  chroma (C) and h (hue) along with the original L value. L is scaled from 0-100, 
     *  h from 0 to 360 degrees. This is in effect a cylindrical transformation of the Luv
     *  colour space.
     *  @param Luv Luv triplet to convert.
     *  @return LCh triplet.
     */
    // TODO: What is the scaling range for C?
    static double[] LuvToLCh(double[] Luv)
    {
        double C = Math.sqrt(Luv[1]*Luv[1] + Luv[2]*Luv[2]);
        //double h = (TWO_PI + Math.atan2(Luv[2],Luv[1]))%(TWO_PI);
        double h = (360 + (RAD2DEG*Math.atan2(Luv[2],Luv[1])))%360;
        
        return new double[] {Luv[0],C,h};
    }
    
    /** Converts a CIELCh triplet into CIE Luv triplet. This transforms from the more 
     *  intuitive chroma (C) and h (hue) space to uv space along with the original L value.
     *  @param LCh LCh triplet to convert. L is scaled from 0-100, h from 0 to 360 degrees.
     *  @return Luv triplet.
     */
    static double[] LChToLuv(double[] LCh)
    {
        // TODO: What is the scaling range for C?
        double hueRad = LCh[2]*DEG2RAD;
        double u = LCh[1]*Math.cos(hueRad);
        double v = LCh[1]*Math.sin(hueRad);     
        return new double[] {LCh[0],u,v};
    }
    
    /** Converts colours in XYZ space into a CIELab triplet.
     *  @param XYZ Colour in XYZ space. 
     *  @param wp Whitepoint used for the transformation.
     *  @return Triplet of CIELab values in the order <i>L</i>, <i>a</i> and <i>b</i>.
     */
    static double[] XYZtoLAB(double[] XYZ, WhitePoint wp)
    {
        double x = XYZ[0] / wp.getTristimulus()[0];
        double y = XYZ[1] / wp.getTristimulus()[1];
        double z = XYZ[2] / wp.getTristimulus()[2];

        if (x > 0.008856) 
        {
            x = Math.pow(x, 1.0 / 3.0);
        }
        else 
        {
            x = (7.787 * x) + (16.0 / 116.0);
        }
        if (y > 0.008856) 
        {
            y = Math.pow(y, 1.0 / 3.0);
        }
        else 
        {
            y = (7.787 * y) + (16.0 / 116.0);
        }
        if (z > 0.008856) 
        {
            z = Math.pow(z, 1.0 / 3.0);
        }
        else 
        {
            z = (7.787 * z) + (16.0 / 116.0);
        }

        return new double[] {116*y - 16, 500*(x-y), 200*(y-z)};
    }  
}