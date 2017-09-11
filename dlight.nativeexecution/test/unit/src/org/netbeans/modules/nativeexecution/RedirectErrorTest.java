/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import junit.framework.Test;
import org.junit.After;
import org.junit.Before;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author akrasny
 */
public final class RedirectErrorTest extends NativeExecutionBaseTestCase {

    public RedirectErrorTest(String name) {
        super(name);
    }

    public RedirectErrorTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(RedirectErrorTest.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            ConnectionManager.getInstance().connect(env);
        }
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            ConnectionManager.getInstance().disconnect(env);
        }
        super.tearDown();
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testRedirectError_remote() throws Exception {
        doTestRedirectError(getTestExecutionEnvironment());
    }

    @org.junit.Test
    public void testRedirectError_local() throws Exception {
        doTestRedirectError(ExecutionEnvironmentFactory.getLocal());
    }

    private void doTestRedirectError(ExecutionEnvironment env, boolean cmdLine, boolean tty, boolean redirectError) {
        if (env == null) {
            return;
        }

        System.out.println();
        System.out.println("RedirectErrorTest @ " + env.getDisplayName()); // NOI18N
        System.out.println("\tconfigure builder using " + (cmdLine ? "setCommandLine()" : "setExecutable()")); // NOI18N
        System.out.println("\tconfigure builder " + (redirectError ? "" : "not ") + "to do redirectError()"); // NOI18N
        System.out.println("\tconfigure builder " + (tty ? "" : "not ") + "to be started in a pseudo-terminal"); // NOI18N

        boolean errorRedirect = (tty || redirectError) ? true : false;
        System.out.println("Extected that error goes to " + (errorRedirect ? "output" : "error")); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

        if (cmdLine) {
            npb.setCommandLine("wrong"); // NOI18N
        } else {
            npb.setExecutable("wrong"); // NOI18N
        }

        npb.setUsePty(tty);

        if (redirectError) {
            npb.redirectError();
        }

        ExitStatus status = ProcessUtils.execute(npb);
        System.out.println("Result is: "); // NOI18N
        System.out.println(status.toString());

        if (errorRedirect) {
            assertTrue("Output is expected to be in the output stream", !status.getOutputString().isEmpty() && status.getErrorString().isEmpty()); // NOI18N
        } else {
            assertTrue("Output is expected to be in the error stream", status.getOutputString().isEmpty() && !status.getErrorString().isEmpty()); // NOI18N
        }
    }

    private void doTestRedirectError(ExecutionEnvironment env) {
        doTestRedirectError(env, false, false, false);
        doTestRedirectError(env, false, false, true);
        doTestRedirectError(env, false, true, false);
        doTestRedirectError(env, false, true, true);
        doTestRedirectError(env, true, false, false);
        doTestRedirectError(env, true, false, true);
        doTestRedirectError(env, true, true, false);
        doTestRedirectError(env, true, true, true);
    }
}
