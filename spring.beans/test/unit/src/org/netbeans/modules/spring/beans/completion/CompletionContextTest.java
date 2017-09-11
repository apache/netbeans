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

package org.netbeans.modules.spring.beans.completion;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.spring.beans.completion.CompletionContext.CompletionType;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class CompletionContextTest extends NbTestCase {

    public CompletionContextTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockMimeLookup.setInstances(MimePath.parse("text/xml"), XMLTokenId.language());
        super.setUp();
    }

    public void testAttributeValueCompletion() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl'/>");
        BaseDocument doc = TestUtils.createSpringXMLConfigDocument(config);
        CompletionContext ctx = new CompletionContext(doc, config.indexOf("'petStore'"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("petStore"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE_VALUE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("Store"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE_VALUE, "pet", "bean");
    }

    public void testAttributeCompletion() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore' p:ag   ='29'/>");
        BaseDocument doc = TestUtils.createSpringXMLConfigDocument(config);
        CompletionContext ctx = new CompletionContext(doc, config.indexOf("id='petStore"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("id='petStore"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("lass='org."), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "c", "bean");
        ctx = new CompletionContext(doc, config.indexOf(" ='29'"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");
    }

    // test for IZ#129020
    public void testAttributeCompletionAtTagEnd() throws Exception {
        // empty tag
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore' />");
        BaseDocument doc = TestUtils.createSpringXMLConfigDocument(config);
        CompletionContext ctx = new CompletionContext(doc, config.indexOf("/>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("/>") + 1, COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf(" />"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf("name"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "p:", "bean");

        config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore'/>");
        doc = TestUtils.createSpringXMLConfigDocument(config);
        ctx = new CompletionContext(doc, config.indexOf("/>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");

        // start tag
        config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore' ></bean>");
        doc = TestUtils.createSpringXMLConfigDocument(config);
        ctx = new CompletionContext(doc, config.indexOf("></bean>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "", "bean");
        ctx = new CompletionContext(doc, config.indexOf(" ></bean>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");

        config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore'></bean>");
        ctx = new CompletionContext(doc, config.indexOf("></bean>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.NONE, "", "bean");
    }

    // test for IZ#191651
    public void testAttributeCompletionAtSingleTagEnd() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl' p:name ='Sample Petstore' p:/>");
        BaseDocument doc = TestUtils.createSpringXMLConfigDocument(config);
        CompletionContext ctx = new CompletionContext(doc, config.indexOf("/>"), COMPLETION_QUERY_TYPE);
        assertContext(ctx, CompletionType.ATTRIBUTE, "p:", "bean");
    }

    private void assertContext(CompletionContext context, CompletionType expectedType,
            String expectedPrefix, String expectedTag) {
        assertEquals(expectedType, context.getCompletionType());
        assertEquals(expectedPrefix, context.getTypedPrefix());
        if(expectedTag == null) {
            assertNull(context.getTag());
        } else {
            assertEquals(expectedTag, context.getTag().getNodeName());
        }
    }
}
