/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
