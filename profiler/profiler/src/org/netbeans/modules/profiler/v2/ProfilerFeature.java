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

package org.netbeans.modules.profiler.v2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerFeature {
    
    public abstract Icon getIcon();
    
    public abstract String getName();
    
    public abstract String getDescription();
    
    public abstract int getPosition();
    
    
    // To be called in EDT
    public abstract JPanel getResultsUI();
    
    // To be called in EDT
    public abstract JPanel getSettingsUI();
    
    // To be called in EDT
    public abstract ProfilerToolbar getToolbar();
    
    
    public abstract boolean supportsSettings(ProfilingSettings settings);
    
    // To be called in EDT
    public abstract void configureSettings(ProfilingSettings settings);
    
    // To be called in EDT
    public abstract boolean currentSettingsValid();
    
    
    public abstract boolean supportsConfiguration(Lookup configuration);
    
    // To be called in EDT
    public abstract void configure(Lookup configuration);
    
    
    protected void activatedInSession() {}
    
    protected void deactivatedInSession() {}
    
    
    public abstract void addChangeListener(ChangeListener listener);
    
    public abstract void removeChangeListener(ChangeListener listener);
    
    
    public abstract static class Basic extends ProfilerFeature {
        
        private Set<ChangeListener> listeners;
        
        private final Icon icon;
        private final String name;
        private final String description;
        private final int position;
        
        private final ProfilerSession session;
        
        private volatile boolean isActive;
        
        
        public Basic(Icon icon, String name, String description, int position, ProfilerSession session) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.position = position;
            
            this.session = session;
        }
        
        public final Icon getIcon() { return icon; }
        
        public final String getName() { return name; }
    
        public final String getDescription() { return description; }
        
        public final int getPosition() { return position; }
        
        
        protected final ProfilerSession getSession() { return session; }
        
        
        public JPanel getSettingsUI() { return null; }
        
        public boolean supportsSettings(ProfilingSettings settings) { return true; }
        
        public boolean currentSettingsValid() { return true; }
        
        
        public ProfilerToolbar getToolbar() { return null; }
        
        
        public boolean supportsConfiguration(Lookup configuration) { return false; }
    
        public void configure(Lookup configuration) {}
        
        
        protected void notifyActivated() {}
        
        protected void notifyDeactivated() {}
        
        protected final boolean isActivated() { return isActive; }
        
        
        protected final void activatedInSession() {
            isActive = true;
            
            notifyActivated();
            
            session.addListener(getListener());
            
            final int state = session.getState();
            Runnable notifier = new Runnable() {
                public void run() { profilingStateChanged(-1, state); }
            };
            UIUtils.runInEventDispatchThread(notifier);
        }
    
        protected final void deactivatedInSession() {
            isActive = false;
            
            notifyDeactivated();
            
            session.removeListener(getListener());
            listener = null;
            
            final int state = Profiler.PROFILING_INACTIVE;
            Runnable notifier = new Runnable() {
                public void run() { profilingStateChanged(-1, state); }
            };
            UIUtils.runInEventDispatchThread(notifier);
        }
        
        protected final int getSessionState() {
            return isActive ? session.getState() : Profiler.PROFILING_INACTIVE;
        }
        
        
        protected final String readFlag(String flag, String defaultValue) {
            String id = getClass().getName();
            return session.getStorage().readFlag(id + "_" + flag, defaultValue); // NOI18N
        }
        
        protected final void storeFlag(String flag, String value) {
            String id = getClass().getName();
            session.getStorage().storeFlag(id + "_" + flag, value); // NOI18N
        }
        
        
        private ProfilingStateListener listener;
        private ProfilingStateListener getListener() {
            if (listener == null) listener = new ProfilingStateListener() {
                public void serverStateChanged(int serverState, int serverProgress) {
                    if (!isActive) return;
                    Basic.this.serverStateChanged(serverState, serverProgress);
                }
                public void instrumentationChanged(int oldType, int newType) {
                    if (!isActive) return;
                    Basic.this.instrumentationChanged(oldType, newType);
                }
                public void profilingStateChanged(ProfilingStateEvent e) {
                    if (!isActive) return;
                    Basic.this.profilingStateChanged(e.getOldState(), e.getNewState());
                }
                public void threadsMonitoringChanged() {
                    if (!isActive) return;
                    Basic.this.threadsMonitoringChanged();
                }
                public void lockContentionMonitoringChanged() {
                    if (!isActive) return;
                    Basic.this.lockContentionMonitoringChanged();
                }
            };
            return listener;
        }
        
        protected void serverStateChanged(int serverState, int serverProgress) {}
        
        protected void instrumentationChanged(int oldType, int newType) {}

        protected void profilingStateChanged(int oldState, int newState) {}

        protected void threadsMonitoringChanged() {}

        protected void lockContentionMonitoringChanged() {}
        
        
        public final synchronized void addChangeListener(ChangeListener listener) {
            if (listeners == null) listeners = new HashSet();
            listeners.add(listener);
        }
    
        public final synchronized void removeChangeListener(ChangeListener listener) {
            if (listeners != null) listeners.remove(listener);
        }
        
        protected final synchronized void fireChange() {
            if (listeners == null) return;
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener listener : listeners) listener.stateChanged(e);
        }
        
    }
    
    
    // --- Provider ------------------------------------------------------------
    
    public abstract static class Provider {
        
        public abstract ProfilerFeature getFeature(ProfilerSession session);
        
    }
    
    
    // --- Registry ------------------------------------------------------------
    
    public static final class Registry {
        
        private static boolean HAS_PROVIDERS;
        
        private Registry() {}
        
        public static boolean hasProviders() {
            return HAS_PROVIDERS;
        }
        
        static Collection<? extends Provider> getProviders() {
            Collection<? extends Provider> providers = Lookup.getDefault().lookupAll(Provider.class);
            HAS_PROVIDERS = !providers.isEmpty();
            return providers;
        }
        
    }
    
}
