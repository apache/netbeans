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
package org.netbeans.modules.nativeexecution.api.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.Ignore;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ConnectionManagerTest extends NativeExecutionBaseTestCase {

    public ConnectionManagerTest(String name) {
        super(name);
    }

    public ConnectionManagerTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    public static junit.framework.Test suite() {
        return new NativeExecutionBaseTestSuite(ConnectionManagerTest.class);
    }

    @Test
    public void testDeleteConnection() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew("test","127.0.0.1");
        ConnectionManager.getInstance().updateRecentConnectionsList(env);
        List<ExecutionEnvironment> ret = ConnectionManager.getInstance().getRecentConnections();
        assertTrue(!ret.isEmpty());
        ConnectionManager.getInstance().deleteConnectionFromRecentConnections(env);
        List<ExecutionEnvironment> ret2 = ConnectionManager.getInstance().getRecentConnections();
        assertTrue(ret2.isEmpty());
    }

    @Test
    @Ignore("requires remote system")
    public void testGetRecentConnections() throws Exception {
        String section = "remote.platforms";
        ExecutionEnvironment[] envs = NativeExecutionTestSupport.getTestExecutionEnvironmentsFromSection(section);
        assertTrue("Empty environmens list for section ", envs.length > 0);
        ConnectionManager.getInstance().clearRecentConnectionsList();
        AbstractList<ExecutionEnvironment> referenceList = new ArrayList<>();
        for (ExecutionEnvironment env : envs) {
            ConnectionManager.getInstance().updateRecentConnectionsList(env);
            referenceList.add(0, env);
        }
        List<ExecutionEnvironment> managersList = ConnectionManager.getInstance().getRecentConnections();
        assertEquals("Connections lists differ", referenceList, managersList);
        ConnectionManager.getInstance().clearRecentConnectionsList();
        assertTrue("Recent connections list should be empty", ConnectionManager.getInstance().getRecentConnections().isEmpty());
        ConnectionManager.getInstance().restoreRecentConnectionsList();
        assertEquals("Restopred connections list differ", referenceList, managersList);
    }

    @RandomlyFails
    @ForAllEnvironments(section = "remote.platforms")
    @Test
    public void testConnectDisconnect() throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assert (execEnv != null);
        final String id = execEnv.toString();

        System.out.println(getName() + " started");

        ConnectionManager cm = ConnectionManager.getInstance();

        if (cm.isConnectedTo(execEnv)) {
            System.out.println(id + " initially connected! Disconnecting from it to proceed with the test.");
            char[] passwd = PasswordManager.getInstance().getPassword(execEnv);
            cm.disconnect(execEnv);
            PasswordManager.getInstance().storePassword(execEnv, passwd, false);
        }

        assertFalse(id + " must be disconnected at this point", cm.isConnectedTo(execEnv));

        try {
            ConnectionManager.getInstance().connectTo(execEnv);
        } catch (Exception ex) {
            // this catch is just for debugging convenience
            // let it fail with exactly the same exception that was thrown by ConnectionManager
            throw ex;
        }

        assertTrue(id + " must be connected at this point", cm.isConnectedTo(execEnv));
        cm.disconnect(execEnv);
        assertFalse(id + " must be disconnected at this point", cm.isConnectedTo(execEnv));
        System.out.println(getName() + " finished");
    }

    @Test
    @Ignore("requires remote system")
    public void testGetConnectToAction() throws Exception {
        final int threadsNum = 10;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        Collection<String> mspecs = rcFile.getKeys("remote.platforms");
        
        if (mspecs.isEmpty()) {
            fail("remote.platforms are not properly configured [rc file == " + rcFile.toString() + "] !");
        }

        final ConcurrentTasksSupport.Counters counters = new ConcurrentTasksSupport.Counters();
        final ConcurrentTasksSupport support = new ConcurrentTasksSupport(threadsNum);

        ExecutionEnvironment env;

        for (String mspec : mspecs) {
            env = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec); // NOI18N
            if (env == null) {
                System.out.println("... skip testing on not configured " + mspec + " ... "); // NOI18N
            } else {
                System.out.println("... test on " + mspec + " [" + env.toString() + "] ..."); // NOI18N
                support.addFactory(new GetConnectToActionTaskFactory(counters, env));
            }
        }


        support.init();
        support.start();
        support.waitCompletion();

        // This is async actions...
        // Some of them will initiate onConnect() method, but some not...
        Thread.sleep(1000);

        counters.dump(System.out);
        counters.assertEquals("Number of started actions", "started", threadsNum);
        counters.assertEquals("Number of onConnect()", "connected", mspecs.size());
        counters.assertEquals("Number of connected environments", "connected", mspecs.size());
        counters.assertEquals("Number of not-connected environments", "not connected", 0);
        counters.assertEquals("Number of exceptions", "exceptions", 0);
    }

    private static final class GetConnectToActionTaskFactory implements ConcurrentTasksSupport.TaskFactory {

        private final ExecutionEnvironment env;
        private final Counters counters;

        private GetConnectToActionTaskFactory(Counters counters, ExecutionEnvironment env) {
            this.env = env;
            this.counters = counters;
        }

        @Override
        public Runnable newTask() {
            final AsynchronousAction connectToAction = ConnectionManager.getInstance().getConnectToAction(env, new Runnable() {

                @Override
                public void run() {
                    counters.getCounter("onConnect").incrementAndGet();
                    if (ConnectionManager.getInstance().isConnectedTo(env)) {
                        counters.getCounter("connected").incrementAndGet();
                    } else {
                        counters.getCounter("not connected").incrementAndGet();
                    }
                }
            });

            return new Runnable() {

                @Override
                public void run() {
                    try {
                        counters.getCounter("started").incrementAndGet();
                        connectToAction.invoke();
                    } catch (Exception ex) {
                        counters.getCounter("exception").incrementAndGet();
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
        }
    }
}
