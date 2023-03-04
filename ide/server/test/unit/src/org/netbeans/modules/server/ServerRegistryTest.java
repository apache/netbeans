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

package org.netbeans.modules.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.server.test.MockInstanceImplementation;
import org.netbeans.modules.server.test.MockInstanceProvider;
import org.netbeans.spi.server.ServerInstanceProvider;

/**
 *
 * @author Petr Hejl
 */
public class ServerRegistryTest extends NbTestCase {

    public ServerRegistryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockInstanceProvider.registerInstanceProvider("Test.instance", new MockInstanceProvider()); // NOI18N
    }

    public void testEmptyProvider() throws IOException {
        assertEquals(1, ServerRegistry.getInstance().getProviders().size());

        ServerInstanceProvider provider = ServerRegistry.getInstance().getProviders().iterator().next();
        assertTrue(provider instanceof MockInstanceProvider);
        ((MockInstanceProvider) provider).clear();
        assertTrue(provider.getInstances().isEmpty());
    }

    @SuppressWarnings("unchecked")
    public void testInstanceProvider() throws IOException {

        assertEquals(1, ServerRegistry.getInstance().getProviders().size());

        ServerInstanceProvider provider = ServerRegistry.getInstance().getProviders().iterator().next();
        assertTrue(provider instanceof MockInstanceProvider);
        MockInstanceProvider testProvider = (MockInstanceProvider) provider;
        testProvider.clear();

        MockInstanceImplementation instance1 = MockInstanceImplementation.createInstance(testProvider,
                "Test server", "Test instance 1", true); // NOI18N
        MockInstanceImplementation instance2 = MockInstanceImplementation.createInstance(testProvider,
                "Test server", "Test instance 2", true); // NOI18N

        List<ServerInstance> step1 = new ArrayList<ServerInstance>();
        Collections.addAll(step1, instance1.getServerInstance());
        List<ServerInstance> step2 = new ArrayList<ServerInstance>();
        Collections.addAll(step2, instance1.getServerInstance(), instance2.getServerInstance());

        InstanceListener listener = new InstanceListener(step1, step2,
                step1, Collections.<ServerInstance>emptyList());
        ServerRegistry.getInstance().addChangeListener(listener);

        testProvider.addInstance(instance1.getServerInstance());
        testProvider.addInstance(instance2.getServerInstance());
        testProvider.removeInstance(instance2.getServerInstance());
        testProvider.removeInstance(instance1.getServerInstance());
    }

    private static class InstanceListener implements ChangeListener {

        private final List<List<ServerInstance>> steps = new ArrayList<List<ServerInstance>>();

        private int stepIndex;

        public InstanceListener(List<ServerInstance>... steps) {
            Collections.addAll(this.steps, steps);
        }

        public void stateChanged(ChangeEvent e) {
            final ServerRegistry registry = (ServerRegistry) e.getSource();

            List<ServerInstance> current = new ArrayList<ServerInstance>();
            for (ServerInstanceProvider provider : registry.getProviders()) {
                current.addAll(provider.getInstances());
            }

            List<ServerInstance> expected = steps.get(stepIndex++);
            assertEquals(expected.size(), current.size());

            for (ServerInstance instance : expected) {
                current.remove(instance);
            }

            assertTrue(current.isEmpty());
        }

    }

}
