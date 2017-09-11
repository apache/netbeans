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


package org.netbeans.core.windows;


import java.awt.EventQueue;
import org.netbeans.core.WindowSystem;
import org.netbeans.core.windows.design.DesignView;
import org.netbeans.core.windows.services.DialogDisplayerImpl;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.openide.util.lookup.ServiceProvider;


/**
 * Implementation of WindowSystem interface
 *
 * @author  Peter Zavadsky
 */
@ServiceProvider(service=WindowSystem.class)
public class WindowSystemImpl implements WindowSystem {

    @Override
    public void init() {
        assert !EventQueue.isDispatchThread();
        if (Boolean.getBoolean("org.netbeans.core.WindowSystem.designMode")) { // NOI18N
            DesignView.initialize();
        }
        MainWindow.init();
    }

    @Override
    public void load() {
        WindowManagerImpl.assertEventDispatchThread();
        
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.fireEvent( WindowSystemEventType.beforeLoad );
        
        PersistenceHandler.getDefault().load();
        
        wm.fireEvent( WindowSystemEventType.afterLoad );
    }
    
    @Override
    public void save() {
        WindowManagerImpl.assertEventDispatchThread();
        
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.fireEvent( WindowSystemEventType.beforeSave );

        PersistenceHandler.getDefault().save();

        wm.fireEvent( WindowSystemEventType.afterSave );
    }
    
    // GUI
    @Override
    public void show() {
        WindowManagerImpl.assertEventDispatchThread();
        
        DialogDisplayerImpl.runDelayed();
        ShortcutAndMenuKeyEventProcessor.install();
        WindowManagerImpl.getInstance().setVisible(true);
    }
    
    @Override
    public void hide() {
        WindowManagerImpl.assertEventDispatchThread();
        
        WindowManagerImpl.getInstance().setVisible(false);
        ShortcutAndMenuKeyEventProcessor.uninstall();
    }
    
//    /**
//     * Clears the window system model - does not delete the configuration
//     * under Windows2Local! You have to delete the folder before calling
//     * this method to really reset the window system state.
//     */
//    public void clear() {
//        WindowManagerImpl.assertEventDispatchThread();
//        hide();
//        WindowManagerImpl.getInstance().resetModel();
//        PersistenceManager.getDefault().clear();
//        PersistenceHandler.getDefault().clear();
//        load();
//        show();
//    }
//
}
