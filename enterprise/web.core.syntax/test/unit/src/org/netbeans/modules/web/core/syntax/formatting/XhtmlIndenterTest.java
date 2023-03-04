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

package org.netbeans.modules.web.core.syntax.formatting;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.html.editor.xhtml.XhtmlElLanguage;
import org.netbeans.modules.html.editor.xhtml.XhtmlElTokenId;
import org.netbeans.modules.web.core.syntax.indent.ExpressionLanguageIndentTaskFactory;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public class XhtmlIndenterTest extends CslTestBase {

    public XhtmlIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;
        ExpressionLanguageIndentTaskFactory elReformatFactory = new ExpressionLanguageIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-el"), ELTokenId.language(), elReformatFactory); //NOI18N
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
    }

    protected DefaultLanguageConfig getPreferredLanguage() {
        return new XhtmlElLanguage();
    }

    protected String getPreferredMimeType() {
        return "text/xhtml";
    }

    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected BaseKit getEditorKit(String mimeType) {
        return new HtmlKit("text/xhtml");
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

             BaseDocument bd = (BaseDocument)ec.openDocument();
             enabledEL(bd);
             return bd;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    private static final String EL_ENABLED_KEY = "el_enabled"; //NOI18N
    
    private static void enabledEL(Document doc) {
        InputAttributes inputAttributes = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (inputAttributes == null) {
            inputAttributes = new InputAttributes();
            doc.putProperty(InputAttributes.class, inputAttributes);
        }
        inputAttributes.setValue(LanguagePath.get(XhtmlElTokenId.language()), EL_ENABLED_KEY, new Object(), false);
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
    }

    protected JEditorPane getPane(String text) throws Exception {
        JEditorPane pane = super.getPane(text);
        enabledEL(pane.getDocument());
        return pane;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testIndentation() throws Exception {
        insertNewline("<html>^#{bean.value}</html>", "<html>\n    ^#{bean.value}</html>", null);
        insertNewline("<html>^#{bean.value}\n</html>", "<html>\n    ^#{bean.value}\n</html>", null);
        insertNewline("<html>^#{bean.value}", "<html>\n    ^#{bean.value}", null);
    }

}
