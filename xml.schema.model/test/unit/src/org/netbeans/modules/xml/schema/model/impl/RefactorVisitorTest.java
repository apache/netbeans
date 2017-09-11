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

/*
 * RefactorVisitorTest.java
 * JUnit based test
 *
 * Created on October 18, 2005, 3:57 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import java.util.Collections;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author Administrator
 */
public class RefactorVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    
    private Schema          schema                  = null;
    private GlobalElement   global_element          = null;
    private GlobalType      global_type             = null;
    private GlobalAttribute global_attribute        = null;
    private SchemaModel model;
    
    public RefactorVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
	model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
        
        for(GlobalType type : schema.getComplexTypes()) {
            if(type.getName().endsWith("USAddress")) {
                this.global_type = type;
            }
        }
        
        for(GlobalElement e : schema.getElements()) {
            if(e.getName().endsWith("comment")) {
                this.global_element = e;
            }
        }        
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RefactorVisitorTest.class);
        return suite;
    }
        
    public void testRenameGlobalType() throws IOException{
        String oldVal = global_type.getName();
        String newVal = "MyAddress";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        String oldName = global_type.getName();
        global_type.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_type, oldName);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        this.assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());        
    }
    
    public void testRenameGlobalElement() throws IOException{
        System.out.println("Renaming global element comment to xcomment...");
        String oldVal = global_element.getName();
        String newVal = "xcomment";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_element);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        String oldName = global_element.getName();
        global_element.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_element, oldName);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_element);
        //System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());
    }

    public static void renameComponent(ReferenceableSchemaComponent component, String newName) throws Exception {
        SchemaModel model = component.getModel();
        Schema schema = model.getSchema();
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), component);
        RefactorVisitor visitor = new RefactorVisitor();

        model.startTransaction();
        String oldName = component.getName();
        component.setName(newName);
        visitor.setRenamedElement(component, oldName);
        visitor.rename(preview_before);
        model.endTransaction();
    }
    
    public void testRenameSimpleTypeInUnionMemberType() throws Exception {
	SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder_union.xsd");
        GlobalSimpleType moneyType = Util.findGlobalSimpleType(model.getSchema(), "Money");
        GlobalSimpleType unionType = Util.findGlobalSimpleType(model.getSchema(), "MoneyOrPercentageType");
        Union u = (Union) unionType.getDefinition();
        String memberTypes = ((AbstractDocumentComponent)u).getAttribute(SchemaAttributes.MEMBER_TYPES);
        model.startTransaction();
        new RefactorVisitor().rename(moneyType, "USDollar");
        model.endTransaction();
        assertEquals("po:USDollar po:Percentage", ((AbstractDocumentComponent)u).getAttribute(SchemaAttributes.MEMBER_TYPES));
    }
}
