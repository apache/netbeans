/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.explorer.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

final class IconPanel extends JPanel implements ListCellRenderer {
    private Icon thumbIcon;
    private boolean selected;
    private boolean focused;

    public IconPanel() {
        initComponents();
    }
  
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Node node = Visualizer.findNode(value);
        thumbIcon = ImageUtilities.image2Icon(node.getIcon(BeanInfo.ICON_COLOR_32x32));
        this.selected = isSelected;
        label.setOpaque(selected);
        if (selected) {
          label.setBackground(UIManager.getColor("List.selectionBackground"));
          label.setForeground(UIManager.getColor("List.selectionForeground"));
        } else {
          label.setBackground(UIManager.getColor("Label.background"));
          label.setForeground(UIManager.getColor("Label.foreground"));
        }
        this.focused = cellHasFocus;
        this.label.setText(node.getDisplayName());

        return this;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPNImage = new Viewer();
        label = new javax.swing.JLabel();

        setBackground(new java.awt.Color(51, 51, 51));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jPNImage.setOpaque(false);
        jPNImage.setLayout(null);
        add(jPNImage, java.awt.BorderLayout.CENTER);

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(label, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPNImage;
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables

  private class Viewer extends JPanel {

        public Viewer() {
            setBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true),
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)))
            );
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            if (selected) {
                g.setColor(Color.BLUE.darker().darker());
                g.fillRect(12, 12, getWidth() - 24, getHeight() - 24);
            }
            if (focused) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{1.0f, 2.0f, 1.0f, 2.0f}, 0.f));
                g.drawRect(18, 18, getWidth() - (2 * 18), getHeight() - (2 * 18));
                g.setStroke(new BasicStroke(1f));
            }
            thumbIcon.paintIcon(this, g,
                getWidth() / 2 - thumbIcon.getIconWidth() / 2,
                getHeight() / 2 - thumbIcon.getIconHeight() / 2);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                thumbIcon.getIconWidth() + getInsets().left + getInsets().right,
                thumbIcon.getIconHeight() + getInsets().top + getInsets().bottom
            );
        }
    }
}
