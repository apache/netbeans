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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.ProcessBuilderFactory;
import org.netbeans.spi.extexecution.ProcessBuilderImplementation;

/**
 *
 * @author Petr Hejl
 */
public class ProcessBuilderTest extends NbTestCase {

    public ProcessBuilderTest(String name) {
        super(name);
    }

    public void testExecutable() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");

        try {
            builder.call();
            fail("Empty executable does not throw exception");
        } catch (IllegalStateException ex) {
            // expected
        }

        builder.setExecutable("ls");
        builder.call();
        assertEquals("ls", testBuilder.getExecutable());

        builder.setExecutable("cd");
        assertEquals("ls", testBuilder.getExecutable());

        builder.call();
        assertEquals("cd", testBuilder.getExecutable());
    }

    public void testWorkingDirectory() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertNull(testBuilder.getWorkingDirectory());

        builder.setWorkingDirectory("test");
        assertNull(testBuilder.getWorkingDirectory());

        builder.call();
        assertEquals("test", testBuilder.getWorkingDirectory());
    }

    public void testArguments() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertTrue(testBuilder.getArguments().isEmpty());

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "test1", "test2");
        builder.setArguments(arguments);
        assertTrue(testBuilder.getArguments().isEmpty());

        builder.call();
        assertEquals(2, testBuilder.getArguments().size());
        assertEquals("test1", testBuilder.getArguments().get(0));
        assertEquals("test2", testBuilder.getArguments().get(1));

        arguments.remove(0);
        assertEquals(2, testBuilder.getArguments().size());
        assertEquals("test1", testBuilder.getArguments().get(0));
        assertEquals("test2", testBuilder.getArguments().get(1));

        builder.call();
        assertEquals(2, testBuilder.getArguments().size());
        assertEquals("test1", testBuilder.getArguments().get(0));
        assertEquals("test2", testBuilder.getArguments().get(1));

        builder.setArguments(arguments);
        builder.call();
        assertEquals(1, testBuilder.getArguments().size());
        assertEquals("test2", testBuilder.getArguments().get(0));
    }

    public void testPaths() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertTrue(testBuilder.getPaths().isEmpty());

        List<String> paths = new ArrayList<String>();
        Collections.addAll(paths, "test1", "test2");
        builder.setPaths(paths);
        assertTrue(testBuilder.getPaths().isEmpty());

        builder.call();
        assertEquals(2, testBuilder.getPaths().size());
        assertEquals("test1", testBuilder.getPaths().get(0));
        assertEquals("test2", testBuilder.getPaths().get(1));

        paths.remove(0);
        assertEquals(2, testBuilder.getPaths().size());
        assertEquals("test1", testBuilder.getPaths().get(0));
        assertEquals("test2", testBuilder.getPaths().get(1));

        builder.call();
        assertEquals(2, testBuilder.getPaths().size());
        assertEquals("test1", testBuilder.getPaths().get(0));
        assertEquals("test2", testBuilder.getPaths().get(1));

        builder.setPaths(paths);
        builder.call();
        assertEquals(1, testBuilder.getPaths().size());
        assertEquals("test2", testBuilder.getPaths().get(0));
    }

    public void testEnvironment() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertTrue(testBuilder.getEnvironment().isEmpty());

        Map<String, String> environment = new HashMap<String, String>();
        environment.put("key1", "value1");
        environment.put("key2", "value2");
        builder.setEnvironmentVariables(environment);
        assertTrue(testBuilder.getEnvironment().isEmpty());

        builder.call();
        assertEquals(2, testBuilder.getEnvironment().size());
        assertEquals("value1", testBuilder.getEnvironment().get("key1"));
        assertEquals("value2", testBuilder.getEnvironment().get("key2"));

        environment.remove("key1");
        assertEquals(2, testBuilder.getEnvironment().size());
        assertEquals("value1", testBuilder.getEnvironment().get("key1"));
        assertEquals("value2", testBuilder.getEnvironment().get("key2"));

        builder.call();
        assertEquals(2, testBuilder.getEnvironment().size());
        assertEquals("value1", testBuilder.getEnvironment().get("key1"));
        assertEquals("value2", testBuilder.getEnvironment().get("key2"));

        builder.setEnvironmentVariables(environment);
        builder.call();
        assertEquals(1, testBuilder.getEnvironment().size());
        assertEquals("value2", testBuilder.getEnvironment().get("key2"));
    }

    public void testRedirectErrorStream() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertFalse(testBuilder.isRedirectErrorStream());

        builder.setRedirectErrorStream(true);
        assertFalse(testBuilder.isRedirectErrorStream());

        builder.call();
        assertTrue(testBuilder.isRedirectErrorStream());
    }

    private class TestProcessBuilder implements ProcessBuilderImplementation {

        private String executable;

        private String workingDirectory;

        private List<String> arguments;

        private List<String> paths;

        private Map<String, String> environment;

        private boolean redirectErrorStream;

        @Override
        public Process createProcess(String executable, String workingDirectory,
                List<String> arguments, List<String> paths, Map<String, String> environment, boolean redirectErrorStream) throws IOException {

            this.executable = executable;
            this.workingDirectory = workingDirectory;
            this.arguments = arguments;
            this.paths = paths;
            this.environment = environment;
            this.redirectErrorStream = redirectErrorStream;

            return null;
        }

        public String getExecutable() {
            return executable;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public List<String> getPaths() {
            return paths;
        }

        public Map<String, String> getEnvironment() {
            return environment;
        }

        public boolean isRedirectErrorStream() {
            return redirectErrorStream;
        }

        public String getWorkingDirectory() {
            return workingDirectory;
        }

    }
}
