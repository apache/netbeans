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
package org.netbeans.modules.html.editor.lib;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class ElementsParserTest extends CslTestBase {

    public ElementsParserTest(String name) {
        super(name);
    }

    public void testBasic() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParser parser = ElementsParser.forTokenIndex(code, ts, 0);
        assertTrue(parser.hasNext());

        Element e = parser.next();
        assertNotNull(e);

        assertEquals(ElementType.OPEN_TAG, e.type());
        assertEquals(0, e.from());
        assertEquals(5, e.to());

        assertTrue(parser.hasNext());

        e = parser.next();
        assertNotNull(e);

        assertEquals(ElementType.CLOSE_TAG, e.type());
        assertEquals(5, e.from());
        assertEquals(11, e.to());

    }

    public void testParseFromMiddleOfTheSource() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParser parser = ElementsParser.forOffset(code, ts, 5);
        assertTrue(parser.hasNext());

        Element e = parser.next();
        assertNotNull(e);

        assertEquals(ElementType.CLOSE_TAG, e.type());
        assertEquals(5, e.from());
        assertEquals(11, e.to());

    }

    public void testParseFromIncorrectPosition() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        //the parser must be always started at token beginning!
        try {
            ElementsParser.forOffset(code, ts, 6);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            //correct, the exception should be thrown
        }
    }

    public void testParseNegativePosition() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        try {
            ElementsParser.forOffset(code, ts, -1);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            //correct, the exception should be thrown
        }
    }

    public void testParseFromTheEnd() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParser parser = ElementsParser.forOffset(code, ts, 11);
        assertFalse(parser.hasNext());

    }

    public void testPerformance() throws IOException {
        FileObject file = getTestFile("testfiles/huge.html");
        String content = file.asText();

        TokenHierarchy hi = TokenHierarchy.create(content, HTMLTokenId.language());
        ElementsParser parser = ElementsParser.forTokenIndex(content, hi.tokenSequence(HTMLTokenId.language()), 0);

        long start = System.currentTimeMillis();
        while (parser.hasNext()) {
            parser.next();
        }
        long end = System.currentTimeMillis();

        float diff1 = end - start;
        System.out.println("first iteration took " + diff1 + "ms.");

        //~2500ms

        //second attempt

        parser = ElementsParser.forTokenIndex(content, hi.tokenSequence(HTMLTokenId.language()), 0);
        start = System.currentTimeMillis();
        while (parser.hasNext()) {
            parser.next();
        }
        end = System.currentTimeMillis();

        float diff2 = end - start;
        System.out.println("second iteration took " + diff2 + "ms.");

        //~600ms

        float ratio = diff1 / diff2;
        System.out.println("first / second ratio = " + ratio);
    }

    public void testParseELInAttributeValue() {
        String code = "<div ng-click=\"pre{{call()}}post\"></div>";
        //             01234567890123 456789012345678901 234567890
        //             0         1          2         3  
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParser parser = ElementsParser.forTokenIndex(code, ts, 0);
        assertTrue(parser.hasNext());
        Element element = parser.next();
        assertEquals(ElementType.OPEN_TAG, element.type());
        OpenTag ot = (OpenTag) element;
        assertEquals(0, ot.from());
        assertEquals(34, ot.to());
        
        Collection<Attribute> attributes = ot.attributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        Attribute a = attributes.iterator().next();
        assertEquals(14, a.valueOffset());
        assertEquals(33, a.valueOffset() + a.value().length());

    }
    
    @MimeRegistration(mimeType = "text/html", service = HtmlLexerPlugin.class)
    public static class TestHtmlLexerPlugin extends HtmlLexerPlugin {

        @Override
        public String getOpenDelimiter() {
            return "{{";
        }

        @Override
        public String getCloseDelimiter() {
            return "}}";
        }
        
    }
    
}
