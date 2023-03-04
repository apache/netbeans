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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**Provider of list of {@link Mark}. The provider is supposed to report marks
 * found in a document. The provider can also tell whether the current list of marks
 * is up to date with the current state of the document. The provider is supposed
 * to fire a property change event if the list of marks or up-to-date property
 * are changed.
 *
 * @author Jan Lahoda
 */
public abstract class MarkProvider {
    
    /**Name of property which should be fired when the list of {@link Mark}s changes.
     */
    public static final String PROP_MARKS = "marks"; // NOI18N
    
    private PropertyChangeSupport pcs;
    
    /** Creates a new instance of MarkProvider */
    public MarkProvider() {
        pcs = new PropertyChangeSupport(this);
    }
    
    /**Return list of {@link Mark}s that are to be shown in the Error Stripe.
     *
     * @return list of {@link Mark}s
     */
    public abstract List<Mark> getMarks();
    
    /**Register a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**Unregister a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**Fire property change event to all registered listener. Subclasses should call
     * this method when they need to fire the {@link java.beans.PropertyChangeEvent}
     * because property {@link #PROP_UP_TO_DATE} or {@link #PROP_MARKS} have changed.
     *
     * @param name name of the property ({@link #PROP_UP_TO_DATE} or {@link #PROP_MARKS})
     * @param old  previous value of the property or null if unknown
     * @param nue  current value of the property or null if unknown
     */
    protected final void firePropertyChange(String name, Object old, Object nue) {
        pcs.firePropertyChange(name, old, nue);
    }
    
}
