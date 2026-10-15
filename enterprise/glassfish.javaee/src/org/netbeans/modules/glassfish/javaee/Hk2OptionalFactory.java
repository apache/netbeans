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

package org.netbeans.modules.glassfish.javaee;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.javaee.ide.FastDeploy;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.Lookup;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 * @author vince kraemer
 */
public class Hk2OptionalFactory extends OptionalDeploymentManagerFactory {

    private final DeploymentFactory df;
    private final ServerUtilities commonUtilities;
    private final boolean hasWizard;

    protected Hk2OptionalFactory(DeploymentFactory df, ServerUtilities su, boolean hasWizard) {
        this.df = df;
        this.commonUtilities = su;
        this.hasWizard = hasWizard;
    }

//    public static Hk2OptionalFactory createPrelude() {
//        ServerUtilities t = ServerUtilities.getPreludeUtilities();
//        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createPrelude(),
//                t, false);
//    }

    public static Hk2OptionalFactory createEe6() {
        ServerUtilities t = ServerUtilities.getEe6Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createEe6(),
                t, true);
    }

    public static Hk2OptionalFactory createEe7() {
        ServerUtilities t = ServerUtilities.getEe7Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createEe7(),
                t, true);
    }
    
    public static Hk2OptionalFactory createEe8() {
        ServerUtilities t = ServerUtilities.getEe8Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createEe8(),
                t, true);
    }

    public static Hk2OptionalFactory createJakartaEe8() {
        ServerUtilities t = ServerUtilities.getJakartaEe8Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createJakartaEe8(),
                t, true);
    }

    public static Hk2OptionalFactory createJakartaEe9() {
        ServerUtilities t = ServerUtilities.getJakartaEe9Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createJakartaEe9(),
                t, true);
    }

    public static Hk2OptionalFactory createJakartaEe91() {
        ServerUtilities t = ServerUtilities.getJakartaEe91Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createJakartaEe91(),
                t, true);
    }

    public static Hk2OptionalFactory createJakartaEe10() {
        ServerUtilities t = ServerUtilities.getJakartaEe10Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createJakartaEe10(),
                t, true);
    }
    
    public static Hk2OptionalFactory createJakartaEe11() {
        ServerUtilities t = ServerUtilities.getJakartaEe11Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createJakartaEe11(),
                t, true);
    }

    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return new Hk2StartServer(dm);
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        IncrementalDeployment result = null;
        if(dm instanceof Hk2DeploymentManager) {
            Hk2DeploymentManager hk2dm = (Hk2DeploymentManager) dm;
            if(hk2dm.isLocal()) {
                result = new FastDeploy(hk2dm);
            }
        }
        return result;
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        FindJSPServlet retVal = null;
        try {
            Hk2DeploymentManager hk2dm = (Hk2DeploymentManager) dm;
            if(!hk2dm.getCommonServerSupport().isRemote()) {
                retVal = new FindJSPServletImpl(hk2dm, this);
            }
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }

    @Override
    public boolean isCommonUIRequired() {
        return false;
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return hasWizard ? new J2eeInstantiatingIterator(commonUtilities) : null;
    }

    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new Hk2DatasourceManager((Hk2DeploymentManager) dm) : null;
    }

    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        JDBCDriverDeployer retVal = null;
        try {
            retVal = new JDBCDriverDeployerImpl((Hk2DeploymentManager) dm, this);
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }

    @Override
     public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new Hk2MessageDestinationManager((Hk2DeploymentManager) dm) : null;
    }

    @Override
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        AntDeploymentProvider retVal = null;
        try {
            retVal = new AntDeploymentProviderImpl((Hk2DeploymentManager) dm, this);
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        ServerInstanceDescriptor result = null;
        if(dm instanceof Hk2DeploymentManager) {
            result = new Hk2ServerInstanceDescriptor((Hk2DeploymentManager) dm);
        } else {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "Invalid deployment manager: {0}", dm); // NOI18N
        }
        return result;
    }

    private static class J2eeInstantiatingIterator implements InstantiatingIterator {

        private final InstantiatingIterator delegate;
        private ServerUtilities su;

        public J2eeInstantiatingIterator(ServerUtilities su) {
            this.delegate = ServerUtilities.getInstantiatingIterator();
            this.su = su;
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            delegate.removeChangeListener(l);
        }

        @Override
        public void previousPanel() {
            delegate.previousPanel();
        }

        @Override
        public void nextPanel() {
            delegate.nextPanel();
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Panel current() {
            return delegate.current();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            delegate.addChangeListener(l);
        }

        @Override
        public void uninitialize(WizardDescriptor wizard) {
            delegate.uninitialize(wizard);
        }

        @Override
        public Set instantiate() throws IOException {
            Set<?> set = delegate.instantiate();
            if(!set.isEmpty()) {
                Object obj = set.iterator().next();
                if(obj instanceof ServerInstance) {
                    ServerInstance instance = (ServerInstance) obj;
                    Lookup lookup = su.getLookupFor(instance);
                    if (lookup != null) {
                        JavaEEServerModule module = lookup.lookup(JavaEEServerModule.class);
                        if(module != null) {
                            return Collections.singleton(module.getInstanceProperties());
                        } else {
                            Logger.getLogger("glassfish-javaee").log(Level.WARNING,"No JavaEE facade found for {0}", instance.getDisplayName()); // NOI18N
                        }
                    } else {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, "No lookup found for {0}", instance.getDisplayName()); // NOI18N
                    }
                } else {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "AddServerWizard iterator must return a set of ServerInstance objects.");
                }
            }
            return Collections.EMPTY_SET;
        }

        @Override
        public void initialize(WizardDescriptor wizard) {
            delegate.initialize(wizard);
        }

    }
}
