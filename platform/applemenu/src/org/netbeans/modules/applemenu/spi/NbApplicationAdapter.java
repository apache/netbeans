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

package org.netbeans.modules.applemenu.spi;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.RootPaneContainer;

import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.*;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/** Adapter class which intercepts action events and passes them to the
 * correct action instance as defined in the system filesystem.
 *
 * @author  Tim Boudreau
 * @since 1.47
 */
public abstract class NbApplicationAdapter {
    
    protected NbApplicationAdapter() {
    }
    
    /** Installs the provider.
     * @exception Throwable if the install fails
     */
    public abstract void install() throws Throwable;
    /** Uninstalls the provider. Only called if the install succeeded.
     */
    public abstract void uninstall();

    protected final void handleInstall() {
        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {

            @Override
            public void beforeLoad(WindowSystemEvent event) {
                WindowManager.getDefault().removeWindowSystemListener(this);
                try {
                    Frame main = WindowManager.getDefault().getMainWindow();
                    ((RootPaneContainer)main).getRootPane().putClientProperty("apple.awt.fullscreenable", true);    // NOI18N
                } catch( Throwable e ) {
                    Logger.getLogger(NbApplicationAdapter.class.getName()).log(Level.FINE, 
                            "Error while setting up full screen support.", e );//NOI18N
                }
            }

            @Override
            public void afterLoad(WindowSystemEvent event) {
            }

            @Override
            public void beforeSave(WindowSystemEvent event) {
            }

            @Override
            public void afterSave(WindowSystemEvent event) {
            }
        });
    }

    protected final void handleAbout() {
        //#221571 - check if About window is showing already
        Window[] windows = Dialog.getWindows();
        if( null != windows ) {
            for( Window w : windows ) {
                if( w instanceof JDialog ) {
                    JDialog dlg = (JDialog) w;
                    if( Boolean.TRUE.equals(dlg.getRootPane().getClientProperty("nb.about.dialog") ) ) { //NOI18N
                        if( dlg.isVisible() ) {
                            dlg.toFront();
                            return;
                        }
                    }
                }
            }
        }
        performAction("Help", "org.netbeans.core.actions.AboutAction"); // NOI18N
    }
    
    protected final void openFiles(List<File> files) {
        for (File f : files) {
            if (f.exists() && !f.isDirectory()) {
                FileObject obj = FileUtil.toFileObject(f);
                if (obj != null) {
                    try {
                        DataObject dob = DataObject.find(obj);
                        OpenCookie oc = dob.getLookup().lookup (OpenCookie.class);
                        if (oc != null) {
                            oc.open();
                        } else {
                            EditCookie ec = dob.getLookup().lookup(EditCookie.class);
                            if (ec != null) {
                                ec.edit();
                            } else {
                                ViewCookie v = dob.getLookup().lookup(ViewCookie.class);
                                if (v != null) {
                                    v.view();
                                }
                            }
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(NbApplicationAdapter.class.getName()).log(Level.INFO, f.getAbsolutePath(), ex);
                    }
                }
            }
        }
    }
    
    protected final void handlePreferences() {
        performAction("Window", "org.netbeans.modules.options.OptionsWindowAction");    // NOI18N
    }
    protected final void handleQuit() {
        performAction("System", "org.netbeans.core.actions.SystemExit");    // NOI18N
    }
    
    private boolean performAction(String category, String id) {
        Action a = Actions.forID(category, id);
        if (a == null) {
            return false;
        }
        ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "whatever");   // NOI18N
        try {
            a.actionPerformed(ae);
            return true;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return false;
        }
    }
    
}
