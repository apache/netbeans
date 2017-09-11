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

package org.netbeans.modules.applemenu;

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
 */

abstract class NbApplicationAdapter {
    
    NbApplicationAdapter() {
    }

    static void install() {
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

    void handleAbout() {
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
    
    void openFiles(List<File> files) {
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
    
    public void handlePreferences() {
        performAction("Window", "org.netbeans.modules.options.OptionsWindowAction");    // NOI18N
    }
    public void handleQuit() {
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
