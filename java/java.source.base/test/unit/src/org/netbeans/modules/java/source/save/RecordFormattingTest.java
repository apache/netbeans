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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.SharedClassObject;

/**
 * Test to show the problem mentioned in #7043.
 *
 * For some strange reason, the setting spaceWithinMethodDeclParens affects the
 * formatting (indentation) of the trailing brace (}) of a record definition,
 * both as inner record and as top level record.
 *
 * @author homberghp
 */
public class RecordFormattingTest extends NbTestCase {

    File testFile = null;
    private String sourceLevel = "1.8";
    private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

    /**
     * Creates a new instance of FormatingTest
     */
    public RecordFormattingTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RecordFormattingTest.class);
        return suite;
    }

    String content
            = """
              record Student(String id,String lastname,String firstname) implements Serializable {
              } // should stay flush to left margin
              """;

    /**
     * The java formatter indents the final brace '}' of a record to far to the
     * right, when the setting spaceWithinMethodDeclParens is True AND the
     * record header defines parameters. This
     *
     * @throws Exception for some reason
     */
    // copied from testSealed
    public void run7043(String golden, boolean spacesInMethodDecl) throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_15, skip test
            return;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie)testSourceDO.getCookie(EditorCookie.class);
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("spaceWithinMethodDeclParens", spacesInMethodDecl);
        preferences.putInt("blankLinesAfterClassHeader", 0);

        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        System.err.println("golden = " + golden.trim().length());
        System.err.println("content = " + content.trim().length());
        reformat(doc, content, golden);

    }

    
    public void test7043NoSpaces() throws Exception{
        run7043("""
                  record Student(String id, String lastname, String firstname) implements Serializable {
                  } // should stay flush to left margin
                  """, false);
    }

    public void test7043WithSpaces() throws Exception{
        run7043("""
                  record Student( String id, String lastname, String firstname ) implements Serializable {
                  } // should stay flush to left margin
                  """, true);
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
//        assertEquals(golden, res);
        assertNoDiff(golden, res);
    }

    static void assertNoDiff(String expected, String actual) {
        var expectedLines = expected.trim().split("\n");
        var actualLines = actual.trim().split("\n");
        // fail if not of equal length
        assertEquals("number of lines differ :\n expected lines ='" + expected
                + "'\n actual lines='\n" + actual
                + "'", expectedLines.length, actualLines.length);
        String assertResult = "";
        for (int i = 0; i < expectedLines.length; i++) {
            String actualLine = actualLines[i];
            String expLine = expectedLines[i];
            if (expLine.equals(actualLine)) {
                continue;
            }
            assertResult += "\n at line " + (i + 1) + ": \n'" + expLine + "'\n <> \n'" + actualLine + "'";
        }
        if (assertResult.equals("")) {
            return;
        }
        System.err.println(assertResult);
        fail(assertResult);

    }

}
