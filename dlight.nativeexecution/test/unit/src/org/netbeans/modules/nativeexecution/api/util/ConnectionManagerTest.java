/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ConnectionManagerTest extends NativeExecutionBaseTestCase {

    public ConnectionManagerTest(String name) {
        super(name);
    }

    public ConnectionManagerTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ConnectionManagerTest.class);
    }

    public void testGetRecentConnections() throws Exception {
        String section = "remote.platforms";
        ExecutionEnvironment[] envs = NativeExecutionTestSupport.getTestExecutionEnvironmentsFromSection(section);
        assertTrue("Empty environmens list for section ", envs.length > 0);
        ConnectionManager.getInstance().clearRecentConnectionsList();
        AbstractList<ExecutionEnvironment> referenceList = new ArrayList<>();
        for (ExecutionEnvironment env : envs) {
            ConnectionManager.getInstance().updateRecentConnectionsList(env);
            referenceList.add(0, env);
        }
        List<ExecutionEnvironment> managersList = ConnectionManager.getInstance().getRecentConnections();
        assertEquals("Connections lists differ", referenceList, managersList);
        ConnectionManager.getInstance().clearRecentConnectionsList();
        assertTrue("Recent connections list should be empty", ConnectionManager.getInstance().getRecentConnections().isEmpty());
        ConnectionManager.getInstance().restoreRecentConnectionsList();
        assertEquals("Restopred connections list differ", referenceList, managersList);
    }

    @RandomlyFails
    @ForAllEnvironments(section = "remote.platforms")
    public void testConnectDisconnect() throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assert (execEnv != null);
        final String id = execEnv.toString();

        System.out.println(getName() + " started");

        ConnectionManager cm = ConnectionManager.getInstance();

        if (cm.isConnectedTo(execEnv)) {
            System.out.println(id + " initially connected! Disconnecting from it to proceed with the test.");
            char[] passwd = PasswordManager.getInstance().getPassword(execEnv);
            cm.disconnect(execEnv);
            PasswordManager.getInstance().storePassword(execEnv, passwd, false);
        }

        assertFalse(id + " must be disconnected at this point", cm.isConnectedTo(execEnv));

        try {
            ConnectionManager.getInstance().connectTo(execEnv);
        } catch (Exception ex) {
            // this catch is just for debugging convenience
            // let it fail with exactly the same exception that was thrown by ConnectionManager
            throw ex;
        }

        assertTrue(id + " must be connected at this point", cm.isConnectedTo(execEnv));
        cm.disconnect(execEnv);
        assertFalse(id + " must be disconnected at this point", cm.isConnectedTo(execEnv));
        System.out.println(getName() + " finished");
    }

    public void testGetConnectToAction() throws Exception {
        final int threadsNum = 10;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        Collection<String> mspecs = rcFile.getKeys("remote.platforms");
        
        if (mspecs.isEmpty()) {
            fail("remote.platforms are not properly configured [rc file == " + rcFile.toString() + "] !");
        }

        final ConcurrentTasksSupport.Counters counters = new ConcurrentTasksSupport.Counters();
        final ConcurrentTasksSupport support = new ConcurrentTasksSupport(threadsNum);

        ExecutionEnvironment env;

        for (String mspec : mspecs) {
            env = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec); // NOI18N
            if (env == null) {
                System.out.println("... skip testing on not configured " + mspec + " ... "); // NOI18N
            } else {
                System.out.println("... test on " + mspec + " [" + env.toString() + "] ..."); // NOI18N
                support.addFactory(new GetConnectToActionTaskFactory(counters, env));
            }
        }


        support.init();
        support.start();
        support.waitCompletion();

        // This is async actions...
        // Some of them will initiate onConnect() method, but some not...
        Thread.sleep(1000);

        counters.dump(System.out);
        counters.assertEquals("Number of started actions", "started", threadsNum);
        counters.assertEquals("Number of onConnect()", "connected", mspecs.size());
        counters.assertEquals("Number of connected environments", "connected", mspecs.size());
        counters.assertEquals("Number of not-connected environments", "not connected", 0);
        counters.assertEquals("Number of exceptions", "exceptions", 0);
    }

    private final static class GetConnectToActionTaskFactory implements ConcurrentTasksSupport.TaskFactory {

        private final ExecutionEnvironment env;
        private final Counters counters;

        private GetConnectToActionTaskFactory(Counters counters, ExecutionEnvironment env) {
            this.env = env;
            this.counters = counters;
        }

        @Override
        public Runnable newTask() {
            final AsynchronousAction connectToAction = ConnectionManager.getInstance().getConnectToAction(env, new Runnable() {

                @Override
                public void run() {
                    counters.getCounter("onConnect").incrementAndGet();
                    if (ConnectionManager.getInstance().isConnectedTo(env)) {
                        counters.getCounter("connected").incrementAndGet();
                    } else {
                        counters.getCounter("not connected").incrementAndGet();
                    }
                }
            });

            return new Runnable() {

                @Override
                public void run() {
                    try {
                        counters.getCounter("started").incrementAndGet();
                        connectToAction.invoke();
                    } catch (Exception ex) {
                        counters.getCounter("exception").incrementAndGet();
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
        }
    }
}
