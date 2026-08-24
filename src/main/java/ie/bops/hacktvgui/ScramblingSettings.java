/*
 * Copyright (C) 2026 Stephen McGarry
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
package ie.bops.hacktvgui;

public record ScramblingSettings (
    boolean showECM,
    boolean scrambleAudio,
    int systerPermTable,
    int eurocryptMaturityRating,
    boolean eurocryptPpv,
    boolean eurocryptNoDate,
    String eurocryptProgNumber,
    String eurocryptProgCost,
    VideoCryptEmmState videocryptEmmState,
    String videocryptCardNumber,
    boolean showCardSerial,
    boolean findKeys
) {
    
    public enum VideoCryptEmmState {
        NO_EMM,
        ENABLE_EMM,
        DISABLE_EMM
    }
    
}
