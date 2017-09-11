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

