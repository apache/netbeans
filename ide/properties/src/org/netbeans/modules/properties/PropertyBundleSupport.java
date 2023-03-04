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

package org.netbeans.modules.properties;

import java.io.Serializable;
import javax.swing.event.EventListenerList;

/**
 * Support for PropertyBundle events, registering listeners, firing events.
 *
 * @author Petr Jiricka
 */
public class PropertyBundleSupport implements Serializable {

    /** generated serial version UID */
    static final long serialVersionUID = -655481419012858008L;

    /** list of listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * object to be provided as a source for any generated events
     *
     * @serial
     */
    private Object source;

    /**
     * Constructs a <code>PropertyBundleSupport</code> object.
     *
     * @param source Bean object to be given as a source of events
     * @exception  java.lang.NullPointerException
     *             if the parameter is <code>null</code>
     */
    public PropertyBundleSupport(Object source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
    }

    /**
     * Registers a given listener so that it will receive notifications
     * about changes in a property bundle.
     * If the given listener is already registered, a duplicite registration
     * will be performed, so that it will get notifications multiple times.
     *
     * @param  l  listener to be registered
     * @see  #removePropertyBundleListener
     */
    public void addPropertyBundleListener(PropertyBundleListener l) {
        listenerList.add(PropertyBundleListener.class, l);
    }

    /**
     * Unregisters a given listener so that it will no more receive
     * notifications about changes in a property bundle.
     * If the given listener has been registered multiple times,
     * only one registration item will be removed.
     *
     * @param	l		the PropertyBundleListener
     * @see  #addPropertyBundleListener
     */
    public void removePropertyBundleListener(PropertyBundleListener l) {
        listenerList.remove(PropertyBundleListener.class, l);
    }

    /**
     * Notifies all registered listeners about a possibly structural change
     * in the property bundle.
     *
     * @see  #addPropertyBundleListener
     * @see  #removePropertyBundleListener
     */
    public void fireBundleStructureChanged() {
        fireBundleChanged(new PropertyBundleEvent(
                source,
                PropertyBundleEvent.CHANGE_STRUCT));
    }

    /**
     * Notifies all registered listeners about a complex change
     * in the property bundle.
     *
     * @see  #addPropertyBundleListener
     * @see  #removePropertyBundleListener
     */
    public void fireBundleDataChanged() {
        fireBundleChanged(new PropertyBundleEvent(
                source,
                PropertyBundleEvent.CHANGE_ALL));
    }

    /**
     * Notifies all registered listeners about a change in a single entry
     * of the property bundle.
     *
     * @param  entryName  name of the changed entry
     * @see  #addPropertyBundleListener
     * @see  #removePropertyBundleListener
     */
    public void fireFileChanged(String entryName) {
        fireBundleChanged(new PropertyBundleEvent(source, entryName));
    }

    /**
     * Notifies all registered listeners about a change of a single item
     * in a single entry of the property bundle.
     *
     * @param  entryName  name of the changed entry
     * @param  itemName  name of the changed item
     * @see  #addPropertyBundleListener
     * @see  #removePropertyBundleListener
     */
    public void fireItemChanged(String entryName, String itemName) {
        fireBundleChanged(new PropertyBundleEvent(source, entryName, itemName));
    }

    /**
     * Forwards the given notification event to all registered
     * <code>PropertyBundleListener</code>s.
     *
     * @param  e  event to be forwarded to the registered listeners
     * @see  #addPropertyBundleListener
     * @see  #removePropertyBundleListener
     */
    public void fireBundleChanged(PropertyBundleEvent e) {
        //System.out.println(e.toString());
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyBundleListener.class) {
                ((PropertyBundleListener) listeners[i + 1]).bundleChanged(e);
            }
        }
    }

} // End of class PropertyBundleSupport
