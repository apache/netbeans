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

package org.netbeans.modules.xml.axi;

import junit.framework.*;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;


/**
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CyclicModelTest extends AbstractTestCase {
    
    public static final String SCHEMA_A         = "resources/schemaA.xsd";
    public static final String SCHEMA_B         = "resources/schemaB.xsd";
    public static final String SCHEMA_C         = "resources/schemaC.xsd";
    
    /**
     * CyclicModelTest
     */
    public CyclicModelTest(String testName) {
        super(testName, SCHEMA_A, null);
    }
    
    /**
     * CyclicModelTest
     */
    public CyclicModelTest(String testName, String schemaFile, String elementName) {
        super(testName, schemaFile, elementName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CyclicModelTest.class);        
        return suite;
    }
    
    public void testCyclicModelsNotExpanded() {
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(getAXIModel().getRoot());
        long endTime = System.currentTimeMillis();
    }
    
    public void testCyclicModelsExpanded() throws Exception {
        AXIModel modelA = getAXIModel();
        AXIModel modelB = getModel(SCHEMA_B);
        AXIModel modelC = getModel(SCHEMA_C);
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        visitor.visit(modelA.getRoot());
        visitor.visit(modelB.getRoot());
        visitor.visit(modelC.getRoot());
        modelA.sync();
        modelB.sync();
        modelC.sync();
    }
}
