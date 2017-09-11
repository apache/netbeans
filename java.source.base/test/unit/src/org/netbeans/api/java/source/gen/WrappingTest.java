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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileUtil;

/**
 * Tests correct wrapping of statements.
 *
 * @author Pavel Flaska
 */
public class WrappingTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public WrappingTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(WrappingTest.class);
        return suite;
    }

    public void testVariableInitWrapped() throws Exception {
        String code = "package hierbas.del.litoral;\n\n" +
            "import java.util.concurrent.atomic.AtomicBoolean;\n\n" +
            "public class Test {\n" +
            "    public void t() {\n" +
            "        new AtomicBoolean();\n" + 
            "    }\n" +
            "}\n";
        runWrappingTest(code, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree init = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                VariableTree nue = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "ab", make.Type("java.util.concurrent.atomic.AtomicBoolean"), init.getExpression());
                workingCopy.rewrite(init, nue);
            }
        }, FmtOptions.wrapAssignOps, WrapStyle.WRAP_IF_LONG.name());
    }
    
    public void testWrapAssignment() throws Exception {
        String code = "package hierbas.del.litoral;\n\n" +
            "import java.util.concurrent.atomic.AtomicBoolean;\n\n" +
            "public class Test {\n" +
            "    public void t(AtomicBoolean ab) {\n" +
            "        new AtomicBoolean();\n" + 
            "    }\n" +
            "}\n";
        runWrappingTest(code, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree init = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree bt = make.Assignment(make.Identifier("ab"), init.getExpression());
                workingCopy.rewrite(init, make.ExpressionStatement(bt));
            }
        }, FmtOptions.wrapAssignOps, WrapStyle.WRAP_IF_LONG.name());
    }
    
    public void testWrapMethod1() throws Exception {
        String code = "package hierbas.del.litoral;\n\n" +
            "import java.util.concurrent.atomic.AtomicBoolean;\n\n" +
            "public class Test {\n" +
            "}\n";
        runWrappingTest(code, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree parsed = workingCopy.getTreeUtilities().parseExpression("new Object() { private void test(int a, int b, int c) throws java.io.FileNotFound, java.net.MalformedURLException { } }", new SourcePositions[1]);
                parsed = GeneratorUtilities.get(workingCopy).importFQNs(parsed);
                MethodTree method = (MethodTree) ((NewClassTree) parsed).getClassBody().getMembers().get(0);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
            }
        },
        FmtOptions.wrapMethodParams, WrapStyle.WRAP_IF_LONG.name(),
        FmtOptions.wrapThrowsKeyword, WrapStyle.WRAP_IF_LONG.name(),
        FmtOptions.wrapThrowsList, WrapStyle.WRAP_IF_LONG.name());
    }
    
    public void testWrapMethod2() throws Exception {
        String code = "package hierbas.del.litoral;\n\n" +
            "import java.util.concurrent.atomic.AtomicBoolean;\n\n" +
            "public class Test {\n" +
            "}\n";
        runWrappingTest(code, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree parsed = workingCopy.getTreeUtilities().parseExpression("new Object() { private void test(int a, int b, int c) throws java.io.FileNotFoundException, java.net.MalformedURLException { } }", new SourcePositions[1]);
                parsed = GeneratorUtilities.get(workingCopy).importFQNs(parsed);
                MethodTree method = (MethodTree) ((NewClassTree) parsed).getClassBody().getMembers().get(0);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
            }
        },
        FmtOptions.wrapMethodParams, WrapStyle.WRAP_ALWAYS.name(),
        FmtOptions.wrapThrowsKeyword, WrapStyle.WRAP_ALWAYS.name(),
        FmtOptions.wrapThrowsList, WrapStyle.WRAP_ALWAYS.name());
    }
    
    public void testWrapMethod3() throws Exception {
        String code = "package hierbas.del.litoral;\n\n" +
            "import java.util.concurrent.atomic.AtomicBoolean;\n\n" +
            "public class Test {\n" +
            "}\n";
        runWrappingTest(code, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree parsed = workingCopy.getTreeUtilities().parseExpression("new Object() { public <T> void test(java.lang.Class<T> c) { } }", new SourcePositions[1]);
                parsed = GeneratorUtilities.get(workingCopy).importFQNs(parsed);
                MethodTree method = (MethodTree) ((NewClassTree) parsed).getClassBody().getMembers().get(0);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
            }
        },
        FmtOptions.wrapMethodParams, WrapStyle.WRAP_ALWAYS.name(),
        FmtOptions.wrapThrowsKeyword, WrapStyle.WRAP_ALWAYS.name(),
        FmtOptions.wrapThrowsList, WrapStyle.WRAP_ALWAYS.name());
    }
    
    private void runWrappingTest(String code, Task<WorkingCopy> task, String... settings) throws Exception {
        String[] augmentedSettings = Arrays.copyOf(settings, settings.length + 4);
        
        augmentedSettings[settings.length + 0] = FmtOptions.blankLinesAfterClassHeader;
        augmentedSettings[settings.length + 1] = "0";
        augmentedSettings[settings.length + 2] = FmtOptions.blankLinesBeforeMethods;
        augmentedSettings[settings.length + 3] = "0";
        
        Map<String, String> originalSettings = alterSettings(augmentedSettings);
        
        try {
            for (int m = 1; m < 200; m++) {
                alterSettings(FmtOptions.rightMargin, Integer.toString(m));
                testFile = new File(getWorkDir(), "Test.java");
                TestUtilities.copyStringToFile(testFile, code);
                JavaSource src = getJavaSource(testFile);

                src.runModificationTask(task).commit();
                String res = TestUtilities.copyFileToString(testFile);
                String formattedRes = Reformatter.reformat(res.replaceAll("[\\s]+", " "), CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
                System.err.println(res);
                System.err.println(formattedRes);
                assertEquals("margin=" + m, formattedRes, res);
            }
        } finally {
            reset(originalSettings);
        }
    }
    
    private Map<String, String> alterSettings(String... settings) {
        Map<String, String> adjustPreferences = new HashMap<String, String>();
        for (int i = 0; i < settings.length; i += 2) {
            adjustPreferences.put(settings[i], settings[i + 1]);
        }
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        Map<String, String> origValues = new HashMap<String, String>();
        for (String key : adjustPreferences.keySet()) {
            origValues.put(key, preferences.get(key, null));
        }
        setValues(preferences, adjustPreferences);
        return origValues;
    }
    
    private void reset(Map<String, String> values) {
        setValues(MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class), values);
    }
    
    private void setValues(Preferences p, Map<String, String> values) {
        for (Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                p.put(e.getKey(), e.getValue());
            } else {
                p.remove(e.getKey());
            }
        }
    }
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    
}
