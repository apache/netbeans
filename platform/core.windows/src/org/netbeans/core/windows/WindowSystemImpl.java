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
