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
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishAdminInterface;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerEntity;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
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
     * Test factory functionality for GlassFish v. 2.
     * <p/>
     * Factory should initialize HTTP <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforVersionGF2() {
        GlassFishServerEntity srv = new GlassFishServerEntity();
        srv.setVersion(GlassFishVersion.GF_2);
        AdminFactory af = AdminFactory.getInstance(srv.getVersion());
        assertTrue(af instanceof AdminFactoryHttp);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (GlassFishIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be HTTP interface.
        assertTrue(runner instanceof RunnerHttp);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for GlassFish v. 3.
     * <p/>
     * Factory should initialize REST <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforVersionGF3() {
        GlassFishServerEntity srv = new GlassFishServerEntity();
        srv.setVersion(GlassFishVersion.GF_3);
        AdminFactory af = AdminFactory.getInstance(srv.getVersion());
        assertTrue(af instanceof AdminFactoryRest);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (GlassFishIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be REST interface.
        assertTrue(runner instanceof RunnerRest);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for GlassFish using REST administration
     * interface.
     * <p/>
     * Factory should initialize REST <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforAdminInterfaceRest() {
        GlassFishServerEntity srv = new GlassFishServerEntity();
        srv.setAdminInterface(GlassFishAdminInterface.REST);
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        assertTrue(af instanceof AdminFactoryRest);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (GlassFishIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be REST interface.
        assertTrue(runner instanceof RunnerRest);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

    /**
     * Test factory functionality for GlassFish  using HTTP administration
     * interface.
     * <p/>
     * Factory should initialize HTTP <code>Runner</code> and point it to
     * provided <code>Command</code> instance.
     */
    @Test
    public void testGetInstanceforAdminInterfaceHttp() {
        GlassFishServerEntity srv = new GlassFishServerEntity();
        srv.setAdminInterface(GlassFishAdminInterface.HTTP);
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        assertTrue(af instanceof AdminFactoryHttp);
        Command cmd = new CommandVersion();
        Runner runner;
        try {
          runner = af.getRunner(srv, cmd);
        } catch (GlassFishIdeException gfie) {
            runner = null;
            fail("Exception in Runner initialization: " + gfie.getMessage());
        }
        // Returned runner should be HTTP interface.
        assertTrue(runner instanceof RunnerHttp);
        // Stored command entity should be the one we supplied.
        assertTrue(cmd.equals(runner.getCommand()));
    }

}
