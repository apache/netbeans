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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.base.EnvironmentFactory;
import org.netbeans.spi.extexecution.base.EnvironmentImplementation;
import org.netbeans.spi.extexecution.base.ProcessBuilderFactory;
import org.netbeans.spi.extexecution.base.ProcessBuilderImplementation;
import org.netbeans.spi.extexecution.base.ProcessParameters;
import org.openide.util.Lookup;

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
        assertEquals("ls", testBuilder.getParameters().getExecutable());

        builder.setExecutable("cd");
        assertEquals("ls", testBuilder.getParameters().getExecutable());

        builder.call();
        assertEquals("cd", testBuilder.getParameters().getExecutable());
    }

    public void testWorkingDirectory() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertNull(testBuilder.getParameters().getWorkingDirectory());

        builder.setWorkingDirectory("test");
        assertNull(testBuilder.getParameters().getWorkingDirectory());

        builder.call();
        assertEquals("test", testBuilder.getParameters().getWorkingDirectory());
    }

    public void testArguments() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertTrue(testBuilder.getParameters().getArguments().isEmpty());

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "test1", "test2");
        builder.setArguments(arguments);
        assertTrue(testBuilder.getParameters().getArguments().isEmpty());

        builder.call();
        assertEquals(2, testBuilder.getParameters().getArguments().size());
        assertEquals("test1", testBuilder.getParameters().getArguments().get(0));
        assertEquals("test2", testBuilder.getParameters().getArguments().get(1));

        arguments.remove(0);
        assertEquals(2, testBuilder.getParameters().getArguments().size());
        assertEquals("test1", testBuilder.getParameters().getArguments().get(0));
        assertEquals("test2", testBuilder.getParameters().getArguments().get(1));

        builder.call();
        assertEquals(2, testBuilder.getParameters().getArguments().size());
        assertEquals("test1", testBuilder.getParameters().getArguments().get(0));
        assertEquals("test2", testBuilder.getParameters().getArguments().get(1));

        builder.setArguments(arguments);
        builder.call();
        assertEquals(1, testBuilder.getParameters().getArguments().size());
        assertEquals("test2", testBuilder.getParameters().getArguments().get(0));
    }

    public void testEnvironmentVariables() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertTrue(testBuilder.getParameters().getEnvironmentVariables().isEmpty());

        Environment environment = builder.getEnvironment();
        environment.setVariable("key1", "value1");
        environment.setVariable("key2", "value2");
        assertTrue(testBuilder.getParameters().getEnvironmentVariables().isEmpty());

        builder.call();
        assertEquals(2, testBuilder.getParameters().getEnvironmentVariables().size());
        assertEquals("value1", testBuilder.getParameters()
                .getEnvironmentVariables().get("key1"));
        assertEquals("value2", testBuilder.getParameters()
                .getEnvironmentVariables().get("key2"));

        environment.removeVariable("key1");
        assertEquals(2, testBuilder.getParameters().getEnvironmentVariables().size());
        assertEquals("value1", testBuilder.getParameters()
                .getEnvironmentVariables().get("key1"));
        assertEquals("value2", testBuilder.getParameters()
                .getEnvironmentVariables().get("key2"));

        builder.call();
        assertEquals(1, testBuilder.getParameters()
                .getEnvironmentVariables().size());
        assertEquals("value2", testBuilder.getParameters()
                .getEnvironmentVariables().get("key2"));
    }

    public void testEnvironment() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.getEnvironment().setVariable("key1", "value1");
        builder.getEnvironment().setVariable("key2", "value2");

        assertEquals("value1", testBuilder.getEnvironment().getVariable("key1"));
        assertEquals("value2", testBuilder.getEnvironment().getVariable("key2"));

        builder.call();
        assertEquals(2, testBuilder.getParameters().getEnvironmentVariables().size());
        assertEquals("value1", testBuilder.getParameters()
                .getEnvironmentVariables().get("key1"));
        assertEquals("value2", testBuilder.getParameters()
                .getEnvironmentVariables().get("key2"));

        builder.getEnvironment().prependPath("PATH", "/test1");
        builder.getEnvironment().prependPath("PATH", "/test2");

        builder.call();
        assertEquals(3, testBuilder.getParameters().getEnvironmentVariables().size());
        assertEquals("/test2:/test1",
                testBuilder.getParameters().getEnvironmentVariables().get("PATH"));

        builder.getEnvironment().appendPath("PATH", "/test3");

        builder.call();
        assertEquals(3, testBuilder.getParameters().getEnvironmentVariables().size());
        assertEquals("/test2:/test1:/test3",
                testBuilder.getParameters().getEnvironmentVariables().get("PATH"));

        builder.getEnvironment().removeVariable("PATH");
        assertNull(builder.getEnvironment().getVariable("PATH"));

        builder.call();
        assertEquals(2, testBuilder.getParameters().getEnvironmentVariables().size());
        assertNull(testBuilder.getParameters().getEnvironmentVariables().get("PATH"));
    }

    public void testRedirectErrorStream() throws IOException {
        TestProcessBuilder testBuilder = new TestProcessBuilder();
        ProcessBuilder builder = ProcessBuilderFactory.createProcessBuilder(testBuilder, "Test builder");
        builder.setExecutable("ls");

        builder.call();
        assertFalse(testBuilder.getParameters().isRedirectErrorStream());

        builder.setRedirectErrorStream(true);
        assertFalse(testBuilder.getParameters().isRedirectErrorStream());

        builder.call();
        assertTrue(testBuilder.getParameters().isRedirectErrorStream());
    }

    private static class TestProcessBuilder implements ProcessBuilderImplementation {

        private final Environment environment = EnvironmentFactory.createEnvironment(new TestEnvironment());

        private ProcessParameters parameters;

        @Override
        public Environment getEnvironment() {
            return environment;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Process createProcess(ProcessParameters parameters) throws IOException {
            this.parameters = parameters;

            return null;
        }

        public ProcessParameters getParameters() {
            return parameters;
        }
    }

    private static class TestEnvironment implements EnvironmentImplementation {

        private final Map<String, String> values = new HashMap<String, String>();

        @Override
        public String getVariable(String name) {
            return values.get(name);
        }

        @Override
        public void appendPath(String name, String value) {
            String orig = values.get(name);
            if (orig == null || orig.isEmpty()) {
                values.put(name, value);
            } else {
                // intentionally hardcoded for tests
                values.put(name, orig + ":" + value);
            }
        }

        @Override
        public void prependPath(String name, String value) {
            String orig = values.get(name);
            if (orig == null || orig.isEmpty()) {
                values.put(name, value);
            } else {
                // intentionally hardcoded for tests
                values.put(name, value + ":" + orig);
            }
        }

        @Override
        public void setVariable(String name, String value) {
            values.put(name, value);
        }

        @Override
        public void removeVariable(String name) {
            values.remove(name);
        }

        @Override
        public Map<String, String> values() {
            return new HashMap<String, String>(values);
        }
    }
}
