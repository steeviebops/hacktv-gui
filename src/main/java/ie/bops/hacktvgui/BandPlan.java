package ie.bops.hacktvgui;

import java.util.List;

public record BandPlan(String id, String band, String region, List<Channel> channels) {
    
    @Override
    public String toString() {
        return region;
    }
    
}