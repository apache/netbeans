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
package org.netbeans.modules.maven.runjar;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.Assume;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author sdedic
 */
public class RunJarStartupArgsTest extends NbTestCase {

    public RunJarStartupArgsTest(String name) {
        super(name);
    }
    
    private FileObject d;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    
    @Override
    protected void tearDown() throws Exception {
        LegacyStartupExtenderImpl.enabled = false;
        CurrentStartupExtenderImpl.enabled = false;
        super.tearDown();
    }

    /**
     * Legacy Extenders give quoted arguments to preserve spaces. The quoted strings
     * must be properly incorporated in exec.vmArgs property.
     * @throws Exception 
     */
    public void testLegacyExtenderWithSpaces() throws Exception {
        Assume.assumeFalse(BaseUtilities.isWindows());
        LegacyStartupExtenderImpl.enabled = true;
        Properties p = doTestSpacedExtender();
        assertEquals("/tmp/spaced folder/", p.getProperty("test.spaced.folder.path"));
        assertEquals("/tmp/another space/", p.getProperty("test.another.space"));
    }
    
    /**
     * Checks that 'modern' StartupExtender that declares it does NOT escape arguments work.
     * @throws Exception 
     */
    public void testNetBeans12_4Extender() throws Exception {
        Assume.assumeFalse(BaseUtilities.isWindows());
        CurrentStartupExtenderImpl.enabled = true;
        Properties p = doTestSpacedExtender();
        assertEquals("/tmp/spaced folder/", p.getProperty("test.spaced.folder.path"));
        assertEquals("/tmp/another space/", p.getProperty("test.another.space"));
    }
    
    private Properties doTestSpacedExtender() throws Exception {
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final", ""));
        TestFileUtils.writeFile(d, "pom.xml",
                "<project>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>testgrp</groupId>\n" +
                "    <artifactId>testart</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "    <properties>\n" +
                "        <project.mainclass>test.PrintCommandLine</project.mainclass>\n" +
                "        <exec.java.bin>${java.home}/bin/java</exec.java.bin>\n" +
                "        <maven.compiler.source>" + System.getProperty("java.specification.version") + "</maven.compiler.source>\n" +
                "        <maven.compiler.target>" + System.getProperty("java.specification.version") + "</maven.compiler.target>\n" +
                "    </properties>\n" +
                "</project>\n");

        FileObject f = FileUtil.createFolder(d, "src/main/java/test");
        FileObject source = FileUtil.toFileObject(getDataDir()).getFileObject("exec/PrintCommandLine.java");
        FileObject result = FileUtil.copyFile(source, f, source.getName());
        System.err.println("Testing application: " + result.getPath());

        Project proj = ProjectManager.getDefault().findProject(d);
        RunConfig rc = ActionToGoalUtils.createRunConfig(ActionProvider.COMMAND_RUN, proj.getLookup().lookup(NbMavenProjectImpl.class), proj.getLookup());
        rc.setProperty("packageClassName", "test.PrintCommandLine");
        ((BeanRunConfig)rc).setShowDebug(true);
        ((BeanRunConfig)rc).setShowError(true);
        
        class CaptureOutput implements InputOutput {
            
            class OW extends OutputWriter {
                public OW(Writer w) {
                    super(w);
                }

                @Override
                public void println(String s, OutputListener l) throws IOException {
                    super.println(s);
                }

                @Override
                public void reset() throws IOException {
                }
            }
            
            StringWriter sw = new StringWriter();

            OW writer = new OW(sw);
            
            @Override
            public OutputWriter getOut() {
                return writer;
            }

            @Override
            public Reader getIn() {
                return InputOutput.NULL.getIn();
            }

            @Override
            public OutputWriter getErr() {
                return InputOutput.NULL.getErr();
            }

            @Override
            public void closeInputOutput() {
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void setOutputVisible(boolean value) {
            }

            @Override
            public void setErrVisible(boolean value) {
            }

            @Override
            public void setInputVisible(boolean value) {
            }

            @Override
            public void select() {
            }

            @Override
            public boolean isErrSeparated() {
                return true;
            }

            @Override
            public void setErrSeparated(boolean value) {
            }

            @Override
            public boolean isFocusTaken() {
                return false;
            }

            @Override
            public void setFocusTaken(boolean value) {
            }

            @Override
            public Reader flushReader() {
                return getIn();
            }
            
        }
        
        final CaptureOutput out = new CaptureOutput();
        
        ExecutorTask t = new ExecutorTask(() -> {
        }) {
            @Override
            public void stop() {
            }

            @Override
            public int result() {
                return 0;
            }

            @Override
            public InputOutput getInputOutput() {
                return out;
            }
        };

        MavenCommandLineExecutor cme = new MavenCommandLineExecutor(rc, out, null);
        
        cme.setTask(t);
        cme.run();
        
        out.writer.flush();
        
        String[] lines = out.sw.toString().split("\n");
        int from = 0;
        for (; from < lines.length; from++) {
            if (lines[from].startsWith("::PrintCommandLineStart"))  {
                from++;
                break;
            }
        }
        assertTrue(from > 0);
        
        Properties p = new Properties();
        while (from < lines.length) {
            String s = lines[from];
            if (s.startsWith("::PrintCommandLineEnd")) {
                break;
            }
            int eq = s.indexOf('=');
            p.put(s.substring(0, eq), s.substring(eq + 1));
            from++;
        }
        
        return p;
    }

    /**
     * Returns a quoted string with spaces, to observe that a legacy behaviour works. This is used by e.g. profiler, which
     * quotes its data path or path to JNI libraries (NB may be installed with a folder-with-spaces).
     */
    @StartupExtenderImplementation.Registration(displayName = "Test legacy", startMode = { StartupExtender.StartMode.NORMAL })
    public static class LegacyStartupExtenderImpl implements StartupExtenderImplementation {
        static boolean enabled = false;
        
        @Override
        public List<String> getArguments(Lookup context, StartupExtender.StartMode mode) {
            if (!enabled) {
                return Collections.emptyList();
            }
            return Arrays.asList(
                "-Dtest.spaced.folder.path=\"/tmp/spaced folder/\"",
                "-Dtest.another.space=\"/tmp/another space/\""    
            );
        }
        
    }

    /**
     * Returns a quoted string with spaces, to observe that a legacy behaviour works. This is used by e.g. profiler, which
     * quotes its data path or path to JNI libraries (NB may be installed with a folder-with-spaces).
     */
    @StartupExtenderImplementation.Registration(displayName = "Test 12.4", startMode = { StartupExtender.StartMode.NORMAL }, argumentsQuoted = false)
    public static class CurrentStartupExtenderImpl implements StartupExtenderImplementation {
        static boolean enabled = false;
        
        @Override
        public List<String> getArguments(Lookup context, StartupExtender.StartMode mode) {
            if (!enabled) {
                return Collections.emptyList();
            }
            return Arrays.asList(
                "-Dtest.spaced.folder.path=/tmp/spaced folder/",
                "-Dtest.another.space=/tmp/another space/"
            );
        }
        
    }
}
