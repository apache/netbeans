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

package org.netbeans.modules.java.hints.declarative;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author lahvac
 */
public class EmbeddingProviderImplTest extends NbTestCase {

    public EmbeddingProviderImplTest(String name) {
        super(name);
    }

    public void testSimpleEmbedding() throws Exception {
        performEmbeddingTest("\'\': 1 + 1 => 1 + 1;;",
                             "//no-errors\n" +
                             "package $; class $ {\n" +
                             "     private void $0() throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "     private void $1() throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "}\n");
    }

    public void testEmbeddingWithVariables1() throws Exception {
        performEmbeddingTest("\'\': $1 + $2 :: $1 instanceof int && $2 instanceof double => 1 + 1;;",
                             "//no-errors\n" +
                             "package $; class $ {\n" +
                             "     private void $0( int $1, double $2) throws Throwable {\n" +
                             "         $1 + $2 ;\n" +
                             "     }\n" +
                             "     private void $1( int $1, double $2) throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "}\n");
    }

    public void testEmbeddingWithVariables2() throws Exception {
        performEmbeddingTest("\'\': $1 + $2 :: $1 instanceof int && $2 instanceof double => 1 + 1;; 1 + 1 => 1 + 1;;",
                             "//no-errors\n" +
                             "package $; class $ {\n" +
                             "     private void $0( int $1, double $2) throws Throwable {\n" +
                             "         $1 + $2 ;\n" +
                             "     }\n" +
                             "     private void $1( int $1, double $2) throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "     private void $2() throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "     private void $3() throws Throwable {\n" +
                             "         1 + 1 ;\n" +
                             "     }\n" +
                             "}\n");
    }

    public void testEmbeddingWithImportsAndConditions() throws Exception {
        performEmbeddingTest("<?import java.util.List;?>\'\': $1 + $2 :: $1 instanceof int && $2 instanceof double && cond($1, $2);; <?private boolean test(Variable v1, Variable v2) {return true;}?>",
                             "//no-errors\n" +
                             "package $;\n" + 
                             "import java.util.List;\n" +
                             "class $ {\n" +
                             "     private void $0( int $1, double $2) throws Throwable {\n" +
                             "         $1 + $2 ;\n" +
                             "     }\n" +
                             "}\n",
                             "package $;\n" +
                             "import java.util.List;\n" +
                             "import org.netbeans.modules.java.hints.declarative.conditionapi.Context;\n" +
                             "import org.netbeans.modules.java.hints.declarative.conditionapi.Matcher;\n" +
                             "import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;\n" +
                             "class $ {\n" +
                             "     private final Context context = null;\n" +
                             "     private final Matcher matcher = null;\n" +
                             "     private boolean test(Variable v1, Variable v2) {return true;}\n" +
                             "}\n");
    }

    private void performEmbeddingTest(String code, String... golden) throws Exception {
        prepareTest(code, -1);

        Source s = Source.create(doc);
        final List<String> output = new LinkedList<>();

        ParserManager.parse(Collections.singletonList(s), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                for (Embedding e : new EmbeddingProviderImpl().getEmbeddings(resultIterator.getSnapshot())) {
                    output.add(e.getSnapshot().getText().toString());
                }
            }
        });

        Iterator<String> goldenIt = Arrays.asList(golden).iterator();
        Iterator<String> outputIt = output.iterator();

        while (goldenIt.hasNext() && outputIt.hasNext()) {
            assertEquals(goldenIt.next().replaceAll("[ \t\n]+", " "), outputIt.next().replaceAll("[ \t\n]+", " "));
        }

        assertEquals(output.toString(), goldenIt.hasNext(), outputIt.hasNext());
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    protected void prepareTest(String code, int testIndex) throws Exception {
        File workDirWithIndexFile = testIndex != (-1) ? new File(getWorkDir(), Integer.toString(testIndex)) : getWorkDir();
        FileObject workDirWithIndex = FileUtil.toFileObject(workDirWithIndexFile);

        if (workDirWithIndex != null) {
            workDirWithIndex.delete();
        }

        workDirWithIndex = FileUtil.createFolder(workDirWithIndexFile);

        assertNotNull(workDirWithIndexFile);

        FileUtil.setMIMEType("hint", "text/x-javahints");

        FileObject sourceRoot = workDirWithIndex.createFolder("src");
//        FileObject buildRoot  = workDirWithIndex.createFolder("build");
//        FileObject cache = workDirWithIndex.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, "rule.hint");

        TestUtilities.copyStringToFile(data, code);

        data.refresh();

//        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();

        doc.putProperty(Language.class, DeclarativeHintTokenId.language());
        doc.putProperty("mimeType", "text/x-javahints");
    }

    private Document doc;

    @ServiceProvider(service=LanguageProvider.class)
    public static final class JavacParserProvider extends LanguageProvider {

        @Override
        public Language<?> findLanguage(String mimeType) {
            if (mimeType.equals("text/x-javahints")) {
                return DeclarativeHintTokenId.language();
            }

            return null;
        }

        @Override
        public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }

    }
    
}
