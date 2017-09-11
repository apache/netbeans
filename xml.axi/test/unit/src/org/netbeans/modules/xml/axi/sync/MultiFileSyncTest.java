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

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.impl.ElementProxy;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalElement;

        
/**
 * This unit test verifies sync involving multiple files.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class MultiFileSyncTest extends AbstractSyncTestCase {

    public static final String TEST_XSD         = "resources/employee.xsd";
    public static final String IMPORTED_XSD     = "resources/address1.xsd";
    public static final String GLOBAL_ELEMENT   = "employee";
    private ContentModel usAddress;
    private PropertyListener pcl;
    private boolean sync1 = false;
    private boolean sync2 = false;
    private boolean fromAXIModel = false;
    
    public MultiFileSyncTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
            
    public static Test suite() {
        TestSuite suite = new TestSuite(MultiFileSyncTest.class);
        return suite;
    }

    public void testUpdateImportedSchema() throws Exception {
        //open the imported schema
        AXIModel iModel = getModel(IMPORTED_XSD);
        usAddress = findContentModel(iModel, "group");
        assert(usAddress != null);
        
        //deep visit the original schema
        AXIModelImpl model = (AXIModelImpl)getAXIModel();
        AXIDocument document = model.getRoot();
        DeepVisitor visitor = new DeepVisitor();
        visitor.traverse(document);
        
        updateAddressInAXIModel();
        Thread.sleep(10000);
        assert(sync1);
        
        //update the imported schema.
        updateAddressInSchemaModel();
        Thread.sleep(10000);
        assert(sync2);
    }
    
    private void updateAddressInAXIModel() throws Exception {
	fromAXIModel = true;
        usAddress.getModel().startTransaction();
	Element name = (Element)usAddress.getChildren().get(0).getChildren().get(0);
        assert(name.getName().equals("name"));
        name.setName("nameUpdatedFromDV");
	usAddress.getModel().endTransaction();
    }
    
    private void updateAddressInSchemaModel() throws Exception {
	fromAXIModel = false;
        GlobalGroup type = (GlobalGroup)usAddress.getPeer();
	type.getModel().startTransaction();
	LocalElement name = (LocalElement)type.getChildren().get(0).getChildren().get(0);
        assert(name.getName().equals("nameUpdatedFromDV"));
        name.setName("nameUpdatedFromSV");
	type.getModel().endTransaction();
    }
    
    private ContentModel findContentModel(AXIModel model, String name) {
        for(ContentModel cm : model.getRoot().getContentModels()) {
            if(cm.getName().equals(name)) {
                return cm;
            }
        }
        
        return null;
    }
        
    private class DeepVisitor extends DeepAXITreeVisitor {
        AXIModel model;
        public void traverse(AXIDocument document) {
            model = document.getModel();
            document.accept(this);
        }
        
        public void visit(Compositor compositor) {
            if(compositor.getComponentType() != ComponentType.PROXY) {
                visitChildren(compositor);
                return;
            }
            
            Element e = (Element)usAddress.getChildren().get(0).getChildren().get(0);
            ElementProxy eP = (ElementProxy)compositor.getChildren().get(0);
            assert(eP.getModel() == model);
            assert(eP.getOriginal().getModel() != model);
            assert(eP.getOriginal().getModel() == usAddress.getModel());
            assert(eP.getName().equals("name"));
            pcl = new PropertyListener(eP);
            eP.addPropertyChangeListener(pcl);
            visitChildren(compositor);
        }
    }
    
    private class PropertyListener implements PropertyChangeListener {
        private AXIComponent source;
        
        public PropertyListener (AXIComponent source) {
            this.source = source;
        }
        
	public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == source) {
                String name = (String)evt.getNewValue();
                if(fromAXIModel) {
                    assert(name.equals("nameUpdatedFromDV"));
                    sync1 = true;
                } else {
                    assert(name.equals("nameUpdatedFromSV"));
                    sync2 = true;
                }
            }
	}        
    }
}
