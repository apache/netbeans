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
package org.netbeans.modules.nativeexecution.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class MacroExpanderFactoryTest extends NativeExecutionBaseTestCase {

    public MacroExpanderFactoryTest(String name) {
        super(name);
    }

    public MacroExpanderFactoryTest(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(MacroExpanderFactoryTest.class);
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
     * Test of getExpander method, of class MacroExpanderFactory.
     */
    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetExpander_ExecutionEnvironment_String() {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String id = "--- getExpander for " + execEnv.toString() + " ---"; // NOI18N

        System.out.println(id + " started.");
        try {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                try {
                    ConnectionManager.getInstance().connectTo(execEnv);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    fail("Failed to connect to " + execEnv.getDisplayName());
                }
            }

            assertTrue(execEnv.getDisplayName() + " must be connected", ConnectionManager.getInstance().isConnectedTo(execEnv));

            MacroExpander expander = MacroExpanderFactory.getExpander(execEnv);//, "SunStudio"); // NOI18N

            Map<String, String> myenv = new HashMap<>();
            try {
                myenv.put("PATH", expander.expandMacros("/bin:$PATH", myenv)); // NOI18N
                myenv.put("PATH", expander.expandMacros("/usr/bin:$platform:$PATH", myenv)); // NOI18N
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                fail("Unable to parse string to expand: " + ex.getMessage());
            }

            System.out.println(myenv.toString());

            String mspec = NativeExecutionTestSupport.getMspec(execEnv);

            String os;

            int mpos = mspec.indexOf('-');

            if (mpos > 0) {
                os = mspec.substring(mpos + 1);
                if (os.equals("S2")) {
                    os = "SunOS";
                }
            } else {
                os = "Cannot guess OS from mspec";
            }

            try {
                String pattern = "$osname-${platform}$_isa";
                String result = expander.expandPredefinedMacros(pattern);
                System.out.println(pattern + " -> " + result); // NOI18N
                assertTrue(result + " should be started with " + os, result.startsWith(os));
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                fail("Unable to parse string to expand: " + ex.getMessage());
            }
        } finally {
            System.out.println(id + " finished");
        }
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testPath() {
        doTestPath(getTestExecutionEnvironment());
    }

    @org.junit.Test
    public void testPathLocal() {
        doTestPath(ExecutionEnvironmentFactory.getLocal());
    }

    private void doTestPath(final ExecutionEnvironment execEnv) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("env"); // NOI18N
        npb.redirectError();
        MacroMap env = npb.getEnvironment();
        env.prependPathVariable("PATH", "/firstPath"); // NOI18N
        env.appendPathVariable("PATH", "${ZZZ}_${platform}"); // NOI18N
        env.appendPathVariable("PATH", "/secondPath"); // NOI18N
        env.put("XXX", "It WORKS!"); // NOI18N

        ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);

        List<String> pout = res.getOutputLines();
        int ok = 0;

        for (String line : pout) {
            if (line.startsWith("PATH")) {
                if (line.contains("firstPath") && line.contains("secondPath")) {
                    ok++;
                }
            } else if (line.startsWith("XXX")) {
                if (line.contains("It WORKS!")) {
                    ok++;
                }
            }
        }

        assertEquals(0, res.exitCode);

        assertEquals(2, ok);
    }
}
