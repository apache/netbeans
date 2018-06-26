/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.indent;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplIndenterTest extends TplTestBase {

    public TplIndenterTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;

        TplIndentTaskFactory tplFactory = new TplIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-tpl"), tplFactory, TplTokenId.language());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/x-tpl"), HTMLTokenId.language());
    }

    @Override
    protected boolean runInEQ() {
        return true;
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

    public void testFormattingCase01() throws Exception {
        reformatFileContents("testfiles/indenting/case001.tpl", new IndentPrefs(4, 4));
    }

    public void testFormattingCase02() throws Exception {
        reformatFileContents("testfiles/indenting/case002.tpl", new IndentPrefs(4, 4));
    }

    public void testFormattingCase03() throws Exception {
        reformatFileContents("testfiles/indenting/case003.tpl", new IndentPrefs(4, 4));
    }

    public void testFormattingCase04() throws Exception {
        reformatFileContents("testfiles/indenting/case004.tpl", new IndentPrefs(4, 4));
    }

    public void testFormattingCase05() throws Exception {
        reformatFileContents("testfiles/indenting/case005.tpl", new IndentPrefs(4, 4));
    }

}
