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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class RemoveUselessCastTest extends ErrorHintsTestBase {
    
    public RemoveUselessCastTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestCompilerSettings.commandLine = "-Xlint:cast";
    }
    
    public void testRedundantCast1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = |(String) get(String.class);\n" +
                       "    }\n" +
                       "    public <T> T get(Class<T> c) {\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = get(String.class);\n" +
                       "    }\n" +
                       "    public <T> T get(Class<T> c) {\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testRedundantCastRemoveParentheses1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = |(String) (\"a\" + \"b\");\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = \"a\" + \"b\";\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testRedundantCastRemoveParentheses2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        Integer b = new Integer(18);\n" +
                       "        int a = (|(Integer) b).intValue();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        Integer b = new Integer(18);\n" +
                       "        int a = b.intValue();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new RemoveUselessCast().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

}
