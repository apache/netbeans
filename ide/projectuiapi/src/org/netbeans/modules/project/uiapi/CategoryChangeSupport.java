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

package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Support for listening on changes in a category's properties. Separated from
 * API.
 *
 * @author Martin Krauskopf
 */
public class CategoryChangeSupport {

    public static final CategoryChangeSupport NULL_INSTANCE = new CategoryChangeSupport() {
        public void firePropertyChange(String pn, Object o, Object n) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        void addPropertyChangeListener(PropertyChangeListener l) {}
    };
    
    private PropertyChangeSupport changeSupport;
    
    /** Name for the <code>valid</code> property. */
    public static final String VALID_PROPERTY = "isCategoryValid"; // NOI18N
    
    /** Property for an error message of the category. */
    public static final String ERROR_MESSAGE_PROPERTY = "categoryErrorMessage"; // NOI18N
    
    synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener == null || changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }
    
    
    public void firePropertyChange(String propertyName,
            Object oldValue, Object newValue) {
        if (changeSupport == null ||
                (oldValue != null && newValue != null && oldValue.equals(newValue))) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
}
