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
package org.netbeans.modules.payara.tooling.admin;

import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraAdminInterface;
import org.netbeans.modules.payara.tooling.data.PayaraServerEntity;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * <code>AdminFactory</code> functional tests.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class AdminFactoryTest extends CommandTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test factory functionality for Payara
     * <p/>
     * Factory should initialize HTTP <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforVersionPF4() {
        PayaraServerEntity srv = new PayaraServerEntity();
        srv.setVersion(PayaraVersion.PF_4_1_144);
        AdminFactory af = AdminFactory.getInstance(srv.getVersion());
        assertTrue(af instanceof AdminFactoryHttp);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (PayaraIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be HTTP interface.
        assertTrue(runner instanceof RunnerHttp);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for Payara
     * <p/>
     * Factory should initialize REST <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforVersionPF5() {
        PayaraServerEntity srv = new PayaraServerEntity();
        srv.setVersion(PayaraVersion.PF_5_181);
        AdminFactory af = AdminFactory.getInstance(srv.getVersion());
        assertTrue(af instanceof AdminFactoryRest);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (PayaraIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be REST interface.
        assertTrue(runner instanceof RunnerRest);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for Payara using REST administration
     * interface.
     * <p/>
     * Factory should initialize REST <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforAdminInterfaceRest() {
        PayaraServerEntity srv = new PayaraServerEntity();
        srv.setAdminInterface(PayaraAdminInterface.REST);
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        assertTrue(af instanceof AdminFactoryRest);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (PayaraIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be REST interface.
        assertTrue(runner instanceof RunnerRest);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for Payara  using HTTP administration
     * interface.
     * <p/>
     * Factory should initialize HTTP <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforAdminInterfaceHttp() {
        PayaraServerEntity srv = new PayaraServerEntity();
        srv.setAdminInterface(PayaraAdminInterface.HTTP);
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        assertTrue(af instanceof AdminFactoryHttp);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (PayaraIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be HTTP interface.
        assertTrue(runner instanceof RunnerHttp);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

}
