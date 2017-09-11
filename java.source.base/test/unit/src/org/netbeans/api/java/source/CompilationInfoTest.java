/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import javax.lang.model.SourceVersion;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Dusan Balek
 */
public class CompilationInfoTest extends NbTestCase {

    private static final String TEST_FILE_CONTENT =
            "public class {0} '{\n" + "   public static void main (String[] args) {\n" + "   }\n" + "}'\n";

    public CompilationInfoTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    /**
     * Test of getSourceVersion method, of class CompilationInfo.
     */
    @Test
    public void testGetSourceVersion() throws Exception {
        FileObject test = createTestFile("Test1");
        JavaSource js = JavaSource.forFileObject(test);
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {

                SourceVersion version = parameter.getSourceVersion();
                assertNotNull(version);
            }
        }, true);
    }

    private FileObject createTestFile(String className) {
        try {
            File workdir = this.getWorkDir();
            File root = new File(workdir, "src");
            root.mkdir();
            File data = new File(root, className + ".java");

            PrintWriter out = new PrintWriter(new FileWriter(data));
            try {
                out.println(MessageFormat.format(TEST_FILE_CONTENT, new Object[]{className}));
            } finally {
                out.close();
            }
            return FileUtil.toFileObject(data);
        } catch (IOException ioe) {
            return null;
        }
    }

    public void testCacheEviction() throws Exception {
        clearWorkDir();

        FileObject source = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(source, "public class Test {\n void test() {\n  //whatever\n }\n}\n");

        DataObject sourceDO = DataObject.find(source);
        EditorCookie ec = sourceDO.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        Document doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());

        TokenHierarchy.get(doc).tokenSequence().tokenCount();

        JavaSource js = JavaSource.forDocument(doc);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                parameter.putCachedValue("1", 1, CacheClearPolicy.ON_TASK_END);
                parameter.putCachedValue("2", 2, CacheClearPolicy.ON_CHANGE);
                parameter.putCachedValue("3", 3, CacheClearPolicy.ON_SIGNATURE_CHANGE);

                assertEquals(1, parameter.getCachedValue("1"));
                assertEquals(2, parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));

                parameter.putCachedValue("rewrite", 4, CacheClearPolicy.ON_SIGNATURE_CHANGE);
                parameter.putCachedValue("rewrite", null, CacheClearPolicy.ON_TASK_END);

                assertNull(parameter.getCachedValue("rewrite"));
            }
        }, true);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertEquals(2, parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));
            }
        }, true);

        doc.insertString(41, "a", null);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertNull(parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));
            }
        }, true);

        doc.insertString(20, "void t2() {}", null);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertNull(parameter.getCachedValue("2"));
                assertNull(parameter.getCachedValue("3"));
            }
        }, true);
    }
}
