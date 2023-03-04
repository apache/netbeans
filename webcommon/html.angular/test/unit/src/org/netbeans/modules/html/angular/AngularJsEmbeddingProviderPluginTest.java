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
package org.netbeans.modules.html.angular;

import java.util.Collections;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProviderTest;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class AngularJsEmbeddingProviderPluginTest extends CslTestBase {
    
    public AngularJsEmbeddingProviderPluginTest(String testName) {
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
    
    public void testIssue229693() throws Exception {
        checkVirtualSource("virtualSource/issue229693.html");
    }
    
    public void testIssue230223() throws Exception {
        checkVirtualSource("virtualSource/issue230223.html");
    }
    
    public void testIssue230480() throws Exception {
        checkVirtualSource("virtualSource/issue230480.html");
    }
    
    public void testIssue232058() throws Exception {
        checkVirtualSource("virtualSource/issue232058.html");
    }
    
    public void testIssue232056() throws Exception {
        checkVirtualSource("angularTestProject/public_html/issue232056.html");
    }
    
    public void testIssue232029() throws Exception {
        checkVirtualSource("virtualSource/issue232029.html");
    }
    
    public void testIssue231902() throws Exception {
        checkVirtualSource("virtualSource/issue231902.html");
    }
    
    public void testIssue232694() throws Exception {
        checkVirtualSource("virtualSource/issue232694.html");
    }
    
    public void testIssue231974() throws Exception {
        checkVirtualSource("virtualSource/issue231974.html");
    }
    
    public void testIssue232062() throws Exception {
        checkVirtualSource("virtualSource/issue232062.html");
    }
    
    public void testIssue232026() throws Exception {
        checkVirtualSource("virtualSource/issue232026.html");
    }
    
    public void testIssue232812() throws Exception {
        checkVirtualSource("virtualSource/issue232812.html");
    }
    
    public void testIssue235643() throws Exception {
        checkVirtualSource("virtualSource/issue235643.html");
    }
    
    public void testIssue240564() throws Exception {
        checkVirtualSource("virtualSource/issue240564.html");
    }
    
    public void testIssue236042() throws Exception {
        checkVirtualSource("virtualSource/issue236042.html");
    }
    
    public void testIssue241073() throws Exception {
        checkVirtualSource("virtualSource/issue241073.html");
    }
     
    public void testIssue240767() throws Exception {
        checkVirtualSource("virtualSource/issue240767.html");
    }
    
    public void testIssue240611() throws Exception {
        checkVirtualSource("virtualSource/issue240611.html");
    }
    
    public void testIssue241870() throws Exception {
        checkVirtualSource("virtualSource/issue241870.html");
    }
    
    private void checkVirtualSource(final String testFile) throws Exception {
        Source testSource = getTestSource(getTestFile(testFile));
        
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Iterable<Embedding> embeddings = resultIterator.getEmbeddings();
                Embedding jsEmbedding = null;
                for (Embedding embedding : embeddings) {
                    if (embedding.getMimeType().equals("text/javascript")) {
                        jsEmbedding = embedding;
                        break;
                    }
                }
                assertNotNull("JS embeding was not found.", jsEmbedding);
                String text = jsEmbedding.getSnapshot().getText().toString();
                
                assertDescriptionMatches(testFile, text, true, ".vs.js");
            }
        });
    }
    
    public void xtestNgControllerFound() {
        FileObject index = getTestFile("angularTestProject/public_html/index.html");
        BaseDocument document = getDocument(index);
        JsEmbeddingProviderTest.assertEmbedding(document, "");
    }
    
}
