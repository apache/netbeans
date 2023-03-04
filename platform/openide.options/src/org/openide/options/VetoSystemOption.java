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
package org.openide.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.util.*;
import java.util.stream.Collectors;


/** Extends the functionality of <CODE>SystemOption</CODE>
* by providing support for veto listeners.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 6, 1997
*/
public abstract class VetoSystemOption extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -614731095908156413L;

    /** vetoable listener property */
    private static final String PROP_VETO_SUPPORT = "vetoSupport"; // NOI18N

    /** Default constructor. */
    public VetoSystemOption() {
    }

    /** Lazy getter for veto hashtable.
    * @return the hashtable
    */
    private Set<VetoableChangeListener> getVeto() {
        Set<VetoableChangeListener> set = (HashSet<VetoableChangeListener>)getProperty(PROP_VETO_SUPPORT);

        if (set == null) {
            set = new HashSet<>();
            putProperty(PROP_VETO_SUPPORT, set);
        }

        return set;
    }

    /** Add a new veto listener to all instances of this exact class.
    * @param list the listener to add
    */
    public final void addVetoableChangeListener(VetoableChangeListener list) {
        synchronized (getLock()) {
            getVeto().add(list);
        }
    }

    /** Remove a veto listener from all instances of this exact class.
    * @param list the listener to remove
    */
    public final void removeVetoableChangeListener(VetoableChangeListener list) {
        synchronized (getLock()) {
            getVeto().remove(list);
        }
    }

    /** Fire a property change event.
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    * @exception PropertyVetoException if the change is vetoed
    */
    public final void fireVetoableChange(String name, Object oldValue, Object newValue)
    throws PropertyVetoException {
        PropertyChangeEvent ev = new PropertyChangeEvent(this, name, oldValue, newValue);

        Iterator<VetoableChangeListener> en;

        synchronized (getLock()) {
            en = getVeto().stream().collect(Collectors.toSet()).iterator();
        }

        while (en.hasNext()) {
            en.next().vetoableChange(ev);
        }
    }
}
