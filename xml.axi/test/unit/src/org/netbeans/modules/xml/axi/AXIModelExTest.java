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

import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.openide.filesystems.FileObject;

        
/**
 * This unit test extends AXIModelTest and tests components from multiple
 * namespaces and their models. Ensures that the components from different
 * namespaces belong to appropriate AXIModels.
 *
 * - Checks that components coming from diff namespaces are read-only.
 * - Checks that children components for a reference should be proxies.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelExTest extends AXIModelTest {
            
    public static final String TEST_XSD         = "resources/multifilePO.xsd";
    
    private static String PO_TNS = "http://xml.netbeans.org/examples/targetNS/PO";
    private static String ITEMS_TNS = "http://xml.netbeans.org/examples/targetNS/Items";
    private static String ADDR_TNS = "http://xml.netbeans.org/examples/targetNS/Address";
    private AXIModel aModelPO;
    
    /**
     * AXIModelExTest
     */
    public AXIModelExTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AXIModelExTest.class);
        return suite;
    }
    
    public void testModel() {
        aModelPO = getAXIModel();
        assertEquals(getAXIModel().getRoot().getTargetNamespace(), PO_TNS);
        CheckTargetNamespaceVisitor visitor = new CheckTargetNamespaceVisitor();
        visitor.checkNamespace(getAXIModel().getRoot());
    }
    
    private class CheckTargetNamespaceVisitor extends DeepAXITreeVisitor {
        
        public void checkNamespace(AXIDocument document) {
            document.accept(this);
        }

        protected void visitChildren(AXIComponent component) {
            String ns = component.getTargetNamespace();
            
            //components must belong to one of three namespaces
            if( !ns.equals(PO_TNS) &&
                !ns.equals(ADDR_TNS) &&
                !ns.equals(ITEMS_TNS)) {
                assert(false);
            }
            
            //components that come from PO namespaces, must all
            //have the same AXIModel
            if(component.getTargetNamespace().equals(PO_TNS)) {
                assert(aModelPO == component.getModel());
            }
            
            //if a component is a reference, its children has to be proxies
            if(component.getComponentType() == ComponentType.REFERENCE) {
                for(AXIComponent c: component.getChildren()) {
                    assert(c.getComponentType() == ComponentType.PROXY);
                }                
            }            
            
            //components that come from other namespaces, must all
            //have the same AXIModel, but diff from the ones from PO namespace.
            if(!component.getTargetNamespace().equals(PO_TNS)) {
                doValidate(component, component.getOriginal().getModel());
            }
            
            super.visitChildren(component);
        }        
        
        private void doValidate(AXIComponent component, AXIModel otherModel) {
            //must be a proxy and the proxy's model must be the same as PO model.
            assert(component.getComponentType() == ComponentType.PROXY);
            assert(component.getModel() == aModelPO);
            assert(component.isReadOnly());
            
            //find the original for this proxy component
            //and the original must belong to a diff model.
            AXIComponent original = component.getOriginal();
            assert(otherModel != aModelPO);
            assert(otherModel == original.getModel());
            
            //also find the FileObject from the two models
            //and they should be different
            FileObject fPO = (FileObject)aModelPO.getModelSource().
                    getLookup().lookup(FileObject.class);
            FileObject otherFO = (FileObject)otherModel.getModelSource().
                    getLookup().lookup(FileObject.class);
            assert(fPO != null);
            assert(otherFO != null);
            assert(fPO != otherFO);            
        }
    }
    
}
