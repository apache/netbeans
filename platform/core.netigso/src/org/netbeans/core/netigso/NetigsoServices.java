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

package org.netbeans.core.netigso;

import java.util.logging.Level;
import org.netbeans.core.startup.MainLookup;
import org.openide.util.lookup.InstanceContent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoServices
implements SynchronousBundleListener, ServiceListener, InstanceContent.Convertor<ServiceReference, Object> {
    private final Netigso netigso;
    
    NetigsoServices(Netigso netigso, Framework f) {
        this.netigso = netigso;
        for (ServiceReference ref : f.getRegisteredServices()) {
            MainLookup.register(ref, this);
        }
        f.getBundleContext().addServiceListener(this);
        f.getBundleContext().addBundleListener(this);
    }

    @Override
    public void serviceChanged(ServiceEvent ev) {
        final ServiceReference ref = ev.getServiceReference();
        if (ev.getType() == ServiceEvent.REGISTERED) {
            MainLookup.register(ref, this);
        }
        if (ev.getType() == ServiceEvent.UNREGISTERING) {
            MainLookup.unregister(ref, this);
        }
    }

    @Override
    public Object convert(ServiceReference obj) {
        final Bundle bundle = obj.getBundle();
        if (bundle != null) {
            return bundle.getBundleContext().getService(obj);
        } else {
            return null;
        }
    }

    @Override
    public Class<? extends Object> type(ServiceReference obj) {
        String[] arr = (String[])obj.getProperty(Constants.OBJECTCLASS);
        if (arr.length > 0) {
            final Bundle bundle = obj.getBundle();
            if (bundle != null) try {
                return (Class<?>)bundle.loadClass(arr[0]);
            } catch (ClassNotFoundException ex) {
                Netigso.LOG.log(Level.INFO, "Cannot load service class", arr[0]); // NOI18N
            }
        }
        return Object.class;
    }

    @Override
    public String id(ServiceReference obj) {
        Long id = (Long) obj.getProperty(Constants.SERVICE_ID);
        return "OSGiService[" + id + "]"; // NOI18N
    }
    
    @Override
    public String displayName(ServiceReference obj) {
        return (String) obj.getProperty(Constants.SERVICE_DESCRIPTION);
    }

    @Override
    public void bundleChanged(BundleEvent be) {
        if (be.getBundle().getLocation().startsWith("netigso://")) {
            return;
        }
        netigso.notifyBundleChange(
            be.getBundle().getSymbolicName(),
            be.getBundle().getVersion(),
            be.getType()
        );
    }
}
