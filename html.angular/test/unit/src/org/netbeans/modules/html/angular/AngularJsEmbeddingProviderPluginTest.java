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
