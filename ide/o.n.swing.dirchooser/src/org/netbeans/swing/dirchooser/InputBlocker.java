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
/*
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Blocks user's input when FileChooser is busy.
 *
 * @author Soot Phengsy
 */
public class InputBlocker extends JComponent implements MouseInputListener {
    
    public InputBlocker() {
    }

    private void addListeners(Component c) {
        for( MouseListener ml : c.getMouseListeners() ) {
            if( ml == this )
                return;
        }
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
    }

    private void removeListeners(Component c) {
        c.removeMouseListener(this);
        c.removeMouseMotionListener(this);
    }
    
    public void block(JRootPane rootPane) {
        if( null == rootPane )
            return;
        Component glassPane = rootPane.getGlassPane();
        if( null == glassPane ) {
            rootPane.setGlassPane(this);
            glassPane = this;
        }
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        addListeners(glassPane);
        glassPane.setVisible(true);
    }
    
    public void unBlock(JRootPane rootPane) {
        if( null == rootPane )
            return;
        Component glassPane = rootPane.getGlassPane();
        if( null == glassPane ) {
            return;
        }
        removeListeners(glassPane);
        glassPane.setCursor(null);
        glassPane.setVisible(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
