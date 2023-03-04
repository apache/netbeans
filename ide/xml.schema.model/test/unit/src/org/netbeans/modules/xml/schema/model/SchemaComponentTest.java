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
 * SchemaComponentTest.java
 *
 * Created on November 2, 2005, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import junit.framework.TestCase;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 */
public class SchemaComponentTest extends TestCase{
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    public static final String EMPTY_XSD = "resources/Empty.xsd";
    
     Schema schema = null;
    /**
     * Creates a new instance of SchemaComponentTest
     */
    public SchemaComponentTest(String testcase) {
        super(testcase);
    }
    
   
    protected void setUp() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }
    
    public void testPosition(){
        //schema position
        this.assertEquals("<schema> position ", 40, schema.findPosition());
        System.out.println("schema position: " + schema.findPosition());
        
        //position of first global element
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement element  = elements.iterator().next();
        System.out.println("position of first element: " + element.findPosition());
        this.assertEquals("<purchaseorder> element position ", 276, element.findPosition());
        
         //position of referenced type PurchaseType
        NamedComponentReference<? extends GlobalType> ref = element.getType();
        GlobalType type = ref.get();
        System.out.println("Position of referenced type: " + type.getName() +  ": " + type.findPosition());
        assertEquals("referenced PurchaseType position ", 387, type.findPosition() );
        
        //position of sequence under PurchaseType
        GlobalComplexType gct = (GlobalComplexType)type;        
        ComplexTypeDefinition def = gct.getDefinition();
        System.out.println("Sequence under PurchaseType position: " + def.findPosition());
        assertEquals("sequence under PurchaseType position ", 430, def.findPosition() );
        
        Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
        GlobalSimpleType simpleType = simpleTypes.iterator().next();
        System.out.println("Position of simple Type: " + simpleType.findPosition());
        assertEquals("simple type allNNI position ", 865, simpleType.findPosition());
    }
    
}
