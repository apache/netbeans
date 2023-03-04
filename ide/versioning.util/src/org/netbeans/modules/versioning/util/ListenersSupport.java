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

package org.netbeans.modules.versioning.util;

import java.util.*;

/**
 * Support for firing versioning events.
 *
 * @author Maros Sandor
 */
public class ListenersSupport {

    private final Object    source;
    private HashSet         listeners = new HashSet(1);

    public ListenersSupport(Object source) {
        this.source = source;
    }

    public synchronized void addListener(VersioningListener listener) {
        if (listener == null) throw new IllegalArgumentException();
        HashSet copy = (HashSet) listeners.clone();
        copy.add(listener);
        listeners = copy;
    }

    public synchronized void removeListener(VersioningListener listener) {
        HashSet copy = (HashSet) listeners.clone();
        copy.remove(listener);
        listeners = copy;
    }
    
    public void fireVersioningEvent(Object eventID) {
        fireVersioningEvent(new VersioningEvent(source, eventID, null));
    }    

    public void fireVersioningEvent(Object eventID, Object param) {
        fireVersioningEvent(new VersioningEvent(source, eventID, new Object [] { param }));
    }    
    
    public void fireVersioningEvent(Object eventID, Object [] params) {
        fireVersioningEvent(new VersioningEvent(source, eventID, params));
    }    
    
    private void fireVersioningEvent(VersioningEvent event) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            VersioningListener listener = (VersioningListener) i.next();
            listener.versioningEvent(event);
        }
    }
}
