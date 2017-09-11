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

package org.netbeans.modules.nativeexecution.test;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * This class test test framework as such -
 * NativeExecutionBaseTestSuite, NativeExecutionBaseTestCase classes,
 * ForAllEnvironments annotation, etc
 * @author Vladimir Kvashin
 */
public class NativeExecutionTestFrameworkTestCase extends NativeExecutionBaseTestCase {

    public NativeExecutionTestFrameworkTestCase(String name) {
        super(name);
    }

    public NativeExecutionTestFrameworkTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    public void testNamedSingle() {
    }

    @org.junit.Test
    public void annotatedSingle() {
    }

    @org.junit.Test
    @ForAllEnvironments(section="remote.platforms")
    public void annotatedForAllRemotePlatforms() {
    }

    @ForAllEnvironments(section="remote.platforms")
    public void testForAllRemotePlatforms() {
    }

    @org.junit.Test
    @ForAllEnvironments
    public void annotatedForAllDefault() {
    }

    @ForAllEnvironments
    public void testForAllDefault() {
    }

    @org.junit.Test
    @ForAllEnvironments(section="test.framework.test.platforms")
    public void annotatedForAllTestPlatforms() {
    }

    @ForAllEnvironments(section="test.framework.test.platforms")
    public void testForAllTestPlatforms() {
    }

    @If(section="test.conditional", key="cond-true")
    public void testConditionalTrue() {
    }

    @If(section="test.conditional", key="cond-false")
    public void testConditionalFalse() {
    }

    @If(section="test.conditional", key="cond-err")
    public void testConditionalErrValue() {
    }

    @If(section="test.conditional.inexistent", key="cond-err")
    public void testConditionalInexistentKey() {
    }

    @If(key="inexistent", section="inexistent", defaultValue=false)
    public void testConditionalDefault() {
    }

    @Ifdef(section="ifdef.section", key="ifdef.key")
    public void testIfdef() {
    }

    @Ifdef(section="ifdef.section", key="ifdef.key")
    @If(section="ifdef.section", key="ifdef.and.if.key")
    public void testIfdefAndIf() {
    }

    public static junit.framework.Test suite() {
        Class testClass = NativeExecutionTestFrameworkTestCase.class;
        return new NativeExecutionBaseTestSuite(
                testClass.getSimpleName(),
                "test.framework.test.default.platforms", testClass);
    }    
}
