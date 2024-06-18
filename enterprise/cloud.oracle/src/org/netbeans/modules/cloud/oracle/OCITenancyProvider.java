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
package org.netbeans.modules.cloud.oracle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Horvath
 */

public class OCITenancyProvider implements ServerInstanceProvider, ChangeListener, PropertyChangeListener {
    private static final String PROPERTY_PROFILE_ID_PREFIX = "profile.id."; // NOI18N
    private static final String PROPERTY_PROFILE_CONFIG_PREFIX = "profile.config."; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(OCITenancyProvider.class);
    
    private final ChangeSupport listeners;
    private Map<ProfileKey, ServerInstance> instances;
    private static OCITenancyProvider instance;

    private OCITenancyProvider() {
        listeners = new ChangeSupport(this);
        instances = new HashMap<> ();
        OCIManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OCIManager.getDefault()));
        RP.post(() -> refresh());
    }

    public static synchronized OCITenancyProvider getProvider() {
        if (instance == null) {
            instance = new OCITenancyProvider();
        }
        return instance;
    }
    
    @Override
    public List<ServerInstance> getInstances() {
        synchronized (this) {
            return new ArrayList<>(instances.values());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (OCIManager.PROP_CONNECTED_PROFILES.equals(evt.getPropertyName())) {
            refresh();
        }
    }
    
    private static class ProfileKey {
        final Path path;
        final String id;
        final String tenancyId;

        public ProfileKey(Path path, String id, String tenancyId) {
            this.path = path;
            this.id = id;
            this.tenancyId = tenancyId;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.path);
            hash = 29 * hash + Objects.hashCode(this.id);
            hash = 29 * hash + Objects.hashCode(this.tenancyId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProfileKey other = (ProfileKey) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.tenancyId, other.tenancyId)) {
                return false;
            }
            return Objects.equals(this.path, other.path);
        }
    }
    
    private void refresh() {
        Map<ProfileKey, ServerInstance> newInstances;
        
        synchronized (this) {
            newInstances = new HashMap<>(instances);
        }
        List<ProfileKey> currentKeys = new ArrayList<>();
        for (OCIProfile p : OCIManager.getDefault().getConnectedProfiles()) {
            String tenanctId = null;
            if (p.getTenancy().isPresent()) {
                tenanctId = p.getTenancy().get().getKey().getValue();
            } 
            ProfileKey k = new ProfileKey(p.getConfigPath(), p.getId(), tenanctId);
            ServerInstance prev = newInstances.get(k);
            if (prev != null) {
                OCIProfile prevProf = prev.getLookup().lookup(OCIProfile.class);
                if (prevProf == null || (prevProf.isValid() != p.isValid())) {
                    continue;
                }
            }
            ServerInstance si = ServerInstanceFactory.createServerInstance(new TenancyInstance(
                    p.getTenancy().orElse(null), p));
            newInstances.put(k, si);
            currentKeys.add(k);
        }
        newInstances.keySet().retainAll(currentKeys);
        synchronized (this) {
            if (this.instances.keySet().equals(newInstances.keySet())) {
                return;
            }
            this.instances = newInstances;
        }
        listeners.fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh();
    }
    
}
