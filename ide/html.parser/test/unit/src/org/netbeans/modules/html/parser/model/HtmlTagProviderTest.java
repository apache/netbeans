/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.html.parser.model;

import org.netbeans.modules.html.parser.model.HtmlTagProvider;
import java.net.URL;
import java.util.Collection;
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.netbeans.modules.html.parser.model.ElementDescriptor;

/**
 *
 * @author marekfukala
 */
public class HtmlTagProviderTest extends NbTestCase {

    public HtmlTagProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlDocumentation.setupDocumentationForUnitTests();
    }



    public void testHtmlTagConversion() {
        HtmlTag t = HtmlTagProvider.getTagForElement(ElementName.HTML.getName());
        assertNotNull(t);

        assertEquals(ElementDescriptor.HTML.getName(), t.getName());
        assertEquals(HtmlTagType.HTML, t.getTagClass());

        Collection<HtmlTag> children = t.getChildren();
        assertNotNull(children);
        assertTrue(children.contains(HtmlTagProvider.getTagForElement(ElementName.BODY.getName())));
        assertTrue(children.contains(HtmlTagProvider.getTagForElement(ElementName.HEAD.getName())));
        
        assertFalse(children.contains(HtmlTagProvider.getTagForElement(ElementName.VIDEO.getName())));

    }

    public void testTypes() {
        HtmlTag t = HtmlTagProvider.getTagForElement("body");
        assertNotNull(t);
        assertSame(HtmlTagType.HTML, t.getTagClass());

        t = HtmlTagProvider.getTagForElement("math");
        assertNotNull(t);
        assertSame(HtmlTagType.MATHML, t.getTagClass());

        t = HtmlTagProvider.getTagForElement("ellipse");
        assertNotNull(t);
        assertSame(HtmlTagType.SVG, t.getTagClass());

    }

    public void testHelp() {
        HtmlTag t = HtmlTagProvider.getTagForElement(ElementName.VIDEO.getName());
        assertNotNull(t);


        HelpItem helpItem = t.getHelp();
        assertNotNull(helpItem);

        HelpResolver help = helpItem.getHelpResolver();
        assertNotNull(help);

        String helpContent = help.getHelpContent(helpItem.getHelpURL());
        assertNotNull(helpContent);

//        System.out.println(helpContent);

    }

    public void testHelplessElements() {
        HtmlTag t = HtmlTagProvider.getTagForElement("ellipse");
        assertNotNull(t);

        HelpItem helpItem = t.getHelp();
        assertNull(helpItem);
    }
    
    public void testAnnotation_XML() {
        HtmlTag t = HtmlTagProvider.getTagForElement("annotation-xml");
        assertNotNull(t);

        assertSame(HtmlTagType.MATHML, t.getTagClass());

    }

}