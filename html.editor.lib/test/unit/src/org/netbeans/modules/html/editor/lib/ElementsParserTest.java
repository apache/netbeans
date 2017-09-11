/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
