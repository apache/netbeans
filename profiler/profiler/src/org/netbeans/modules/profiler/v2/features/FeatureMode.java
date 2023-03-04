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

package org.netbeans.modules.profiler.v2.features;

import java.util.Collection;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.ProfilingSettings;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class FeatureMode {
    
    // --- API -----------------------------------------------------------------
    
    abstract String getID();
    
    abstract String getName();
    
    abstract void configureSettings(ProfilingSettings settings);
    
    abstract void confirmSettings();
    
    abstract boolean pendingChanges();
    
    abstract boolean currentSettingsValid();
    
    abstract JComponent getUI();
    
    
    // --- External implementation ---------------------------------------------
    
    abstract void settingsChanged();
    
    abstract String readFlag(String flag, String defaultValue);

    abstract void storeFlag(String flag, String value);
    
    
    // --- Roots Set -----------------------------------------------------------
    
    // To be only accessed in EDT
    static class Selection extends HashSet<ClientUtils.SourceCodeSelection> {
        
        private boolean trans;
        private boolean dirty;
        private boolean events;
        private boolean changing;
        
        final void beginTrans() {
            assert SwingUtilities.isEventDispatchThread();
            trans = true;
        }
        
        final void endTrans() {
            assert SwingUtilities.isEventDispatchThread();
            trans = false;
            if (dirty) _changed();
        }
        
        final void enableEvents() {
            assert SwingUtilities.isEventDispatchThread();
            events = true;
        }
        
        final void disableEvents() {
            assert SwingUtilities.isEventDispatchThread();
            events = false;
        }
        
        public final boolean add(ClientUtils.SourceCodeSelection selection) {
            assert SwingUtilities.isEventDispatchThread();
            _changing();
            if (super.add(selection)) return _changed();
            else return false;
        }
        
        public final boolean addAll(Collection<? extends ClientUtils.SourceCodeSelection> selections) {
            boolean _trans = trans;
            beginTrans();
            
            boolean addAll = super.addAll(selections);
            
            endTrans();
            trans = _trans;
            
            return addAll;
        }
        
        public final boolean remove(Object selection) {
            assert SwingUtilities.isEventDispatchThread();
            _changing();
            if (super.remove(selection)) return _changed();
            else return false;
        }
        
        public final boolean removeAll(Collection<?> selections) {
            boolean _trans = trans;
            beginTrans();
            
            _changing();
            boolean removeAll = super.removeAll(selections);
            if (removeAll) _changed();
            
            endTrans();
            trans = _trans;
            
            return removeAll;
        }
        
        public final boolean retainAll(Collection<?> selections) {
            boolean _trans = trans;
            beginTrans();
            
            _changing();
            boolean retainAll = super.retainAll(selections);
            if (retainAll) _changed();
            
            endTrans();
            trans = _trans;
            
            return retainAll;
        }
        
        public final void clear() {
            assert SwingUtilities.isEventDispatchThread();
            _changing();
            super.clear();
            _changed();
        }
        
        private void _changing() {
            if (trans) {
                if (!changing) changing = true;
                else return;
            }
            if (events) changing();
        }
        
        protected void changing() {}
        
        private boolean _changed() {
            changing = false;
            if (!trans) {
                if (events) changed();
                dirty = false;
            } else {
                dirty = true;
            }
            return true;
        }
        
        protected void changed() {}
        
    }
    
}
