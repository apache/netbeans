/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
