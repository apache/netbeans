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
package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.util.Collection;
import javax.swing.JButton;
import org.netbeans.modules.cnd.debugger.common2.debugger.DialogManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.AttachPanelFactory;
import org.netbeans.modules.cnd.debugger.common2.ui.AttachPanelImpl;
import org.netbeans.modules.cnd.debugger.common2.ui.processlist.AttachToProcessTopComponent;
import org.openide.util.Lookup;

/**
 * Client can user AttachPanel provider or AttachPanelFactory directly
 */
public class AttachPanelProvider {
    
    public static AttachPanel getAttachPanel(DialogManager dialogManager, JButton okButton, EngineType debuggerType) {
        
        //can defind a special command line arg to use an old attach dialog,
        //if it is not defined: use service providers, if not registered use default        
        boolean useOldAttachDialog = Boolean.getBoolean("cnd.debugger.common2.attach.old");//NOI18N
        if (useOldAttachDialog) {
            return AttachPanelImpl.getInstance(dialogManager, okButton, debuggerType);
        }
        Collection<? extends AttachPanelFactory> factories = Lookup.getDefault().lookupAll(AttachPanelFactory.class);
        for (AttachPanelFactory factory : factories) {
            if (factory.supports(debuggerType)) {
                return factory.create(dialogManager, okButton, debuggerType);
            }
        }
        return AttachToProcessTopComponent.getInstance(dialogManager, okButton, debuggerType);
    }
}
