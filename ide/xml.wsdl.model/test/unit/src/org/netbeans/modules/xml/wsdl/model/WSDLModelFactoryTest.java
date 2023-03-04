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
/*
 * WSDLModelFactoryTest.java
 * JUnit based test
 *
 * Created on November 15, 2005, 4:51 PM
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import junit.framework.*;

/**
 *
 * @author nn136682
 */
public class WSDLModelFactoryTest extends TestCase {

    public WSDLModelFactoryTest(String testName) {
        super(testName);
    }

    WSDLModel model;
    protected void setUp() throws Exception {
        model = Util.loadWSDLModel("resources/stockquote.xml");
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WSDLModelFactoryTest.class);
        return suite;
    }

    public void testGetModel() {
        Documentation documentation = model.getDefinitions().getDocumentation();
        System.out.println("content: " + documentation.getTextContent());
        assertEquals("test getModel", "Hello World!", documentation.getTextContent());
    }
    
    public void testExtension(){
        Definitions definitions = model.getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        System.out.println("number of bindings: " + bindings.size());
        for(Binding binding : bindings){
            Collection<ExtensibilityElement> ee = binding.getExtensibilityElements();
            for(ExtensibilityElement e : ee){
                System.out.println("ExtensibilityElement: " + e.getClass().getName());
            }
        }
    }
    
}
