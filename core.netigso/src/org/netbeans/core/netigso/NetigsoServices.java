/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
