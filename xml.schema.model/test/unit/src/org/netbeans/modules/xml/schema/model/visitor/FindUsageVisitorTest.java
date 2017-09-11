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
 * FindUsageVisitorTest.java
 * JUnit based test
 *
 * Created on November 3, 2005, 2:34 PM
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.xam.NamedReferenceable;
/**
 *
 * @author Samaresh
 */
public class FindUsageVisitorTest extends TestCase {
    
    public static final String TEST_XSD                     = "resources/J1_TravelItinerary.xsd";    
    public static final String FIND_USAGE_FOR_ATTR_GROUP    = "OTA_PayloadStdAttributes";
    public static final String FIND_USAGE_FOR_ELEMENT       = "TPA_Extensions";
    public static final String FIND_USAGE_FOR_TYPE          = "TransactionActionType";
    public static final String NO_TARGET_NAMESPACE = "resources/CTDerivations.xsd";
    private Schema          schema                  = null;
    private GlobalElement   global_element          = null;
    private GlobalType      global_type             = null;
    private GlobalAttributeGroup global_attribute_group        = null;
    
    public FindUsageVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }
    protected void setUp1() throws Exception {
	SchemaModel model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
        
        Collection<GlobalType> types = new ArrayList(schema.getComplexTypes());
        types.addAll(schema.getSimpleTypes());
        for(GlobalType type : types) {
            if(type.getName().equals(FIND_USAGE_FOR_TYPE)) {
                this.global_type = type;
            }
        }
        
        for(GlobalElement e : schema.getElements()) {
            if(e.getName().equals(FIND_USAGE_FOR_ELEMENT)) {
                this.global_element = e;
            }
        }
        
        for(GlobalAttributeGroup gag : schema.getAttributeGroups()) {
            if(gag.getName().equals(FIND_USAGE_FOR_ATTR_GROUP)) {
                this.global_attribute_group = gag;
            }
        }        
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FindUsageVisitorTest.class);
        return suite;
    }
    
    public void testFindPath() throws Exception {
        setUp1();
        this.assertEquals(49, findUsageCountForItem(global_element));
        this.assertEquals(3, findUsageCountForItem(global_type));
        this.assertEquals(4, findUsageCountForItem(global_attribute_group));        
    }

    public int findUsageCountForItem(NamedReferenceable<SchemaComponent> ref) {
        long startTime = System.currentTimeMillis();
        System.out.println("Finding Usage for " + ref.getName() == null? ref : ref.getName());
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview = usage.findUsages(Collections.singletonList(schema), ref);
        System.out.println(preview.getUsages().size() + " occurances found!!!");
                
        Map<SchemaComponent, List<SchemaComponent>> usageMap = preview.getUsages();
        for(SchemaComponent c : usageMap.keySet()) {
            System.out.println("Path for component: " + c);
            List<SchemaComponent> path = usageMap.get(c);
            for(SchemaComponent e : path) {
                System.out.println(getComponentDetail(e));
            }
            System.out.println("\n");            
            //if u want to compare with the paths as it were obtained using PathFromRootVisitor
            //getPathUsingPathFromRootVisitor(schema, c);            
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime));
        return usageMap.keySet().size();
        //this.assertEquals(49, usageMap.keySet().size());
    }
    
    private String getComponentDetail(SchemaComponent component) {
        String details = component.toString();
        if(component instanceof GlobalComplexType) {
            return details + ":" + ((GlobalComplexType)component).getName();
        }
        if(component instanceof GlobalSimpleType) {
            return details + ":" + ((GlobalSimpleType)component).getName();
        }
        if(component instanceof LocalElement) {
            return details + ":" + ((LocalElement)component).getName();
        }        
        if(component instanceof GlobalElement) {
            return details + ":" + ((GlobalElement)component).getName();
        }
        return details;
    }
    
    public void testNoTargetNamespace() throws Exception {
        SchemaModel model = Util.loadSchemaModel(NO_TARGET_NAMESPACE);
        Schema schema = model.getSchema();
        GlobalComplexType gct = schema.getComplexTypes().iterator().next();
        assertEquals("Base-For-Restriction", gct.getName());
        FindUsageVisitor fuv = new FindUsageVisitor();
        Preview pv = fuv.findUsages(Collections.singleton(schema), gct);
        assertEquals("notargetnamespace.usages.count", 2, pv.getUsages().size());
    }
    
    public void testUnion() throws Exception {
	SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder_union.xsd");
        FindUsageVisitor fuv = new FindUsageVisitor();
        GlobalSimpleType type = Util.findGlobalSimpleType(model.getSchema(), "Money");
        assertEquals("Money", type.getName());
        model.getSchema().getSimpleTypes();
        Preview pv = fuv.findUsages(Collections.singleton(model.getSchema()), type);
        assertEquals("findusage on Money count", 1, pv.getUsages().size());
    }
}
