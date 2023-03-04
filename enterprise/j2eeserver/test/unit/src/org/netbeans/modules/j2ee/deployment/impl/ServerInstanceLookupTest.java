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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.Collection;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Petr Hejl
 */
public class ServerInstanceLookupTest extends ServerRegistryTestBase {

    private static final String URL = "fooservice:testInstance"; // NOI18N

    public ServerInstanceLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(URL, "user", "password", "TestInstance", true, false, null); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.removeServerInstance(URL);
        super.tearDown();
    }

    public void testBaseLookup() throws DeploymentManagerCreationException {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);

        ServerInstanceLookup lookup = new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), null);
        assertEquals(instance.getServer().getDeploymentFactory(), lookup.lookup(DeploymentFactory.class));

        // nothing initialized - has to be disconnected
        DeploymentManager discManager = instance.getDisconnectedDeploymentManager();
        assertEquals(discManager, lookup.lookup(DeploymentManager.class));
        // now it is sure we are connected
        DeploymentManager manager = instance.getDeploymentManager();
        assertEquals(manager, lookup.lookup(DeploymentManager.class));
        assertNotSame(discManager, manager);

        // check with target
        ServerTarget target = instance.getServerTarget("Target 1"); // NOI18N
        lookup = new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), target.getTarget());
        assertEquals(target.getTarget(), lookup.lookup(Target.class));

        // reset and recheck
        instance.reset();
        manager = instance.getDeploymentManager();
        assertEquals(manager, lookup.lookup(DeploymentManager.class));

        // must remain the same
        assertEquals(instance.getServer().getDeploymentFactory(), lookup.lookup(DeploymentFactory.class));
        assertEquals(target.getTarget(), lookup.lookup(Target.class));
    }

    public void testResultLookup() throws DeploymentManagerCreationException {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);

        // because of this - instance is connected
        ServerTarget target = instance.getServerTarget("Target 1"); // NOI18N
        ServerInstanceLookup lookup = new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), target.getTarget());

        // test factory
        Result<DeploymentFactory> resultFactory =
                lookup.lookup(new Template<DeploymentFactory>(DeploymentFactory.class));
        assertResultContainsInstance(DeploymentFactory.class,
                instance.getServer().getDeploymentFactory(), resultFactory);

        // test target
        Result<Target> resultTarget = lookup.lookup(new Template<Target>(Target.class));
        assertResultContainsInstance(Target.class, target.getTarget(), resultTarget);

        // test manager
        DeploymentManager manager = instance.getDeploymentManager();
        Result<DeploymentManager> resultManager =
                lookup.lookup(new Template<DeploymentManager>(DeploymentManager.class));
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);
    }

    @RandomlyFails
    public void testListener() throws DeploymentManagerCreationException {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);

        // because of this - instance is connected
        ServerTarget target = instance.getServerTarget("Target 1"); // NOI18N
        ServerInstanceLookup lookup = new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), target.getTarget());

        DeploymentManager manager = instance.getDeploymentManager();
        Result<DeploymentManager> resultManager =
                lookup.lookup(new Template<DeploymentManager>(DeploymentManager.class));
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);

        // test listeners
        TestLookupListener listener = new TestLookupListener(resultManager);
        resultManager.addLookupListener(listener);
        instance.reset();
        // invoked by reset
        assertEquals(1, listener.getCount());

        manager = instance.getDisconnectedDeploymentManager();
        // invoked by getter
        assertEquals(2, listener.getCount());
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);

        manager = instance.getDisconnectedDeploymentManager();
        // count must remain same
        assertEquals(2, listener.getCount());
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);

        manager = instance.getDeploymentManager();
        // invoked by getter
        assertEquals(3, listener.getCount());
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);

        manager = instance.getDeploymentManager();
        // count must remain same
        assertEquals(3, listener.getCount());
        assertResultContainsInstance(DeploymentManager.class, manager, resultManager);
    }

    public void testEmptyTarget() {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);

        ServerInstanceLookup lookup = new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), null);

        // target is null
        assertNull(lookup.lookup(Target.class));

        Result<Target> result = lookup.lookup(new Template<Target>(Target.class));
        assertTrue(result.allClasses().isEmpty());
        assertTrue(result.allInstances().isEmpty());
        assertTrue(result.allItems().isEmpty());
    }

    public void testProxyLookup() {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);

        Lookup lookup = new ProxyLookup(new ServerInstanceLookup(instance,
                instance.getServer().getDeploymentFactory(), null), Lookups.singleton(this));

        assertNotNull(lookup.lookup(DeploymentFactory.class));
        assertNotNull(lookup.lookup(DeploymentManager.class));
        assertNull(lookup.lookup(Target.class));
        assertEquals(this, lookup.lookup(ServerInstanceLookupTest.class));
    }

    private static <T> void assertResultContainsInstance(Class<T> clazz, T instance, Result<T> result) {
        Set<Class<? extends T>> classes = result.allClasses();
        assertEquals(1, classes.size());
        assertTrue(clazz.isAssignableFrom(classes.iterator().next()));

        Collection<? extends T> instances = result.allInstances();
        assertEquals(1, instances.size());
        assertEquals(instance, instances.iterator().next());

        Collection<? extends Item<T>> items = result.allItems();
        assertEquals(1, items.size());
        assertEquals(instance, items.iterator().next().getInstance());
    }

    private static class TestLookupListener implements LookupListener {

        private final Result<DeploymentManager> registered;

        private int count;

        public TestLookupListener(Result<DeploymentManager> registered) {
            this.registered = registered;
        }

        public void resultChanged(LookupEvent ev) {
            count++;

            Result<DeploymentManager> result = (Result<DeploymentManager>) ev.getSource();
            assertEquals(registered, result);
        }

        public int getCount() {
            return count;
        }
    }
}
