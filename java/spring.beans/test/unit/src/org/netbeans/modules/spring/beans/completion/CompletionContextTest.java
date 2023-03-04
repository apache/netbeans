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
