/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.ide.ergonomics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Pavel Flaska
 */
public abstract class CommonServersBase extends NbTestCase {

    private final Logger LOG;
    
    public CommonServersBase(final String name) {
        super(name);
        LOG = Logger.getLogger("commonserversbase." + name);
    }
    
    //
    // for CloudNodeCheck
    //
    
    protected abstract String forPath();
    protected abstract String propPrefix();
    
    //
    // test methods
    //
    
    protected final void doGetAllInstancesReal() {
        int cnt = 0;
        List<ServerWizardProvider> providers = new ArrayList<ServerWizardProvider>(Lookups.forPath(forPath()).lookupAll(ServerWizardProvider.class));
        for (ServerWizardProvider w : providers.toArray(new ServerWizardProvider[0])) {
            if (w.getInstantiatingIterator() == null) {
                providers.remove(w);
            }
        }
        providers.sort(comparator); // ?
        LOG.info("Iterating full");
        for (ServerWizardProvider wizard : providers.toArray(new ServerWizardProvider[0])) {
           System.setProperty(propPrefix() + ++cnt, wizard.getDisplayName());
           LOG.log(Level.INFO, "full: {0}", wizard.getDisplayName());
        }
        LOG.info("Iteration done for full");
    }

    protected final void doGetAllInstancesErgo() {
        int cnt = 0;
        List<ServerWizardProvider> providers = new ArrayList<ServerWizardProvider>(Lookups.forPath(forPath()).lookupAll(ServerWizardProvider.class));
        for (ServerWizardProvider w : providers.toArray(new ServerWizardProvider[0])) {
            if (w.getInstantiatingIterator() == null) {
                providers.remove(w);
            }
        }
        providers.sort(comparator);
        LOG.info("Iterating ergo");
        for (ServerWizardProvider wizard : providers) {
           String name = System.getProperty(propPrefix() + ++cnt);
           LOG.log(Level.INFO, "ergo: {0}", wizard.getDisplayName());
           assertEquals(name, wizard.getDisplayName());
           System.clearProperty(propPrefix() + cnt);
        }
        LOG.info("Iteration done for ergo"); 
        for (Object key : System.getProperties().keySet()) {
            assertFalse("Found additional server " + System.getProperty((String) key), ((String) key).startsWith(propPrefix()));
        }
    }
    
    private static final Comparator<ServerWizardProvider> comparator = new Comparator<ServerWizardProvider>() {
        @Override
        public int compare(ServerWizardProvider arg0, ServerWizardProvider arg1) {
            return arg0.getDisplayName().compareTo(arg1.getDisplayName());
        }
    };
}
