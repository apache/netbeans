/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CollectionRemoveTest extends NbTestCase {

    public CollectionRemoveTest(String name) {
        super(name);
    }
    
    @Override
    protected int timeOut() {
        return 30000;
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:128:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:130:verifier:SC: java.util.Collection.contains, Object, String");
    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:134:verifier:SCIT: java.util.Collection.remove, Integer, String");
    }

    public void testSimple4() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:136:verifier:SCIT: java.util.Collection.contains, Integer, String");
    }

    public void testSimple5() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.Map<String, Number> l = null;" +
                       "        l.containsKey(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:113-0:146:verifier:SCIT: java.util.Map.containsKey, Integer, String");
    }

    public void testSimple6() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.Map<String, Number> l = null;" +
                       "        l.containsValue(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:113-0:132:verifier:SCIT: java.util.Map.containsValue, String, Number");
    }

    public void testExtends1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.LinkedList<String> {" +
                       "    private void test () {" +
                       "        remove(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:103-0:123:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testExtends2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.LinkedList<String> {" +
                       "    private void test () {" +
                       "        new Runnable() {" +
                       "            public void run() {" +
                       "                remove(new Object());" +
                       "            }" +
                       "        };" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:166-0:186:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testBoxing1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<Integer> l = null;" +
                       "        l.contains(1);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testBoxing2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(1);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:119:verifier:SCIT: java.util.Collection.contains, int, String");
    }

    public void testExtendsWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<? extends String> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testExtendsWildcard2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test (boolean b) {" +
                       "        test(get().contains(\"\"));\n" +
                       "    }\n" +
                       "    private java.util.List<? extends String> get() {return null;}\n" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testSuperWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<? super String> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<?> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testCollectionRemoveInteger() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(0);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testCollectionRemoveInteger2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.subList(0, 0).remove(0);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testInsideItsOverride196606() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.ArrayList<String> {" +
                       "    @Override public boolean remove(Object o) {" +
                       "        return super.remove(o);\n" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    static {
        NbBundle
                .setBranding("test");
    }
}
