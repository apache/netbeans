/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.editor;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;

/**
 * Support for Sticky Windows in the editor. JComponents can be added to a layer
 * on top of the editor. Components will update their vertical position on editor
 * changes.
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 * @since 2.9
 */
public final class StickyWindowSupport {

    private final JTextComponent jtc;

    StickyWindowSupport(final JTextComponent jtc) {
        this.jtc = jtc;
        ViewHierarchy.get(jtc).addViewHierarchyListener(new ViewHierarchyListener() {
            @Override
            public void viewHierarchyChanged(ViewHierarchyEvent evt) {
                JTextComponent editor = jtc;
                Container container = editor.getParent();
                if(container instanceof JLayeredPane && evt.isChangeY()) {
                    JLayeredPane pane = (JLayeredPane) container;
                    double deltaY = evt.deltaY();
                    Component[] components = pane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
                    Rectangle rv = null;
                    for (final Component component : components) {
                        rv = component.getBounds(rv);
                        if(rv.getY() > evt.startY() ||
                                rv.contains(new Point(rv.x, (int) evt.startY())) ||
                                rv.contains(new Point(rv.x, (int) evt.endY()))) {
                            final Point p = rv.getLocation();
                            p.translate(0, (int) deltaY);
                            EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {      
                                  component.setLocation(p);
                                }
                            });
                            
                        }
                    }
                }
            }
        });
    }

    /**
     * Add a sticky window to the editor.
     * @param window the JComponent to add to the editor
     */
    public void addWindow(JComponent window) {
        Container container = jtc.getParent();
        if(container instanceof JLayeredPane) {
            JLayeredPane pane = (JLayeredPane) container;
            pane.add(window, JLayeredPane.PALETTE_LAYER);
            window.setVisible(true);
        }
    }
    
    /**
     * Convert a <code>aPoint</code> in root component coordinate system to the
     * editor coordinate system. <code>aPoint</code> is assumed to be in the
     * root component coordinate system of the editor. If conversion is not
     * possible, return <code>aPoint</code> without any conversion.
     *
     * @param aPoint the Point to convert
     * @return aPoint converted to editor coordinate system
     */
    public @NonNull Point convertPoint(Point aPoint) {
        Point value = aPoint;
        Container container = jtc.getParent();
        if(container instanceof JLayeredPane) {
            JLayeredPane pane = (JLayeredPane) container;
            value = SwingUtilities.convertPoint(pane.getRootPane(), value, pane);
        }
        return value;
    }

    /**
     * Remove a sticky window from the editor.
     * @param window the JComponent to remove
     */
    public void removeWindow(JComponent window) {
        Container container = jtc.getParent();
        if(container instanceof JLayeredPane) {
            JLayeredPane pane = (JLayeredPane) container;
            pane.remove(window);
            pane.repaint(window.getBounds());
        }
    }
}
