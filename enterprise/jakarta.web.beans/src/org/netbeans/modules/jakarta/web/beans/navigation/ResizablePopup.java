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
package org.netbeans.modules.jakarta.web.beans.navigation;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

import org.openide.windows.WindowManager;

/**
 * Copied from ResizablePopup at java.navigation. 
 *
 * @author ads
 */
public final class ResizablePopup {   
    static final String HELP_COOKIE = "help"; // NOI18N
    
    private static final WindowListener windowListener = new WindowAdapter() {
        
        public void windowClosing(WindowEvent windowEvent) {
            cleanup(windowEvent.getWindow());
        }

        private void cleanup(Window window) {
            window.setVisible(false);
            if (window instanceof RootPaneContainer) {
                ((RootPaneContainer) window).setContentPane(new JPanel());
            }
            window.removeWindowListener(this);
            window.dispose();
        }
        
        /*private boolean aboutToShowHelp(Window window) {
            if (window instanceof RootPaneContainer) {
                JComponent rootPane = ((RootPaneContainer) window).getRootPane();
                if (Boolean.TRUE.equals(rootPane.getClientProperty(HELP_COOKIE))) {
                    rootPane.putClientProperty(HELP_COOKIE, null);
                    return true;
                }
            }
            return false;
        }*/
    };

    public static JDialog getDialog() {
        JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), 
                "", false) 
        {
            private static final long serialVersionUID = -2488334519927160789L;

            public void setVisible(boolean visible) {
                boolean wasVisible = isVisible();
                if (wasVisible && !visible) {
                    WebBeansNavigationOptions.setLastBounds(getBounds());
                }
                super.setVisible(visible);
            }
        };
        //dialog.setUndecorated(true);
        dialog.setBounds(WebBeansNavigationOptions.getLastBounds());
        dialog.addWindowListener(windowListener);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        return dialog;
    }
}
