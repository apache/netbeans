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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;

/**
 * Testdata taken from {@link MakeVariableFinalTest}
 *
 * @author markiewb
 */
public class AddFinalModifierTest extends ErrorHintsTestBase {

    public AddFinalModifierTest(String testName) {
	super(testName);
    }

    public void testAddFinalToVariable() throws Exception {
	performFixTest("test/Test.java",
		"package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main() {"
		+ "        byte g = 9;"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(g);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_MakeVariableFinal("g"),
		("package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main() {"
		+ "        final byte g = 9;"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(g);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }
    public void testAddFinalToParameter() throws Exception {
	performFixTest("test/Test.java",
		"package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main(byte f) {"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(f);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_MakeVariableFinal("f"),
		("package org.netbeans.test.java.hints;"
		+ ""
		+ "public class MakeVariableFinal {"
		+ "    public static void main(final byte f) {"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(f);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
	return AddOrRemoveFinalModifier.createAddFinalModifier().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
	return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
	return AddOrRemoveFinalModifier.createAddFinalModifier().getCodes();
    }
}
