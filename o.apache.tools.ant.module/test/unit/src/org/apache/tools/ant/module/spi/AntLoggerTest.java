/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

// For debugging info, add to nbproject/private/private.properties:
// test-unit-sys-prop.org.apache.tools.ant.module.bridge.impl.NbBuildLogger.level=FINEST

/**
 * Tests functionality of {@link AntLogger}.
 * Specifically, NbBuildLogger.
 * @author Jesse Glick
 */
public class AntLoggerTest extends NbTestCase {
    
    static {
        AntLoggerTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }

    public AntLoggerTest(String name) {
        super(name);
    }

    private File testdir;
    private FileObject testdirFO;
    private TestLogger LOGGER;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AntBridge.NO_MODULE_SYSTEM = true;
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        MockServices.setServices(IFL.class, TestLogger.class);
        LOGGER = Lookup.getDefault().lookup(TestLogger.class);
        LOGGER.reset();
        testdir = new File(this.getDataDir(), "antlogger");
        assertTrue("have a dir " + testdir, testdir.isDirectory());
        testdirFO = FileUtil.toFileObject(testdir);
        assertNotNull("have testdirFO", testdirFO);
    }

    protected @Override int timeOut() {
        return 300000;
    }

    private void run(FileObject script) throws Exception {
        run(script, null, AntEvent.LOG_INFO);
    }
    private void run(FileObject script, String[] targets, int verbosity) throws Exception {
        AntTargetExecutor.Env env = new AntTargetExecutor.Env();
        env.setVerbosity(verbosity);
        int res = AntTargetExecutor.createTargetExecutor(env).execute(new AntProjectSupport(script), targets).result();
        if (res != 0) {
            throw new IOException("Nonzero exit code: " + res + "; messages: " + LOGGER.getMessages());
        }
    }

    public void testRunningAnt() throws Exception {
        File something = new File(System.getProperty("java.io.tmpdir"), "something");
        if (something.exists()) {
            something.delete();
        }
        run(testdirFO.getFileObject("trivial.xml"));
        assertTrue("now " + something + " exists", something.isFile());
    }
    
    public void testLocationOfImportedTargetsWithoutLineNumbers() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run (NOTE: you need Ant 1.6.0+)", Arrays.asList(new String[] {
            imported + "#subtarget",
            importing + "#main",
        }), LOGGER.getTargetsStarted());
    }
    
    public void testLocationOfImportedTargetsWithLineNumbers() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.collectLineNumbersForTargets = true;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run (NOTE: you need Ant 1.6.3+)", Arrays.asList(new String[] {
            imported + ":3#subtarget",
            importing + ":4#main",
        }), LOGGER.getTargetsStarted());
    }

    public void testLocationOfImportedTasks() throws Exception { // #104103
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_DEBUG};
        run(testdirFO.getFileObject("importing.xml"));
        // Interesting because WhichResource uses Project.log rather than Task.log:
        // getProject().log("using system classpath: " + classpath, Project.MSG_DEBUG);
        assertEquals(new File(testdir, "imported.xml") + ":6", LOGGER.importedTaskLocation);
    }
    
    public void testTaskdef() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_INFO, AntEvent.LOG_WARN};
        run(testdirFO.getFileObject("taskdefs.xml"));
        //System.err.println("messages=" + LOGGER.messages);
        assertTrue("got info message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_INFO + ":MyTask info message"));
        assertFalse("did not get verbose message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_VERBOSE + ":MyTask verbose message"));
        assertTrue("got warn message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_WARN + ":MyTask warn message"));
    }
    
    public void testCorrectTaskFromIndirectCall() throws Exception {
        // #49464: if a task calls something which in turn does Project.log w/o the Task handle,
        // you lose all useful information. But you can guess which Task it is - you know some
        // task has been started and not yet finished. Within limits. Imports, <ant>, etc. can
        // screw up the accounting.
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_DEBUG};
        run(testdirFO.getFileObject("property.xml"));
        //System.err.println("messages=" + LOGGER.messages);
        List<String> messages = LOGGER.getMessages();
        assertTrue("have message with task ID in " + messages, messages.contains("property:4:Setting project property: propname -> propval"));
    }

    public void testAntEventDetails() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = new String[] {"echo"};
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_INFO};
        run(testdirFO.getFileObject("property.xml"));
        assertTrue(LOGGER.antEventDetailsOK);
        LOGGER.antEventDetailsOK = false;
        run(testdirFO.getFileObject("property.xml"), new String[] {"run2"}, AntEvent.LOG_INFO);
        assertTrue("#71816: works even inside <antcall>", LOGGER.antEventDetailsOK);
    }
    
    public void testSimultaneousLogging() throws Exception {
        // #84704: just because one log call is locked, ought not block others
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_VERBOSE};
        LOGGER.halt = true;
        run(testdirFO.getFileObject("trivial.xml"), null, AntEvent.LOG_VERBOSE);
        // see TestLogger.taskStarted for details
    }

    public void testReferences() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        FileObject rxml = testdirFO.getFileObject("reference.xml");
        run(rxml);
        assertEquals(FileUtil.toFile(rxml).getAbsolutePath(), LOGGER.referenceValue);
        assertTrue(LOGGER.hasReference);
    }
    
    /**
     * Sample logger which collects results.
     */
    public static final class TestLogger extends AntLogger {
        
        public boolean interestedInSessionFlag;
        public boolean interestedInAllScriptsFlag;
        public Set<File> interestingScripts;
        public String[] interestingTargets;
        public String[] interestingTasks;
        public int[] interestingLogLevels;
        public boolean collectLineNumbersForTargets;
        public boolean halt;
        /** Format of each: "/path/to/file.xml:line#targetName" (line numbers only if collectLineNumbersForTargets) */
        private List<String> targetsStarted;
        /** Format of each: "taskname:level:message" */
        private List<String> messages;
        private boolean antEventDetailsOK;
        private String referenceValue;
        private boolean hasReference;
        private String importedTaskLocation;
        
        public TestLogger() {}
        
        /** Set everything back to default values as in AntLogger base class. */
        public synchronized void reset() {
            interestedInSessionFlag = false;
            interestedInAllScriptsFlag = false;
            interestingScripts = new HashSet<File>();
            interestingTargets = AntLogger.NO_TARGETS;
            interestingTasks = AntLogger.NO_TASKS;
            interestingLogLevels = new int[0];
            collectLineNumbersForTargets = false;
            targetsStarted = new ArrayList<String>();
            messages = new ArrayList<String>();
            antEventDetailsOK = false;
            referenceValue = null;
            hasReference = false;
            importedTaskLocation = null;
            halt = false;
        }
        
        public synchronized List<String> getTargetsStarted() {
            return new ArrayList<String>(targetsStarted);
        }
        
        public synchronized List<String> getMessages() {
            return new ArrayList<String>(messages);
        }

        @Override
        public boolean interestedInAllScripts(AntSession session) {
            return interestedInAllScriptsFlag;
        }

        @Override
        public String[] interestedInTasks(AntSession session) {
            return interestingTasks;
        }

        @Override
        public boolean interestedInScript(File script, AntSession session) {
            return interestingScripts.contains(script);
        }

        @Override
        public String[] interestedInTargets(AntSession session) {
            return interestingTargets;
        }

        @Override
        public boolean interestedInSession(AntSession session) {
            return interestedInSessionFlag;
        }

        @Override
        public int[] interestedInLogLevels(AntSession session) {
            return interestingLogLevels;
        }

        @Override
        public synchronized void targetStarted(AntEvent event) {
            int line = event.getLine();
            targetsStarted.add(event.getScriptLocation() +
                (collectLineNumbersForTargets && line != -1 ? ":" + line : "") +
                '#' + event.getTargetName());
        }
        
        @Override
        public void targetFinished(AntEvent event) {
            referenceValue = event.getProperty("p");
            hasReference = event.getPropertyNames().contains("p");
        }

        @Override
        public synchronized void messageLogged(AntEvent event) {
            String toadd = "" + event.getLogLevel() + ":" + event.getMessage();
            String taskname = event.getTaskName();
            if (taskname != null) {
                toadd = taskname + ":" + toadd;
            }
            messages.add(toadd);
            if ("whichresource".equals(event.getTaskName())) {
                importedTaskLocation = event.getScriptLocation() + ":" + event.getLine();
            }
        }

        @Override
        public synchronized void buildFinished(AntEvent event) {
            Throwable t = event.getException();
            if (t != null) {
                messages.add("EXC:" + t);
            }
        }

        @Override
        public synchronized void taskStarted(final AntEvent event) {
            antEventDetailsOK |=
                    "echo".equals(event.getTaskName()) &&
                    "meaningless".equals(event.getTaskStructure().getText()) &&
                    "info".equals(event.getTaskStructure().getAttribute("Level")) &&
                    event.getPropertyNames().contains("propname") &&
                    "propval".equals(event.getProperty("propname"));
            if (halt && event.getTaskName().equals("touch")) {
                try {
                    Thread t = new Thread() {
                        public @Override void run() {
                            synchronized (TestLogger.this) {
                                assertEquals("${foobie}", event.evaluate("${foobie}"));
                                TestLogger.this.notify();
                            }
                        }
                    };
                    t.start();
                    wait(9999);
                    t.join(9999);
                    boolean found = false;
                    for (String m : messages) {
                        if (m.contains("foobie")) {
                            found = true;
                            break;
                        }
                    }
                    assertTrue("message about ${foobie} exists", found);
                } catch (InterruptedException x) {
                    fail(x.toString());
                }
            }
        }
        
    }
    
    public static final class IFL extends InstalledFileLocator {
        public IFL() {}
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
