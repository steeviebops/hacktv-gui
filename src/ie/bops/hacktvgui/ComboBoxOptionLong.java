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

// Class for comboboxes storing <Long, String> instead of <String, String>
// Avoids the complexity of explicit type casting everywhere if using generics
public final class ComboBoxOptionLong {
    
    private final Long value;
    private final String label;

    public ComboBoxOptionLong(Long value, String label) {
        this.value = value;
        this.label = label == null ? "" : label;
    }

    public Long value() { return value; }
    public String label() { return label; }

    @Override public String toString() { return label; }
        
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ComboBoxOptionLong)) return false;
        ComboBoxOptionLong other = (ComboBoxOptionLong) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
}
