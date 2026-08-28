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

import java.util.Collections;
import java.util.List;

public record ScramblingInfo (
    String id,
    String displayName,
    String ca2Id,
    Long preferredSampleRate,
    boolean ecmSupported,
    boolean scrambleAudioSupported,
    boolean systerFeatures,
    boolean eurocryptFeatures,
    boolean videocryptFeatures,
    List<ComboBoxOption> caKeys,
    List<ComboBoxOption> ca2Keys
) {
    
    public ScramblingInfo {
        caKeys = Collections.unmodifiableList(caKeys);
        ca2Keys = Collections.unmodifiableList(ca2Keys);
    }
    
    @Override
    public String toString() {
        // Lets the JComboBox display friendly names
        return displayName;
    }
}
