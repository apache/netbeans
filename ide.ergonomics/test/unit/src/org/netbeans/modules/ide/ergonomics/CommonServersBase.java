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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        Collections.sort(providers, comparator); // ?
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
        Collections.sort(providers, comparator);
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
    
    private final static Comparator<ServerWizardProvider> comparator = new Comparator<ServerWizardProvider>() {
        @Override
        public int compare(ServerWizardProvider arg0, ServerWizardProvider arg1) {
            return arg0.getDisplayName().compareTo(arg1.getDisplayName());
        }
    };
}
