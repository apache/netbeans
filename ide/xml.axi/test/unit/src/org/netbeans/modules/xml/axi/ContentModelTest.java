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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 * This test traverses the AXI model for a given schema and
 * checks the parent component at each level.
 *
 * @author Samaresh
 */
public class ContentModelTest extends AbstractTestCase {
    public static final String TEST_XSD         = "resources/po.xsd";
    
    public ContentModelTest(String testName) {
        super(testName, TEST_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(ContentModelTest.class);
        return suite;
    }
        
    public void testContentModels() {        
        axiModel = getAXIModel();
        
        //traverse entire tree, so that the model gets fully initialized.
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        axiModel.getRoot().accept(visitor);
        
        //check all content models
        ContentModelVisitor cmv = new ContentModelVisitor();
        cmv.checkContentModels(getAXIModel());
    }
        
    private class ContentModelVisitor extends DeepAXITreeVisitor {
        private int refCount = 0;
        private ContentModel contentModel;
        private AXIModel axiModel;
        
        public void checkContentModels(AXIModel model) {
            axiModel = getAXIModel();
            for(ContentModel cm : model.getRoot().getContentModels()) {
                //must belong to the same model
                assert(axiModel == cm.getModel());
                
                contentModel = cm;
                print("checking ContentModel: " + 
                        cm.getName() + " Type: " + cm.getType() + ".....");
                if(cm.getRefSet() == null)
                    continue;
                
                refCount = cm.getRefSet().size();
                cm.accept(this);
            }
        }

        protected void visitChildren(AXIComponent component) {
            if(component instanceof ContentModel) {
                super.visitChildren(component);
                return;
            }
            print("Component: " + component + " type: " + component.getComponentType());
            assert(component.getRefSet().size() == refCount);
            for(AXIComponent ref : component.getRefSet()) {
                AXIComponent original = ref.getSharedComponent();
                assert(original == component);
            }
            super.visitChildren(component);
        }
    }
}
