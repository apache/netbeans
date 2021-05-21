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

package org.netbeans.spi.xml.cookies;

import java.net.URL;
import junit.framework.TestCase;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.xml.sax.InputSource;

/**
 * Trivial golden type support tests.
 * <p>
 * It tests class that is exposed by CheckXMLSupport
 * or ValidateXMLSupport.
 *
 * @author Petr Kuzel
 */
public class SharedXMLSupportTest extends TestCase {

    public SharedXMLSupportTest(String testName) {
        super(testName);
    }

    /** Test of checkXML method, of class org.netbeans.spi.xml.cookies.SharedXMLSupport. */
    public void testCheckXML() {
        System.out.println("testCheckXML");
                
        URL dtd = getClass().getResource("data/DTD.dtd");
        URL entity = getClass().getResource("data/Entity.ent");
        URL invalidDTD = getClass().getResource("data/InvalidDTD.dtd");
        URL invalidDocument = getClass().getResource("data/InvalidDocument.xml");
        URL invalidEntity = getClass().getResource("data/InvalidEntity.ent");
        URL validDocument = getClass().getResource("data/ValidDocument.xml");
        URL wellformedDocument = getClass().getResource("data/WellformedDocument.xml");
        URL namespacesDocument = getClass().getResource("data/NamespacesDocument.xml");
        
        CheckXMLSupport support;
        support = new CheckXMLSupport(new InputSource(dtd.toExternalForm()), CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE);
        assertTrue("DTD check failed!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(entity.toExternalForm()), CheckXMLSupport.CHECK_ENTITY_MODE);
        assertTrue("Entity check failed!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(invalidDTD.toExternalForm()), CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE);
        assertTrue("Invalid DTD must not pass!",  support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(invalidDocument.toExternalForm()));
        assertTrue("Invalid document must not pass", support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(invalidEntity.toExternalForm()), CheckXMLSupport.CHECK_ENTITY_MODE);
        assertTrue("Invalid rntity must not pass!", support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(validDocument.toExternalForm()));
        assertTrue("Valid document must pass!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(wellformedDocument.toExternalForm()));
        assertTrue("Wellformed document must pass", support.checkXML(null));

        Observer observer = new Observer();
        support = new CheckXMLSupport(new InputSource(namespacesDocument.toExternalForm()));
        assertTrue("Wellformed document with namespaces must pass", support.checkXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);
        
    }
    
    /** Test of validateXML method, of class org.netbeans.spi.xml.cookies.SharedXMLSupport. */
    public void testValidateXML() {
        System.out.println("testValidateXML");

        URL dtd = getClass().getResource("data/DTD.dtd");
        URL entity = getClass().getResource("data/Entity.ent");
        URL invalidDTD = getClass().getResource("data/InvalidDTD.dtd");
        URL invalidDocument = getClass().getResource("data/InvalidDocument.xml");
        URL invalidEntity = getClass().getResource("data/InvalidEntity.ent");
        URL validDocument = getClass().getResource("data/ValidDocument.xml");
        URL wellformedDocument = getClass().getResource("data/WellformedDocument.xml");
        URL validNamespacesDocument = getClass().getResource("data/ValidNamespacesDocument.xml");
        URL conformingNamespacesDocument = getClass().getResource("data/ConformingNamespacesDocument.xml");
        
        SharedXMLSupport support;
        support = new ValidateXMLSupport(new InputSource(dtd.toExternalForm()));
        assertTrue("DTD validation must fail!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(entity.toExternalForm()));
        assertTrue("Entity validation must fail!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidDTD.toExternalForm()));
        assertTrue("Invalid DTD must not pass!",  support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidDocument.toExternalForm()));
        assertTrue("Invalid document must not pass", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidEntity.toExternalForm()));
        assertTrue("Invalid rntity must not pass!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(validDocument.toExternalForm()));
        assertTrue("Valid document must pass!", support.validateXML(null));

        support = new ValidateXMLSupport(new InputSource(wellformedDocument.toExternalForm()));
        assertTrue("Wellformed document must not pass", support.validateXML(null) == false);

        Observer observer = new Observer();
        support = new ValidateXMLSupport(new InputSource(validNamespacesDocument.toExternalForm()));
        assertTrue("Valid document with namespaces must pass", support.validateXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);

        observer = new Observer();
        support = new ValidateXMLSupport(new InputSource(conformingNamespacesDocument.toExternalForm()));
        assertTrue("Conforming document must pass", support.validateXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);
        
    }
    
    private static class Observer implements CookieObserver {
        private int warnings;
        public void receive(CookieMessage msg) {
            if (msg.getLevel() >= CookieMessage.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }
    };
        
}
