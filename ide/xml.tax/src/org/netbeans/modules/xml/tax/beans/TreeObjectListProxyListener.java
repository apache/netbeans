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
package org.netbeans.modules.xml.tax.beans;

import java.beans.*;
import java.util.*;

import org.netbeans.tax.*;

/**
 * This class listens on all members of object list and
 * joins all member events into this one source.
 * <p>
 * <pre>
 *   TreeObjectListProxyListener proxy =
 *       new TreeObjectListProxyListener(list);
 *   proxy.addPropertyChangeListener(WeakListener.propertyChange(this, proxy));
 * </pre>
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class TreeObjectListProxyListener implements PropertyChangeListener {

    private final TreeObjectList list;
    private PropertyChangeSupport changeSupport;
        
    /** Creates new TreeObjectListListener */
    public TreeObjectListProxyListener(TreeObjectList list) {
        this.list = list;
        list.addPropertyChangeListener(this);
        for (Iterator<TreeObject> it = list.iterator(); it.hasNext();) {
            TreeObject next = it.next();
            if (next != null) next.addPropertyChangeListener(this);
        }
    }

    /*
     * Update listening on list members, forward member events.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        String name = e.getPropertyName();
        Object source = e.getSource();
        
        if (source == list) {
            if (TreeObjectList.PROP_CONTENT_INSERT.equals(name)) {
                TreeObject newObject = (TreeObject)e.getNewValue();
                if (newObject != null) newObject.addPropertyChangeListener(this);
            } else if (TreeObjectList.PROP_CONTENT_REMOVE.equals(name)) {
                TreeObject oldObject = (TreeObject)e.getOldValue();
                if (oldObject != null) oldObject.removePropertyChangeListener(this);
            }
        }
        
        forward(e);
    }    
    
    /**
     */
    private void forward(final PropertyChangeEvent e) {
        if (changeSupport != null)
            changeSupport.firePropertyChange(e);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }
}
