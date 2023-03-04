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