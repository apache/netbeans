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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveTest extends NbTestCase {

    public MoveTest(String name) {
        super(name);
    }
    
    public void test219932() throws Exception { // #219932 - Move initializer to constructor
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private int[] mess|age = {1,2,3,4};\n"
                + "    public Test() {\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:22-2:22:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private int[] message;\n"
                + "    public Test() {\n"
                + "        this.message = new int[]{1, 2, 3, 4};\n"
                + "    }\n"
                + "}\n");
    }

    public void testMoveToConstructor() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "    public Test() {\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToConstructors() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "    public Test() {\n"
                + "    }\n"
                + "    public Test(String something) {\n"
                + "        super();\n"
                + "    }\n"
                + "    public Test(String something, boolean replace) {\n"
                + "        if(replace) {\n"
                + "            message = something;\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "    public Test(String something) {\n"
                + "        super();\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "    public Test(String something, boolean replace) {\n"
                + "        this.message = \"Hello World!\";\n"
                + "        if(replace) {\n"
                + "            message = something;\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToNewConstructor() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToNewConstructorAnonymous222391() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    {\n"
                + "        new Runnable() {\n"
                + "            private String mess|age = \"Hello World!\";\n"
                + "            public void run() {}\n"
                + "        };\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("4:31-4:31:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    {\n"
                + "        new Runnable() {\n"
                + "            private String message;\n"
                + "            {\n"
                + "                this.message = \"Hello World!\";\n"
                + "            }\n"
                + "            public void run() {}\n"
                + "        };\n"
                + "    }\n"
                + "}\n");
    }
}
