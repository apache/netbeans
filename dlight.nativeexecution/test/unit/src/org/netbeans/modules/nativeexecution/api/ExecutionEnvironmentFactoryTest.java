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
package org.netbeans.modules.nativeexecution.api;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

public class ExecutionEnvironmentFactoryTest extends NativeExecutionBaseTestCase {

    public ExecutionEnvironmentFactoryTest(String name) {
        super(name);
    }

    public ExecutionEnvironmentFactoryTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ExecutionEnvironmentFactoryTest.class);
    }

    private void doTestToFromUniqueID(ExecutionEnvironment env) {
        String id1 = ExecutionEnvironmentFactory.toUniqueID(env);
        assertNotNull("ExecutionEnvironmentFactory.toUniqueID returned null", id1);
        ExecutionEnvironment env2 = ExecutionEnvironmentFactory.fromUniqueID(id1);
        assertNotNull("ExecutionEnvironmentFactory.fromUniqueID returned null", env2);
        String id2 = ExecutionEnvironmentFactory.toUniqueID(env2);
        assertNotNull("ExecutionEnvironmentFactory.toUniqueID returned null", id2);
        assertTrue("fromUniqueID + toUniqueID resulted in non-equal IDs!", id1.equals(id2));
        assertTrue("toUniqueID + fromUniqueID resulted in non-equal objects!", env.equals(env2));
        assertTrue("equals() isn't symmetric!", env2.equals(env));
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testUniqueStringRemote() throws Exception {
        doTestToFromUniqueID(getTestExecutionEnvironment());
    }

    @org.junit.Test
    public void testUniqueStringLocal() throws Exception {
        doTestToFromUniqueID(ExecutionEnvironmentFactory.getLocal());
    }
}
