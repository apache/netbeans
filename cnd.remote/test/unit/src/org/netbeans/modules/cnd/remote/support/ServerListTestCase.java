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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import org.junit.AfterClass;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ServerListTestCase extends RemoteTestBase {

    public ServerListTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    @ForAllEnvironments
    public void testAdd() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerRecord rec = ServerList.get(execEnv);
        assertNotNull("Null server record", rec);
        assertEquals(rec.getExecutionEnvironment(), execEnv);
        assertEquals(rec.getExecutionEnvironment(), execEnv);
    }

    @AfterClass
    public void cleanup() {
        ServerRecord local = ServerList.get(ExecutionEnvironmentFactory.getLocal());
        ServerList.set(Arrays.asList(local), local);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerList.addServer(execEnv, execEnv.getDisplayName(), RemoteSyncFactory.getDefault(), false, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
        cleanup();
    }
            

    @ForAllEnvironments
    public void testGetEnvironments() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertTrue("getEnvironments should contain " + execEnv, ServerList.getEnvironments().contains(execEnv));
    }

    @ForAllEnvironments
    public void testGetRecords() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        Collection<? extends ServerRecord> records = ServerList.getRecords();
        for (ServerRecord rec : records) {
            if (execEnv.equals(rec.getExecutionEnvironment())) {
                return;
            }
        }        
        assertTrue("getRecords should contain " + execEnv, false);
    }

    @ForAllEnvironments
    public void testDefaultRecord() throws Exception {
        Collection<? extends ServerRecord> tcoll = ServerList.getRecords();
        ServerRecord[] records = tcoll.toArray(new ServerRecord[tcoll.size()]);
        for (int i = 0; i < records.length; i++) {
            ServerRecord rec = records[i];
            ServerList.setDefaultRecord(rec);
            assertTrue(ServerList.getDefaultRecord().equals(rec));
            assertTrue(ServerList.getDefaultRecord().getExecutionEnvironment().equals(rec.getExecutionEnvironment()));
        }
    }

    private void dumpRecords(String title) {
        if (log.isLoggable(Level.FINEST)) {
            Collection<? extends ServerRecord> records = ServerList.getRecords();
            System.err.printf("RECORDS %s:\n", title);
            for (ServerRecord rec : records) {
                System.err.printf("%s\n", rec);
                log.finest("" + rec);
            }
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(ServerListTestCase.class);
    }
}
