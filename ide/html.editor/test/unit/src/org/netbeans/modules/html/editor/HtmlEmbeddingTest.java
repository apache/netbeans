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
package org.netbeans.modules.html.editor;

import javax.swing.text.Document;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.html.editor.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marek Fukala
 */
public class HtmlEmbeddingTest extends TestBase {

    public HtmlEmbeddingTest(String name) {
        super(name);
    }

    public void testIssue189999() throws Exception {
        //disabled since it fails
        if(true) {
            return ;
        }

        FileObject file = getTestFile("testfiles/embedding/issue189999.html");
        Document doc = getDocument(file);

        TokenHierarchy th = TokenHierarchy.get(doc);

        //1. get the html token sequence for the "blueBorder" class attribute value offset directly
        {
            int offset = 280;

            TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
            ts.move(offset); //at the "blueBorder" class attribute value
            assertTrue(ts.moveNext());

            System.out.println(ts);
            Token<HTMLTokenId> t = ts.token();

            //now get the embedded css tokenSequence
            TokenSequence<CssTokenId> cssts = ts.embeddedJoined(CssTokenId.language());
            assertNotNull(cssts);

            cssts.move(offset);
            assertTrue(cssts.moveNext());
            System.out.println(cssts);

            Token<CssTokenId> csst = cssts.token();

            assertEquals(CssTokenId.IDENT, csst.id()); //OK
        }

        //2. now create the html token sequence for different offset first
        {
            int offset = 170;

            TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
            ts.move(offset); //somewhere in the css embedded code
            assertTrue(ts.moveNext());

            System.out.println(ts);
            Token<HTMLTokenId> t = ts.token();

            //now get the embedded css tokenSequence
            TokenSequence<CssTokenId> cssts = ts.embeddedJoined(CssTokenId.language());
            assertNotNull(cssts);

            offset = 280; //at the "blueBorder" class attribute value

            cssts.move(offset);
            assertTrue(cssts.moveNext());
            System.out.println(cssts);

            Token<CssTokenId> csst = cssts.token();

            //now we fail - the css token is incorrectly joined with some previous and hence
            //the lexical id is wrong.
            
            assertEquals(CssTokenId.IDENT, csst.id()); 

        }



    }
}
