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

package org.netbeans.modules.cnd.debugger.gdb2.actions;

import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.gdb2.GdbEngineCapabilityProvider;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.AttachPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.AttachPanelProvider;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;

/**
 *
 */
public final class GdbAttachType extends AttachType {
    private static final boolean isOssTooolchainOnly = Boolean.valueOf(System.getProperty("oss.toolchain.only", "false")); // NOI18N
    private static final boolean forceGDB = Boolean.valueOf(System.getProperty("oss.force.gdb", "false")); // NOI18N
    
    private static GdbAttachType INSTANCE = null;
    
    private Reference<AttachPanel> customizerRef = new WeakReference<AttachPanel>(null);
    
    @AttachType.Registration(displayName = "#GdbDebuggerEngine") // NOI18N
    public static synchronized AttachType get() {
        if (INSTANCE == null)  {
            INSTANCE = new GdbAttachType();
        }
        
        return INSTANCE;
    }

    private GdbAttachType() {
    }

    @Override
    public JComponent getCustomizer() {
        EngineType et = GdbEngineCapabilityProvider.getGdbEngineType();
        AttachPanel panel = AttachPanelProvider.getAttachPanel(null, null, et);
        customizerRef = new WeakReference<AttachPanel>(panel);
        return panel;
    }

    @Override
    public Controller getController() {
        AttachPanel panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeDisplayName() {
        if (isEnabled()) {
            return Catalog.get("GdbDebuggerEngine"); // NOI18N
        }
        
        return null;
    }
    
    private boolean isEnabled() {
        if (forceGDB) {
            return true;
        }
        return !isOssTooolchainOnly;
    }
}
