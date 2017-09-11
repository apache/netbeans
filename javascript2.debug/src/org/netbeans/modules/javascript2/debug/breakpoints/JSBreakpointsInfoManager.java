/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
