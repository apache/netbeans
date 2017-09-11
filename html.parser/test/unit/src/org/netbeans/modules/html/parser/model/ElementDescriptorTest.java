/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.parser.model;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.HtmlDocumentation;

/**
 *
 * @author marekfukala
 */
public class ElementDescriptorTest extends NbTestCase {

    public ElementDescriptorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlDocumentation.setupDocumentationForUnitTests();
    }



    public void testNamePattern() {
        assertEquals("title", Attribute.parseName("title"));
        assertEquals("href", Attribute.parseName("attr-link-href"));
        assertEquals("http-equiv", Attribute.parseName("attr-meta-http-equiv"));
    }

    public void testBodyElement() {
        ElementDescriptor body = ElementDescriptor.forName("body");
        assertNotNull(body);

        assertEquals("body", body.getName());

        Collection<ElementDescriptor> directChildren = body.getChildrenElements();
        assertNotNull(directChildren);
        assertTrue(directChildren.isEmpty());

        Collection<ContentType> contentTypes = body.getChildrenTypes();
        assertNotNull(contentTypes);
        assertTrue(contentTypes.contains(ContentType.FLOW));

        assertFalse(body.isEmpty());
    }

    public void testHtmlElement() {
        ElementDescriptor html = ElementDescriptor.forName("html");
        assertNotNull(html);

        assertEquals("html", html.getName());

        Collection<ElementDescriptor> directChildren = html.getChildrenElements();
        assertNotNull(directChildren);
        assertTrue(directChildren.contains(ElementDescriptor.BODY));
        assertTrue(directChildren.contains(ElementDescriptor.HEAD));

        Collection<ContentType> contentTypes = html.getChildrenTypes();
        assertNotNull(contentTypes);
        assertTrue(contentTypes.isEmpty());

        assertFalse(html.isEmpty());
    }

    public void testHeadElement() {
        ElementDescriptor head = ElementDescriptor.forName("head");
        assertNotNull(head);

        assertEquals("head", head.getName());

        Collection<ElementDescriptor> directChildren = head.getChildrenElements();
        assertNotNull(directChildren);
        assertTrue(directChildren.isEmpty());
        
        Collection<ContentType> contentTypes = head.getChildrenTypes();
        assertNotNull(contentTypes);

        assertTrue(contentTypes.contains(ContentType.METADATA));
        assertFalse(head.isEmpty());
    }

    public void testBr() {
        ElementDescriptor br = ElementDescriptor.forName("br");
        assertNotNull(br);
        assertTrue(br.isEmpty());
    }

    public void testTitleEmpty() {
        ElementDescriptor title = ElementDescriptor.forName("title");
        assertNotNull(title);
        assertFalse(title.isEmpty());
    }

    public void testMathML() {
        ElementDescriptor math = ElementDescriptor.forName("math");
        assertNotNull(math);

        assertEquals("math", math.getName());
        Collection<ContentType> cats = math.getCategoryTypes();

        assertTrue(cats.contains(ContentType.EMBEDDED));
        assertTrue(cats.contains(ContentType.FLOW));
        assertTrue(cats.contains(ContentType.PHRASING));
    }

    public void testSVG() {
        ElementDescriptor math = ElementDescriptor.forName("ellipse");
        assertNotNull(math);

        assertEquals("ellipse", math.getName());
        Collection<ContentType> cats = math.getCategoryTypes();

        assertTrue(cats.contains(ContentType.EMBEDDED));
        assertTrue(cats.contains(ContentType.FLOW));
        assertTrue(cats.contains(ContentType.PHRASING));

    }

    public void testAnnotation_XML() {
        ElementDescriptor e = ElementDescriptor.forName("annotation_xml");
        assertNotNull(e);

        assertEquals("annotation-xml", e.getName());
        assertSame(HtmlTagType.MATHML, e.getTagType());

    }

    public void testAttributes() {
        Collection<String> attrs = ElementDescriptor.getAttrNamesForElement("div"); //only global attrs

        assertNotNull(attrs);
        assertTrue(attrs.contains("class"));
        assertTrue(attrs.contains("onclick"));

        assertFalse(attrs.contains("bla"));
        assertFalse(attrs.contains("href"));

        attrs = ElementDescriptor.getAttrNamesForElement("meta"); //some specific attrs as well

        assertNotNull(attrs);
        assertTrue(attrs.contains("class"));
        assertTrue(attrs.contains("onclick"));
        assertTrue(attrs.contains("http-equiv"));

        assertFalse(attrs.contains("bla"));
        assertFalse(attrs.contains("href"));
    }

    public void testAllElementsHasHelpContent() {
        for(ElementDescriptor d : ElementDescriptor.values()) {
            if(d.getTagType() != HtmlTagType.HTML) {
                continue;
            }
//            System.err.println("tag: " + d.getName());
            HtmlTag tag = HtmlTagProvider.getTagForElement(d.getName());
            assertNotNull(tag);

            HelpItem item = tag.getHelp();
            assertNotNull(item);

            URL helpUrl = item.getHelpURL();
            assertNotNull(helpUrl);

            HelpResolver resolver = item.getHelpResolver();
            assertNotNull(resolver);

            String helpContent = resolver.getHelpContent(helpUrl);
            assertNotNull(String.format("No help content for tag %s, help url is %s ", d.getName(), helpUrl.toExternalForm()), helpContent);

        }
    }


    public void testElementsWithEmptyContent() {
        assertFalse(ElementDescriptor.A.isEmpty());
        assertFalse(ElementDescriptor.COLGROUP.isEmpty());

        assertTrue(ElementDescriptor.COL.isEmpty());
    }

    public void testElementsOptionalEndTag() {
        assertFalse(ElementDescriptor.A.hasOptionalEndTag());

        assertTrue(ElementDescriptor.COLGROUP.hasOptionalEndTag());
        assertTrue(ElementDescriptor.COL.hasOptionalEndTag());
    }
    
    //not real unit tests - just for generation of the html tag names for org.netbeans.modules.css.editor.HtmlTags
    public void test_GenerateAllElementNamesArray() {
        Collection<String> elementNames = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String t, String t1) {
                return t.compareTo(t1);
            }
        });
        for(ElementDescriptor ed : ElementDescriptor.values()) {
            elementNames.add(ed.getName());
        }
        StringBuilder list = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for(String eName : elementNames) {
            if(line.length() > 60) {
                list.append(line);
                list.append('\n');
                line = new StringBuilder();
            }
            line.append(String.format("\"%s\", ", eName));
        }
        list.append(line);
//        System.out.println(list);
    }

}