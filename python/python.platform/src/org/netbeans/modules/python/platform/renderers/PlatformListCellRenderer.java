/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.platform.renderers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;

public class PlatformListCellRenderer extends JLabel implements ListCellRenderer {

    public PlatformListCellRenderer() {
        setOpaque(true);

    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        PythonPlatform platform = (PythonPlatform)value;
        boolean isDefault = platform.getId().equals(PythonPlatformManager.getInstance().getDefaultPlatform());
        if (isDefault) {
            setText(platform.getName() + " (Default)");
        } else {
            setText(platform.getName());
        }
        Color background;
        Color forground;

        if (isSelected) {
            background = list.getSelectionBackground();
            forground = list.getSelectionForeground();
        } else {
            background = list.getBackground();
            forground = list.getForeground();
        }
        setBackground(background);
        setForeground(forground);
        return this;
    }
}
