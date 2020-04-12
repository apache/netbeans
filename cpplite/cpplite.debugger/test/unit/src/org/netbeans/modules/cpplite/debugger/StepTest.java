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

package org.netbeans.modules.cpplite.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
//import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.*;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIUserInteraction;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpointActionProvider;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;



/**
 * Tests Ant debugger stepping actions: step in, step out and step over.
 *
 * @author Jan Jancura
 */
public class StepTest extends NbTestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName ());
    }

//    private String          sourceRoot = System.getProperty ("debuggerant.dir");

    public StepTest (String s) {
        super (s);
    }
    
    public void testStepOver () throws Exception {
//        FileObject source = FileUtil.toFileObject(new File("/home/lahvac/src/nb/cpplite-debugger/debugger/test/unit/data/step-test/main.cpp"));
//        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
//        assertNotNull(lc);
//        DebuggerManager.getDebuggerManager().addBreakpoint(new CPPLiteBreakpoint(lc.getLineSet().getCurrent(4)));
//        CPPLiteDebugger d = CPPLiteDebugger.startDebugging(new CPPLiteDebuggerConfig(Arrays.asListdd("/home/lahvac/src/nb/cpplite-debugger/debugger/test/unit/data/step-test/main")));
//        Object suspendWait = new Object();
//        d.addStateListener(new CPPLiteDebugger.StateListener() {
//            @Override
//            public void suspended(boolean suspended) {
//                synchronized (suspendWait) {
//                    suspendWait.notifyAll();
//                }
//            }
//
//            @Override
//            public void finished() {
//            }
//        });
//        
//        synchronized (suspendWait) {
//            while (!d.isSuspended()) {
//                suspendWait.wait();
//            }
//        }
//        
//        d.doAction(ActionsManager.ACTION_STEP_OVER);
//        
//        synchronized (suspendWait) {
//            while (!d.isSuspended()) {
//                suspendWait.wait();
//            }
//        }
//
//        System.err.println("suspended!");
//        Process debuggee = new ProcessBuilder("gdb", "--interpreter=mi", "/home/lahvac/src/nb/cpplite-debugger/debugger/test/unit/data/step-test/main").start();
//        
//        class CPPLiteInjector implements MICommandInjector {
//
//            @Override
//            public void inject(String data) {
//                try {
//                    debuggee.getOutputStream().write(data.getBytes());
//                    debuggee.getOutputStream().flush();
//                } catch (IOException ex) {
//                    throw new IllegalStateException(ex);
//                }
//            }
//
//            @Override
//            public void log(String data) {
//                System.err.println(data);
//            }
//            
//        }
//        
//        CPPLiteInjector injector = new CPPLiteInjector();
//        
//        class CPPLiteProxy extends MIProxy {
//            public CPPLiteProxy(MICommandInjector injector) {
//                super(injector, "(gdb)", "UTF-8"); //TODO: encoding!
//            }
//        }
//        
//        CountDownLatch waitStarted = new CountDownLatch(1);
//        CPPLiteProxy proxy = new CPPLiteProxy(injector) {
//            @Override
//            protected void prompt() {
//                waitStarted.countDown();
//            }
//
//            @Override
//            protected void execAsyncOutput(MIRecord record) {
//                if (record.token() == 0) {
//                    switch (record.cls()) {
//                        case "stopped":
//                            break;
//                        default:
//                            //unknown class, ignore
//                            System.err.println("Unknown class:" + record.cls());
//                            break;
//                    }
//                    return;
//                }
//                super.execAsyncOutput(record);
//            }
//            
//        };
//        
//        new Thread(() -> {
//            try (BufferedReader r = new BufferedReader(new InputStreamReader(debuggee.getInputStream()))) {
//                String line;
//            
//                while ((line = r.readLine()) != null) {
//                    proxy.processLine(line);
//                }
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }).start();
//        
//        waitStarted.await();
//
//        proxy.send(new MICommand(0, "-break-insert main.cpp:4") {
//            @Override
//            protected void onDone(MIRecord record) {
//                System.err.println("done:" + record);
//            }
//            
//            @Override
//            protected void onRunning(MIRecord record) {
//                System.err.println("running:" + record);
//            }
//            
//            @Override
//            protected void onError(MIRecord record) {
//                System.err.println("error:" + record);
//            }
//            
//            @Override
//            protected void onExit(MIRecord record) {
//                System.err.println("exit:" + record);
//            }
//            
//            @Override
//            protected void onStopped(MIRecord record) {
//                System.err.println("stopped:" + record);
//            }
//            
//            @Override
//            protected void onOther(MIRecord record) {
//                System.err.println("other:" + record);
//            }
//            
//            @Override
//            protected void onUserInteraction(MIUserInteraction ui) {
//                System.err.println("user interaction:" + ui);
//            }
//        });
//
//        proxy.send(new MICommand(0, "-exec-run") {
//            @Override
//            protected void onDone(MIRecord record) {
//                System.err.println("done:" + record);
//            }
//            
//            @Override
//            protected void onRunning(MIRecord record) {
//                System.err.println("running:" + record);
//            }
//            
//            @Override
//            protected void onError(MIRecord record) {
//                System.err.println("error:" + record);
//            }
//            
//            @Override
//            protected void onExit(MIRecord record) {
//                System.err.println("exit:" + record);
//            }
//            
//            @Override
//            protected void onStopped(MIRecord record) {
//                System.err.println("stopped:" + record);
//            }
//            
//            @Override
//            protected void onOther(MIRecord record) {
//                System.err.println("other:" + record);
//            }
//            
//            @Override
//            protected void onUserInteraction(MIUserInteraction ui) {
//                System.err.println("user interaction:" + ui);
//            }
//        });
//
//        if (false)
//        proxy.send(new MICommand(0, "-quit") {
//            @Override
//            protected void onDone(MIRecord record) {
//                System.err.println("done:" + record);
//            }
//            
//            @Override
//            protected void onRunning(MIRecord record) {
//                System.err.println("running:" + record);
//            }
//            
//            @Override
//            protected void onError(MIRecord record) {
//                System.err.println("error:" + record);
//            }
//            
//            @Override
//            protected void onExit(MIRecord record) {
//                System.err.println("exit:" + record);
//            }
//            
//            @Override
//            protected void onStopped(MIRecord record) {
//                System.err.println("stopped:" + record);
//            }
//            
//            @Override
//            protected void onOther(MIRecord record) {
//                System.err.println("other:" + record);
//            }
//            
//            @Override
//            protected void onUserInteraction(MIUserInteraction ui) {
//                System.err.println("user interaction:" + ui);
//            }
//        });
//        
//        debuggee.waitFor();
//        File file = new File (sourceRoot, "build.xml");
//        file = FileUtil.normalizeFile(file);
//        DebuggerAntLogger.getDefault ().debugFile (file);
//        FileObject fileObject = FileUtil.toFileObject (file);
//        ActionUtils.runTarget (
//            fileObject, 
//            new String[] {"run"},
//            null
//        );
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            setLookups(new Lookup[] {
                Lookups.fixed(new Object[] {
//                    new IFL(),
                    Class.forName("org.netbeans.modules.masterfs.MasterURLMapper").newInstance(),
//                    new DebuggerAntLogger ()
                    StepTest.class.getClassLoader(),
                }),
                Lookups.metaInfServices(StepTest.class.getClassLoader()),
            });
        }
    }

//    private static final class IFL extends InstalledFileLocator {
//        public IFL() {}
//        @Override
//        public File locate(String relativePath, String codeNameBase, boolean localized) {
//            if (relativePath.equals("ant/nblib/bridge.jar")) {
//                String path = System.getProperty("test.bridge.jar");
//                assertNotNull("must set test.bridge.jar", path);
//                return new File(path);
//            } else if (relativePath.equals("ant")) {
//                String path = System.getProperty("test.ant.home");
//                assertNotNull("must set test.ant.home", path);
//                return new File(path);
//            } else if (relativePath.startsWith("ant/")) {
//                String path = System.getProperty("test.ant.home");
//                assertNotNull("must set test.ant.home", path);
//                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
//            } else {
//                return null;
//            }
//        }
//    }
}
