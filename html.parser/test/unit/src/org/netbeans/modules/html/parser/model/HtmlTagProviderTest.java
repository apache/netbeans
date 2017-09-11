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
        HtmlTag t = HtmlTagProvider.getTagForElement(ElementName.HTML.name);
        assertNotNull(t);

        assertEquals(ElementDescriptor.HTML.getName(), t.getName());
        assertEquals(HtmlTagType.HTML, t.getTagClass());

        Collection<HtmlTag> children = t.getChildren();
        assertNotNull(children);
        assertTrue(children.contains(HtmlTagProvider.getTagForElement(ElementName.BODY.name)));
        assertTrue(children.contains(HtmlTagProvider.getTagForElement(ElementName.HEAD.name)));
        
        assertFalse(children.contains(HtmlTagProvider.getTagForElement(ElementName.VIDEO.name)));

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
        HtmlTag t = HtmlTagProvider.getTagForElement(ElementName.VIDEO.name);
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