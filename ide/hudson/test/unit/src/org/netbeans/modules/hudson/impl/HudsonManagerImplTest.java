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
package org.netbeans.modules.hudson.impl;

import java.util.Collection;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertNull;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class HudsonManagerImplTest {

    /**
     * Clear manager's list of instances before each test.
     */
    @Before
    public void setUp() {
        Collection<HudsonInstanceImpl> instances
                = HudsonManagerImpl.getDefault().getInstances();
        for (HudsonInstanceImpl instance : instances) {
            HudsonManagerImpl.getDefault().removeInstance(instance);
        }
    }

    /**
     * Test for bug 245529 - ConcurrentModificationException from
     * HudsonManagerImpl.getInstances.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSynchronization() throws InterruptedException {

        final Throwable[] thrown = new Throwable[1];
        Thread addInstancesThread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        delay();
                        HudsonManagerImpl.getDefault().addInstance(
                                HudsonInstanceImpl.createHudsonInstance(
                                        "TestJenkinsInstance" + i,
                                        "http://testHudsonInstance" + i + "/",
                                        "0"));
                    } catch (Throwable e) {
                        thrown[0] = e;
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }, "AddJenkinsInstances");
        addInstancesThread.start();

        for (int i = 0; i < 10; i++) {
            try {
                delay();
                HudsonManagerImpl.getDefault().getInstances();
            } catch (Throwable e) {
                thrown[0] = e;
                e.printStackTrace();
                break;
            }
        }

        addInstancesThread.join();
        assertNull("No exception should be thrown", thrown[0]);
    }

    private static void delay() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
