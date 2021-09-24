/*
 * Copyright (C) 2021 Stephen McGarry
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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * Right-click context menu listener
 * @author Stephen McGarry
 * 
 */
public class ContextMenuListener extends MouseAdapter {
    
    private final JPopupMenu contextMenu = new JPopupMenu();
    private final Action cut;
    private final Action copy;
    private final Action paste;
    private final Action delete;
    private final Action selectAll;
    private JTextComponent jtc;

    public ContextMenuListener() {
        // Cut
        cut = new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jtc.cut();
            }
        };
        contextMenu.add(cut);

        // Copy
        copy = new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jtc.copy();
            }
        };
        contextMenu.add(copy);

        // Paste
        paste = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jtc.paste();
            }
        };
        contextMenu.add(paste);
        
        // Delete
        delete = new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jtc.replaceSelection("");
            }
        };
        contextMenu.add(delete);
        
        // Separator
        contextMenu.addSeparator();

        // Select all
        selectAll = new AbstractAction("Select All") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jtc.selectAll();
            }
        };
        contextMenu.add(selectAll);
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if ( (evt.getButton() == MouseEvent.BUTTON3) && (evt.getSource() instanceof JTextComponent) ) {
            jtc = (JTextComponent) evt.getSource();
            // Don't display the menu if the text component is disabled
            if (!jtc.isEnabled()) {
                evt.consume();
                return;
            }
            jtc.requestFocusInWindow();
            
            // Conditional statements to enable/disable the options
            boolean isEnabled = jtc.isEnabled();
            boolean isEditable = jtc.isEditable();
            boolean isEmpty = jtc.getText().isEmpty();
            boolean isSelected = jtc.getSelectedText() != null;
            boolean canPaste = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
            cut.setEnabled(isEnabled && isEditable && isSelected);
            copy.setEnabled(isEnabled && isSelected);
            paste.setEnabled(isEnabled && isEditable && canPaste);
            delete.setEnabled(isEnabled && !isEmpty && isSelected && isEditable);
            selectAll.setEnabled(isEnabled && !isEmpty);
            
            // Display the menu
            contextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
}
