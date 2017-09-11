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
public class BoxedIdentityComparisonTest extends NbTestCase {

    public BoxedIdentityComparisonTest(String name) {
        super(name);
    }
    
    public void testBoxedEquals() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" + 
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a == b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }
    
    public void testBoxedNotEquals() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (!Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }
    
    public void testNoFixInSource6() throws Exception {
        HintTest.create().sourceLevel("1.6")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").assertFixes();
    }
    
    public void testBoxedEqualsWithPrimitiveRight() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        int b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings(
                );
    }

    public void testBoxedEqualsWithPrimitiveLeft() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        int a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings(
                );
    }
}
