package ie.bops.hacktvgui;

import ie.bops.hacktvgui.ModeInfo.ColourMode;

public record ColourOption(ColourMode colourMode, String displayName) {
    
    @Override
    public String toString() {
        return displayName;
    }
    
}
