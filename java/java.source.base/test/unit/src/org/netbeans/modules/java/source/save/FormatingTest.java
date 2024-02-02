/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.*;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.code.Flags;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.java.ui.FmtOptions;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test different formatting options
 *
 * @author Dusan Balek
 */
public class FormatingTest extends NbTestCase {

    File testFile = null;
    private String sourceLevel = "1.8";
    private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

    /** Creates a new instance of FormatingTest */
    public FormatingTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FormatingTest.class);
//        suite.addTest(new FormatingTest("testLabelled"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        ClassPathProvider cpp = new ClassPathProvider() {

            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                if (type.equals(ClassPath.SOURCE)) {
                    return ClassPathSupport.createClassPath(new FileObject[]{FileUtil.toFileObject(getDataDir())});
                }
                if (type.equals(ClassPath.COMPILE)) {
                    return ClassPathSupport.createClassPath(new FileObject[0]);
                }
                if (type.equals(ClassPath.BOOT)) {
                    return BootClassPathUtil.getBootClassPath();
                }
                return null;
            }
        };
        SourceLevelQueryImplementation slqi = file -> sourceLevel;
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/source/resources/layer.xml","org/netbeans/modules/java/source/base/layer.xml"}, new Object[]{loader, slqi/*, cpp*/});
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class).clear();
    }

    public void testClass() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, " ");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[]{0};
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putInt("text-limit-width", 30);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                MethodTree method = maker.Method(maker.Modifiers(EnumSet.of(Modifier.PUBLIC)), "run", maker.Identifier("void"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                List<Tree> impl = new ArrayList<Tree>();
                impl.add(maker.Identifier("Runnable"));
                impl.add(maker.Identifier("Serializable"));
                ClassTree clazz = maker.Class(maker.Modifiers(Collections.<Modifier>emptySet()), "Test" + counter[0]++, Collections.<TypeParameterTree>emptyList(), maker.Identifier("Integer"), impl, Collections.singletonList(method));
                if (counter[0] == 1) {
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.CompilationUnit(maker.Identifier("hierbas.del.litoral"), Collections.<ImportTree>emptyList(), Collections.singletonList(clazz), workingCopy.getCompilationUnit().getSourceFile()));
                } else {
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.addCompUnitTypeDecl(workingCopy.getCompilationUnit(), clazz));
                }
            }
        };
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentTopLevelClassMembers", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentTopLevelClassMembers", true);

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", true);
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", false);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_NEVER.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test0 extends Integer implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test1 extends Integer implements Runnable, Serializable{\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test2 extends Integer implements Runnable, Serializable\n"
                + "{\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test3 extends Integer implements Runnable, Serializable\n"
                + "  {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "  }\n\n"
                + "class Test4 extends Integer implements Runnable, Serializable\n"
                + "    {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "    }\n\n"
                + "class Test5 extends Integer implements Runnable, Serializable {\n\n"
                + "public void run() {\n"
                + "}\n"
                + "}\n\n"
                + "class Test6 extends Integer\n"
                + "        implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test7\n"
                + "        extends Integer\n"
                + "        implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test8\n"
                + "        extends Integer\n"
                + "        implements Runnable,\n"
                + "        Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test9\n"
                + "        extends Integer\n"
                + "        implements Runnable,\n"
                + "                   Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test10 extends Integer implements Runnable,\n"
                + "                                        Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n\n"
                + "class Test11 extends Integer implements Runnable,\n"
                + "        Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "class Test extends Integer implements Runnable, Serializable{"
                + "public void run(){"
                + "}"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable{\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable\n"
                + "{\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable\n"
                + "  {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "  }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable\n"
                + "    {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "    }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable, Serializable {\n\n"
                + "public void run() {\n"
                + "}\n"
                + "}\n";
        preferences.putBoolean("indentTopLevelClassMembers", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentTopLevelClassMembers", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer\n"
                + "        implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test\n"
                + "        extends Integer\n"
                + "        implements Runnable, Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test\n"
                + "        extends Integer\n"
                + "        implements Runnable,\n"
                + "        Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test\n"
                + "        extends Integer\n"
                + "        implements Runnable,\n"
                + "                   Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignMultilineImplements", true);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable,\n"
                + "                                      Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        preferences.putInt("text-limit-width", 50);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "class Test extends Integer implements Runnable,\n"
                + "        Serializable {\n\n"
                + "    public void run() {\n"
                + "    }\n"
                + "}\n";

        preferences.putBoolean("alignMultilineImplements", false);
        reformat(doc, content, golden);
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putInt("text-limit-width", 80);
    }

    public void testEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, " ");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[]{0};
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putInt("text-limit-width", 20);
        preferences.putInt("blankLinesAfterEnumHeader", 1);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                String name = "Test" + counter[0]++;
                List<Tree> members = new ArrayList<Tree>();
                ModifiersTree mods = maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL | Flags.ENUM, Collections.<AnnotationTree>emptyList());
                IdentifierTree type = maker.Identifier(name);
                List<ExpressionTree> empty = Collections.<ExpressionTree>emptyList();
                members.add(maker.Variable(mods, "NORTH", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "EAST", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "SOUTH", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "WEST", type, maker.NewClass(null, empty, type, empty, null)));
                ClassTree clazz = maker.Enum(maker.Modifiers(Collections.<Modifier>emptySet()), name, Collections.<Tree>emptyList(), members);
                if (counter[0] == 1) {
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.CompilationUnit(maker.Identifier("hierbas.del.litoral"), Collections.<ImportTree>emptyList(), Collections.singletonList(clazz), workingCopy.getCompilationUnit().getSourceFile()));
                } else {
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.addCompUnitTypeDecl(workingCopy.getCompilationUnit(), clazz));
                }
            }
        };
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentTopLevelClassMembers", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentTopLevelClassMembers", true);

        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_NEVER.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test0 {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n\n"
                + "enum Test1{\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n\n"
                + "enum Test2\n"
                + "{\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n\n"
                + "enum Test3\n"
                + "  {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "  }\n\n"
                + "enum Test4\n"
                + "    {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "    }\n\n"
                + "enum Test5 {\n\n"
                + "NORTH, EAST, SOUTH, WEST\n"
                + "}\n\n"
                + "enum Test6 {\n\n"
                + "    NORTH, EAST,\n"
                + "    SOUTH, WEST\n"
                + "}\n\n"
                + "enum Test7 {\n\n"
                + "    NORTH,\n"
                + "    EAST,\n"
                + "    SOUTH,\n"
                + "    WEST\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "enum Test{"
                + "NORTH,EAST,SOUTH,WEST"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test{\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test\n"
                + "{\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "}\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test\n"
                + "  {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "  }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test\n"
                + "    {\n\n"
                + "    NORTH, EAST, SOUTH, WEST\n"
                + "    }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test {\n\n"
                + "NORTH, EAST, SOUTH, WEST\n"
                + "}\n";
        preferences.putBoolean("indentTopLevelClassMembers", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentTopLevelClassMembers", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test {\n\n"
                + "    NORTH, EAST,\n"
                + "    SOUTH, WEST\n"
                + "}\n";
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "enum Test {\n\n"
                + "    NORTH,\n"
                + "    EAST,\n"
                + "    SOUTH,\n"
                + "    WEST\n"
                + "}\n";
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putInt("text-limit-width", 80);
    }

    public void testMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[]{0};
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = maker.Modifiers(Collections.<Modifier>emptySet());
                MethodTree method = maker.Method(mods, "test" + counter[0]++, maker.Identifier("int"), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(maker.Variable(mods, "i", maker.Identifier("int"), null)), Collections.<ExpressionTree>emptyList(), "{return i;}", null);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, method));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeMethodDeclParen", true);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeMethodDeclParen", false);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", true);

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test0(int i) {\n"
                + "        return i;\n"
                + "    }\n\n"
                + "    int test1 ( int i ){\n"
                + "        return i;\n"
                + "    }\n\n"
                + "    int test2(int i)\n"
                + "    {\n"
                + "        return i;\n"
                + "    }\n\n"
                + "    int test3(int i)\n"
                + "      {\n"
                + "        return i;\n"
                + "      }\n\n"
                + "    int test4(int i)\n"
                + "        {\n"
                + "        return i;\n"
                + "        }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "int test(int i){"
                + "return i;"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test(int i) {\n"
                + "        return i;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test ( int i ){\n"
                + "        return i;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeMethodDeclParen", true);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeMethodDeclParen", false);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test(int i)\n"
                + "    {\n"
                + "        return i;\n"
                + "    }\n"
                + "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test(int i)\n"
                + "      {\n"
                + "        return i;\n"
                + "      }\n"
                + "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    int test(int i)\n"
                + "        {\n"
                + "        return i;\n"
                + "        }\n"
                + "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }

    public void testStaticBlock() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[]{0};
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                BlockTree block = maker.Block(Collections.<StatementTree>emptyList(), true);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, block));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    static {\n"
                + "    }\n"
                + "    static{\n"
                + "    }\n"
                + "    static\n"
                + "    {\n"
                + "    }\n"
                + "    static\n"
                + "      {\n"
                + "      }\n"
                + "    static\n"
                + "        {\n"
                + "        }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "static{"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    static {\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    static{\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    static\n"
                + "    {\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    static\n"
                + "      {\n"
                + "      }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    static\n"
                + "        {\n"
                + "        }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }

    public void testFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_NEVER.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_ALWAYS.name());

        preferences.put("wrapFor", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineFor", true);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapFor", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("alignMultilineFor", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        for( int i = 0; i < 10; i++ ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");\n"
                + "        for (int i = 0;\n"
                + "                i < 10;\n"
                + "                i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        for (int i = 0;\n"
                + "             i < 10;\n"
                + "             i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(){"
                + "for(int i=0;i<10;i++)"
                + "System.out.println(\"TRUE\");"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for( int i = 0; i < 10; i++ ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++)\n"
                + "            System.out.println(\"TRUE\");\n"
                + "    }\n"
                + "}\n";
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_NEVER.name());
        reformat(doc, content, golden);
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_ALWAYS.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0;\n"
                + "                i < 10;\n"
                + "                i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapFor", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        for (int i = 0;\n"
                + "             i < 10;\n"
                + "             i++) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignMultilineFor", true);
        reformat(doc, content, golden);
        preferences.put("wrapFor", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("alignMultilineFor", false);
    }

    public void testForEach() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(String[] args) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "for (String s : args) System.out.println(s);";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args) {\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "        for( String s : args ){\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "        for (String s : args)\n"
                + "        {\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "        for (String s : args)\n"
                + "          {\n"
                + "            System.out.println(s);\n"
                + "          }\n"
                + "        for (String s : args)\n"
                + "            {\n"
                + "            System.out.println(s);\n"
                + "            }\n"
                + "        for (String s : args)\n"
                + "            System.out.println(s);\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(String[] args){"
                + "for(String s:args)"
                + "System.out.println(s);"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args) {\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for( String s : args ){\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args)\n"
                + "        {\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args)\n"
                + "          {\n"
                + "            System.out.println(s);\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args)\n"
                + "            {\n"
                + "            System.out.println(s);\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(String[] args) {\n"
                + "        for (String s : args)\n"
                + "            System.out.println(s);\n"
                + "    }\n"
                + "}\n";
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
    }

    public void testIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "if (a) System.out.println(\"A\") else if (b) System.out.println(\"B\") else System.out.println(\"NONE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeIfParen", false);
        preferences.putBoolean("spaceWithinIfParens", true);
        preferences.putBoolean("spaceBeforeIfLeftBrace", false);
        preferences.putBoolean("spaceBeforeElse", false);
        preferences.putBoolean("spaceBeforeElseLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeIfParen", true);
        preferences.putBoolean("spaceWithinIfParens", false);
        preferences.putBoolean("spaceBeforeIfLeftBrace", true);
        preferences.putBoolean("spaceBeforeElse", true);
        preferences.putBoolean("spaceBeforeElseLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeElseOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeElseOnNewLine", false);

        preferences.putBoolean("specialElseIf", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("specialElseIf", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else if (b) {\n"
                + "            System.out.println(\"B\");\n"
                + "        } else {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "        if( a ){\n"
                + "            System.out.println(\"A\");\n"
                + "        }else if( b ){\n"
                + "            System.out.println(\"B\");\n"
                + "        }else{\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "        if (a)\n"
                + "        {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else if (b)\n"
                + "        {\n"
                + "            System.out.println(\"B\");\n"
                + "        } else\n"
                + "        {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "        if (a)\n"
                + "          {\n"
                + "            System.out.println(\"A\");\n"
                + "          } else if (b)\n"
                + "          {\n"
                + "            System.out.println(\"B\");\n"
                + "          } else\n"
                + "          {\n"
                + "            System.out.println(\"NONE\");\n"
                + "          }\n"
                + "        if (a)\n"
                + "            {\n"
                + "            System.out.println(\"A\");\n"
                + "            } else if (b)\n"
                + "            {\n"
                + "            System.out.println(\"B\");\n"
                + "            } else\n"
                + "            {\n"
                + "            System.out.println(\"NONE\");\n"
                + "            }\n"
                + "        if (a)\n"
                + "            System.out.println(\"A\");\n"
                + "        else if (b)\n"
                + "            System.out.println(\"B\");\n"
                + "        else\n"
                + "            System.out.println(\"NONE\");\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        }\n"
                + "        else if (b) {\n"
                + "            System.out.println(\"B\");\n"
                + "        }\n"
                + "        else {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else {\n"
                + "            if (b) {\n"
                + "                System.out.println(\"B\");\n"
                + "            } else {\n"
                + "                System.out.println(\"NONE\");\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(boolean a,boolean b){"
                + "if(a)"
                + "System.out.println(\"A\");"
                + "else if(b)"
                + "System.out.println(\"B\");"
                + "else "
                + "System.out.println(\"NONE\");"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else if (b) {\n"
                + "            System.out.println(\"B\");\n"
                + "        } else {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if( a ){\n"
                + "            System.out.println(\"A\");\n"
                + "        }else if( b ){\n"
                + "            System.out.println(\"B\");\n"
                + "        }else{\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeIfParen", false);
        preferences.putBoolean("spaceWithinIfParens", true);
        preferences.putBoolean("spaceBeforeIfLeftBrace", false);
        preferences.putBoolean("spaceBeforeElse", false);
        preferences.putBoolean("spaceBeforeElseLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeIfParen", true);
        preferences.putBoolean("spaceWithinIfParens", false);
        preferences.putBoolean("spaceBeforeIfLeftBrace", true);
        preferences.putBoolean("spaceBeforeElse", true);
        preferences.putBoolean("spaceBeforeElseLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a)\n"
                + "        {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else if (b)\n"
                + "        {\n"
                + "            System.out.println(\"B\");\n"
                + "        } else\n"
                + "        {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a)\n"
                + "          {\n"
                + "            System.out.println(\"A\");\n"
                + "          } else if (b)\n"
                + "          {\n"
                + "            System.out.println(\"B\");\n"
                + "          } else\n"
                + "          {\n"
                + "            System.out.println(\"NONE\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a)\n"
                + "            {\n"
                + "            System.out.println(\"A\");\n"
                + "            } else if (b)\n"
                + "            {\n"
                + "            System.out.println(\"B\");\n"
                + "            } else\n"
                + "            {\n"
                + "            System.out.println(\"NONE\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a)\n"
                + "            System.out.println(\"A\");\n"
                + "        else if (b)\n"
                + "            System.out.println(\"B\");\n"
                + "        else\n"
                + "            System.out.println(\"NONE\");\n"
                + "    }\n"
                + "}\n";
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        }\n"
                + "        else if (b) {\n"
                + "            System.out.println(\"B\");\n"
                + "        }\n"
                + "        else {\n"
                + "            System.out.println(\"NONE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("placeElseOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeElseOnNewLine", false);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean a, boolean b) {\n"
                + "        if (a) {\n"
                + "            System.out.println(\"A\");\n"
                + "        } else {\n"
                + "            if (b) {\n"
                + "                System.out.println(\"B\");\n"
                + "            } else {\n"
                + "                System.out.println(\"NONE\");\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("specialElseIf", false);
        reformat(doc, content, golden);
        preferences.putBoolean("specialElseIf", true);
    }

    public void testWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean b) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "while (b) System.out.println(\"TRUE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        while( b ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        while (b)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        while (b)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "        while (b)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "        while (b)\n"
                + "            System.out.println(\"TRUE\");\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(boolean b){"
                + "while(b)"
                + "System.out.println(\"TRUE\");"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while( b ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        while (b)\n"
                + "            System.out.println(\"TRUE\");\n"
                + "    }\n"
                + "}\n";
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
    }

    public void testSwitch() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
            return;
        } catch (IllegalArgumentException ex) {
            //OK,RELEASE_17, skip test
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "switch (i) {case 0: System.out.println(i); break; default: System.out.println(\"DEFAULT\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentCasesFromSwitch", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentCasesFromSwitch", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch( i ){\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch (i)\n"
                + "        {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch (i)\n"
                + "          {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "          }\n"
                + "        switch (i)\n"
                + "            {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "            }\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "            System.out.println(i);\n"
                + "            break;\n"
                + "        default:\n"
                + "            System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "switch(i){"
                + "case 0:"
                + "System.out.println(i);"
                + "break;"
                + "default:"
                + "System.out.println(\"DEFAULT\");"
                + "}"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch( i ){\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "        {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "          {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "            {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "            System.out.println(i);\n"
                + "            break;\n"
                + "        default:\n"
                + "            System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("indentCasesFromSwitch", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentCasesFromSwitch", true);
    }

    public void testSwitchOnJDK17() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "switch (i) {case 0: System.out.println(i); break; default: System.out.println(\"DEFAULT\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentCasesFromSwitch", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentCasesFromSwitch", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch( i ){\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch (i)\n"
                + "        {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "        switch (i)\n"
                + "          {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "          }\n"
                + "        switch (i)\n"
                + "            {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "            }\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "            System.out.println(i);\n"
                + "            break;\n"
                + "        default:\n"
                + "            System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "switch(i){"
                + "case 0:"
                + "System.out.println(i);"
                + "break;"
                + "default:"
                + "System.out.println(\"DEFAULT\");"
                + "}"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch( i ){\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "        {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "          {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "            {\n"
                + "            case 0:\n"
                + "                System.out.println(i);\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "            System.out.println(i);\n"
                + "            break;\n"
                + "        default:\n"
                + "            System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("indentCasesFromSwitch", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentCasesFromSwitch", true);

    }

    public void testSwitchWithTryExpr() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "        try {\n"
                + "        System.out.println(i);\n"
                + "        } catch (Exception ex) {\n"
                + "        System.out.println(ex);\n"
                + "        }\n"
                + "        break;\n"
                + "        case 1:\n"
                + "        try {\n"
                + "        System.out.println(i);\n"
                + "        } catch (Exception ex) {\n"
                + "        System.out.println(ex);\n"
                + "        } finally {\n"
                + "        System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "        break;\n"
                + "        case 2:\n"
                + "        try (  StringReader r = new StringReader(\"\")  ) {\n"
                + "        System.out.println(i);\n"
                + "        } catch (Exception ex) {\n"
                + "        System.out.println(ex);\n"
                + "        }\n"
                + "        break;\n"
                + "        default:\n"
                + "        try {\n"
                + "        System.out.println(\"DEFAULT\");\n"
                + "        } catch (Exception ex) {\n"
                + "        System.out.println(ex);\n"
                + "        }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                try {\n"
                + "                    System.out.println(i);\n"
                + "                } catch (Exception ex) {\n"
                + "                    System.out.println(ex);\n"
                + "                }\n"
                + "                break;\n"
                + "            case 1:\n"
                + "                try {\n"
                + "                    System.out.println(i);\n"
                + "                } catch (Exception ex) {\n"
                + "                    System.out.println(ex);\n"
                + "                } finally {\n"
                + "                    System.out.println(\"FINALLY\");\n"
                + "                }\n"
                + "                break;\n"
                + "            case 2:\n"
                + "                try (StringReader r = new StringReader(\"\")) {\n"
                + "                    System.out.println(i);\n"
                + "                } catch (Exception ex) {\n"
                + "                    System.out.println(ex);\n"
                + "                }\n"
                + "                break;\n"
                + "            default:\n"
                + "                try {\n"
                + "                    System.out.println(\"DEFAULT\");\n"
                + "                } catch (Exception ex) {\n"
                + "                    System.out.println(ex);\n"
                + "                }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        reformat(doc, content, golden);

        content = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "        case 0:\n"
                + "        int x = i*2;\n"
                + "        try {\n"
                + "        System.out.println(x);\n"
                + "        } catch (Exception ex) {\n"
                + "        System.out.println(ex);\n"
                + "        }\n"
                + "        break;\n"
                + "        default:\n"
                + "        System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        golden = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0:\n"
                + "                int x = i * 2;\n"
                + "                try {\n"
                + "                    System.out.println(x);\n"
                + "                } catch (Exception ex) {\n"
                + "                    System.out.println(ex);\n"
                + "                }\n"
                + "                break;\n"
                + "            default:\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

    }

    public void testRuleSwitch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "switch(i){"
                + "case 0->"
                + "System.out.println(i);"
                + "default->"
                + "System.out.println(\"DEFAULT\");"
                + "}"
                + "}"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "            case 0 ->\n"
                + "                System.out.println(i);\n"
                + "            default ->\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch( i ){\n"
                + "            case 0 ->\n"
                + "                System.out.println(i);\n"
                + "            default ->\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "        {\n"
                + "            case 0 ->\n"
                + "                System.out.println(i);\n"
                + "            default ->\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "          {\n"
                + "            case 0 ->\n"
                + "                System.out.println(i);\n"
                + "            default ->\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i)\n"
                + "            {\n"
                + "            case 0 ->\n"
                + "                System.out.println(i);\n"
                + "            default ->\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        switch (i) {\n"
                + "        case 0 ->\n"
                + "            System.out.println(i);\n"
                + "        default ->\n"
                + "            System.out.println(\"DEFAULT\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("indentCasesFromSwitch", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentCasesFromSwitch", true);
    }
    public void testSwitchExpression() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "int i = switch(i){"
                + "case 0:"
                + "{System.out.println(i);"
                + "yield 5;}"
                + "default:"
                + "{System.out.println(\"DEFAULT\");"
                + "yield 6;}"
                + "};"
                + "System.out.println(i);"
                + "}"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch (i) {\n"
                + "            case 0: {\n"
                + "                System.out.println(i);\n"
                + "                yield 5;\n"
                + "            }\n"
                + "            default: {\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "                yield 6;\n"
                + "            }\n"
                + "        };\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch( i ){\n"
                + "            case 0: {\n"
                + "                System.out.println(i);\n"
                + "                yield 5;\n"
                + "            }\n"
                + "            default: {\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "                yield 6;\n"
                + "            }\n"
                + "        };\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n";

        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
        reformat(doc, content, golden);

        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch (i)\n"
                + "          {\n"
                + "              case 0:\n"
                + "                {\n"
                + "                  System.out.println(i);\n"
                + "                  yield 5;\n"
                + "                }\n"
                + "              default:\n"
                + "                {\n"
                + "                  System.out.println(\"DEFAULT\");\n"
                + "                  yield 6;\n"
                + "                }\n"
                + "          };\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch (i)\n"
                + "            {\n"
                + "                case 0:\n"
                + "                    {\n"
                + "                    System.out.println(i);\n"
                + "                    yield 5;\n"
                + "                    }\n"
                + "                default:\n"
                + "                    {\n"
                + "                    System.out.println(\"DEFAULT\");\n"
                + "                    yield 6;\n"
                + "                    }\n"
                + "            };\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n";

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);

        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }
    public void testSwitchExprWithRuleCase() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int i) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "int i = switch(i){"
                + "case 0->"
                + "{System.out.println(i);"
                + "yield 5;}"
                + "default->"
                + "{System.out.println(\"DEFAULT\");"
                + "yield 6;}"
                + "}"
                + "}"
                + "}\n";
        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch( i )\n"
                + "        {\n"
                + "            case 0 ->\n"
                + "            {\n"
                + "                System.out.println(i);\n"
                + "                yield 5;\n"
                + "            }\n"
                + "            default ->\n"
                + "            {\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "                yield 6;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
                preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
                reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
            content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int i){"
                + "Runnable r = switch(i){"
                + "case 0-> new Runnable(){public void run(){}};"
                + "default->"
                + "{System.out.println(\"DEFAULT\");"
                + "yield new Runnable(){public void run(){}};}"
                + "}"
                + "}"
                + "}\n";
                golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int i) {\n"
                + "        Runnable r = switch (i) {\n"
                + "            case 0 ->\n"
                + "                new Runnable() {\n"
                + "                    public void run() {\n"
                + "                    }\n"
                + "                };\n"
                + "            default -> {\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "                yield new Runnable() {\n"
                + "                    public void run() {\n"
                + "                    }\n"
                + "                };\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
         content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                +"public int get(){ return 1; }"
                + "public void taragui(int i){"
                + "int i = switch(i){"
                + "case 0-> get();"
                + "default->"
                + "{System.out.println(\"DEFAULT\");"
                + "yield get();}"
                + "}"
                + "}"
                + "}\n";
                golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public int get() {\n"
                + "        return 1;\n"
                + "    }\n\n"
                + "    public void taragui(int i) {\n"
                + "        int i = switch (i) {\n"
                + "            case 0 ->\n"
                + "                get();\n"
                + "            default -> {\n"
                + "                System.out.println(\"DEFAULT\");\n"
                + "                yield get();\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseNull() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseNull() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + " case \n"
                + "         null           ->                      System.out.println(\"case with null formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseNull() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case null ->\n"
                + "                System.out.println(\"case with null formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseDefault() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + " void testSwitchCaseDefault() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case\n"
                + "          \n"
                + " default\n"
                + "                    ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseDefault() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseNullAndDefault() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseNullAndDefault() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "   case \n"
                + "null,        default         ->System.out.println(\"case with null and default formatting\");\n"
                + "           \n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseNullAndDefault() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case null, default ->\n"
                + "                System.out.println(\"case with null and default formatting\");\n"
                + "\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + " void testSwitchCaseBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "    case \n"
                + "            String          \n"
                + "            s \n"
                + "            ->      System.out.println(\"case with binding pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case String s ->\n"
                + "                System.out.println(\"case with binding pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseNullAndBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseNullAndBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "    case            null,\n"
                + "            String          \n"
                + "            s \n"
                + "            ->      System.out.println(\"case with null and binding pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseNullAndBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case null, String s ->\n"
                + "                System.out.println(\"case with null and binding pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseGuardedPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "case \n"
                + "      String       s when       s.length() == 1\n"
                + "                    \n"
                + "                    ->\n"
                + "                System.out.println(\"case with guarded pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case String s when s.length() == 1 ->\n"
                + "                System.out.println(\"case with guarded pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseNullAndGuardedPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseNullAndGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "case            \n"
                + "                    null,\n"
                + "      String       s when       s.length() == 1\n"
                + "                    \n"
                + "                    ->\n"
                + "                System.out.println(\"case with null and guarded pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseNullAndGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case null, String s when s.length() == 1 ->\n"
                + "                System.out.println(\"case with null and guarded pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseParenthesizedBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseParenthesizedBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "case \n"
                + "        (String       \n"
                + "        s )\n"
                + "                    \n"
                + "                    ->\n"
                + "                System.out.println(\"case with Parenthesized Binding Pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    } "
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseParenthesizedBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case (String s) ->\n"
                + "                System.out.println(\"case with Parenthesized Binding Pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseNullAndParenthesizedBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseNullAndParenthesizedBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "case            null,\n"
                + "        (String       \n"
                + "        s )\n"
                + "                    \n"
                + "                    ->System.out.println(\"case with null and Parenthesized Binding Pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseNullAndParenthesizedBindingPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case null, (String s) ->\n"
                + "                System.out.println(\"case with null and Parenthesized Binding Pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseParenthesizedGuardedPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseParenthesizedGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "case \n"
                + "        (String       \n"
                + "        s)       when\n"
                + "        s.length() == 1\n"
                + "                    \n"
                + "                    ->\n"
                + "                System.out.println(\"case with Parenthesized Guarded Pattern formatting\");default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseParenthesizedGuardedPattern() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        switch (str) {\n"
                + "            case (String s) when s.length() == 1 ->\n"
                + "                System.out.println(\"case with Parenthesized Guarded Pattern formatting\");\n"
                + "            default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseAllPatterns() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "    void testSwitchCaseAllPatterns() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        IntStream iso = IntStream.of(1, 2);\n"
                + "        switch (str) {\n"
                + "case null ->\n"
                + "     System.out.println(\"case with null formatting\");\n"
                + "  case String string\n"
                + "          when (string.length()==  iso.filter(i -> i / 2 == 0).count() || string.length() == 0)\n"
                + "          ->\n"
                + "      System.out.println(\"case with pattern matching + condition + lambda expression formatting\");\n"
                + "  case                  String s when s.length() == 1 ->System.out.println(\"case with pattern matching + condition formatting\");\n"
                + "  case      (String s )-> \n"
                + "      System.out.println(\"case with pattern matching + condition formatting\");\n"
                + "  case CharSequence\n"
                + "          s ->\n"
                + "                System.out.println(\"case with pattern matching formatting\");\n"
                + "            case\n"
                + "                    default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "\n"
                + "        }\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseAllPatterns() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        IntStream iso = IntStream.of(1, 2);\n"
                + "        switch (str) {\n"
                + "            case null ->\n"
                + "                System.out.println(\"case with null formatting\");\n"
                + "            case String string when (string.length() == iso.filter(i -> i / 2 == 0).count() || string.length() == 0) ->\n"
                + "                System.out.println(\"case with pattern matching + condition + lambda expression formatting\");\n"
                + "            case String s when s.length() == 1 ->\n"
                + "                System.out.println(\"case with pattern matching + condition formatting\");\n"
                + "            case (String s) ->\n"
                + "                System.out.println(\"case with pattern matching + condition formatting\");\n"
                + "            case CharSequence s ->\n"
                + "                System.out.println(\"case with pattern matching formatting\");\n"
                + "            case default ->\n"
                + "                System.out.println(\"default formatting\");\n"
                + "\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testSwitchCaseAllPatternsWithReturnValue() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package p;"
                + "public class Test{    \n"
                + "void testSwitchCaseAllPatterns() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        IntStream iso = IntStream.of(1, 2);\n"
                + "        String retVal = switch (str) {\n"
                + "case null ->\n"
                + "     \"case with null formatting\";\n"
                + "  case String string\n"
                + "          when (string.length()==  iso.filter(i -> i / 2 == 0).count() || string.length() == 0)\n"
                + "          ->\n"
                + "      \"case with pattern matching + condition + lambda expression formatting\";\n"
                + "  case                  String s when s.length() == 1 ->\"case with pattern matching + condition formatting\";\n"
                + "  case      (String s )-> \n"
                + "      \"case with pattern matching + condition formatting\";\n"
                + "  case CharSequence\n"
                + "          s ->\n"
                + "                \"case with pattern matching formatting\";\n"
                + "            case\n"
                + "                    default ->\n"
                + "                \"default formatting\";\n"
                + "\n"
                + "        };\n"
                + "    }"
                + "}";

        String golden
                = "package p;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    void testSwitchCaseAllPatterns() {\n"
                + "        Object str = \"pattern matching switch\";\n"
                + "        IntStream iso = IntStream.of(1, 2);\n"
                + "        String retVal = switch (str) {\n"
                + "            case null ->\n"
                + "                \"case with null formatting\";\n"
                + "            case String string when (string.length() == iso.filter(i -> i / 2 == 0).count() || string.length() == 0) ->\n"
                + "                \"case with pattern matching + condition + lambda expression formatting\";\n"
                + "            case String s when s.length() == 1 ->\n"
                + "                \"case with pattern matching + condition formatting\";\n"
                + "            case (String s) ->\n"
                + "                \"case with pattern matching + condition formatting\";\n"
                + "            case CharSequence s ->\n"
                + "                \"case with pattern matching formatting\";\n"
                + "            case default ->\n"
                + "                \"default formatting\";\n"
                + "\n"
                + "        };\n"
                + "    }\n"
                + "}\n"
                + "";
        reformat(doc, content, golden);
    }

    public void testNormalRecordPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_19"); //NOI18N
        } catch (IllegalArgumentException ex) {
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package test;\n"
                + "record Point(int x, int y){}\n"
                + "public class Test {\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof Point\n"
                + "                (int     x, int    \n"
                + "                y) ) {\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}";

        String golden = "package test;\n"
                + "\n"
                + "record Point(int x, int y) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof Point(int x, int y)) {\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}"
                + "\n";
        reformat(doc, content, golden);
    }

    public void testNestedRecordPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_19"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_19, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package test;\n"
                + "\n"
                + "record Rect(ColoredPoint ul,ColoredPoint lr) {}\n"
                + "enum Color {RED,GREEN,BLUE}\n"
                + "record ColoredPoint(Point p, Color c) {}\n"
                + "record Point(int x, int y) {}\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof\n"
                + "                Rect\n"
                + "                (       ColoredPoint      ul,   ColoredPoint\n"
                + "                        lr)  ) {\n"
                + "            Point p = ul.p();\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}";

        String golden = "package test;\n"
                + "\n"
                + "record Rect(ColoredPoint ul, ColoredPoint lr) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "enum Color {\n"
                + "    RED, GREEN, BLUE\n"
                + "}\n"
                + "\n"
                + "record ColoredPoint(Point p, Color c) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "record Point(int x, int y) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof Rect(ColoredPoint ul, ColoredPoint lr)) {\n"
                + "            Point p = ul.p();\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}"
                + "\n";
        reformat(doc, content, golden);
    }

    public void testMultipleNestingRecordPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_19"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_19, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package test;\n"
                + "\n"
                + "record Rect(ColoredPoint ul,ColoredPoint lr) {}\n"
                + "enum Color {RED,GREEN,BLUE}\n"
                + "record ColoredPoint(Point p, Color c) {}\n"
                + "record Point(int x, int y) {}\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof\n"
                + "                Rect\n"
                + "                (       ColoredPoint(Point   \n"
                + "                        p, Color \n"
                + "                                c\n"
                + "                        )      ,   ColoredPoint\n"
                + "                        lr)    \n"
                + "                ) {\n"
                + "            int x = p.x();\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}";

        String golden = "package test;\n"
                + "\n"
                + "record Rect(ColoredPoint ul, ColoredPoint lr) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "enum Color {\n"
                + "    RED, GREEN, BLUE\n"
                + "}\n"
                + "\n"
                + "record ColoredPoint(Point p, Color c) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "record Point(int x, int y) {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test(Object o) {\n"
                + "        if (o instanceof Rect(ColoredPoint(Point p, Color c), ColoredPoint lr)) {\n"
                + "            int x = p.x();\n"
                + "            System.out.println(\"Hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}"
                + "\n";
        reformat(doc, content, golden);
    }

    public void testAnnotatedRecord() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_19"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_19, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getLookup().lookup(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                  "package test;\n"
                + "\n"
                + "/**\n"
                + " * @author duke\n"
                + " */\n"
                + "@Deprecated public record DeprecatedRecord(int a) {}"
                + "\n";

        String golden =
                  "package test;\n"
                + "\n"
                + "/**\n"
                + " * @author duke\n"
                + " */\n"
                + "@Deprecated\n"
                + "public record DeprecatedRecord(int a) {\n"
                + "\n"
                + "}"
                + "\n";
        reformat(doc, content, golden);
    }

    public void testDoWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean b) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "do System.out.println(\"TRUE\"); while (b);\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeDoLeftBrace", false);
        preferences.putBoolean("spaceBeforeWhile", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeDoLeftBrace", true);
        preferences.putBoolean("spaceBeforeWhile", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeWhileOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeWhileOnNewLine", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(boolean b) {\n"
                + "        do {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        } while (b);\n"
                + "        do{\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }while( b );\n"
                + "        do\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        } while (b);\n"
                + "        do\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          } while (b);\n"
                + "        do\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            } while (b);\n"
                + "        do\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        while (b);\n"
                + "        do {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        while (b);\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(boolean b){"
                + "do "
                + "System.out.println(\"TRUE\");"
                + "while(b);"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        } while (b);\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do{\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }while( b );\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeDoLeftBrace", false);
        preferences.putBoolean("spaceBeforeWhile", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeDoLeftBrace", true);
        preferences.putBoolean("spaceBeforeWhile", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        } while (b);\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          } while (b);\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            } while (b);\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        while (b);\n"
                + "    }\n"
                + "}\n";
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(boolean b) {\n"
                + "        do {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        while (b);\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("placeWhileOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeWhileOnNewLine", false);
    }

    public void testSynchronized() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "synchronized (this) {System.out.println(\"TRUE\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeSynchronizedParen", false);
        preferences.putBoolean("spaceWithinSynchronizedParens", true);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSynchronizedParen", true);
        preferences.putBoolean("spaceWithinSynchronizedParens", false);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "        synchronized (this) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        synchronized( this ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        synchronized (this)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "        synchronized (this)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "        synchronized (this)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(){"
                + "synchronized(this){"
                + "System.out.println(\"TRUE\");"
                + "}"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        synchronized (this) {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        synchronized( this ){\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeSynchronizedParen", false);
        preferences.putBoolean("spaceWithinSynchronizedParens", true);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSynchronizedParen", true);
        preferences.putBoolean("spaceWithinSynchronizedParens", false);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        synchronized (this)\n"
                + "        {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        synchronized (this)\n"
                + "          {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        synchronized (this)\n"
                + "            {\n"
                + "            System.out.println(\"TRUE\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }

    public void testTry() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "try {System.out.println(\"TEST\");} catch(Exception e) {System.out.println(\"CATCH\");} finally {System.out.println(\"FINALLY\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceBeforeCatchParen", false);
        preferences.putBoolean("spaceWithinCatchParens", true);
        preferences.putBoolean("spaceBeforeTryLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", false);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatch", false);
        preferences.putBoolean("spaceBeforeFinally", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeCatchParen", true);
        preferences.putBoolean("spaceWithinCatchParens", false);
        preferences.putBoolean("spaceBeforeTryLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", true);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatch", true);
        preferences.putBoolean("spaceBeforeFinally", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("placeCatchOnNewLine", true);
        preferences.putBoolean("placeFinallyOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeCatchOnNewLine", false);
        preferences.putBoolean("placeFinallyOnNewLine", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui() {\n"
                + "        try {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "        try{\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }catch( Exception e ){\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        }finally{\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "        try\n"
                + "        {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e)\n"
                + "        {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally\n"
                + "        {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "        try\n"
                + "          {\n"
                + "            System.out.println(\"TEST\");\n"
                + "          } catch (Exception e)\n"
                + "          {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "          } finally\n"
                + "          {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "          }\n"
                + "        try\n"
                + "            {\n"
                + "            System.out.println(\"TEST\");\n"
                + "            } catch (Exception e)\n"
                + "            {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "            } finally\n"
                + "            {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "            }\n"
                + "        try {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "        catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(){"
                + "try{"
                + "System.out.println(\"TEST\");"
                + "}catch(Exception e){"
                + "System.out.println(\"CATCH\");"
                + "}finally{"
                + "System.out.println(\"FINALLY\");"
                + "}"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try{\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }catch( Exception e ){\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        }finally{\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceBeforeCatchParen", false);
        preferences.putBoolean("spaceWithinCatchParens", true);
        preferences.putBoolean("spaceBeforeTryLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", false);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatch", false);
        preferences.putBoolean("spaceBeforeFinally", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeCatchParen", true);
        preferences.putBoolean("spaceWithinCatchParens", false);
        preferences.putBoolean("spaceBeforeTryLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", true);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatch", true);
        preferences.putBoolean("spaceBeforeFinally", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try\n"
                + "        {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e)\n"
                + "        {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally\n"
                + "        {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try\n"
                + "          {\n"
                + "            System.out.println(\"TEST\");\n"
                + "          } catch (Exception e)\n"
                + "          {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "          } finally\n"
                + "          {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "          }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try\n"
                + "            {\n"
                + "            System.out.println(\"TEST\");\n"
                + "            } catch (Exception e)\n"
                + "            {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "            } finally\n"
                + "            {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui() {\n"
                + "        try {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "        catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("placeCatchOnNewLine", true);
        preferences.putBoolean("placeFinallyOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeCatchOnNewLine", false);
        preferences.putBoolean("placeFinallyOnNewLine", false);
    }

    public void testOperators() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int x, int y) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "for (int i = 0; i < x; i++) y += (y ^ 123) << 2;";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceWithinParens", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinParens", false);

        preferences.putBoolean("spaceAroundUnaryOps", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundUnaryOps", false);

        preferences.putBoolean("spaceAroundBinaryOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundBinaryOps", true);

        preferences.putBoolean("spaceAroundAssignOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundAssignOps", true);

        preferences.put("wrapAssignOps", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        preferences.putBoolean("wrapAfterAssignOps", true);
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineAssignment", true);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapAssignOps", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("wrapAfterAssignOps", false);
        preferences.putBoolean("alignMultilineAssignment", false);

        preferences.put("wrapBinaryOps", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineBinaryOp", true);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapBinaryOps", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("alignMultilineBinaryOp", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i < x; i++) {\n"
                + "            y += (y ^ 123) << 2;\n"
                + "        }\n"
                + "        for (int i = 0; i < x; i++) {\n"
                + "            y += ( y ^ 123 ) << 2;\n"
                + "        }\n"
                + "        for (int i = 0; i < x; i ++ ) {\n"
                + "            y += (y ^ 123) << 2;\n"
                + "        }\n"
                + "        for (int i = 0; i<x; i++) {\n"
                + "            y += (y^123)<<2;\n"
                + "        }\n"
                + "        for (int i=0; i < x; i++) {\n"
                + "            y+=(y ^ 123) << 2;\n"
                + "        }\n"
                + "        for (int i =\n"
                + "                0; i < x; i++) {\n"
                + "            y +=\n"
                + "                    (y ^ 123) << 2;\n"
                + "        }\n"
                + "        for (int i =\n"
                + "                 0; i < x; i++) {\n"
                + "            y +=\n"
                + "            (y ^ 123) << 2;\n"
                + "        }\n"
                + "        for (int i = 0; i <\n"
                + "                x; i++) {\n"
                + "            y += (y ^\n"
                + "                    123) <<\n"
                + "                    2;\n"
                + "        }\n"
                + "        for (int i = 0; i <\n"
                + "                        x; i++) {\n"
                + "            y += (y ^\n"
                + "                  123) <<\n"
                + "                 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(int x, int y){"
                + "for(int i=0;i<x;i++)"
                + "y+=(y^123)<<2;"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i < x; i++) {\n"
                + "            y += (y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i < x; i++) {\n"
                + "            y += ( y ^ 123 ) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceWithinParens", true);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinParens", false);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i < x; i ++) {\n"
                + "            y += (y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceAroundUnaryOps", true);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundUnaryOps", false);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i<x; i++) {\n"
                + "            y += (y^123)<<2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceAroundBinaryOps", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundBinaryOps", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i=0; i < x; i++) {\n"
                + "            y+=(y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceAroundAssignOps", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundAssignOps", true);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i =\n"
                + "                0; i < x; i++) {\n"
                + "            y +=\n"
                + "                    (y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapAssignOps", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        preferences.putBoolean("wrapAfterAssignOps", true);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i\n"
                + "                = 0; i < x; i++) {\n"
                + "            y\n"
                + "                    += (y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("wrapAfterAssignOps", false);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i\n"
                + "                 = 0; i < x; i++) {\n"
                + "            y\n"
                + "            += (y ^ 123) << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignMultilineAssignment", true);
        reformat(doc, content, golden);
        preferences.put("wrapAssignOps", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("alignMultilineAssignment", false);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i\n"
                + "                < x; i++) {\n"
                + "            y += (y\n"
                + "                    ^ 123)\n"
                + "                    << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.put("wrapBinaryOps", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(int x, int y) {\n"
                + "        for (int i = 0; i\n"
                + "                        < x; i++) {\n"
                + "            y += (y\n"
                + "                  ^ 123)\n"
                + "                 << 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignMultilineBinaryOp", true);
        reformat(doc, content, golden);
        preferences.put("wrapBinaryOps", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putBoolean("alignMultilineBinaryOp", false);
    }

    public void testTypeCast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "if (cs instanceof String) {String s = (String)cs;}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceWithinTypeCastParens", true);
        preferences.putBoolean("spaceAfterTypeCast", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinTypeCastParens", false);
        preferences.putBoolean("spaceAfterTypeCast", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        if (cs instanceof String) {\n"
                + "            String s = (String) cs;\n"
                + "        }\n"
                + "        if (cs instanceof String) {\n"
                + "            String s = ( String )cs;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(CharSequence cs){"
                + "if(cs instanceof String){"
                + "String s=(String)cs;"
                + "}"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        if (cs instanceof String) {\n"
                + "            String s = (String) cs;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        if (cs instanceof String) {\n"
                + "            String s = ( String )cs;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("spaceWithinTypeCastParens", true);
        preferences.putBoolean("spaceAfterTypeCast", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinTypeCastParens", false);
        preferences.putBoolean("spaceAfterTypeCast", true);
    }

    public void testLabelled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt =
                "label: System.out.println();";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putInt("labelIndent", 4);
        testSource.runModificationTask(task).commit();
        preferences.putInt("labelIndent", 0);

        preferences.putBoolean("absoluteLabelIndent", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("absoluteLabelIndent", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        label:\n"
                + "        System.out.println();\n"
                + "        label:\n"
                + "            System.out.println();\n"
                + "label:  System.out.println();\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "public void taragui(CharSequence cs){"
                + "label:"
                + "System.out.println();"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        label:\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "        label:\n"
                + "            System.out.println();\n"
                + "    }\n"
                + "}\n";
        preferences.putInt("labelIndent", 4);
        reformat(doc, content, golden);
        preferences.putInt("labelIndent", 0);

        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public void taragui(CharSequence cs) {\n"
                + "label:  System.out.println();\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("absoluteLabelIndent", true);
        reformat(doc, content, golden);
        preferences.putBoolean("absoluteLabelIndent", false);
    }

    public void testJavadoc() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs, Object obj) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);

        preferences.putBoolean("enableBlockCommentFormatting", true);

        String content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "/**\n"
                + "*This is a test JavaDoc  for the taragui method.\n"
                + "*  Method description is here.\n"
                + "*\n"
                + "*<pre>\n"
                + "* Test t = new Test();\n"
                + "* try {\n"
                + "*     t.taragui(\"TEST\", t);\n"
                + "* } catch (Exception e) {}\n"
                + "*</pre>\n"
                + "* @param cs this is the first parameter description.\n"
                + "* @param obj this is the second parameter description.\n"
                + "*@return this is the return value description.\n"
                + "*  @throws MyRuntimeException the first exception description.\n"
                + "* @throws AnException the second exception description.\n"
                + "*/\n"
                + "public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "/*  This is a block comment.*/\n"
                + "return null;\n"
                + "}\n"
                + "}\n";

        preferences.putInt("text-limit-width", 45);
        preferences.putBoolean("generateParagraphTagOnBlankLines", true);

        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *This is a test JavaDoc  for the taragui method.\n"
                + "     *  Method description is here.\n"
                + "     *\n"
                + "     *<pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     *</pre>\n"
                + "     * @param cs this is the first parameter description.\n"
                + "     * @param obj this is the second parameter description.\n"
                + "     *@return this is the return value description.\n"
                + "     *  @throws MyRuntimeException the first exception description.\n"
                + "     * @throws AnException the second exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*  This is a block comment.*/\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("enableCommentFormatting", false);
        reformat(doc, content, golden);
        preferences.remove("enableCommentFormatting");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui method.\n"
                + "     * Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter description.\n"
                + "     * @param obj this is the second parameter description.\n"
                + "     * @return this is the return value description.\n"
                + "     * @throws MyRuntimeException the first exception description.\n"
                + "     * @throws AnException the second exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("wrapCommentText", false);
        reformat(doc, content, golden);
        preferences.remove("wrapCommentText");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     This is a test JavaDoc for the taragui\n"
                + "     method. Method description is here.\n"
                + "     <p>\n"
                + "     <pre>\n"
                + "     Test t = new Test();\n"
                + "     try {\n"
                + "         t.taragui(\"TEST\", t);\n"
                + "     } catch (Exception e) {}\n"
                + "     </pre>\n"
                + "\n"
                + "     @param cs this is the first parameter\n"
                + "     description.\n"
                + "     @param obj this is the second parameter\n"
                + "     description.\n"
                + "     @return this is the return value\n"
                + "     description.\n"
                + "     @throws MyRuntimeException the first\n"
                + "     exception description.\n"
                + "     @throws AnException the second exception\n"
                + "     description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("addLeadingStarInComment", false);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     This is a test JavaDoc for the taragui\n"
                + "     method.\n"
                + "     Method description is here.\n"
                + "     <p>\n"
                + "     <pre>\n"
                + "     Test t = new Test();\n"
                + "     try {\n"
                + "         t.taragui(\"TEST\", t);\n"
                + "     } catch (Exception e) {}\n"
                + "     </pre>\n"
                + "\n"
                + "     @param cs this is the first parameter\n"
                + "     description.\n"
                + "     @param obj this is the second parameter\n"
                + "     description.\n"
                + "     @return this is the return value\n"
                + "     description.\n"
                + "     @throws MyRuntimeException the first\n"
                + "     exception description.\n"
                + "     @throws AnException the second exception\n"
                + "     description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("preserveNewLinesInComments", true);
        reformat(doc, content, golden);
        preferences.remove("addLeadingStarInComment");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method.\n"
                + "     * Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        preferences.remove("preserveNewLinesInComments");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     *\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("generateParagraphTagOnBlankLines", false);
        reformat(doc, content, golden);

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     This is a test JavaDoc for the taragui\n"
                + "     method. Method description is here.\n"
                + "\n"
                + "     <pre>\n"
                + "     Test t = new Test();\n"
                + "     try {\n"
                + "         t.taragui(\"TEST\", t);\n"
                + "     } catch (Exception e) {}\n"
                + "     </pre>\n"
                + "\n"
                + "     @param cs this is the first parameter\n"
                + "     description.\n"
                + "     @param obj this is the second parameter\n"
                + "     description.\n"
                + "     @return this is the return value\n"
                + "     description.\n"
                + "     @throws MyRuntimeException the first\n"
                + "     exception description.\n"
                + "     @throws AnException the second exception\n"
                + "     description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("addLeadingStarInComment", false);
        reformat(doc, content, golden);
        preferences.remove("addLeadingStarInComment");
        preferences.putBoolean("generateParagraphTagOnBlankLines", true);

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /* This is a block comment. */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("wrapOneLineComment", false);
        reformat(doc, content, golden);
        preferences.remove("wrapOneLineComment");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("blankLineAfterJavadocDescription", false);
        reformat(doc, content, golden);
        preferences.remove("blankLineAfterJavadocDescription");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs  this is the first parameter\n"
                + "     *            description.\n"
                + "     * @param obj this is the second\n"
                + "     *            parameter description.\n"
                + "     * @return this is the return value\n"
                + "     *         description.\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     *                            exception\n"
                + "     *                            description.\n"
                + "     * @throws AnException        the second\n"
                + "     *                            exception\n"
                + "     *                            description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignJavadocParameterDescriptions", true);
        preferences.putBoolean("alignJavadocReturnDescription", true);
        preferences.putBoolean("alignJavadocExceptionDescriptions", true);
        reformat(doc, content, golden);
        preferences.remove("alignJavadocExceptionDescriptions");
        preferences.remove("alignJavadocReturnDescription");
        preferences.remove("alignJavadocParameterDescriptions");

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method. Method description is here.\n"
                + "     * <p>\n"
                + "     * <pre>\n"
                + "     * Test t = new Test();\n"
                + "     * try {\n"
                + "     *     t.taragui(\"TEST\", t);\n"
                + "     * } catch (Exception e) {}\n"
                + "     * </pre>\n"
                + "     *\n"
                + "     * @param cs this is the first parameter\n"
                + "     * description.\n"
                + "     * @param obj this is the second\n"
                + "     * parameter description.\n"
                + "     *\n"
                + "     * @return this is the return value\n"
                + "     * description.\n"
                + "     *\n"
                + "     * @throws MyRuntimeException the first\n"
                + "     * exception description.\n"
                + "     * @throws AnException the second\n"
                + "     * exception description.\n"
                + "     */\n"
                + "    public String taragui(CharSequence cs, Object obj) throws MyRuntimeException, AnExeption {\n"
                + "        /*\n"
                + "         * This is a block comment.\n"
                + "         */\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("blankLineAfterJavadocParameterDescriptions", true);
        preferences.putBoolean("blankLineAfterJavadocReturnTag", true);
        reformat(doc, content, golden);
        preferences.remove("blankLineAfterJavadocReturnTag");
        preferences.remove("blankLineAfterJavadocParameterDescriptions");

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "         * This is a test JavaDoc for the taragui method.\n"
                + "         * @see Exception see tag example\n"
                + "         */\n"
                + "    public void taragui() {\n"
                + "    }\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is a test JavaDoc for the taragui\n"
                + "     * method.\n"
                + "     *\n"
                + "     * @see Exception see tag example\n"
                + "     */\n"
                + "    public void taragui() {\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        preferences.remove("generateParagraphTagOnBlankLines");

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * Test JavaDoc \n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * Test JavaDoc\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * The link in javadoc test shows \n"
                + " * the {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read} and\n"
                + " * {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write} methods.\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * The link in javadoc test shows the\n"
                + " * {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read}\n"
                + " * and\n"
                + " * {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write}\n"
                + " * methods.\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the class.\n"
                + " * <p>\n"
                + " * Some additional detail.\n"
                + " * <p>\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the class.\n"
                + " * <p>\n"
                + " * Some additional detail.\n"
                + " * <p>\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * <pre>\n"
                + " * Some    verbatim  text.\n"
                + " * </pre>\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * <pre>\n"
                + " * Some    verbatim  text.\n"
                + " * </pre>\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the Test class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is a test JavaDoc for the Test class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is longer test JavaDoc for the Test class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is longer test JavaDoc for the Test\n"
                + " * class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " * This is longer test JavaDoc for the Test class.\n"
                + " *\n"
                + " * @author XYZ\n"
                + " * @Deprecated\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "/**\n"
                + " This is longer test JavaDoc for the Test class.\n"
                + "\n"
                + " @author XYZ\n"
                + " @Deprecated\n"
                + " */\n"
                + "public class Test {\n"
                + "}\n";
        preferences.putBoolean("addLeadingStarInComment", false);
        preferences.putBoolean("wrapCommentText", false);
        preferences.putBoolean("blankLineAfterJavadocDescription", false);
        reformat(doc, content, golden);
        preferences.remove("blankLineAfterJavadocDescription");
        preferences.remove("wrapCommentText");
        preferences.remove("addLeadingStarInComment");

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is test JavaDoc for the get method.\n"
                + "     *\n"
                + "     * @param <S> generic param\n"
                + "     * @param in     input parameter long description\n"
                + "     * @param o          param\n"
                + "     * @param vararg variable length argument\n"
                + "     */\n"
                + "    public <S> S get(S in, Object o, String... vararg) {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * This is test JavaDoc for the get\n"
                + "     * method.\n"
                + "     *\n"
                + "     * @param <S>    generic param\n"
                + "     * @param in     input parameter long\n"
                + "     *               description\n"
                + "     * @param o      param\n"
                + "     * @param vararg variable length argument\n"
                + "     */\n"
                + "    public <S> S get(S in, Object o, String... vararg) {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignJavadocParameterDescriptions", true);
        reformat(doc, content, golden);
        preferences.remove("alignJavadocParameterDescriptions");

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o \n"
                + "     */\n"
                + "    public void get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o\n"
                + "     */\n"
                + "    public void get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        preferences.putBoolean("alignJavadocParameterDescriptions", true);
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o\n"
                + "     * @return\n"
                + "     */\n"
                + "    public Object get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o\n"
                + "     * @return\n"
                + "     */\n"
                + "    public Object get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o\n"
                + "     * @param str\n"
                + "     */\n"
                + "    public Object get(Object o, String str) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * @param o\n"
                + "     * @param str\n"
                + "     */\n"
                + "    public Object get(Object o, String str) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        preferences.remove("alignJavadocParameterDescriptions");

        preferences.remove("text-limit-width");
    }

    /**
     * Do not put spaces to parenthesis when method declaration has no
     * parameters. The same rule should be applied to method invocation.
     * Regression test.
     *
     * http://www.netbeans.org/issues/show_bug.cgi?id=116225
     */
    public void test116225() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = maker.Modifiers(Collections.<Modifier>emptySet());
                MethodTree method = maker.Method(
                        mods,
                        "test",
                        maker.Identifier("int"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ System.err.println(i); System.err.println(); "
                        + " new ArrayList(); new ArrayList(i); return i; }",
                        null);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, method));
            }
        };
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceWithinMethodCallParens", true);
        testSource.runModificationTask(task).commit();

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    int test() {\n"
                + "        System.err.println( i );\n"
                + "        System.err.println();\n"
                + "        new ArrayList();\n"
                + "        new ArrayList( i );\n"
                + "        return i;\n"
                + "    }\n"
                + "}\n";
        assertEquals(golden, res);

        String content =
                "package hierbas.del.litoral;"
                + "public class Test{"
                + "int test(){"
                + "System.err.println(i);"
                + "System.err.println();"
                + "new ArrayList();"
                + "new ArrayList(i);"
                + "return i;"
                + "}"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    int test() {\n"
                + "        System.err.println( i );\n"
                + "        System.err.println();\n"
                + "        new ArrayList();\n"
                + "        new ArrayList( i );\n"
                + "        return i;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceWithinMethodCallParens", false);
    }

    public void testJavadocSnippetAnnotation()throws Exception{
        
        try {
            SourceVersion.valueOf("RELEASE_18"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "    public void taragui(CharSequence cs, Object obj) {\n"
                + "    }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        String content =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "/**\n"
                + "     * {@snippet :\n"
                + "     *   public static void main (String... args) {\n"
                + "     *       for (var arg : args) {                 //@highlight substring=\"arg\" regi = rg1 type=\"highlighted\"\n"
                + "     *           if (!arg.isBlargk()) {\n"
                + "     *               System.arg.println(\"arg\");     //@highlight region substring=\"print\"\n"
                + "     *               System.arg.println(\"arg\");\n"
                + "     *               System.arg.println(\"tests\");\n"
                + "     *               System.out.println(\"\\barg\\b\"); // @highlight substring = \"\\barg\\b\" @end\n"
                + "     *               System.out.println(\"\\barg\\b\");         // to-do\n"
                + "     *               System.out.println(\"bargs\"); //@highlight regex = \"bargs\" @highlight regex=\"b\"\n"
                + "     *               System.arg.println(\"arg\");   //@highlight substring=\"arg\" type=\"highlighted\" @highlight substring=\"span\" type=\"highlighted\"\n"
                + "     * }\n"
                + "     * }\n"
                + "     * }\n"
                + "     * }\n"
                + "     */"
                + "    public Object get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n";
        String golden = "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * {@snippet :\n"
                + "     *   public static void main (String... args) {\n"
                + "     *       for (var arg : args) {                 //@highlight substring=\"arg\" regi = rg1 type=\"highlighted\"\n"
                + "     *           if (!arg.isBlargk()) {\n"
                + "     *               System.arg.println(\"arg\");     //@highlight region substring=\"print\"\n"
                + "     *               System.arg.println(\"arg\");\n"
                + "     *               System.arg.println(\"tests\");\n"
                + "     *               System.out.println(\"\\barg\\b\"); // @highlight substring = \"\\barg\\b\" @end\n"
                + "     *               System.out.println(\"\\barg\\b\");         // to-do\n"
                + "     *               System.out.println(\"bargs\"); //@highlight regex = \"bargs\" @highlight regex=\"b\"\n"
                + "     *               System.arg.println(\"arg\");   //@highlight substring=\"arg\" type=\"highlighted\" @highlight substring=\"span\" type=\"highlighted\"\n"
                + "     * }\n"
                + "     * }\n"
                + "     * }\n"
                + "     * }\n"
                + "     */\n"
                + "    public Object get(Object o) {\n"
                + "        return o;\n"
                + "    }\n"
                + "}\n"
                + "";

        reformat(doc, content, golden);
    }
    
    /**
     * Problems with code formatting and comments put in the wrong place.
     * Regression test.
     *
     * http://www.netbeans.org/issues/show_bug.cgi?id=137626
     */
    public void test137626() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "public void test(int i){\n"
                + "    if(i>100)\n"
                + "        i=100;\n"
                + "\n"
                + "    //Comment\n"
                + "    System.err.println();\n"
                + "}\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    public void test(int i) {\n"
                + "        if (i > 100) {\n"
                + "            i = 100;\n"
                + "        }\n"
                + "\n"
                + "        //Comment\n"
                + "        System.err.println();\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "public void test(int i){\n"
                + "    if(i>100)\n"
                + "        i=100; //Comment\n"
                + "    System.err.println();\n"
                + "}\n"
                + "}\n";

        golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    public void test(int i) {\n"
                + "        if (i > 100) {\n"
                + "            i = 100; //Comment\n"
                + "        }\n"
                + "        System.err.println();\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    /**
     * Unexpected new line after comment.
     * Regression test.
     *
     * http://www.netbeans.org/issues/show_bug.cgi?id=131954
     */
    public void test131954() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "public void test(){\n"
                + "int a; // Uff\n"
                + "int b;\n"
                + "}\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    public void test() {\n"
                + "        int a; // Uff\n"
                + "        int b;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    /**
     * SIOOBE when reformatting code with unclosed javadoc comment.
     * Regression test.
     *
     * http://www.netbeans.org/issues/show_bug.cgi?id=135210
     */
    public void test135210() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "/**\n"
                + "*\n"
                + "*\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     *\n"
                + "     * }\n";
        reformat(doc, content, golden);
    }

    /**
     * Unexpected new line after comment.
     * Regression test.
     *
     * http://www.netbeans.org/issues/show_bug.cgi?id=133225
     */
    public void test133225() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "    public void test() {\n"
                + "        int i = 5;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "    }\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "    public void test() {\n"
                + "        int i = 5;\n"
                + "        if (i > 0) {\n"
                + "            i++;\n"
                + "        }\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "    }\n"
                + "}\n";
        String golden2 =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "    public void test() {\n"
                + "        int i = 5;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden, 92, 128);
        reformat(doc, content, golden2, 92, 127);

        golden =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "    public void test() {\n"
                + "        int i = 5;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "        if (i > 0) {\n"
                + "            i++;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        golden2 =
                "package hierbas.del.litoral;\n"
                + "public class Test{\n"
                + "    public void test() {\n"
                + "        int i = 5;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "        if (i > 0)\n"
                + "            i++;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden, 128, 164);
        reformat(doc, content, golden2, 127, 163);
    }

    public void test177858() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "     {\n"
                + "         java.util.Set<String\n"
                + "     }\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    {\n"
                + "        java.util.Set<String\n"
                + "    }\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    {\n"
                + "        java.util.Set<String\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void test231874() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "}\n");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System\n"
                + ".getProperty(\n"
                + "\"property\",\n"
                + "\"default\")\n"
                + ".charAt(\n"
                + "0);\n"
                + "}\n";
        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System\n"
                + "            .getProperty(\n"
                + "                    \"property\",\n"
                + "                    \"default\")\n"
                + "            .charAt(\n"
                + "                    0);\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System\n"
                + ".getProperty(\n"
                + "\"property\",\n"
                + "\"default\"\n"
                + ").charAt(\n"
                + "0\n"
                + ");\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System\n"
                + "            .getProperty(\n"
                + "                    \"property\",\n"
                + "                    \"default\"\n"
                + "            ).charAt(\n"
                + "                    0\n"
                + "            );\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System\n"
                + ".getProperty(\n"
                + "\"property\",\n"
                + "\"default\"\n"
                + ")\n"
                + ".charAt(\n"
                + "0\n"
                + ");\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System\n"
                + "            .getProperty(\n"
                + "                    \"property\",\n"
                + "                    \"default\"\n"
                + "            )\n"
                + "            .charAt(\n"
                + "                    0\n"
                + "            );\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System.getProperty(\n"
                + "\"property\",\n"
                + "\"default\")\n"
                + ".charAt(\n"
                + "0);\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System.getProperty(\n"
                + "            \"property\",\n"
                + "            \"default\")\n"
                + "            .charAt(\n"
                + "                    0);\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System.getProperty(\n"
                + "\"property\",\n"
                + "\"default\"\n"
                + ").charAt(\n"
                + "0\n"
                + ");\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System.getProperty(\n"
                + "            \"property\",\n"
                + "            \"default\"\n"
                + "    ).charAt(\n"
                + "            0\n"
                + "    );\n"
                + "}\n";
        reformat(doc, content, golden);

        content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "private char c = System.getProperty(\n"
                + "\"property\",\n"
                + "\"default\"\n"
                + ")\n"
                + ".charAt(\n"
                + "0\n"
                + ");\n"
                + "}\n";
        golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    private char c = System.getProperty(\n"
                + "            \"property\",\n"
                + "            \"default\"\n"
                + "    )\n"
                + "            .charAt(\n"
                + "                    0\n"
                + "            );\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testLambdaParameterWithInferredType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        java.util.Arrays.asList(args).map((val) -> val.length());\n"
                + "    }\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        java.util.Arrays.asList(args).map((val) -> val.length());\n"
                + "    }\n"
                + "}\n";
        // Testing with wrapping lambda arrow deactivated
        reformat(doc, content, golden);

        final String wrapAfterLambdaArrow = FmtOptions.wrapAfterLambdaArrow;
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean(wrapAfterLambdaArrow, true);

        // Testing with wrapping lambda arrow activated
        reformat(doc, content, golden);

        // Returning the setting to the default value
        preferences.putBoolean(wrapAfterLambdaArrow, FmtOptions.getDefaultAsBoolean(wrapAfterLambdaArrow));
    }

    public void testForNoCondition() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (;;);\n"
                + "    }\n"
                + "}\n";

        String golden =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (;;);\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testForVar1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        String oldLevel = JavaSourceTest.SourceLevelQueryImpl.sourceLevel;
        JavaSourceTest.SourceLevelQueryImpl.sourceLevel = "1.10";
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "            var    v    =   10; \n"
                + "    }\n"
                + "}\n";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        var v = 10;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        JavaSourceTest.SourceLevelQueryImpl.sourceLevel = oldLevel;
    }

    public void testForVar2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        String oldLevel = JavaSourceTest.SourceLevelQueryImpl.sourceLevel;
        JavaSourceTest.SourceLevelQueryImpl.sourceLevel = "1.10";
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "           final   var    v    =   10; \n"
                + "    }\n"
                + "}\n";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        final var v = 10;\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
        JavaSourceTest.SourceLevelQueryImpl.sourceLevel = oldLevel;
    }

    public void testTryBlockAfterIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        if (2 == 2) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden =  // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        if (2 == 2) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testTryBlockAfterElse() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        if (2 == 2) {\n"
                + "            int x = 3;\n"
                + "        } else try {\n"
                + "            int x = 6;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden = // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        if (2 == 2) {\n"
                + "            int x = 3;\n"
                + "        } else try {\n"
                + "            int x = 6;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testTryBlockAfterWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        while (2 == 2) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden = // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        while (2 == 2) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testTryBlockAfterFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int y : Arrays.asList(1, 2, 3)) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden = // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int y : Arrays.asList(1, 2, 3)) try {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testTryWithResources() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try (PrintStream out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try (PrintStream out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        } catch (Exception e) {\n"
                + "            System.out.println(\"CATCH\");\n"
                + "        } finally {\n"
                + "            System.out.println(\"FINALLY\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        content = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try ( final   PrintStream  out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        golden = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try (final PrintStream out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        content = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try ( var  out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        golden = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try (var out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);

        content = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try ( final  var  out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        golden = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        try (final var out = System.out) {\n"
                + "            System.out.println(\"TEST\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSynchronizedBlockAfterFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int y : Arrays.asList(1, 2, 3)) synchronized(Test.class) {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden = // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int y : Arrays.asList(1, 2, 3)) synchronized (Test.class) {\n"
                + "            int x = 3;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testTypeTestPatterns() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content =
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public boolean main(Object o) {\n"
                + "        if (o instanceof String s) {\n"
                + "            return s.isEmpty();\n"
                + "        } else {\n"
                + "            return false;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        String golden = // no change
                "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public boolean main(Object o) {\n"
                + "        if (o instanceof String s) {\n"
                + "            return s.isEmpty();\n"
                + "        } else {\n"
                + "            return false;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testRecord1() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_16"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return;
        }
        sourceLevel="16";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "public record g3<T extends Object>() implements Cloneable{\n"
                + "public g3 {\n"
                + "System.out.println(\"hello\");\n"
                + "}}}";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public record g3<T extends Object>() implements Cloneable {\n\n"
                + "        public g3 {\n"
                + "            System.out.println(\"hello\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testRecord2() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_16"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return;
        }
        sourceLevel="16";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n"
                + "public record g3<T extends Object>() {static int r =10;\n"
                + "public g3 {\n"
                + "System.out.println(\"hello\");\n"
                + "}"
                + "static{}"
                + "}}";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public record g3<T extends Object>() {\n\n"
                + "        static int r = 10;\n\n"
                + "        public g3 {\n"
                + "            System.out.println(\"hello\");\n"
                + "        }\n\n"
                + "        static {\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        reformat(doc, content, golden);
    }

    public void testRecord3() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_16"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return;
        }
        sourceLevel="16";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "public record g3(){}}";

        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public record g3() {\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    
    public void testRecord4() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_16"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return;
        }
        sourceLevel="16";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "public record g3(@Override int a, @Override int b){}}";
        String golden
                = "package hierbas.del.litoral;\n\n"
                + "public class Test {\n\n"
                + "    public record g3(@Override int a, @Override int b) {\n\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }
    @ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
    public static class TestCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return new CompilerOptionsQueryImplementation.Result() {
                @Override
                public List<? extends String> getArguments() {
                    return EXTRA_OPTIONS;
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }

    }
    public void testSealed() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_15, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content = "sealed class x{}\n"
                + "non-sealed class y extends x {}\n"
                + "final class z extends x {}";

        String golden
                = "\nsealed class x {\n"
                + "}\n"
                + "\n"
                + "non-sealed class y extends x {\n"
                + "}\n"
                + "\n"
                + "final class z extends x {\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSealed2() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_15, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "sealed class x{}\n"
                + "non-sealed class y extends x {}";

        String golden
                = "\nsealed class x {\n"
                + "}\n"
                + "\n"
                + "non-sealed class y extends x {\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    public void testSealed3() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_15, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "sealed class x{}\n"
                + "final class c1 extends x {}\n"
                + "non-sealed class c2 extends x {}\n"
                + "final class c3 extends x {}\n"
                + "non-sealed class c4 extends x {}";

        String golden
                = "\nsealed class x {\n"
                + "}\n"
                + "\n"
                + "final class c1 extends x {\n"
                + "}\n"
                + "\n"
                + "non-sealed class c2 extends x {\n"
                + "}\n"
                + "\n"
                + "final class c3 extends x {\n"
                + "}\n"
                + "\n"
                + "non-sealed class c4 extends x {\n"
                + "}\n";
        reformat(doc, content, golden);
    }
  
    public void testStringTemplate() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        String content
                = "\n"
                + "class Test {\n\n"
                + "    private String t() {\n"
                + "        return STR.\"a\\{1 + 2}b\";\n"
                + "    }\n"
                + "}\n";

        String golden
                = "\n"
                + "class Test {\n\n"
                + "    private String t() {\n"
                + "        return STR.\"a\\{1 + 2}b\";\n"
                + "    }\n"
                + "}\n";
        reformat(doc, content, golden);
    }

    private void reformat(Document doc, String content, String golden) throws Exception {
        reformat(doc, content, golden, 0, content.length());
    }

    private void reformat(Document doc, String content, String golden, int startOffset, int endOffset) throws Exception {
        doc.remove(0, doc.getLength());
        doc.insertString(0, content, null);

        Reformat reformat = Reformat.get(doc);
        reformat.lock();
        try {
            reformat.reformat(startOffset, endOffset);
        } finally {
            reformat.unlock();
        }
        String res = doc.getText(0, doc.getLength());
        System.err.println(res);
        assertEquals(golden, res);
    }

    private ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List/*<PathResourceImplementation>*/ list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url != null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }

    private static URL getRootURL(File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(Utilities.toURI(f).toURL());
            } else {
                url = Utilities.toURI(f).toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl + "/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }

    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }
}
