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

package org.netbeans.spi.editor.errorstripe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;

/**UpToDateStatus provider.
 *
 * TODO: better javadoc.
 *
 * @author Jan Lahoda
 */
public abstract class UpToDateStatusProvider {

    static {
        SPIAccessor.DEFAULT = new SPIAccessorImpl();
    }

    /**Name of property which should be fired when the up-to-date status changes.
     */
    public static final String PROP_UP_TO_DATE = "upToDate"; // NOI18N

    private PropertyChangeSupport pcs;
    
    /** Creates a new instance of MarkProvider */
    public UpToDateStatusProvider() {
        pcs = new PropertyChangeSupport(this);
    }
    
    /**Report whether the current annotations attached to the documents are up-to-date
     * (the meaning of up-to-date is left on the provider).
     *
     * If a provider does not provide this information, it should
     * always return {@link UpToDateStatus#UP_TO_DATE_OK} value.
     *
     * @return a value of the {@link UpToDateStatus} enum.
     *
     * @see UpToDateStatus#UP_TO_DATE_OK
     * @see UpToDateStatus#UP_TO_DATE_PROCESSING
     * @see UpToDateStatus#UP_TO_DATE_DIRTY
     *
     */
    public abstract UpToDateStatus getUpToDate();
    
    /**Register a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    /*package private*/ final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**Unregister a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    /*package private*/ final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**Fire property change event to all registered listener. Subclasses should call
     * this method when they need to fire the {@link java.beans.PropertyChangeEvent}
     * because property {@link #PROP_UP_TO_DATE} have changed.
     *
     * @param name name of the property ({@link #PROP_UP_TO_DATE})
     * @param old  previous value of the property or null if unknown
     * @param nue  current value of the property or null if unknown
     */
    protected final void firePropertyChange(String name, Object old, Object nue) {
        pcs.firePropertyChange(name, old, nue);
    }
    
}
