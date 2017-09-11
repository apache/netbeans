/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;



/**
 * Custom popup factory to create popup menus without the background shadow.
 * 
 * Copied and modified from org.netbeans.modules.applemenu.ApplePopupFactory
 * which only worked for Mac OS X, while the background was present in other
 * GUIs like Linux/GTK.
 * 
 * @see org.openide.explorer.view.ViewTooltips
 *
 * @author Tim Boudreau
 * @author Jiri Sedlacek
 */
class CustomPopupFactory extends PopupFactory {
        
    CustomPopupFactory() {
    }
    
    @Override
    public Popup getPopup(Component owner, Component contents,
                          int x, int y) throws IllegalArgumentException {
        assert owner instanceof JComponent;
        Dimension d = contents.getPreferredSize();
        Container c = ((JComponent) owner).getTopLevelAncestor();
        if (c == null) {
            throw new IllegalArgumentException ("Not onscreen: " + owner);
        }
        Point p = new Point (x, y);
        SwingUtilities.convertPointFromScreen(p, c);
        Rectangle r = new Rectangle (p.x, p.y, d.width, d.height);
        if (c.getBounds().contains(r)) {
            //XXX need API to determine if editor area comp is heavyweight,
            //and if so, return a "medium weight" popup of a java.awt.Component
            //that embeds the passed contents component
            return new LWPopup (owner, contents, x, y);
        } else {
            return new HWPopup (owner, contents, x, y);
        }
    }
    
    private static abstract class OurPopup extends Popup {
        protected Component owner = null;
        protected Component contents = null;
        protected int x = -1;
        protected int y = -1;
        public OurPopup (Component owner, Component contents, int x, int y) {
            configure (owner, contents, x, y);
        }
        
        final void configure (Component owner, Component contents, int x, int y) {
            this.owner = owner;
            this.contents = contents;
            this.x = x;
            this.y = y;
        }
        
        protected abstract void prepareResources();
        protected abstract void doShow();
        public abstract boolean isShowing();
        protected abstract void doHide();
        
        @Override
        public final void show() {
            prepareResources();
            doShow();
        }
        
        @Override
        public final void hide() {
            doHide();
        }
        
        void dispose() {
            owner = null;
            contents = null;
            x = -1;
            y = -1;
        }
        
        private boolean canReuse = false;
        public final void clear() {
            canReuse = true;
            dispose();
        }
        
        boolean isInUse() {
            return canReuse;
        }
    }
    
    private static class LWPopup extends OurPopup {
        public LWPopup (Component owner, Component contents, int x, int y) {
            super (owner, contents, x, y);
        }

        private Rectangle bounds = null;
        @Override
        protected void prepareResources() {
            JComponent jc = (JComponent) owner;
            Container w = jc.getTopLevelAncestor();
            JComponent pane = null;
            if (w instanceof JFrame) {
                pane = (JComponent) ((JFrame) w).getGlassPane();
            } else if (w instanceof JDialog) {
                pane = (JComponent) ((JDialog) w).getGlassPane();
            } else if (w instanceof JWindow) {
                pane = (JComponent) ((JWindow) w).getGlassPane();
            }
            if (w == null) {
                throw new IllegalArgumentException ("Not a JFrame/" + //NOI18N
                        "JWindow/JDialog: " + owner); //NOI18N
            }
            Point p = new Point (x, y);
            SwingUtilities.convertPointFromScreen(p, pane);
            if (pane.getLayout() != null) {
                pane.setLayout (null);
            }
            pane.setVisible(true);
            contents.setVisible (false);
            Dimension d = contents.getPreferredSize();
            pane.add (contents);
            bounds = new Rectangle (p.x, p.y, d.width, d.height);
            contents.setBounds (p.x, p.y, d.width, d.height);
        }
        
        @Override
        protected void doShow() {
            contents.setVisible (true);
        }
        
        @Override
        public boolean isShowing() {
            return contents != null && contents.isShowing();
        }
        
        @Override
        protected void doHide() {
            Container parent = contents.getParent();
            if (parent != null) {
                contents.getParent().remove (contents);
                parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
                parent.setVisible(false);
            }
            //If doShow() was never called, we've modified the visibility
            //of the contents component, which could cause problems elsewhere
            contents.setVisible (true);
        }
    }
    
    private static class HWPopup extends OurPopup {
        private JWindow window = null;
        public HWPopup (Component owner, Component contents, int x, int y) {
            super (owner, contents, x, y);
        }
        
        @Override
        public boolean isShowing() {
            return window != null && window.isShowing();
        }
        
        @Override
        void dispose() {
            window = null;
            super.dispose();
        }
        
        @Override
        protected void prepareResources() {
            window = new JWindow(SwingUtilities.getWindowAncestor(owner));
            window.setType(JWindow.Type.POPUP);
            window.getContentPane().add (contents);
            window.setLocation (new Point (x, y));
            window.pack();
            disableShadow(window);
        }
        
        @Override
        protected void doShow() {
            window.setVisible(true);
        }
        
        @Override
        protected void doHide() {
            if (window != null) {
                window.setVisible(false);
                window.getContentPane().remove (contents);
                //Try to force a reset
                dispose();
            }
        }
    }
    
    private static void disableShadow(JWindow win) {
        safeSetBackground(win, new Color(255, 255, 255, 0)); // Linux // #262670
        win.getRootPane().putClientProperty("Window.shadow", Boolean.FALSE.toString()); // Mac OS X // NOI18N
    }
    
    // See Window.setBackground() documentation
    private static void safeSetBackground(JWindow window, Color background) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        
        if (!gc.isTranslucencyCapable()) return; // PERPIXEL_TRANSLUCENT not supported
        if (gc.getDevice().getFullScreenWindow() == window) return; // fullscreen windows not supported
        
        window.setBackground(background);
    }
    
}
