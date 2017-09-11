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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.html.editor.lib.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.test.TestBase;

/**
 * @author Marek Fukala
 */
public class SyntaxAnalyzerTest extends TestBase {

    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());

    public SyntaxAnalyzerTest(String testName) throws IOException, BadLocationException {
        super(testName);
    }


    public static Test xsuite() throws IOException, BadLocationException {
        TestSuite suite = new TestSuite();
        suite.addTest(new SyntaxAnalyzerTest("testParseBrokenSource"));
        suite.addTest(new SyntaxAnalyzerTest("testParseBrokenSource2"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        Logger.getLogger(SyntaxAnalyzer.class.getName()).setLevel(Level.FINE);
        MockServices.setServices(MockMimeLookup.class);
    }

    public void testOpenTag() throws BadLocationException {
        String text = "<div>";
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertFalse(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

    }

    public void testEndTag() throws BadLocationException {
        String text = "</div>";
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.CLOSE_TAG, div.type());
        assertTrue(div instanceof CloseTag);

        CloseTag divTag = (CloseTag) div;

        assertEquals("div", divTag.name());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

    }

    public void testTagWithOneAttribute() throws BadLocationException {
        String text = "<div align=\"center\"/>";
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertTrue(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        Attribute align = attributes.iterator().next();

        assertEquals("align", align.name().toString());
        assertEquals(5, align.nameOffset());
        assertEquals("\"center\"", align.value().toString());
        assertEquals(11, align.valueOffset());

    }
    
    public void testTagWithOneCssEmbeddingAttribute() throws BadLocationException {
        String text = "<div class=\"myclass\"/>";
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertTrue(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        Attribute align = attributes.iterator().next();

        assertEquals("class", align.name().toString());
        assertEquals(5, align.nameOffset());
        assertEquals("\"myclass\"", align.value().toString());
        assertEquals(11, align.valueOffset());

    }

    public void testTagWithUnquotedAttribute() throws BadLocationException {
        String text = "<div align=center/>";
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertTrue(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        Attribute align = attributes.iterator().next();

        assertEquals("align", align.name().toString());
        assertEquals(5, align.nameOffset());
        assertEquals("center", align.value().toString());
        assertEquals(11, align.valueOffset());

    }

    //+ new line and tab in the tag and whitespaces around the equal operator
    public void testTagWithOneAttribute2() throws BadLocationException {
        String text = "<div \t \n align =\t \"center\"/>";
        //             012345 67 890123456 78 9012345 678
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertTrue(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        Attribute align = attributes.iterator().next();

        assertEquals("align", align.name().toString());
        assertEquals(9, align.nameOffset());
        assertEquals("\"center\"", align.value().toString());
        assertEquals(18, align.valueOffset());

    }

    public void testTagWithMoreAttributes() throws BadLocationException {
        String text = "<div align=\"center\" \t\n title=\"mydiv\" />";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        assertEquals("div", divTag.name());
        assertTrue(divTag.isEmpty());
        assertEquals(0, divTag.from());
        assertEquals(text.length(), divTag.to() - divTag.from());
        assertEquals(text, divTag.image());

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(2, attributes.size());

        Iterator<Attribute> itr = attributes.iterator();
        Attribute attr = itr.next();

        assertEquals("align", attr.name().toString());
        assertEquals(5, attr.nameOffset());
        assertEquals("\"center\"", attr.value().toString());
        assertEquals(11, attr.valueOffset());

        attr = itr.next();

        assertEquals("title", attr.name().toString());
        assertEquals(23, attr.nameOffset());
        assertEquals("\"mydiv\"", attr.value().toString());
        assertEquals(29, attr.valueOffset());

    }

    public void testEntityReference() throws BadLocationException {

        String text = "&nbsp; &amp;";
        //             012345678901
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(3, elements.size());

        Element e1 = elements.get(0);
        Element e2 = elements.get(2);

        assertNotNull(e1);
        assertNotNull(e2);

        assertEquals(ElementType.ENTITY_REFERENCE, e1.type());
        assertEquals(ElementType.ENTITY_REFERENCE, e2.type());

        assertEquals(0, e1.from());
        assertEquals(7, e2.from());

        assertEquals(6, e1.to() - e1.from());
        assertEquals(5, e2.to() - e2.from());

        assertEquals("&nbsp;", e1.image());
        assertEquals("&amp;", e2.image());

    }

    public void testComment() throws BadLocationException {
        String text = "<!-- comment -->";
        //             01234567890123456
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.COMMENT, e1.type());

        assertEquals(0, e1.from());

        assertEquals(text.length(), e1.to() - e1.from());

        assertEquals(text, e1.image());

    }

    public void testMultipleComments() throws BadLocationException {
        String comments = "<!-- comment1 --><!-- comment2 -->";
        String text = comments + "\n";

        //"<!-- comment1 --><!-- comment2 -->\n"
        //             01234567890123456
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(2, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.COMMENT, e1.type());

        assertEquals(0, e1.from());

        assertEquals(comments.length(), e1.to() - e1.from());

        assertEquals(comments, e1.image());

    }

    public void testMultipleCommentsSeparated() throws BadLocationException {
        String comment1 = "<!-- comment1 -->";
        String comment2 = "<!-- comment2 -->";
        String text = comment1 + "\t\n " + comment2 + "\n";
        //             <!-- comment1 -->\t\n <!-- comment2 -->\n
        //             012345678901234567 8 9012345678901234567 890
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(4, elements.size());

        Element e1 = elements.get(0);
        Element e2 = elements.get(2);

        assertNotNull(e1);
        assertNotNull(e2);

        assertEquals(ElementType.COMMENT, e1.type());
        assertEquals(ElementType.COMMENT, e2.type());

        assertEquals(0, e1.from());
        assertEquals(20, e2.from());

        assertEquals(comment1.length(), e1.to() - e1.from());
        assertEquals(comment2.length(), e2.to() - e2.from());

        assertEquals(comment1, e1.image());
        assertEquals(comment2, e2.image());

    }

    public void testSimpleSGMLDeclaration() throws BadLocationException {
        String text = "<!X Y Z>";
        //             0123456789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.DECLARATION, e1.type());

        Declaration declaration = (Declaration) e1;

        assertEquals(0, e1.from());

        assertEquals(text.length(), e1.to() - e1.from());

        assertEquals(text, e1.image());

        assertNull(declaration.rootElementName());
        assertNull(declaration.publicId());
        assertNull(declaration.systemId());

    }

    public void testSGMLDeclaration() throws BadLocationException {
        String text = "<!X -- comment -- Y \n \t Z>";
        //             0123456789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.DECLARATION, e1.type());

        Declaration declaration = (Declaration) e1;

        assertEquals("X", declaration.declarationName());

        assertEquals(0, e1.from());

        assertEquals(text.length(), e1.to() - e1.from());

        assertEquals(text, e1.image());

        assertNull(declaration.rootElementName());
        assertNull(declaration.publicId());
        assertNull(declaration.systemId());

    }

    public void testDoctype() throws BadLocationException {
        String text = "<!DOCTYPE html \t PUBLIC \"id part 2\" \n \"file\">";
        //             0123456789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.DECLARATION, e1.type());

        Declaration declaration = (Declaration) e1;

        assertEquals(0, e1.from());

        assertEquals(text.length(), e1.to() - e1.from());

        assertEquals(text, e1.image());

        assertEquals("html", declaration.rootElementName());
        assertEquals("id part 2", declaration.publicId());
        assertEquals("\"file\"", declaration.systemId());

    }

    public void testCorruptedDoctype() throws BadLocationException {
        String text = "<!DOCTYP html>";
        //             0123456789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.DECLARATION, e1.type());

        Declaration decl = (Declaration)e1;
        assertEquals("DOCTYP", decl.declarationName());


    }

    public void testDoctypeSimplePublicId() throws BadLocationException {
        String text = "<!DOCTYPE html \t PUBLIC \"simpleid\" \n \"file\">";
        //             0123456789

        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(ElementType.DECLARATION, e1.type());

        Declaration declaration = (Declaration) e1;

//        assertTrue(declaration.isValidDoctype());

        assertEquals(0, e1.from());

        assertEquals(text.length(), e1.to() - e1.from());

        assertEquals(text, e1.image());

        assertEquals("html", declaration.rootElementName());
        assertEquals("simpleid", declaration.publicId());
        assertEquals("\"file\"", declaration.systemId());

    }

    public void testTagWithStyleAttributes() throws BadLocationException {
        String text = "<div style=\"color:red\"/>";
        //             012345678901 2345678 90 1 23456789 012345 6789

        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        OpenTag divTag = (OpenTag) div;

        Collection<Attribute> attributes = divTag.attributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        Attribute attr = attributes.iterator().next();

        assertEquals("style", attr.name().toString());
        assertEquals(5, attr.nameOffset());
        assertEquals("\"color:red\"", attr.value().toString());
        assertEquals(11, attr.valueOffset());


    }

    public void testParseText() throws BadLocationException {
        String text = "<div>text</div>last";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals("Unexpected number of elements: " + elementsToString(elements), 4, elements.size());

        Element se = elements.get(1);
        assertNotNull(se);
        assertEquals(ElementType.TEXT, se.type());

        assertEquals(null, se.image());

        se = elements.get(3);
        assertNotNull(se);
        assertEquals(ElementType.TEXT, se.type());
        assertEquals(null, se.image());
    }

    public void testParseNewLineText() throws BadLocationException {
        String text = "<div>\n</div>\nlast";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals("Unexpected number of elements: " + elementsToString(elements), 4, elements.size());

        Element se = elements.get(1);
        assertNotNull(se);
        assertEquals(ElementType.TEXT, se.type());

        se = elements.get(3);
        assertNotNull(se);
        assertEquals(ElementType.TEXT, se.type());
    }

    public void testParseBrokenSource() throws BadLocationException {
        String text = "<div></";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(2, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        Element error = elements.get(1);

        assertNotNull(error);
        assertEquals(ElementType.ERROR, error.type());

    }

    public void testParseBrokenSource2() throws BadLocationException {
        String text = "<div></</div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(3, elements.size());

        Element se = elements.get(0);
        assertNotNull(se);
        assertEquals(ElementType.OPEN_TAG, se.type());
        assertTrue(se instanceof OpenTag);

        se = elements.get(1);
        assertNotNull(se);
        assertEquals(ElementType.ERROR, se.type());

        se = elements.get(2);
        assertNotNull(se);
        assertEquals(ElementType.CLOSE_TAG, se.type());
        assertTrue(se instanceof CloseTag);

    }

    //we do not properly parse the end tag here
    public void testParseBrokenOpenTag() throws BadLocationException {
        String text = "<div align= </div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        System.out.println(elementsToString(elements));

        assertNotNull(elements);
        assertEquals(2, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        Element endtag = elements.get(1);

        assertNotNull(endtag);
        assertEquals(ElementType.CLOSE_TAG, endtag.type());

    }

    //in this case the end tag should be properly parsed
    public void testParseBrokenOpenTag2() throws BadLocationException {
        String text = "<div align=@# </div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        System.out.println(elementsToString(elements));

        assertNotNull(elements);
        assertEquals(2, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

        Element endtag = elements.get(1);

        assertNotNull(endtag);
        assertEquals(ElementType.CLOSE_TAG, endtag.type());

    }

    public void testParseBrokenOpenTagAtFileEnd() throws BadLocationException {
        String text = "<div align=";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);

    }

    //we do not properly parse the end tag here
    public void testParseUnfinishedOpenTag() throws BadLocationException {
        String text = "<col";
        //             01234567
        List<Element> elements = getSyntaxElements(text);

        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element div = elements.get(0);

        assertNotNull(div);
        assertEquals(ElementType.OPEN_TAG, div.type());
        assertTrue(div instanceof OpenTag);
        assertEquals("<col", div.image());

    }

    private List<Element> getSyntaxElements(String code) {
        HtmlSource source = new HtmlSource(code);
        //wrap with list so I do not have to rewrite all the tests
        List<Element> list = new ArrayList<>(SyntaxAnalyzer.create(source).elements().items());
        return list;
    }

    private static String elementsToString(List<Element> elements) {
        StringBuilder sb = new StringBuilder();
        for(Element se : elements) {
            sb.append(se.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


}
