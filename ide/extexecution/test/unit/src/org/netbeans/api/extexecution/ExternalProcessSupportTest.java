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

package org.netbeans.api.extexecution;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.destroy.ProcessDestroyPerformer;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Hejl
 */
public class ExternalProcessSupportTest extends NbTestCase {

    public ExternalProcessSupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new TestProcessDestroyPerformer());
    }

    public void testDestroy() {
        TestProcess process = new TestProcess();
        Map<String, String> env = new HashMap<String, String>();
        env.put("test1", "value1");
        env.put("test2", "value2");

        ExternalProcessSupport.destroy(process, env);

        assertFalse(process.destroyCalled());

        ProcessDestroyPerformer performer = Lookup.getDefault().lookup(ProcessDestroyPerformer.class);
        assertNotNull(performer);

        TestProcessDestroyPerformer testPerformer = (TestProcessDestroyPerformer) performer;
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

    private static class TestProcessDestroyPerformer implements ProcessDestroyPerformer {

        private Process process;

        private Map<String, String> env;

        public void destroy(Process process, Map<String, String> env) {
            this.process = process;
            this.env = env;
        }

        public Process getProcess() {
            return process;
        }

        public Map<String, String> getEnv() {
            return env;
        }
    }
}
