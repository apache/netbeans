/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
