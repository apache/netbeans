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
        TestSuite suite = new TestSuite(ContentModelTest.class);
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
