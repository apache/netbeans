/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHintTest extends NbTestCase {

    public EqualsHintTest(String testName) {
        super(testName);
    }

    public void testSimpleAnalysis1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:83-0:89:verifier:AE");
    }

    public void testSimpleAnalysis2() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = c.equals(s);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:100-0:106:verifier:IE");
    }

    public void testSimpleAnalysis3() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = s.equals(c);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:100-0:106:verifier:IE");
    }

    public void testSimpleAnalysis4() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = o.equals(c);}}")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testSimpleAnalysis5() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = c.equals(o);}}")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testFix1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .findWarning("0:83-0:89:verifier:AE")
                .applyFix("FIX_ReplaceWithArraysEquals")
                .assertCompilable()
                .assertOutput("package test; import java.util.Arrays; public class Test{ public void test() {int[] a = null; boolean b = Arrays.equals(a, a);}}");
    }

    public void testFix2() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .findWarning("0:83-0:89:verifier:AE")
                .applyFix("FIX_ReplaceWithInstanceEquals")
                .assertCompilable()
                .assertOutput("package test; public class Test{ public void test() {int[] a = null; boolean b = a == a;}}");
    }

    public void testAnalysis132853() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = this.equals(c, o);} private boolean equals(Object o1, Object o2) { return false; } }")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testUnresolved1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(aa);}}", false)
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testNoThis() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test { public boolean test(Integer o) {return equals(o);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:73-0:79:verifier:IE");
    }
}
