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
package org.netbeans.modules.websvc.wsitconf.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin Grebac
 */
public final class DesignerListenerProvider {

    public static final String WSITCONFIG_CREATED = "wsitConfigCreated";
    
    private static List<WeakReference<PropertyChangeListener>> listeners = new ArrayList<WeakReference<PropertyChangeListener>>();
    
    public static final synchronized void registerListener(PropertyChangeListener pcl) {
        if (pcl == null) throw new IllegalArgumentException("Cannot pass nulls here!");
        listeners.add(new WeakReference(pcl));
    }
    
    public static synchronized void configCreated() {
        for (WeakReference<PropertyChangeListener> wRef : listeners) {
            wRef.get().propertyChange(new PropertyChangeEvent(DesignerListenerProvider.class, WSITCONFIG_CREATED, null, null));
        }
    }

}
