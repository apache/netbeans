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
 * Based on {@link AddCastTest}
 *
 * @author markiewb
 */
public class RemoveFinalModifierFromVariableTest extends ErrorHintsTestBase {

    public RemoveFinalModifierFromVariableTest(String testName) {
	super(testName);
    }

    public void testRemoveFinalFromLocalVariable() throws Exception {
	performFixTest("test/Test.java",
		"package test;"
		+ "public class Test {"
		+ "     public static void main1() {"
		+ "         final int localvar = 1;"
		+ "         localvar = 2;"
		+ "     }"
		+ "}",
		-1,
		Bundle.FIX_RemoveFinalModifierFromVariable("localvar"),
		("package test;"
		+ "public class Test {"
		+ "     public static void main1() {"
		+ "         int localvar = 1;"
		+ "         localvar = 2;"
		+ "     }"
		+ "}").replaceAll("[\\s]+", " "));
    }

    public void testRemoveFinalFromClassMemberVariable() throws Exception {
	performFixTest("test/Test.java",
		"package test;"
		+ "public class Test {"
		+ "    final static int member = 1;"
		+ "    public static void main1() {"
		+ "        member = 2;"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_RemoveFinalModifierFromVariable("member"),
		("package test;"
		+ "public class Test {"
		+ "    static int member = 1;"
		+ "    public static void main1() {"
		+ "        member = 2;"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }

    public void testRemoveFinalFromStaticClassMemberVariable() throws Exception {
	performFixTest("test/Test.java",
		"package test;"
		+ "public class Test {"
		+ "    static final int staticmember = 1;"
		+ "    public static void main1() {"
		+ "        staticmember = 2;"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_RemoveFinalModifierFromVariable("staticmember"),
		("package test;"
		+ "public class Test {"
		+ "    static int staticmember = 1;"
		+ "    public static void main1() {"
		+ "        staticmember = 2;"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
	return AddOrRemoveFinalModifier.createRemoveFinalFromVariable().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
	return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
	return AddOrRemoveFinalModifier.createRemoveFinalFromVariable().getCodes();
    }
}
