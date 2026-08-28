package ie.bops.hacktvgui;

import java.util.List;

public record BandPlan(String id, String band, String region, List<Channel> channels) {
    
    public BandPlan {
        channels = List.copyOf(channels);
    }
    
    @Override
    public String toString() {
        return region;
    }
    
}