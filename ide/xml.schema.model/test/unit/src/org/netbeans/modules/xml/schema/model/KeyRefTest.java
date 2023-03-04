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
 * KeyRefTest.java
 *
 * Created on November 6, 2005, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import junit.framework.TestCase;

/**
 *
 * @author rico
 */
public class KeyRefTest extends TestCase{
    public static final String TEST_XSD = "resources/KeyRef.xsd";
    
    /** Creates a new instance of KeyRefTest */
    public KeyRefTest(String testcase) {
        super(testcase);
    }
    
    Schema schema = null;
    protected void setUp() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testKeyRef(){
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement elem = elements.iterator().next();
        LocalType localType = elem.getInlineType();
        assertTrue("localType instanceof LocalComplexType",
                     localType instanceof LocalComplexType);
        LocalComplexType lct = (LocalComplexType)localType;
        ComplexTypeDefinition ctd = lct.getDefinition();
        assertTrue("ComplextTypeDefinition instanceof Sequence",
                    ctd instanceof Sequence);
        Sequence seq = (Sequence)ctd;
        java.util.List <SequenceDefinition> seqDefs = seq.getContent();
        SequenceDefinition seqDef = seqDefs.iterator().next();
        assertTrue("SequenceDefinition instanceof LocalElement",
                seqDef instanceof LocalElement);
        LocalElement le = (LocalElement)seqDef;  
        Collection<Constraint> constraints = le.getConstraints();
        Constraint constraint = constraints.iterator().next();
        assertTrue("Constraint instanceof KeyRef", constraint instanceof KeyRef);
        KeyRef keyRef = (KeyRef)constraint;
        Constraint key = keyRef.getReferer();
        System.out.println("key: " + key.getName());
        assertEquals("Referred key", "pNumKey", key.getName());
    }
}
