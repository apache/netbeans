/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public final class ServerRegistry {

    public static final String SERVERS_PATH = "Servers"; // NOI18N
    public static final String CLOUD_PATH = "Cloud"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(ServerRegistry.class.getName());

    private static ServerRegistry registry;
    private static ServerRegistry cloudRegistry;

    private static ProviderLookupListener l;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final Lookup.Result<ServerInstanceProvider> result;

    private final Lookup lookup;
    
    private final String path;
    
    private final boolean cloud;

    private ServerRegistry(String path, boolean cloud) {
        this.path = path;
        this.cloud = cloud;
        lookup = Lookups.forPath(path);
        result = lookup.lookupResult(ServerInstanceProvider.class);
    }

    public String getPath() {
        return path;
    }

    public boolean isCloud() {
        return cloud;
    }
    
    public static synchronized ServerRegistry getInstance() {
        if (registry == null) {
            registry = new ServerRegistry(SERVERS_PATH, false);
            registry.result.allItems();
            registry.result.addLookupListener(l = new ProviderLookupListener(registry.changeSupport));
        }
        return registry;
    }

    public static synchronized ServerRegistry getCloudInstance() {
        if (cloudRegistry == null) {
            cloudRegistry = new ServerRegistry(CLOUD_PATH, true);
            cloudRegistry.result.allItems();
            cloudRegistry.result.addLookupListener(l = new ProviderLookupListener(cloudRegistry.changeSupport));
        }
        return cloudRegistry;
    }

    public Collection<? extends ServerInstanceProvider> getProviders() {
        Collection<? extends ServerInstanceProvider> ret = result.allInstances();
        LOGGER.log(Level.FINE, "Returning providers {0}", ret);
        return ret;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private static class ProviderLookupListener implements LookupListener {

        private final ChangeSupport changeSupport;

        public ProviderLookupListener(ChangeSupport changeSupport) {
            this.changeSupport = changeSupport;
        }

        public void resultChanged(LookupEvent ev) {
            LOGGER.log(Level.FINE, "Provider lookup change {0}", ev);
            changeSupport.fireChange();
        }

    }
}
