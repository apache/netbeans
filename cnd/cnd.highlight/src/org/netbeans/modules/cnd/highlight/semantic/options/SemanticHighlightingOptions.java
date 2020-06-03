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

package org.netbeans.modules.cnd.highlight.semantic.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;

/**
 *
 */
public final class SemanticHighlightingOptions {

    private SemanticHighlightingOptions() {}
    
    private static class Instantiator {
        public static SemanticHighlightingOptions instance = new SemanticHighlightingOptions();
        private Instantiator() {
        }
    }
    
    public static SemanticHighlightingOptions instance() {
        return Instantiator.instance;
    }

    private final Preferences preferences = NbPreferences.forModule(SemanticHighlightingOptions.class);

    private static final String ENABLE_MARK_OCCURRENCES = "EnableMarkOccurrences"; // NOI18N
    private static final String KEEP_MARKS = "KeepMarks"; // NOI18N

    public static final boolean SEMANTIC_ADVANCED = Boolean.getBoolean("cnd.semantic.advanced"); // NOI18N

    private final Set<PropertyChangeListener> listeners = new WeakSet<>();
    private final Object lock = new Object();

    public void addPropertyChangeListener(PropertyChangeListener listener){
        synchronized(lock) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        synchronized(lock) {
            listeners.remove(listener);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized(lock) {
            for(PropertyChangeListener listener : listeners){
                listener.propertyChange(evt);
            }
        }
    }

    private boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private void setOption(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
    
    public boolean getEnableMarkOccurrences() {
        return getOption(ENABLE_MARK_OCCURRENCES, true);
    }

    public void setEnableMarkOccurrences(boolean value) {
        setOption(ENABLE_MARK_OCCURRENCES, value);
    }

    public boolean getKeepMarks() {
        return getOption(KEEP_MARKS, true);
    }

    public void setKeepMarks(boolean value) {
        setOption(KEEP_MARKS, value);
    }
}
