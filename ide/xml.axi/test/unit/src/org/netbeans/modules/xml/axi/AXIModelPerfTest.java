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
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

        
/**
 * The test measures the performance by creating an AXI tree for all
 * elements in the OTA schema. See reverseEngineer().
 * 1. Run it by making AXIModelBuilder.makeSharable as false.
 * 2. Run it by making AXIModelBuilder.makeSharable as true.
 * See the difference in numbers.
 *
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelPerfTest extends AbstractTestCase {
            
    public static final String TEST_XSD  = "resources/OTA_TravelItinerary.xsd";
    public static final String CYCLE_XSD  = "resources/cycle.xsd";
        
    /**
     * AXIModelPerfTest
     */
    public AXIModelPerfTest(String testName) {
        super(testName, TEST_XSD, null);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//         Disabled as referenced XSD file were partly not donated by oracle to apache
//         suite.addTestSuite(AXIModelPerfTest.class);
        return suite;
    }
    
    public void testPerformance() throws Exception {
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(getAXIModel().getRoot());
        long endTime = System.currentTimeMillis();
        PerfVisitor visitor1 = new PerfVisitor();
        visitor1.visit(getAXIModel().getRoot());
        assert(visitor1.getComponentCount() == 
               getAXIModel().getComponentFactory().getComponentCount());
        print("Time taken to create AXI model for OTA: " + (endTime - startTime));
        print(getAXIModel().getComponentFactory().toString());
    }
            
    public void testCyclicSchema() throws Exception {
        AXIModel cyclicModel = getModel(CYCLE_XSD);
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(cyclicModel.getRoot());
        long endTime = System.currentTimeMillis();
        print("Time taken to deep visit cyclic schema: " + (endTime - startTime));
    }
        
    private class PerfVisitor extends DeepAXITreeVisitor {
        long componentCount = 0;
        public void traverse(AXIDocument document) {
            document.accept(this);
        }
        
        public long getComponentCount() {
            return componentCount;
        }

        protected void visitChildren(AXIComponent component) {
            componentCount++;
            ComponentType type = component.getComponentType();
            AXIComponent original = component.getOriginal();
            switch(type) {
                case PROXY:
                    assert(component.isShared());
                    assert(original != component);
                    if(original.getComponentType() == ComponentType.REFERENCE)
                        assert(original.isShared());
                    else
                        assert(!original.isShared());
                    //assert(component.getContentModel() != null);
                    break;
            
                case REFERENCE:
                    assert(component.isShared());
                    break;
                
                case SHARED:
                    assert(!component.isShared());
                    assert(component.getParent() instanceof AXIDocument);
                    break;
                
                case LOCAL:
                    assert(!component.isShared());
                    assert(original == component);
                    break;
                    
                default:
                    assert(false);
            }
            super.visitChildren(component);
        }
    }
    
}
