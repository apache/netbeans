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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.dbx.DbxDebuggerInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
 

import org.netbeans.modules.cnd.debugger.dbx.test.CndBaseTestCase;

/*
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger.State;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
*/
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileUtil;

/**
 * Base class for each debugger test case should extend this class. It provides a handle
 * to a Debugger engine as well as the test apps.
 *
 *
 */
public abstract class DebuggerTestCase extends CndBaseTestCase implements ContextProvider {

    protected ProjectActionEvent pae = null;
    protected Project project = null;
    protected MakeConfiguration conf = null;
    protected String testapp = null;
    protected String testproj = null;
    private final String testapp_dir = null;
    private String project_dir = null;
    private String executable = "";
    protected VariablesTestCase.MockDebugger debugger;

    // protected GdbProxy gdb;
    protected static final Logger tlog = Logger.getLogger("dbx.testlogger"); // NOI18N


    private final Object STATE_WAIT_LOCK = new String("State Wait Lock");
    private final Object BP_WAIT_LOCK = new String("Breakpoint Wait Lock");
    private final long WAIT_TIMEOUT = 5000;

    private final NotifyingListener stateListener = new NotifyingListener(STATE_WAIT_LOCK);
    private final NotifyingListener stackListener = new NotifyingListener(BP_WAIT_LOCK);

    protected static final NativeDebuggerManager dm = NativeDebuggerManager.get();

    public DebuggerTestCase(String name) {
        super(name);
        System.setProperty("debugger.testsuite", "true");
        tlog.setLevel(Level.FINE);
	System.out.println("DebuggerTestCase");
        // TODO: need to get test apps dir from the environment
	/* LATER
        String workdir = System.getProperty("nbjunit.workdir"); // NOI18N
        if (workdir != null && workdir.endsWith("/build/test/unit/work")) { // NOI18N
            testapp_dir = workdir.substring(0, workdir.length() - 21) + "/build/testapps"; // NOI18N
            File dir = new File(testapp_dir);
            if (!dir.exists()) {
                assert false : "Missing testapps directory";
            }
        }
	*/
    }

    protected void tlog(String msg) {
        System.out.println("    " + testapp + ": " + msg); // NOI18N
    }

    private class TestStateListener  implements StateListener {
	public void update(State state) {
	    waitForState(state);
	}
    }

    private final StateListener sl = new TestStateListener();

    protected void startDebugger(String testproj, String executable, String args) {
        this.testproj = testproj;
        this.executable = testapp_dir + '/' + executable;
        project_dir = new File(testapp_dir, testproj).getAbsolutePath();
        conf = createTestConfiguration(args);
	DbxDebuggerInfo ddi = DbxDebuggerInfo.create();
	ddi.setConfiguration(conf);
        ddi.setAction(NativeDebuggerManager.STEP);
        dm.debugNoAsk(ddi);
        /* LATER
        debugger = dm.currentDebugger();
	debugger.addStateListener(sl);
         *
         */

/*
        pae = new ProjectActionEvent(project, ProjectActionEvent.Type.DEBUG_STEPINTO, testapp, executable, conf, null, false);
        CompilerSetManager.getDefault().getCompilerSet(0).getTool(Tool.DebuggerTool).setPath("/opt/csw/bin/gdb");
        dm.startDebugging( DbxDebuggerInfo.create("dbx"),
            new Object[]{pae}));
        debugger = GdbDebugger.getGdbDebugger();
        debugger.addPropertyChangeListener(GdbDebugger.PROP_STATE, stateListener);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, stackListener);
        waitForState(State.STOPPED);
*/
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.testapp = getName();
        System.out.println("\n" + testapp); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //debugger.postKill();
        project = null;
        pae = null;
        debugger = null;
        testapp = null;
        executable = null;
        dm.postDeleteAllWatches();
        removeAllBreakpoints();
    }

    protected void waitForState(State state) {
        /*
        long timeout = WAIT_TIMEOUT;
        long start = System.currentTimeMillis();

        System.out.println("    waitForStateChange: Waiting for state " + state + " [current is " + debugger.getState() + "]");
        synchronized (STATE_WAIT_LOCK) {
            for (;;) {
                timeout = timeout - (System.currentTimeMillis() - start);

                if (debugger.getState() == state) {
                    System.out.println("    waitForStateChange: Got expected state " + state + ", wait time is :" + (WAIT_TIMEOUT - timeout));
                    return;
                } else if (timeout < 0) {
                    System.out.println("    waitForStateChange: Timeout exceeded");
                    fail("Timeout while waiting for State " + state + "(Current: " + debugger.getState() + ")");
                    return;
                }
                try {
                    STATE_WAIT_LOCK.wait(timeout);
                } catch (InterruptedException ie) {
                }
            }
        }
         */
    }

    //protected void waitForBreakpoint(CndBreakpoint breakpoint) {
        /*
        long timeout = WAIT_TIMEOUT;
        long start = System.currentTimeMillis();

        System.out.println("    waitForBreakpoint: Waiting for breakpoint" + breakpoint);
        synchronized (BP_WAIT_LOCK) {
            for (;;) {
                timeout = timeout - (System.currentTimeMillis() - start);
                GdbCallStackFrame csf = debugger.getCurrentCallStackFrame();
                if (csf != null && breakpoint.getPath().equals(csf.getFullname()) && breakpoint.getLineNumber() == csf.getLineNumber()) {
                    System.out.println("    waitForBreakpoint: Got expected stop position, wait time is :" + (WAIT_TIMEOUT - timeout));
                    return;
                } else if (timeout < 0) {
                    System.out.println("    waitForBreakpoint: Timeout exceeded");
                    if (csf != null) {
                        fail("Timeout while waiting for breakpoint (Current position is " + csf.getFullname() + ":" + csf.getLineNumber() + ")");
                    } else {
                        fail("Timeout while waiting for breakpoint (Current position is nowhere)");
                    }
                    return;
                }
                try {
                    BP_WAIT_LOCK.wait(timeout);
                } catch (InterruptedException ie) {
                }
            }
        }
         */
    //}

//    protected void waitForBPValid(CndBreakpoint breakpoint) {
//        long timeout = WAIT_TIMEOUT;
//        long start = System.currentTimeMillis();
//
//        final Object lock = new String("Validity wait lock " + breakpoint);
//
//        PropertyChangeListener listener = new NotifyingListener(lock);
//
//        breakpoint.addPropertyChangeListener(listener);
//
//        System.out.println("    waitForBPValid: Waiting for breakpoint " + breakpoint + " to be valid");
//        synchronized (lock) {
//            for (;;) {
//                timeout = timeout - (System.currentTimeMillis() - start);
//
//                if (breakpoint.getValidity() == Breakpoint.VALIDITY.VALID) {
//                    System.out.println("    waitForBPValid: Got expected validity, wait time is :" + (WAIT_TIMEOUT - timeout));
//                    break;
//                } else if (timeout < 0) {
//                    System.out.println("    waitForBPValid: Timeout exceeded");
//                    // LATER fail("Timeout while waiting for breakpoint valid " + breakpoint);
//                }
//                try {
//                    lock.wait(timeout);
//                } catch (InterruptedException ie) {
//                }
//            }
//        }
//        breakpoint.removePropertyChangeListener(listener);
//    }

//    protected CndBreakpoint setLineBreakpoint(String filename, int lineNo) {
//        String bpPath = new File(project_dir, filename).getAbsolutePath();
//        LineBreakpoint lb = LineBreakpoint.create(bpPath, lineNo);
//        //new LineBreakpointImpl(lb, debugger);
//        //dm.addBreakpoint(lb);
//        waitForBPValid(lb);
//        return lb;
//    }
//
//    protected CndBreakpoint setFunctionBreakpoint(String function) {
//        FunctionBreakpoint fb = FunctionBreakpoint.create(function);
//        //new FunctionBreakpointImpl(fb, debugger);
//        //dm.addBreakpoint(fb);
//        waitForBPValid(fb);
//        return fb;
//    }

    private void removeAllBreakpoints() {
        /*
        for (Breakpoint bp : dm.getBreakpoints()) {
            dm.removeBreakpoint(bp);
	}
         *
         */
    }

    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return null;
        // return dm.lookup(folder, service);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookupFirst(String folder, Class<T> service) {
        if (service == ProjectActionEvent.class) {
            if (pae == null) {
                conf = createTestConfiguration("");      
                pae = new ProjectActionEvent(project, ProjectActionEvent.PredefinedType.DEBUG_STEPINTO, executable, null, null, false);
            }
            return (T) pae;
        } else {
            return null;
            // return dm.lookupFirst(folder, service);
        }
    }

    private MakeConfiguration createTestConfiguration(String args) {
        FSPath toFSPath = FSPath.toFSPath(FileUtil.toFileObject(new File(project_dir)));
        MakeConfiguration makeConf = MakeConfiguration.createConfiguration(toFSPath, testproj, MakeConfiguration.TYPE_APPLICATION, null, HostInfoUtils.LOCALHOST);
        RunProfile profile = conf.getProfile();
        profile.getConsoleType().setValue(RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW);
        profile.setArgs(args);
        return makeConf;
    }

    private class NotifyingListener implements PropertyChangeListener {
        private final Object lock;

        public NotifyingListener(Object lock) {
            this.lock = lock;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private class StackListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (BP_WAIT_LOCK) {
                BP_WAIT_LOCK.notifyAll();
            }
        }
    }
}
