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

package org.netbeans.modules.remote.ui.spi;

import java.beans.PropertyChangeListener;

/**
 *
 */
public interface ServerRecordUIController {
    
    /** valid property. */
    public static final String      PROP_VALID = "valid"; // NOI18N

    /** Called when "Ok" button is pressed.*/
    public void ok ();

    /** Called when "Cancel" button is pressed. */
    public void cancel ();

    /**
     * Return <code>true</code> whether value of this customizer
     * is valid (and OK button can be enabled).
     * <p>
     * When this interface is implemented by a class that extends
     * {@link javax.swing.JComponent}, this method clashes with
     * {@link javax.swing.JComponent#isValid()} method. In this case please implement
     * this by a different class and override {@link AttachType#getController()},
     * resp. {@link BreakpointType#getController()}.
     *
     * @return <code>true</code> if the value of this customizer is valid, otherewise false
     */
    public boolean isValid ();

    /**
     * Adds property changes listener.
     * @param listener l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener listener);

    /**
     * Removes property changes listener
     * @param listener  the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener listener);
}
