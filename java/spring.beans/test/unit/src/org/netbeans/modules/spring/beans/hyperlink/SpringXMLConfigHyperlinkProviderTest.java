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

package org.netbeans.modules.spring.beans.hyperlink;

import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spring.beans.TestUtils;

/**
 *
 * @author Andrei Badea, Rohan Ranade
 */
public class SpringXMLConfigHyperlinkProviderTest extends NbTestCase {

    private SpringXMLConfigHyperlinkProvider hyperlinkProvider;
    
    public SpringXMLConfigHyperlinkProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        hyperlinkProvider = new SpringXMLConfigHyperlinkProvider();
    }

    public void testBeanHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='propertyConfigurer' " +
                "class='org.dummy.config.PropertyConfigurer' " +
                "parent='dummyBean' " +
                "depends-on='initialBean,initialBean2' " +
                "factory-method='getInstance' " +
                "init-method='myInitMethod' " +
                "destroy-method='myDestroyMethod' " +
                "p:location='/WEB-INF/jdbc.properties'" +
                "p:foobar-ref='sampleBeanRef'/>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "org.dummy.config.PropertyConfigurer");
        assertHyperlink(testDoc, "getInstance");
        assertHyperlink(testDoc, "dummyBean");
        assertHyperlink(testDoc, "initialBean");
        assertHyperlink(testDoc, "initialBean2");
        assertHyperlink(testDoc, "myInitMethod");
        assertHyperlink(testDoc, "myDestroyMethod");
        assertHyperlink(testDoc, "p:location");
        assertHyperlink(testDoc, "p:foobar-ref");
        assertHyperlink(testDoc, "sampleBeanRef");
    }

    public void testImportHyperlink() throws Exception {
        String config = TestUtils.createXMLConfigText("<import resource='/WEB-INF/applicationContext.xml'/>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "/WEB-INF/applicationContext.xml");
    }
    
    public void testRefHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl'>" +
                "<constructor-arg>" +
                "<ref bean='sampleBean'/>" +
                "</constructor-arg>" +
                "<property name='accountDao'>" +
                "<ref bean='foobarBean'/>" + 
                "</property>" +
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "sampleBean");
        assertHyperlink(testDoc, "foobarBean");
    }
    
    public void testIdRefHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl'>" +
                "<property name='accountDao'>" +
                "<idref bean='foobarBean'/>" + 
                "</property>" +
                "<property name='accountDao2'>" +
                "<idref local='localBean'/>" + 
                "</property>" +
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "localBean");
        assertHyperlink(testDoc, "foobarBean");
    }
    
    public void testConstructorArgHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl'>" +
                "<constructor-arg ref='sampleBean'/>" +
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "sampleBean");
    }

    public void testPropertyHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='petStore' " +
                "class='org.springframework.PetStoreImpl'>" +
                "<property name='accountDao' ref='accountBean'>" +
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "accountDao");
        assertHyperlink(testDoc, "accountBean");
    }
    
    public void testAliasHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<alias name='foo' alias='bar'");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "foo");
    }
    
    public void testLookupMethodHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='commandManager' class='fiona.apple.CommandManager'>" +
                "<lookup-method name='createCommand' bean='commandBean'/>" +
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "createCommand");
        assertHyperlink(testDoc, "commandBean");
    }
    
    public void testReplacedMethodHyperlinks() throws Exception {
        String config = TestUtils.createXMLConfigText("<bean id='myValueCalculator' class='x.y.z.MyValueCalculator'>" +
                "<replaced-method name='computeValue' replacer='replacementComputeValue'>" +
                "<arg-type>String</arg-type>" + 
                "</replaced-method>" + 
                "</bean>");
        BaseDocument testDoc = TestUtils.createSpringXMLConfigDocument(config);
        assertHyperlink(testDoc, "computeValue");
        assertHyperlink(testDoc, "replacementComputeValue");
    }

    private void assertHyperlink(BaseDocument testDoc, String hyperlink) throws Exception {
        String contents = testDoc.getText(0, testDoc.getLength());
        int offset = contents.indexOf(hyperlink);
        assertTrue(hyperlinkProvider.isHyperlinkPoint(testDoc, offset));
        int[] span = hyperlinkProvider.getHyperlinkSpan(testDoc, offset);
        assertEquals(offset, span[0]);
        assertEquals(offset + hyperlink.length(), span[1]);
    }
}
