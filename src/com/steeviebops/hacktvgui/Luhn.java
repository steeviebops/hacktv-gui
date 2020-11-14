/*
 * Copyright (C) 2020 Stephen McGarry
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

import java.util.ArrayList;

/**
* Calculates a check digit for the specified input using the Luhn
* algorithm.
* @author Stephen McGarry
*/

public class Luhn {
    
    public static int CalculateLuhnCheckDigit(String input) {
        ArrayList<Integer> Numbers;
        int j;
        int sum = 0;
        Numbers = new ArrayList<>();
        // Check if the input is numeric. If not, return -1
	try {
	    double d = Double.parseDouble(input);
	} catch (NumberFormatException nfe) {
	    return -1;
	}
        // Read backwards, doubling every second digit
        for (int i = input.length() -1; i >= 0; i -= 2) {
            j = Character.getNumericValue(input.charAt(i));
            j = (j * 2);
            // If the returned value is 10 or higher, subtract 9 from it
            if (j >= 10) {
                j = (j - 9);
            }
            Numbers.add(j);
        }
        // Read backwards again, add the remaining digits as-is
        for (int i = input.length() -2; i >= 0; i -= 2) {
            j = Character.getNumericValue(input.charAt(i));
            Numbers.add(j);
        }
        // Add what we got
        for (int i = 0; i < Numbers.size(); i++) {
            sum = (sum + Numbers.get(i));
        }
        // Multiply the value by 9 and do a Mod10 on it. The returned value is
        // the check digit.
        return (sum * 9 % 10);
    }
    
    public static boolean LuhnCheck(String input) {
        /** 
         * Feed the full number to this method and it will return true or 
         * false based on whether the check digit is valid or not.
         */
        int p = CalculateLuhnCheckDigit(input.substring(0, input.length() -1));
        int q = Integer.parseInt(input.substring(input.length() -1));
        return p == q;
    }
    
}