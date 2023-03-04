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

package org.netbeans.modules.javascript2.debug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Martin
 */
public class JSBreakpointsInfoManager {
    
    private static JSBreakpointsInfoManager INSTANCE;
    
    private JSBreakpointsInfo[] infoServices;
    private final Object infoServicesLock = new Object();
    private final PropertyChangeListener servicePCL = new ServicePropertyChangeListener();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Boolean lastActivated = null;
    
    private JSBreakpointsInfoManager(final Lookup.Result<JSBreakpointsInfo> lookupResult,
                                     JSBreakpointsInfo[] activeServices) {
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                 updateServices(lookupResult.allInstances().toArray(new JSBreakpointsInfo[]{}));
            }
        });
        initServices(activeServices);
    }
    
    public static JSBreakpointsInfoManager getDefault() {
        synchronized (JSBreakpointsInfoManager.class) {
            if (INSTANCE != null) {
                return INSTANCE;
            }
        }
        // Examine the lookup outside of synchronization block.
        // It can create new instances and do a lot of stuff.
        final Lookup.Result<JSBreakpointsInfo> lookupResult =
                Lookup.getDefault().lookupResult(JSBreakpointsInfo.class);
        JSBreakpointsInfo[] activeServices = lookupResult.allInstances().toArray(new JSBreakpointsInfo[]{});
        synchronized (JSBreakpointsInfoManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new JSBreakpointsInfoManager(lookupResult, activeServices);
            }
        }
        return INSTANCE;
    }
    
    private void initServices(JSBreakpointsInfo[] activeServices) {
        for (JSBreakpointsInfo as : activeServices) {
            as.addPropertyChangeListener(servicePCL);
        }
        synchronized (infoServicesLock) {
            this.infoServices = activeServices;
            lastActivated = null;
        }
    }
    
    private void updateServices(JSBreakpointsInfo[] activeServices) {
        initServices(activeServices);
        fireChange(JSBreakpointsInfo.PROP_BREAKPOINTS_ACTIVE);
    }
    
    private JSBreakpointsInfo[] getServices() {
        JSBreakpointsInfo[] services;
        synchronized (infoServicesLock) {
            services = infoServices;
        }
        return services;
    }
    
    public boolean areBreakpointsActivated() {
        Boolean activated;
        synchronized (infoServicesLock) {
            activated = lastActivated;
        }
        boolean are = true;
        if (activated != null) {
            are = activated.booleanValue();
        } else {
            for (JSBreakpointsInfo bi : getServices()) {
                if (!bi.areBreakpointsActivated()) {
                    are = false;
                    break;
                }
            }
            synchronized (infoServicesLock) {
                lastActivated = are;
            }
        }
        return are;
    }
    
    public boolean isAnnotatable(FileObject fo) {
        for (JSBreakpointsInfo bi : getServices()) {
            if (bi.isAnnotatable(fo)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isTransientURL(URL url) {
        for (JSBreakpointsInfo bi : getServices()) {
            if (bi.isTransientURL(url)) {
                return true;
            }
        }
        return false;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void fireChange(String propertyName) {
        pcs.firePropertyChange(propertyName, null, null);
    }
    
    private final class ServicePropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JSBreakpointsInfo.PROP_BREAKPOINTS_ACTIVE.equals(evt.getPropertyName())) {
                synchronized (infoServicesLock) {
                    lastActivated = null;
                }
                fireChange(JSBreakpointsInfo.PROP_BREAKPOINTS_ACTIVE);
            }
        }
        
    }
    
}
