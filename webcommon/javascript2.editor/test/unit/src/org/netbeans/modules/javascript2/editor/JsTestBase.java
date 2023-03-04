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

package org.netbeans.modules.javascript2.editor;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.editor.index.JsIndexer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;

/**
 * @author Tor Norbye
 */
public abstract class JsTestBase extends CslTestBase {
    
    public static String JS_SOURCE_ID = "classpath/js-source"; // NOI18N
    
    public JsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TestJsLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new JsIndexer.Factory();
    }

    public static class TestJsLanguage extends JsLanguage {

        public TestJsLanguage() {
            super();
        }

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(JS_SOURCE_ID);
        }
        
        
        
    }

    @Override
    protected void setUp() throws Exception {
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        super.setUp();
    }

    @Override
    public void checkCompletion(String file, String caretLine, boolean includeModifiers) throws Exception {
        waitScanningFinished();
        super.checkCompletion(file, caretLine, includeModifiers);
    }

    @Override
    protected void checkDeclaration(String relFilePath, String caretLine, String file, int offset) throws Exception {
        waitScanningFinished();
        super.checkDeclaration(relFilePath, caretLine, file, offset);
    }

    @Override
    protected void checkDeclaration(String relFilePath, String caretLine, String declarationLine) throws Exception {
        waitScanningFinished();
        super.checkDeclaration(relFilePath, caretLine, declarationLine);
    }

    @Override
    protected void checkDeclaration(String relFilePath, String caretLine, URL url) throws Exception {
        waitScanningFinished();
        super.checkDeclaration(relFilePath, caretLine, url);
    }

    @Override
    protected void checkOccurrences(String relFilePath, String caretLine, boolean symmetric) throws Exception {
        waitScanningFinished();
        super.checkOccurrences(relFilePath, caretLine, symmetric);
    }

    @Override
    protected void checkStructure(String relFilePath, boolean embedded, boolean inTestDir, boolean includePositions) throws Exception {
        waitScanningFinished();
        super.checkStructure(relFilePath, embedded, inTestDir, includePositions);
    }

    @Override
    protected void checkStructure(String relFilePath) throws Exception {
        waitScanningFinished();
        super.checkStructure(relFilePath);
    }

    @Override
    protected void checkSemantic(String relFilePath) throws Exception {
        waitScanningFinished();
        super.checkSemantic(relFilePath);
    }

    @Override
    protected void checkSemantic(String relFilePath, String caretLine) throws Exception {
        waitScanningFinished();
        super.checkSemantic(relFilePath, caretLine);
    }

    @SuppressWarnings("SleepWhileInLoop")
    protected void waitScanningFinished() {
        while(IndexingUtils.isScanInProgress()) {
            if(IndexingUtils.getIndexingState().contains(RepositoryUpdater.IndexingState.STARTING)) {
                RepositoryUpdater.getDefault().start(true);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }
}
