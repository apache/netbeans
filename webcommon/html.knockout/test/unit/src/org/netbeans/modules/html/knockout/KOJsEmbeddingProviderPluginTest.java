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
