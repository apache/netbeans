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
package org.netbeans.modules.versioning.ui.diff;

import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * @author Maros Sandor
 */
class DiffActionTooltipWindow implements AWTEventListener, WindowFocusListener {

    private static final int SCREEN_BORDER = 20;
    
    private JWindow actionsWindow;
    private JWindow contentWindow;

    private final DiffSidebar       master;    
    private final Difference        diff;

    public DiffActionTooltipWindow(DiffSidebar master, Difference diff) {
        this.master = master;
        this.diff = diff;
        Window w = SwingUtilities.windowForComponent(master.getTextComponent());
        actionsWindow = new JWindow(w);
        if (diff.getType() != Difference.ADD) {
            contentWindow = new JWindow(w);
        }
    }

    DiffSidebar getMaster() {
        return master;
    }

    public void show(Point location) {
        final DiffTooltipActionsPanel tp = new DiffTooltipActionsPanel(this, diff);
        actionsWindow.add(tp);
        actionsWindow.pack();
        actionsWindow.setLocation(location);

        if (contentWindow != null) {
            DiffTooltipContentPanel cp = new DiffTooltipContentPanel(master.getTextComponent(), master.getMimeType(), diff);
            contentWindow.add(cp);
            contentWindow.pack();
            cp.resize();
            Dimension dim = contentWindow.getSize();
                        
            Rectangle screenBounds = null;
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice [] gds = ge.getScreenDevices();
            for (GraphicsDevice device : gds) {
                GraphicsConfiguration gc = device.getDefaultConfiguration();
                screenBounds = gc.getBounds();
                if (screenBounds.contains(location)) break;
            }
        
            if (location.y + dim.height + SCREEN_BORDER > screenBounds.y + screenBounds.height) {
                dim.height = (screenBounds.y + screenBounds.height) - (location.y + SCREEN_BORDER);
            }
            if (location.x + dim.width + SCREEN_BORDER > screenBounds.x + screenBounds.width) {
                dim.width = (screenBounds.x + screenBounds.width) - (location.x + SCREEN_BORDER);  
            }
            
            if (dim.width < actionsWindow.getWidth()) {
                // make the content window at least match the width of the actions window 
                dim.width = actionsWindow.getWidth();
            }
            
            contentWindow.setSize(dim);

            contentWindow.setLocation(location.x, location.y + actionsWindow.getHeight() - 1);  // slight visual adjustment
            contentWindow.setVisible(true);
        }

        actionsWindow.setVisible(true);

        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        actionsWindow.addWindowFocusListener(this);
        actionsWindow.getOwner().addWindowFocusListener(this);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                tp.focusButton();
            }
        });
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(event);
/*
        } else if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE) {
                shutdown();
            }
*/
        }
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != actionsWindow && (contentWindow == null || w != contentWindow)) shutdown();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        //
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        if (actionsWindow != null && e.getOppositeWindow() == null) {
            shutdown();
        }
    }

    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        actionsWindow.getOwner().removeWindowFocusListener(this);
        actionsWindow.removeWindowFocusListener(this);
        actionsWindow.dispose();
        if (contentWindow != null) contentWindow.dispose();
    }
}
