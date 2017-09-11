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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


package org.netbeans.performance.scalability;

import java.awt.EventQueue;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.RequestProcessor;

public class AWTThreadFreeTest extends NbTestCase {
    public AWTThreadFreeTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        CountingSecurityManager.register();


        
        NbTestSuite s = new NbTestSuite();
        s.addTest(create(".*", ".*"));
        return s;
    }

    private static Test create(String clusters, String modules) {
        System.setProperty("initTestClass", InitRun.class.getName());
        Configuration config = NbModuleSuite.createConfiguration(AWTThreadFreeTest.class)
            .clusters(clusters)
            .enableModules(modules)
            .gui(true);
        return NbModuleSuite.create(config);
    }
    

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
    }

    public void testIsAWTBlockedForTooLong() throws Exception {
        Thread.sleep(10000);
        org.netbeans.jemmy.EventDispatcher.waitQueueEmpty();
        InitRun.assertAWT("AWT was not blocked for too long.", new File(getWorkDir(), "dump.nps"));
    }

    static Object callMethod(Object obj, String name, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = args[i].getClass();
            if (Map.class.isAssignableFrom(types[i])) {
                types[i] = Map.class;
            }
            if (types[i] == Long.class) {
                types[i] = long.class;
            }
            if (types[i] == Integer.class) {
                types[i] = long.class;
            }
        }
        Class<?> clazz = obj instanceof Class ? (Class)obj : obj.getClass();
        Method m = clazz.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(obj, args);
    }

    public static final class InitRun implements Runnable {
        private static Thread awt;
        private static RequestProcessor RP = new RequestProcessor("AWT Watchdog");
        private RequestProcessor.Task snapshot = RP.create(this);
        static List<StackTraceElement[]> ticks = Collections.synchronizedList(new ArrayList<StackTraceElement[]>());
        private static final int DELAY = 10;
        private static final int COUNT = 10;
        static List<StackTraceElement[]> traces = Collections.synchronizedList(new ArrayList<StackTraceElement[]>());


        public static void assertAWT(String msg, File dump) throws Exception {
            if (traces.isEmpty()) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(msg).append('\n');
            int cnt = traces.size();
            for (StackTraceElement[] arr : traces) {
                sb.append("trace: ").append(cnt--).append('\n');
                for (StackTraceElement e : arr) {
                    sb.append("    ")
                      .append(e.getClassName())
                      .append('.')
                      .append(e.getMethodName())
                      .append(':')
                      .append(e.getLineNumber())
                      .append('\n');
                }
                sb.append('\n');
                if (sb.length() > 10000) {
                    sb.delete(0, 5000);
                }
            }
            sb.append("\nThere was ").append(traces.size()).append(" stacktraces\n");

            try {
                if (dump != null) {
                    Class<?> type = Class.forName("org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder");
                    Object builder = type.newInstance();
                    long base = System.currentTimeMillis();
                    long time = base;
                    for (StackTraceElement[] arr : traces) {
                        Map<Thread,StackTraceElement[]> map = Collections.singletonMap(awt, arr);
                        callMethod(builder, "addStacktrace", map, time);
                        time += 10L * 1000 * 1000; // nano seconds
                    }
                    Object snapshot = callMethod(builder, "createSnapshot", base, time);
                    Class<?> loaded = Class.forName("org.netbeans.modules.profiler.LoadedSnapshot");
                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(dump));
                    callMethod(loaded, "writeToStream", snapshot, dos);
                    dos.close();
                    sb.append("\n\n\nProfiler snapshot available at ").append(dump);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                fail(sb.toString());
            }
        }

        public void run() {
            if (awt == null) {
                initThread();
                tick();
                return;
            }
            if (EventQueue.isDispatchThread()) {
                awt = Thread.currentThread();
                if (ticks.size() > COUNT) {
                    traces.addAll(ticks);
                }
                ticks.clear();
                return;
            }

            assert RP.isRequestProcessorThread();

            ticks.add(awt.getStackTrace());
            tick();
        }

        private void tick() {
            snapshot.schedule(DELAY);
            EventQueue.invokeLater(this);
        }

        private void initThread() {
            assertNull("Not initialized yet", awt);
            if (EventQueue.isDispatchThread()) {
                awt = Thread.currentThread();
            } else {
                try {
                    EventQueue.invokeAndWait(this);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
                assertNotNull("AWT thread found", awt);
            }
        }
    } // end of InitRun

}
