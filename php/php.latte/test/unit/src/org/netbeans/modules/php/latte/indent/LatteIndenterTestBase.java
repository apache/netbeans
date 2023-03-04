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
package org.netbeans.modules.php.latte.indent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.php.latte.LatteTestBase;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class LatteIndenterTestBase extends LatteTestBase {

    public LatteIndenterTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;
        LatteIndentTask.Factory factory = new LatteIndentTask.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-latte"), factory, LatteTopTokenId.language());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/x-latte"), HTMLTokenId.language());
        MockLookup.setInstances(new FileEncodingQueryImplementation() {
            @Override
            public Charset getEncoding(FileObject file) {
                return StandardCharsets.UTF_8;
            }
        });
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            assertNotNull(ec);
            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected void indent(String fileName) throws Exception {
        indent(fileName, new IndentPrefs(4, 4));
    }

    protected void indent(String fileName, IndentPrefs indentPreferences) throws Exception {
        assert fileName != null;
        reformatFileContents("testfiles/indent/" + fileName + ".latte", indentPreferences); //NOI18N
    }

}
