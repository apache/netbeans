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
package org.netbeans.modules.versioning.shelve;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;

/**
 *
 * @author ondra
 */
public final class ShelveChangesActionsRegistry {

    private static ShelveChangesActionsRegistry instance;
    private final Map<VersioningSystem, ShelveChangesActionProvider> actionProviders;
    
    public static synchronized ShelveChangesActionsRegistry getInstance () {
        if (instance == null) {
            instance = new ShelveChangesActionsRegistry();
        }
        return instance;
    }
    
    private ShelveChangesActionsRegistry () {
        actionProviders = new WeakHashMap<VersioningSystem, ShelveChangesActionProvider>();
    }

    public void registerAction (VersioningSystem vs, ShelveChangesActionProvider prov) {
        synchronized (actionProviders) {
            actionProviders.put(vs, prov);
        }
    }

    public ShelveChangesActionProvider getActionProvider (VersioningSystem owner) {
        synchronized(actionProviders) {
            return actionProviders.get(owner);
        }
    }
    
    public abstract static class ShelveChangesActionProvider {
        
        public abstract Action getAction ();
        
        public JComponent[] getUnshelveActions (VCSContext ctx, boolean popup) {
            return new JComponent[0];
        }
        
    }
}
