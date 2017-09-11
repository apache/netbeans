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
