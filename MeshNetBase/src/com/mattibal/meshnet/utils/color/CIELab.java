package com.mattibal.meshnet.utils.color;

import java.awt.Color;

import com.mattibal.meshnet.utils.color.ColourConverter.WhitePoint;


//****************************************************************************************
/** Utilities for handling the CIELab colour space. This space is approximately perceptually
 *  uniform in that two colours a fixed distance apart in the colour space should be equally
 *  distinguishable from each other regardless of their location.
 *  <br /><br />
 *  For details see <a href="http://en.wikipedia.org/wiki/Lab_color_space" target="_blank">
 *  en.wikipedia.org/wiki/Lab_color_space</a>. 
 *  @author Jo Wood, giCentre, City University London.
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

public class CIELab 
{
    // ---------------------------- Class and object Variables ----------------------------
        
    private WhitePoint wp;          // Whitepoint used for colour conversion.
    
    
    // ---------------------------------- Constructors ------------------------------------
    
    /** Creates a CIELab converter using the default D65 illuminant with 2 degree observer
     *  position.
     */
    public CIELab()
    {
        this(WhitePoint.D65);
    }

    /** Creates a CIELab converter using the given reference calibration settings. These 
     *  allow different whitepoints to be set.
     *  @param wp Colour calibration reference to use in the CIELab conversion.
     */ 
    public CIELab(WhitePoint wp)
    {       
        this.wp = wp;
    }
        
    // ------------------------------------ Methods ---------------------------------------
    
    /** Finds the CIELab triplet representing the given colour. CIELab L value scaled between 0-100, 
     *  and a and b values scaled between -100 and 100. Based on the conversion code  by Duane 
     *  Schwartzwald, 12th March, 2006 and Harry Parker, Feb 27th, 2007.
     *  See <a href="http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java" target="_blank">
     *  rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java</a>.
     *  @param colour Colour to convert.
     *  @return Vector holding CIELab values where the x and y components hold the <i>a</i>
     *          and <i>b</i> values respectively and the z values holds the <i>L</i> value.
     */
    public float[] getLab(Color colour) 
    {
        double[] lab = ColourConverter.getLab(colour, wp);
        float[] ret = {(float)lab[1],(float)lab[2],(float)lab[0]};
        return ret;
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
     *  @param useNearest If true, out-of-gamut colours will be converted to their nearest visible 
     *                    equivalent. If not, out-of-gamut colours returned as null.
     *  @return Colour representing the given Lab values.
     */
    public Color getColour(double L, double a, double b, boolean useNearest)
    {
        double[] rgb = ColourConverter.labToRGB(L, a, b, wp);
        double[] Lab = new double[] {L,a,b};
        
        if (useNearest)
        {
            return findNearest(rgb, Lab);
        }
        else if ((rgb[0] < 0) || (rgb[1] < 0) || (rgb[2] < 0) || (rgb[0] > 1) || (rgb[1] > 1) || (rgb[2] >1))
        {
            return null;
        }
        return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);        
    }
         
    /** Finds the nearest in-gamut colour to the given RGB triplet.
     *  @param rgb Colour to find. Each component can be out of range 0-1.
     *  @param Lab The Lab triplet that give rise to the given RGB triplet.
     *  @return A guaranteed in-gamut colour as close as possible in CIELab space to the given colour. 
     */
    private Color findNearest(double[]rgb, double[] Lab)
    {
        // Constrain out of bounds colours to their nearest visible colour (not strictly CIELab)
        // TODO: Improve iteration to do a few random near neighbours.
        if ((rgb[0] < 0) || (rgb[1] < 0) || (rgb[2] < 0) || (rgb[0] > 1) || (rgb[1] > 1) || (rgb[2] > 1))
        {
            double L = Lab[0];
            double a = Lab[1];
            double b = Lab[2];
            
            // Move point towards the whitepoint and until it is just inside the bounds.
            double theta = Math.atan2(a,b);
            double xInc = Math.sin(theta);
            double yInc = Math.cos(theta);
            double minDistSq = a*a + b*b;
            double mag = Math.sqrt(minDistSq)/2;
            
            double a2 = a,
                   b2 = b;
            
            if (minDistSq > 1)
            {
                a2 -= xInc*mag;
            }
            
            if (minDistSq > 1)
            {
                b2 -= yInc*mag;
            }
            
            Color lastColour = null;
            boolean isOutside = true;
            while (Math.abs(mag) > 0.01)
            {
                Color newColour = getColour(L,a2,b2,false);
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
                    double distSq = (a2-a)*(a2-a) + (b2-b)*(b2-b);
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
                
                if (a2*a2 > 1)
                {
                    a2 -= xInc*mag;
                }
                
                if (b2*b2 > 1)
                {
                    b2 -= yInc*mag;
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