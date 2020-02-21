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
package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import org.junit.AfterClass;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ServerListTestCase extends RemoteTestBase {

    public ServerListTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    @ForAllEnvironments
    public void testAdd() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerRecord rec = ServerList.get(execEnv);
        assertNotNull("Null server record", rec);
        assertEquals(rec.getExecutionEnvironment(), execEnv);
        assertEquals(rec.getExecutionEnvironment(), execEnv);
    }

    @AfterClass
    public void cleanup() {
        ServerRecord local = ServerList.get(ExecutionEnvironmentFactory.getLocal());
        ServerList.set(Arrays.asList(local), local);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerList.addServer(execEnv, execEnv.getDisplayName(), RemoteSyncFactory.getDefault(), false, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
        cleanup();
    }
            

    @ForAllEnvironments
    public void testGetEnvironments() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertTrue("getEnvironments should contain " + execEnv, ServerList.getEnvironments().contains(execEnv));
    }

    @ForAllEnvironments
    public void testGetRecords() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        Collection<? extends ServerRecord> records = ServerList.getRecords();
        for (ServerRecord rec : records) {
            if (execEnv.equals(rec.getExecutionEnvironment())) {
                return;
            }
        }        
        assertTrue("getRecords should contain " + execEnv, false);
    }

    @ForAllEnvironments
    public void testDefaultRecord() throws Exception {
        Collection<? extends ServerRecord> tcoll = ServerList.getRecords();
        ServerRecord[] records = tcoll.toArray(new ServerRecord[tcoll.size()]);
        for (int i = 0; i < records.length; i++) {
            ServerRecord rec = records[i];
            ServerList.setDefaultRecord(rec);
            assertTrue(ServerList.getDefaultRecord().equals(rec));
            assertTrue(ServerList.getDefaultRecord().getExecutionEnvironment().equals(rec.getExecutionEnvironment()));
        }
    }

    private void dumpRecords(String title) {
        if (log.isLoggable(Level.FINEST)) {
            Collection<? extends ServerRecord> records = ServerList.getRecords();
            System.err.printf("RECORDS %s:\n", title);
            for (ServerRecord rec : records) {
                System.err.printf("%s\n", rec);
                log.finest("" + rec);
            }
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(ServerListTestCase.class);
    }
}
