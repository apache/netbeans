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

package org.netbeans.api.extexecution.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Hejl
 */
public class ProcessesTest extends NbTestCase {

    public ProcessesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new TestProcessesImplementation(), new TestProcessesImplementation());
    }

    public void testKillTree() {
        TestProcess process = new TestProcess();
        Map<String, String> env = new HashMap<String, String>();
        env.put("test1", "value1");
        env.put("test2", "value2");

        Processes.killTree(process, env);

        ProcessesImplementation impl = Lookup.getDefault().lookup(ProcessesImplementation.class);
        assertNotNull(impl);

        TestProcessesImplementation testPerformer = (TestProcessesImplementation) impl;
        assertEquals(process, testPerformer.getProcess());

        Map<String, String> perfEnv = testPerformer.getEnv();
        assertEquals(2, perfEnv.size());

        assertEquals(env.get("test1"), perfEnv.get("test1"));
        assertEquals(env.get("test2"), perfEnv.get("test2"));
    }

    public void testAnotherImplementation() {
        TestProcess process = new TestProcess();
        Map<String, String> env = new HashMap<String, String>();
        env.put("test1", "value1");
        env.put("test2", "value2");

        ProcessesImplementation impl = Lookup.getDefault().lookup(ProcessesImplementation.class);
        assertNotNull(impl);

        TestProcessesImplementation testPerformer = (TestProcessesImplementation) impl;
        testPerformer.setEnabled(false);

        Processes.killTree(process, env);

        Collection<? extends ProcessesImplementation> impls = Lookup.getDefault().lookupAll(ProcessesImplementation.class);
        assertEquals(2, impls.size());

        Iterator<? extends ProcessesImplementation> it = impls.iterator();
        it.next();
        testPerformer = (TestProcessesImplementation) it.next();
        assertEquals(process, testPerformer.getProcess());

        Map<String, String> perfEnv = testPerformer.getEnv();
        assertEquals(2, perfEnv.size());

        assertEquals(env.get("test1"), perfEnv.get("test1"));
        assertEquals(env.get("test2"), perfEnv.get("test2"));
    }

    private static class TestProcess extends Process {

        private boolean destroyed;

        public boolean destroyCalled() {
            return destroyed;
        }

        @Override
        public void destroy() {
            this.destroyed = true;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public InputStream getErrorStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

    }

    private static class TestProcessesImplementation implements ProcessesImplementation {

        private boolean enabled = true;

        private Process process;

        private Map<String, String> env;

        @Override
        public void killTree(Process process, Map<String, String> environment) {
            if (!enabled) {
                throw new UnsupportedOperationException("Not enabled");
            }
            this.process = process;
            this.env = environment;
        }

        public Process getProcess() {
            return process;
        }

        public Map<String, String> getEnv() {
            return env;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
