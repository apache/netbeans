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

package org.netbeans.modules.debugger.ui;

import java.lang.reflect.Method;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 *
 * @author Martin Entlicher
 */
public class DebuggerModule extends ModuleInstall implements WindowSystemListener {
    
    private volatile boolean closing = false;
    
    /** Creates a new instance of DebuggerModule */
    public DebuggerModule() {
    }

    @Override
    public void restored() {
        WindowManager.getDefault().addWindowSystemListener(this);
        super.restored();
    }
    
    @Override
    public boolean closing() {
        closing = true;
        return true;
    }

    public boolean isClosing() {
        return closing && isExiting();
    }
    
    private boolean isExiting() {
        LifecycleManager lcm = LifecycleManager.getDefault();
        try {
            Method isExitingMethod = lcm.getClass().getMethod("isExiting");
            return (Boolean) isExitingMethod.invoke(lcm);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    @Override
    public void close() {
        WindowManager.getDefault().removeWindowSystemListener(this);
        super.close();
    }

    @Override
    public void uninstalled() {
        super.uninstalled();
        WindowManager.getDefault().removeWindowSystemListener(this);
    }
    
    @Override
    public void beforeLoad(WindowSystemEvent event) {
    }

    @Override
    public void afterLoad(WindowSystemEvent event) {
    }

    @Override
    public void beforeSave(WindowSystemEvent event) {
        if (isClosing()) {
            DebuggerManagerListener.closeDebuggerUI();
        }
    }

    @Override
    public void afterSave(WindowSystemEvent event) {
    }
    
}
