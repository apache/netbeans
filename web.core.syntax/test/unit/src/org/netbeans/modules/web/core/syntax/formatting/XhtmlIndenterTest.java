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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
