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
package org.netbeans.modules.html.knockout;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProviderTest;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class KOJsEmbeddingProviderPluginTest extends CslTestBase {

    public KOJsEmbeddingProviderPluginTest(String testName) {
        super(testName);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    public void testSimple() throws Exception {
        checkVirtualSource("KOTestProject/public_html/simple.html");
    }

    public void testComplex() throws Exception {
        checkVirtualSource("KOTestProject/public_html/complex.html");
    }

    public void testForEach() throws Exception {
        checkVirtualSource("completion/foreach/index.html");
    }

    public void testForEachAlias() throws Exception {
        checkVirtualSource("completion/foreachAlias/index.html");
    }

    public void testWith() throws Exception {
        checkVirtualSource("completion/with/index.html");
    }

    public void testInner() throws Exception {
        checkVirtualSource("completion/inner/index.html");
    }

    public void testTemplate() throws Exception {
        checkVirtualSource("completion/template/index.html");
    }

    public void testTemplateForEach() throws Exception {
        checkVirtualSource("completion/templateForEach/index.html");
    }

    public void testTemplateForEachAlias() throws Exception {
        checkVirtualSource("completion/templateForEachAlias/index.html");
    }

    public void testTemplateInner() throws Exception {
        checkVirtualSource("completion/templateInner/index.html");
    }
    
    public void testTemplateCycle() throws Exception {
        checkVirtualSource("completion/templateCycle/index.html");
    }

    public void testDoNotCreateKOVirtualSourceForPlainFiles() {
        FileObject index = getTestFile("KOTestProject/public_html/plain.html");
        BaseDocument document = getDocument(index);
        JsEmbeddingProviderTest.assertEmbedding(document, null); //no embedded js code
    }

    private void checkVirtualSource(String file) throws Exception {
        FileObject fo = getTestFile(file);
        BaseDocument doc = getDocument(fo);

        Source source = Source.create(doc);
        final AtomicReference<String> jsCodeRef = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator jsRi = WebUtils.getResultIterator(resultIterator, "text/javascript");
                if (jsRi != null) {
                    jsCodeRef.set(jsRi.getSnapshot().getText().toString());
                } else {
                    //no js embedded code
                }
            }
        });
        String jsCode = jsCodeRef.get();
        assertDescriptionMatches(fo, jsCode, false, ".virtual", true);
    }
}
