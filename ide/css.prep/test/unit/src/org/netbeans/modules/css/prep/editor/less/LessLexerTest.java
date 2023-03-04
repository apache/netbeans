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
package org.netbeans.modules.css.prep.editor.less;

import org.netbeans.modules.css.prep.editor.less.LessCslLanguage;
import org.netbeans.modules.css.prep.editor.CPTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class LessLexerTest extends CslTestBase {
    
    public LessLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new LessCslLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/less";
    }
    
    public void testLexing() {
        FileObject testFile = getTestFile("testFiles/test.less");
        BaseDocument document = getDocument(testFile);
        final TokenHierarchy th = TokenHierarchy.get(document);
        document.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence tokenSequence = th.tokenSequence();
                assertTrue(tokenSequence.moveNext());
                //just one big fat token                
                Token token = tokenSequence.token();
                assertEquals(CPTokenId.CSS, token.id()); 
                
                //with embedded plain css tokens
                TokenSequence<CssTokenId> embedded = tokenSequence.embedded(CssTokenId.language());
                assertNotNull(embedded);
                
                assertFalse(tokenSequence.moveNext()); //no more tokens
            }
            
        });
        
    }
    
    
}
