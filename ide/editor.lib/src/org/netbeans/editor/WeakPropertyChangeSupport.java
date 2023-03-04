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

package org.netbeans.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
* Similair functionality as PropertyChangeSupport but holds only
* weak references to listener classes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class WeakPropertyChangeSupport {

    private transient ArrayList listeners = new ArrayList();

    private transient ArrayList interestNames = new ArrayList();


    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        addLImpl(null, l);
    }

    /** Add weak listener to listen to change of the specified property.
    * The caller must hold the listener object in some instance variable
    * to prevent it from being garbage collected.
    */
    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener l) {
        addLImpl(propertyName, l);
    }

    /** Remove listener for changes in properties */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        int cnt = listeners.size();
        for (int i = 0; i < cnt; i++) {
            Object o = ((WeakReference)listeners.get(i)).get();
            if (o == null || o == l) { // remove null references and the required one
                listeners.remove(i);
                interestNames.remove(i);
                i--;
                cnt--;
            }
        }
    }

    public void firePropertyChange(Object source, String propertyName,
                                   Object oldValue, Object newValue) {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        PropertyChangeListener la[];
        String isa[];
        int cnt;
        synchronized (this) {
            cnt = listeners.size();
            la = new PropertyChangeListener[cnt];
            for (int i = 0; i < cnt; i++) {
                PropertyChangeListener l = (PropertyChangeListener)
                                           ((WeakReference)listeners.get(i)).get();
                if (l == null) { // remove null references
                    listeners.remove(i);
                    interestNames.remove(i);
                    i--;
                    cnt--;
                } else {
                    la[i] = l;
                }
            }
            isa = (String [])interestNames.toArray(new String[cnt]);
        }

        // now create and fire the event
        PropertyChangeEvent evt = new PropertyChangeEvent(source, propertyName,
                                  oldValue, newValue);
        for (int i = 0; i < cnt; i++) {
            if (isa[i] == null || propertyName == null || isa[i].equals(propertyName)) {
                la[i].propertyChange(evt);
            }
        }
    }

    private void addLImpl(String sn, PropertyChangeListener l) {
        listeners.add(new WeakReference(l));
        interestNames.add(sn);
    }

}
