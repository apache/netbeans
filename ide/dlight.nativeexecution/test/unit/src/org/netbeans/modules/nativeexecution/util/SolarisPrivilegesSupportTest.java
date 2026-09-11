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
package org.netbeans.modules.nativeexecution.util;

import java.io.IOException;
import java.util.Arrays;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupportProvider;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
@SuppressWarnings("removal")
public class SolarisPrivilegesSupportTest extends NativeExecutionBaseTestCase {

    public SolarisPrivilegesSupportTest(String name) {
        super(name);
    }

    public SolarisPrivilegesSupportTest(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(SolarisPrivilegesSupportTest.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getInstance method, of class SolarisPrivilegesSupportImpl.
     */
    @org.junit.Test
    @ForAllEnvironments(section = "dlight.nativeexecution.sps")
    public void test() {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        try {
            ConnectionManager.getInstance().connectTo(execEnv);
            SolarisPrivilegesSupport sps = SolarisPrivilegesSupportProvider.getSupportFor(execEnv);
            System.out.println(sps.getExecutionPrivileges());
            try {
                sps.requestPrivileges(Arrays.asList("dtrace_kernel"), true); // NOI18N
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SolarisPrivilegesSupport.NotOwnerException ex) {
                System.out.println(ex);
            }
            System.out.println(sps.getExecutionPrivileges());
        } catch (IOException | CancellationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
