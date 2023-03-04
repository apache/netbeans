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
package org.netbeans.modules.javafx2.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.parser.FxModelBuilder;
import org.netbeans.modules.javafx2.editor.parser.PrintVisitor;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author sdedic
 */
public abstract class GoldenFileTestBase extends FXMLCompletionTestBase {
    protected DataObject  sourceDO;
    protected Document    document;
    protected TokenHierarchy hierarchy;
    protected String fname;
    protected Collection<ErrorMark>   problems;
    protected StringBuilder report = new StringBuilder();

    public GoldenFileTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File dataDir = getDataDir();
        fname = getName().replace("test", "");
        File f = new File(dataDir, getClass().getName().
                replaceAll("\\.", "/") + "/" + fname + ".fxml");
        
        File w = new File(getWorkDir(), f.getName());
        InputStream is = new FileInputStream(f);
        OutputStream os = new FileOutputStream(w);
        FileUtil.copy(is, os);
        os.close();
        is.close();
        FileObject fo = FileUtil.toFileObject(w);
        sourceDO = DataObject.find(fo);
        document = ((EditorCookie)sourceDO.getCookie(EditorCookie.class)).openDocument();
        hierarchy = TokenHierarchy.get(document);
    }

    @Override
    protected void tearDown() throws Exception {
        if (document != null) {
            ((EditorCookie)sourceDO.getCookie(EditorCookie.class)).close();
        }
        super.tearDown();
    }
    
    protected void appendParsedTree(FxmlParserResult result, StringBuilder sb) {
        PrintVisitor pv = new PrintVisitor(result.getTreeUtilities());
        pv.out = sb;
        result.getSourceModel().accept(pv);
    }
    
    protected void appendErrors(final FxmlParserResult parser, StringBuilder sb) {
        for (ErrorMark em : parser.getProblems()) {
            sb.append(em).append("\n");
        }
    }


    protected void assertContents(CharSequence sb) throws IOException {
        File out = new File(getWorkDir(), fname + ".parsed");
        FileWriter wr = new FileWriter(out);
        wr.append(sb);
        wr.close();
        
        assertFile(out, getGoldenFile(fname + ".pass"), new File(getWorkDir(), fname + ".diff"));
    }
}
