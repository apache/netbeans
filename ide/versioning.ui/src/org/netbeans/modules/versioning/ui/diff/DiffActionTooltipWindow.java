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
