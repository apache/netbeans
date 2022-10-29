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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.WeakListeners;


/**
 *
 * @author  Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession", types={SmartSteppingFilter.class})
public class SmartSteppingFilterImpl implements SmartSteppingFilter {

    private final HashSet<String> filter = new HashSet<String>();
    private final PropertyChangeSupport pcs;
    {pcs = new PropertyChangeSupport (this);}
    private final Properties options = Properties.getDefault().
            getProperties("debugger.options.JPDA");
    private final Properties classFiltersProperties = Properties.getDefault().
            getProperties("debugger").getProperties("sources").
            getProperties("class_filters");
    private final PropertyChangeListener exclusionPatternsListener;

    {
        exclusionPatternsListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setExclusionPatterns ();
            }
        };
        options.addPropertyChangeListener(WeakListeners.propertyChange(
                exclusionPatternsListener, options));
        classFiltersProperties.addPropertyChangeListener(WeakListeners.propertyChange(
                exclusionPatternsListener, classFiltersProperties));
        setExclusionPatterns ();
    }

    private void setExclusionPatterns () {
        Set<String> patterns;
        if (options.getBoolean("UseStepFilters", true)) {
            patterns = (Set<String>) classFiltersProperties.getCollection (
                    "enabled", Collections.emptySet());
        } else {
            patterns = Collections.emptySet();
        }
        synchronized (filter) {
            filter.clear();
            filter.addAll (patterns);
        }
        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, null, patterns);
    }
    
    
    /**
     * Adds a set of new class exclusion filters. Filter is 
     * {@link java.lang.String} containing full class name. Filter can 
     * begin or end with '*' to define more than one class, for example 
     * "*.Ted", or "examples.texteditor.*".
     *
     * @param patterns a set of class exclusion filters to be added
     */
    @Override
    public void addExclusionPatterns (Set<String> patterns) {
        Set<String> reallyNew = new HashSet<String>(patterns);
        reallyNew.removeAll (filter);
        if (reallyNew.size () < 1) return;

        synchronized (filter) {
            filter.addAll (reallyNew);
        }

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, null, reallyNew);
    }

    /**
     * Removes given set of class exclusion filters from filter.
     *
     * @param patterns a set of class exclusion filters to be added
     */
    @Override
    public void removeExclusionPatterns (Set<String> patterns) {
        synchronized (filter) {
            filter.removeAll (patterns);
        }

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, patterns, null);
    }
    
    /**
     * Returns list of all exclusion patterns.
     */
    @Override
    public String[] getExclusionPatterns () {
        synchronized (filter) {
            String[] ef = new String [filter.size ()];
            return filter.toArray (ef);
        }
    }

    public boolean stopHere (String className) {
        // Retained for compatibility
        return StepActionProvider.stopInClass(className, this);
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    @Override
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }

}
