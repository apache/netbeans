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
package org.netbeans.core.execution;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.execution.ExecutorTask;
import org.openide.execution.NbClassLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author lahvac
 */
public class CloseTest extends NbTestCase {

    public CloseTest(String name) {
        super(name);
    }

    public void testClose() throws Exception {
        MockServices.setServices(TestIOProvider.class);
        org.openide.execution.ExecutionEngine engine = new ExecutionEngine();
        boolean[] wasSecurityException = new boolean[1];
        Runnable task = () -> {
            try {
                System.out.println("Hello!");
                System.exit(15);
            } catch (SecurityException ex) {
                wasSecurityException[0] = true;
                throw ex;
            }
        };
        ExecutorTask executorTask = engine.execute("TestX", task, null);
        executorTask.waitFinished();
        assertEquals(1, ios.size());
        TestInputOutput io = ios.remove(0);
        assertEquals("Hello!\n", io.outData.toString());
        assertTrue(wasSecurityException[0]);
    }

    public void testCloseNbClassLoader() throws Exception {
        MockServices.setServices(TestIOProvider.class);
        clearWorkDir();
        File wd = getWorkDir();
        File classes = new File(wd, "classes");
        classes.mkdirs();
        CompilationTask compilation = ToolProvider.getSystemJavaCompiler().getTask(null, null, null, Arrays.asList("-classpath", CloseTest.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "-d", classes.getAbsolutePath()), null, Arrays.asList(new SimpleJavaFileObject(new URI("mem://Test.java"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return "import java.awt.*;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        try {\n" +
                       "            javax.swing.SwingUtilities.invokeAndWait(() -> {\n" +
                       "                java.awt.Window opened = new Window((Window) null);\n" +
                       "                opened.setVisible(true);\n" +
                       "                System.getProperties().put(\"opened.window\", opened);\n" +
                       "            });\n" +
                       "        } catch (InterruptedException | java.lang.reflect.InvocationTargetException ex) {\n" +
                       "            throw new IllegalStateException(ex);\n" +
                       "        }\n" +
                       "        System.out.println(\"Hello!\");\n" +
                       "        System.exit(0);\n" +
                       "    }\n" +
                       "}\n";
            }

            @Override
            public boolean isNameCompatible(String simpleName, Kind kind) {
                return true;
            }
        }));
        assertTrue(compilation.call());
        org.openide.execution.ExecutionEngine engine = new ExecutionEngine();
        boolean[] wasThreadDeath = new boolean[1];
        Runnable task = () -> {
            try {
                FileObject root = FileUtil.toFileObject(classes);
                assertEquals(1, ios.size());
                TestInputOutput io = ios.get(0);
                NbClassLoader loader = new NbClassLoader(new FileObject[] {root}, CloseTest.class.getClassLoader(), io);
                Class<?> test = loader.loadClass("Test");
                test.getDeclaredMethod("test").invoke(null);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof ThreadDeath) {
                    wasThreadDeath[0] = true;
                    throw ((ThreadDeath) ex.getTargetException());
                } else {
                    throw new IllegalStateException(ex);
                }
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ExecutorTask executorTask = engine.execute("Test", task, null);
        executorTask.waitFinished();
        assertEquals(1, ios.size());
        TestInputOutput io = ios.remove(0);
        assertEquals("Hello!\n", io.outData.toString());
        assertTrue(wasThreadDeath[0]);
        boolean[] wasClosed = new boolean[1];
        SwingUtilities.invokeAndWait(() -> {
            Window opened = (Window) System.getProperties().get("opened.window");
            wasClosed[0] = opened.isVisible();
        });
        assertFalse(wasClosed[0]);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(CloseTest.class).parentClassLoader(CloseTest.class.getClassLoader().getParent()).suite();
    }

    private static List<TestInputOutput> ios = new ArrayList<>();

    public static class TestIOProvider extends IOProvider {

        @Override
        public InputOutput getIO(String name, boolean newIO) {
            TestInputOutput tio = new TestInputOutput(name);

            ios.add(tio);

            return tio;
        }

        @Override
        public OutputWriter getStdOut() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    private static final class TestInputOutput implements InputOutput {

        private final String name;
        private final StringWriter outData = new StringWriter();
        private final OutputWriter out = new TestOutputWriter(outData);
        private final StringWriter errData = new StringWriter();
        private final OutputWriter err = new TestOutputWriter(errData);
        private final StringReader in = new StringReader("");

        private boolean closed;

        private TestInputOutput(String name) {
            this.name = name;
        }

        @Override
        public OutputWriter getOut() {
            return out;
        }

        @Override
        public Reader getIn() {
            return in;
        }

        @Override
        public OutputWriter getErr() {
            return err;
        }

        @Override
        public void closeInputOutput() {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void setOutputVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setErrVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setInputVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void select() {
        }

        @Override
        public boolean isErrSeparated() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setErrSeparated(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFocusTaken() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFocusTaken(boolean value) {
        }

        @Override
        public Reader flushReader() {
            return in;
        }

        private static final class TestOutputWriter extends OutputWriter {

            public TestOutputWriter(StringWriter delegate) {
                super(delegate);
            }

            @Override
            public void println(String s, OutputListener l) throws IOException {
                println(s);
            }

            @Override
            public void reset() throws IOException {
            }

        }
    }
}
