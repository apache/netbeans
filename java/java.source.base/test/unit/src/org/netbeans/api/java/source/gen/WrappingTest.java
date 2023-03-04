/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                //System.err.println(res);
                //System.err.println(formattedRes);
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
