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
public class CheckParentTest extends AbstractTestCase {
    public static final String TEST_XSD         = "resources/po.xsd";
    
    public CheckParentTest(String testName) {
        super(testName, TEST_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(CheckParentTest.class);
        return suite;
    }
    
    public void testParentNotNull() {
        DeepModelVisitor visitor = new DeepModelVisitor();
        visitor.traverse(getAXIModel().getRoot());
    }
        
    private class DeepModelVisitor extends DeepAXITreeVisitor {
        private int counter = 0;

        public void traverse(AXIDocument document) {
            document.accept(this);
        }

        protected void visitChildren(AXIComponent component) {
            if(!(component instanceof AXIDocument))
                assert(component.getParent() != null);
            counter++;
            super.visitChildren(component);
        }
    }    
}
