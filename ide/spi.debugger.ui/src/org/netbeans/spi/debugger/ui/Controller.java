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

package org.netbeans.spi.debugger.ui;

import java.beans.PropertyChangeListener;


/**
 * Support for validation of various customizers. This interface can not be
 * implemented directly by the customizer component. See
 * {@link AttachType#getController}) and
 * {@link BreakpointType#getController}).
 *
 * @author   Jan Jancura
 */
public interface Controller {

    /** Property name constant for valid property. */
    public static final String      PROP_VALID = "valid"; // NOI18N


    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok ();
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel ();
    
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
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    public boolean isValid ();

    /** 
     * Add a listener to property changes.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /** 
     * Remove a listener to property changes.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}

