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