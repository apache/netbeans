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
