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


package org.netbeans.core.windows.actions;


import java.awt.EventQueue;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

import javax.swing.SwingUtilities;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.awt.Mnemonics;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;


/**
 * @author   S. Aubrecht
 */
public class ToggleFullScreenAction extends SystemAction implements DynamicMenuContent, Runnable {

    private JCheckBoxMenuItem [] menuItems;
    
    public ToggleFullScreenAction() {
        addPropertyChangeListener( new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( Action.ACCELERATOR_KEY.equals(evt.getPropertyName()) ) {
                    synchronized( ToggleFullScreenAction.this ) {
                        //119127 - make sure shortcut gets updated in the menu
                        menuItems = null;
                        createItems();
                    }
                }
            }
        });
    }
    
    @Override
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return menuItems;
    }
    
    private void updateState() {
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** Updates state of action. Uses Runnable interface impl to save one class */ 
    @Override
    public void run () {
        Frame frame = WindowManager.getDefault().getMainWindow();
        synchronized( this ) {
            createItems();
            menuItems[0].setSelected(null != frame 
                    && MainWindow.getInstance().isFullScreenMode());
        }
    }
    
    private void createItems() {
        synchronized( this ) {
            if (menuItems == null) {
                menuItems = new JCheckBoxMenuItem[1];
                menuItems[0] = new JCheckBoxMenuItem(this);
                menuItems[0].setIcon(null);
                Mnemonics.setLocalizedText(menuItems[0], NbBundle.getMessage(ToggleFullScreenAction.class, "CTL_ToggleFullScreenAction"));
            }
        }
    }

    /** Perform the action. Sets/unsets maximzed mode. */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        MainWindow mainWindow = MainWindow.getInstance();
        mainWindow.setFullScreenMode( !mainWindow.isFullScreenMode() );
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleFullScreenAction.class, "CTL_ToggleFullScreenAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToggleFullScreenAction.class);
    }

    @Override
    public boolean isEnabled() {
        return WindowManager.getDefault().getMainWindow() == MainWindow.getInstance().getFrame()
                && !Utilities.isMac();
    }
}

