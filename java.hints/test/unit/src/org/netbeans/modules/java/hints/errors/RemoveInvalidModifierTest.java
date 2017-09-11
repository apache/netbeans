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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

 /**
  *
  * @author markiewb
  */
public class RemoveInvalidModifierTest extends ErrorHintsTestBase {

    public RemoveInvalidModifierTest(String name) {
	super(name);
    }

    public void testRemovePrivate() throws Exception {
	performFixTest("test/AnotherClass.java",
		"public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            private class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}",
		-1,
		Bundle.FIX_RemoveInvalidModifier("private", 1),
		("public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}").replaceAll("[ \t\n]+", " "));
    }

    public void testRemoveProtected() throws Exception {
	performFixTest("test/AnotherClass.java",
		"public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            protected class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}",
		-1,
		Bundle.FIX_RemoveInvalidModifier("protected", 1),
		("public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}").replaceAll("[ \t\n]+", " "));
    }

    public void testRemovePublic() throws Exception {
	performFixTest("test/AnotherClass.java",
		"public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            public class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}",
		-1,
		Bundle.FIX_RemoveInvalidModifier("public", 1),
		("public class AnotherClass {\n"
		+ "    public void testMethod() {\n"
		+ "        setRunnable(new Runnable() {\n"
		+ "            public void run() {}\n"
		+ "            class MemberClass {} // quick fix for this line - remove modifier\n"
		+ "        }\n"
		+ "        );\n"
		+ "    }\n"
		+ "    private void setRunnable(Runnable runnable) {}\n"
		+ "}").replaceAll("[ \t\n]+", " "));
    }
    public void testRemoveStatic() throws Exception {
	performFixTest("test/AnotherClass.java",
		"public class AnotherClass {\n"
		+ "    public class Foo {\n"
		+ "         public static class Bar {\n"
		+ "         }\n"
		+ "    }\n"
		+ "}",
		-1,
		Bundle.FIX_RemoveInvalidModifier("static", 1),
		("public class AnotherClass {\n"
		+ "    public class Foo {\n"
		+ "         public class Bar {\n"
		+ "         }\n"
		+ "    }\n"
		+ "}").replaceAll("[ \t\n]+", " "));
    }
    
    public void testRemoveNativeFromInterface() throws Exception {
        performFixTest("test/AnotherInterface.java",
                "public native interface AnotherInterface {}",
                -1,
                Bundle.FIX_RemoveInvalidModifier("native", 1),
                ("public interface AnotherInterface {}").replaceAll("[ \t\n]+", " "));
    }
    
    public void testRemoveMultipleFromMethod() throws Exception {
        performFixTest("test/AnotherInterface.java",
                "public interface I {\n"
                + "    protected native void ttt();\n"
                + "}",
                -1,
                Bundle.FIX_RemoveInvalidModifier("protected,native", 2),
                ("public interface I {\n"
                + "    void ttt();\n"
                + "}").replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
	return new RemoveInvalidModifier().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
	return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
	return new RemoveInvalidModifier().getCodes();
    }
}