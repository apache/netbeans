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

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;

/**
 *
 * @author Petr Hejl
 */
public class Deadlock126640Test extends ServerRegistryTestBase {

    private static final Logger CONTROL_LOGGER = Logger.getLogger("org.netbeans.modules.j2ee.deployment.impl"); // NOI18N

    private static final long DEADLOCK_TIMEOUT = 10000;

    public Deadlock126640Test(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    public void testMain() throws InterruptedException {

        Log.controlFlow(CONTROL_LOGGER, null,
                "THREAD: LOOKUP MSG: Registered bridging listener" // NOI18N
                 + "THREAD: INIT MSG: Entering registry initialization" // NOI18N
                 + "THREAD: LOOKUP MSG: Updating the lookup content" // NOI18N
                 + "THREAD: INIT MSG: Loading server plugins", // NOI18N
                0);

        Thread lookupThread = new LookupThread();
        Thread initThread = new InitThread();
        lookupThread.start();
        initThread.start();

        lookupThread.join(DEADLOCK_TIMEOUT);
        initThread.join(DEADLOCK_TIMEOUT);

        assertFalse(lookupThread.isAlive());
        assertFalse(initThread.isAlive());
    }

    private static class LookupThread extends Thread {

        public LookupThread() {
            super("LOOKUP"); // NOI18N
        }

        @Override
        public void run() {
            ServerInstanceProviderLookup lookup = ServerInstanceProviderLookup.getInstance();
            lookup.lookup(org.netbeans.api.server.ServerInstance.class);
        }
    }

    private static class InitThread extends Thread {

        public InitThread() {
            super("INIT"); // NOI18N
        }

        @Override
        public void run() {
            ServerRegistry.getInstance().getInstances();
        }
    }
}
