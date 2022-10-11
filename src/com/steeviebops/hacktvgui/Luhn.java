/*
 * Copyright (C) 2022 Stephen McGarry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.steeviebops.hacktvgui;

/**
* Calculates a check digit for the specified input using the Luhn algorithm.
* @author Stephen McGarry
*/

public class Luhn {
    
    public static int CalculateLuhnCheckDigit(long input) {
        long t = 0;
        // Read backwards, doubling every other digit
        for (long l = input; l > 0; l = l / 100) {
            // Double l and add it to t.
            // If the result is greater than 9, the formula below will
            // add the individual digits, e.g. 14 is 1 + 4 = 5.
            t = t + (((l % 10) * 2 / 10) + (((l % 10) * 2) % 10));
        }
        // Read backwards again, add the remaining digits as-is
        for (long l = input / 10; l > 0; l = l / 100) {
            t = t + (l % 10);
        }
        // Multiply t by 9, the result of Mod10 is the check digit
        return (int) ((t * 9) % 10);
    }
        
    public static boolean LuhnCheck(long input) {
        /** 
         * Feed the full number to this method and it will return true or 
         * false based on whether the check digit is valid or not.
         */
        return CalculateLuhnCheckDigit(input / 10) == (input % 10);
    }
    
}