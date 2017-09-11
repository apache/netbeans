/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.openide.nodes.Node;

final class IconPanel extends JPanel implements ListCellRenderer {
    private Image thumbImage;
    private boolean selected;
    private boolean focused;

    public IconPanel() {
        initComponents();
    }
  
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Node node = Visualizer.findNode(value);
        thumbImage = node.getIcon(BeanInfo.ICON_COLOR_32x32);
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
            g.drawImage(thumbImage,
                getWidth() / 2 - thumbImage.getWidth(this) / 2,
                getHeight() / 2 - thumbImage.getHeight(this) / 2, this
            );
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                thumbImage.getWidth(this) + getInsets().left + getInsets().right,
                thumbImage.getHeight(this) + getInsets().top + getInsets().bottom
            );
        }
    }
}
