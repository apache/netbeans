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
package org.netbeans.modules.debugger.jpda.visual.spi;

import javax.swing.SwingUtilities;
import org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent;

/**
 * 
 * @author Martin Entlicher
 */
public final class ScreenshotUIManager {
    
    public static final String ACTION_TAKE_SCREENSHOT = "takeScreenshot";   // NOI18N
    
    private RemoteScreenshot rs;
    private ScreenshotComponent sc;
    
    ScreenshotUIManager(RemoteScreenshot rs) {
        this.rs = rs;
    }
    
    /**
     * Get an active opened remote screenshot.
     * @return The active screenshot or <code>null</code>.
     */
    public static ScreenshotUIManager getActive() {
        ScreenshotComponent activeComponent = ScreenshotComponent.getActive();
        if (activeComponent != null) {
            return activeComponent.getManager();
        }
        return null;
    }
    
    public RemoteScreenshot getScreenshot() {
        return rs;
    }
    
    public void open() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    open();
                }
            });
            return;
        }
        if (sc == null) {
            sc = new ScreenshotComponent(rs, this);
        }
        sc.open();
        sc.requestActive();
    }
    
    public void requestActive() {
        sc.requestActive();
    }
    
    public boolean close() {
        return sc.close();
    }
    
    /**
     * Get the component selected in the screenshot or <code>null</code>.
     * @return the component or <code>null</code>
     */
    public ComponentInfo getSelectedComponent() {
        if (sc == null) return null;
        return sc.getSelectedComponent();
    }
    
    public void markBreakpoint(ComponentInfo ci) {
        if (sc == null) return ;
        sc.markBreakpoint(ci);
    }
    
    public void unmarkBreakpoint(ComponentInfo ci) {
        if (sc == null) return ;
        sc.unmarkBreakpoint(ci);
    }
    
}
