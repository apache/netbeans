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
package org.netbeans.modules.java.editor.imports;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author lahvac
 */
public class ClipboardHandlerTest extends NbTestCase {

    public ClipboardHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"META-INF/generated-layer.xml", "org/netbeans/modules/java/source/resources/layer.xml", "org/netbeans/modules/java/editor/resources/layer.xml", "org/netbeans/modules/editor/settings/storage/layer.xml"}, new Object[0]);
        ClipboardHandler.autoImport = true;
        Main.initializeURLFactory();
        super.setUp();
    }

    public void testSimple() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |List l;| }\n", "package test;\npublic class Target {\n^\n}", "package test;\n\nimport java.util.List;\n\npublic class Target {\nList l;\n}");
    }

    public void testFieldGroup1() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |List l1, l2;| }\n", "package test;\npublic class Target {\n^\n}", "package test;\n\nimport java.util.List;\n\npublic class Target {\nList l1, l2;\n}");
    }

    public void testFieldGroup2() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |@SuppressWarnings(\"deprecated\") List l1, l2;| }\n", "package test;\npublic class Target {\n^\n}", "package test;\n\nimport java.util.List;\n\npublic class Target {\n@SuppressWarnings(\"deprecated\") List l1, l2;\n}");
    }

    public void testCopyIntoComment() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |List l;| }\n", "package test;\npublic class Target {\n/*^*/\n}", "package test;\npublic class Target {\n/*List l;*/\n}");
    }
    
    public void testClassCopied() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |class Inner { } Inner i = new InnerSub(); class InnerSub extends Inner { }| }\n", "package test;\npublic Target {\n^\n}", "package test;\npublic Target {\nclass Inner { } Inner i = new InnerSub(); class InnerSub extends Inner { }\n}");
    }
    
    public void testClassNotCopied() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { static class Inner { } |Inner i;| }\n", "package test;\npublic class Target {\n^\n}", "package test;\npublic class Target {\nTest.Inner i;\n}");
    }
    
    public void testMethodCopied() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { |public static int m1() {return 0;} int one = m1(); int two = m2(); public static int m2() {return 0;}| }\n", "package test;\npublic class Target {\n^\n}", "package test;\npublic class Target {\npublic static int m1() {return 0;} int one = m1(); int two = m2(); public static int m2() {return 0;}\n}");
    }
    
    public void testAnnotationWithValueCopied() throws Exception {
        copyAndPaste("package test;\nimport java.util.List;\npublic class Test { @Retention(value = RetentionPolicy.RUNTIME) @Target(value = {ElementType.TYPE}) public static @interface R { public Class value() } |@R(List.class) |public static class X { }\n", 
                "package test;\n^public class Target {\n\n}", "package test;\n\nimport java.util.List;\n\n@Test.R(List.class) public class Target {\n\n}");
    }
    
    public void testAnonymousClass() throws Exception {
        copyAndPaste("package test;\nimport java.util.ArrayList;\npublic class Test { void t() { |new ArrayList<String>() {};| } }\n", "package test;\npublic class Target {\nvoid t() { ^ }\n}", "package test;\n\nimport java.util.ArrayList;\n\npublic class Target {\nvoid t() { new ArrayList<String>() {}; }\n}");
    }
    
    public void testCopyIntoTextBlock() throws Exception {
        copyAndPaste("|List l1;\nList l2;\nList l3;\n\n| ", "package test;\npublic class Target {\nString s = \"\"\"\n^\"\"\"\n}", "package test;\npublic class Target {\nString s = \"\"\"\nList l1;\nList l2;\nList l3;\n\n\"\"\"\n}");
    }
    
    public void testCopyTextBlockIntoTextBlock() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        copyAndPaste("|\"\"\"\nList l1;\"\"\"| ", "package test;\npublic class Target {\nString s = \"\"\"\ntest^ block\n\"\"\"\n}", "package test;\npublic class Target {\nString s = \"\"\"\ntest\\\"\"\"\nList l1;\\\"\"\" block\n\"\"\"\n}");
    }

    public void testStaticImportsOn11_JIRA3019() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        copyAndPaste("package test;\nimport java.util.List; public class Test { public static final String CONST = null; |void t() { String s = CONST; List lst;}|\n", 
                     "package test;\npublic class Target {\n^\n}",
                     "package test;\n\nimport java.util.List;\nimport static test.Test.CONST;\n\npublic class Target {\nvoid t() { String s = CONST; List lst;}\n}",
                     "11");
    }

    private void copyAndPaste(String from, final String to, String golden) throws Exception {
        copyAndPaste(from, to, golden, null);
    }

    private void copyAndPaste(String from, final String to, String golden, String sourceLevel) throws Exception {
        final int pastePos = to.indexOf('^');

        assertTrue(pastePos >= 0);

        String[] split = from.split(Pattern.quote("|"));

        assertEquals(3, split.length);

        final String cleanFrom = split[0] + split[1] + split[2];

        final int start = split[0].length();
        final int end = start + split[1].length();

        FileObject wd = SourceUtilsTestUtil.makeScratchDir(this);
        final FileObject src = wd.createFolder("src");
        FileObject build = wd.createFolder("build");
        FileObject cache = wd.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(src, build, cache);
        SourceUtilsTestUtil.compileRecursively(src);

        final JEditorPane[] target = new JEditorPane[1];
        final Exception[] fromAWT = new Exception[1];
        
        final JEditorPane source = paneFor(src, "test/Test.java", cleanFrom, sourceLevel);
        target[0] = paneFor(src, "test/Target.java", to.replaceAll(Pattern.quote("^"), ""), sourceLevel);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                try {
                    source.setSelectionStart(start);
                    source.setSelectionEnd(end);

                    source.copy();

                    target[0].setCaretPosition(pastePos);

                    target[0].paste();
                } catch (Exception ex) {
                    fromAWT[0] = ex;
                }
            }
        });

        if (fromAWT[0] != null) throw fromAWT[0];

        final String[] actual = new String[1];

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                actual[0] = target[0].getText();
            }
        });
        assertEquals(golden, actual[0]);
    }

    private JEditorPane paneFor(FileObject src, String fileName, String code, String sourceLevel) throws Exception, DataObjectNotFoundException, IOException {
        FileObject fromFO = FileUtil.createData(src, fileName);
        TestUtilities.copyStringToFile(fromFO, code);
        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(fromFO, sourceLevel);
        }
        DataObject od = DataObject.find(fromFO);
        final EditorCookie.Observable ec = od.getCookie(EditorCookie.Observable.class);
        final Exchanger<JEditorPane> exch = new Exchanger<>();
        
        class L implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    if (!EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                        return;
                    }
                    // we are in AWT
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes == null) {
                        return;
                    }
                    exch.exchange(panes[0]);
                } catch (InterruptedException ex) {
                }
            }
        }
        L listener = new L();
        ec.addPropertyChangeListener(listener);
        JEditorPane pane = null;
        try {
            ec.open();
            ec.openDocument().putProperty(Language.class, JavaTokenId.language());
            pane = exch.exchange(null, 5, TimeUnit.SECONDS);
        } finally {
            ec.removePropertyChangeListener(listener);
        }
        assertNotNull("Editor pane not opened", pane);
        return pane;
    }

}
