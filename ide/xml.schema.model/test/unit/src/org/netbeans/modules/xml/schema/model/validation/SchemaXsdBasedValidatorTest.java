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

package org.netbeans.modules.xml.schema.model.validation;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author nn136682
 */
public class SchemaXsdBasedValidatorTest extends TestCase {
    
    public SchemaXsdBasedValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaXsdBasedValidatorTest.class);
        
        return suite;
    }
    
    public void testResolveResource() throws Exception {
        Validation validation = new Validation();
        SchemaModel model = Util.loadSchemaModel("validation/SynchronousSample.xsd");
        SchemaModelReference imported = model.getSchema().getSchemaReferences().iterator().next();
        SchemaModel importedModel = imported.resolveReferencedModel();
        String expected1 = "s4s-att-not-allowed: Attribute 'nameXXXX' cannot appear in element 'attribute'.";
        String expected2 = "s4s-att-must-appear: Attribute 'name' must appear in element 'attribute'.";

        validation.validate(importedModel, Validation.ValidationType.COMPLETE);
        List<ResultItem> results0 = validation.getValidationResult();
        assertEquals(2, results0.size());
        assertEquals("from imported model", importedModel, results0.get(0).getModel());
        assertEquals(expected1, results0.get(0).getDescription());
        assertEquals("from imported model", importedModel, results0.get(1).getModel());
        assertEquals(expected2, results0.get(1).getDescription()); 
        
        Validation validation2 = new Validation();
        validation2.validate(model, Validation.ValidationType.COMPLETE);
        List<ResultItem> results = validation2.getValidationResult();
        assertEquals(2, results.size());
        assertEquals("from imported model", importedModel, results.get(0).getModel());
        assertEquals(expected1, results.get(0).getDescription());
        assertEquals("from imported model", importedModel, results.get(1).getModel());
        assertEquals(expected2, results.get(1).getDescription()); 
    }
    
}
