/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class SuspiciousToArrayTest extends NbTestCase {

    public SuspiciousToArrayTest(String name) {
        super(name);
    }

    public void testParametrizedCollection() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection<String> col = new ArrayList<String>();  \n"
                + "        Object[] arr = col.toArray(/* comment */ new Integer[/* something */ 0x6f]);  \n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:49-6:82:verifier:Suspicious Collection.toArray() call. Collection item type java.lang.String is not assignable to array component type java.lang.Integer").
                findWarning("6:49-6:82:verifier:Suspicious Collection.toArray() call. Collection item type java.lang.String is not assignable to array component type java.lang.Integer").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection<String> col = new ArrayList<String>();  \n"
                + "        Object[] arr = col.toArray(/* comment */ new String[/* something */ 0x6f]);  \n"
                + "    }\n"
                + "}");
    }

    public void testOuterTypeCast() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[] arr = (Integer[])col.toArray(/* comment */ new  String[/* something */ 0x6f]);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:61-6:94:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[] is not the same as casted-to type java.lang.Integer[]").
                findWarning("6:61-6:94:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[] is not the same as casted-to type java.lang.Integer[]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[] arr = (Integer[])col.toArray(/* comment */ new  Integer[/* something */ 0x6f]);\n"
                + "    }\n"
                + "}");
    }

    public void testChangeMultidimArrayTypeFix() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ new  String[/* something */ 0x6f][]);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:65-6:100:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("6:65-6:100:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ new  Integer[/* something */ 0x6f][]);\n"
                + "    }\n"
                + "}");
    }

    /**
     * requires fix for #236893 public void testChangeDifferentDimensionFix() throws Exception { }
     */
    public void testReplaceExpressionFix() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ x/* something */);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("7:65-7:66:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("7:65-7:66:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])col.toArray( /* comment */ new Integer[col.size()][] /* something */ );\n"
                + "    }\n"
                + "}");
    }

    /**
     * Checks that the fix that just replaces expression is not available.
     *
     * @throws Exception
     */
    public void testCollectionWithSideEffects() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    private static int i;\n"
                + "\n"
                + "    private static Collection c() { i++; return null; }\n"
                + "    public static void main(String[] args) {\n"
                + "        String[][] a = new String[7][];\n"
                + "        Integer[][] arr = (Integer[][])c().toArray( /* comment */ a /* something */ );\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("8:66-8:67:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("8:66-8:67:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                assertFixes();
    }
    
    /**
     * Checks that the hint applies on the collection itself
     * @throws Exception 
     */
    public void testExtendsCollection() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test extends ArrayList {\n"
                + "    public void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])toArray(/* comment */ x/* something */);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class)
                .findWarning("7:61-7:62:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]")
                .applyFix()
                .assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test extends ArrayList {\n"
                + "    public void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])toArray( /* comment */ new Integer[size()][] /* something */ );\n"
                + "    }\n"
                + "}");
        
    }
    
    public void testDoNotReportWildcards() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.Collection;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    class Item {\n" +
                    "    }\n" +
                    "    static Item[] gen(Collection<? extends Item> col) {\n" +
                    "        Item[] items = col.toArray(new Item[0]);  //<<<<<< hint\n" +
                    "        return items;\n" +
                    "    }\n" +
                    "}"
                )
                .run(SuspiciousToArray.class)
                .assertWarnings();
    }
}
