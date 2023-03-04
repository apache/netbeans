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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.HtmlDocumentation;

/**
 *
 * @author marekfukala
 */
public class AttributeTest extends NbTestCase {

    public AttributeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlDocumentation.setupDocumentationForUnitTests();
    }



    public void testBasic() {
        ElementDescriptor div = ElementDescriptor.forName("div");
        assertNotNull(div);
        assertEquals("div", div.getName());
        assertEquals("http://www.whatwg.org/specs/web-apps/current-work/multipage/" + ElementDescriptor.DIV.getHelpLink(),
                div.getHelpUrl().toExternalForm());

        Collection<ContentType> cats = div.getCategoryTypes();
        assertNotNull(cats);
        assertTrue(cats.contains(ContentType.FLOW));
        assertFalse(cats.contains(ContentType.METADATA));
        Collection<FormAssociatedElementsCategory> fasecs = div.getFormCategories();
        assertTrue(fasecs.isEmpty());


        Collection<ContentType> parentTypes = div.getParentTypes();
        assertNotNull(parentTypes);
        assertTrue(parentTypes.contains(ContentType.FLOW));
        assertFalse(cats.contains(ContentType.METADATA));

        Collection<ElementDescriptor> parentElements = div.getParentElements();
        assertTrue(parentElements.isEmpty());

        Collection<ContentType> childrenTypes = div.getChildrenTypes();
        assertNotNull(childrenTypes);
        assertTrue(parentTypes.contains(ContentType.FLOW));
        assertFalse(cats.contains(ContentType.METADATA));

        Collection<ElementDescriptor> childrenElements = div.getChildrenElements();
        assertTrue(childrenElements.isEmpty());

        Link domInterface = div.getDomInterface();
        assertNotNull(domInterface);
        assertEquals("HTMLDivElement", domInterface.getName());

    }

    public void testEachAttributesHasSomeHelp() {
        ElementDescriptor a = ElementDescriptor.A;
        Collection<Attribute> attrs = a.getAttributes();

        for(Attribute attr : attrs) {

            String link = attr.getHelpLink();
            assertNotNull(link);
            URL url = HtmlDocumentation.getDefault().resolveLink(link);
            assertNotNull(url);
            String content = HtmlDocumentation.getDefault().getHelpContent(url);

            if(content == null) {
                System.err.println(String.format("Attribute %s is missing help content for the URL %s", attr.getName(), url));
            }
        }

    }

    public void testNonExisting() {
        ElementDescriptor el = ElementDescriptor.forName("nosuchelement");
        assertNull(el);

    }

}