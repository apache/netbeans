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
package org.netbeans.modules.css.visual;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Descriptor of a filter.
 *
 * @author Jan Stola
 */
public class Filter {
    /** Name of the property fired when the pattern changes. */
    public static final String PROPERTY_PATTERN = "pattern"; // NOI18N
    /** Pattern of this filter. */
    private String pattern;
    /** Property change support. */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Returns the pattern of this filter.
     *
     * @return pattern of this filter.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern of this filter.
     *
     * @param pattern new pattern of this filter.
     */
    void setPattern(String pattern) {
        String oldPattern = this.pattern;
        this.pattern = pattern;
        changeSupport.firePropertyChange(PROPERTY_PATTERN, oldPattern, pattern);
    }

    /**
     * Registers a property change listener.
     *
     * @param listener listener to register.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a property change listener.
     *
     * @param listener listener to unregister.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Unregisters all previously registered property change listeners.
     */
    public void removePropertyChangeListeners() {
        for (PropertyChangeListener listener : changeSupport.getPropertyChangeListeners()) {
            removePropertyChangeListener(listener);
        }
    }

}
