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
import java.util.LinkedHashMap;
import java.util.Map;

public record ModeInfo(
        String modeId,
        String altModeId,
        String displayName,
        Integer lines,
        Double fieldRate,
        VideoModulation modulation,
        AudioModulation audioModulation,
        Long audioCarrierFrequency,
        Long sampleRate,
        ColourMode colourMode,
        boolean audio,
        boolean nicam,
        boolean a2,
        AudioMode defaultAudioMode,
        boolean teletext,
        boolean wss,
        boolean vits,
        boolean acp,
        boolean scrambling,
        boolean nonStandard,
        Map<String, BandPlan> uhf,
        Map<String, BandPlan> vhf,
        Map<String, BandPlan> sat,
        String description
    ) {
    
    public ModeInfo {
        uhf = Collections.unmodifiableMap(new LinkedHashMap<>(uhf));
        vhf = Collections.unmodifiableMap(new LinkedHashMap<>(vhf));
        sat = Collections.unmodifiableMap(new LinkedHashMap<>(sat));
    }
    
    public enum VideoModulation {
        UNMODULATED,
        VSB,
        FM
    }
    
    public enum ColourMode {
        NONE,
        PAL,
        NTSC,
        SECAM,
        MAC,
        OTHER
    }    
    
    public enum AudioModulation {
        NO_AUDIO,
        AM_AUDIO,
        FM_AUDIO,
        DIGITAL_AUDIO
    }
    
    public enum AudioMode {
        MONO,
        NICAM,
        A2,
        MTS
    }
    
    public BandPlan[] getUhfPlans() {
        return uhf.values().toArray(BandPlan[]::new);
    }
    
    public BandPlan[] getVhfPlans() {
        return vhf.values().toArray(BandPlan[]::new);
    }

    public BandPlan[] getSatellitePlans() {
        return sat.values().toArray(BandPlan[]::new);
    }
    
    public BandPlan getUhfPlan(String id) {
        return uhf.get(id);
    }

    public BandPlan getVhfPlan(String id) {
        return vhf.get(id);
    }
    
    public BandPlan getSatellitePlan(String id) {
        return sat.get(id);
    }

    @Override
    public String toString() {
        return displayName;
    }
    
    @Override
    public int hashCode() {
        return displayName.hashCode();
    }
    
}
