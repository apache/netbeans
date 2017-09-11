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
 * SchemaComponentTest.java
 *
 * Created on November 2, 2005, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import junit.framework.TestCase;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 */
public class SchemaComponentTest extends TestCase{
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    public static final String EMPTY_XSD = "resources/Empty.xsd";
    
     Schema schema = null;
    /**
     * Creates a new instance of SchemaComponentTest
     */
    public SchemaComponentTest(String testcase) {
        super(testcase);
    }
    
   
    protected void setUp() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }
    
    public void testPosition(){
        //schema position
        this.assertEquals("<schema> position ", 40, schema.findPosition());
        System.out.println("schema position: " + schema.findPosition());
        
        //position of first global element
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement element  = elements.iterator().next();
        System.out.println("position of first element: " + element.findPosition());
        this.assertEquals("<purchaseorder> element position ", 276, element.findPosition());
        
         //position of referenced type PurchaseType
        NamedComponentReference<? extends GlobalType> ref = element.getType();
        GlobalType type = ref.get();
        System.out.println("Position of referenced type: " + type.getName() +  ": " + type.findPosition());
        assertEquals("referenced PurchaseType position ", 387, type.findPosition() );
        
        //position of sequence under PurchaseType
        GlobalComplexType gct = (GlobalComplexType)type;        
        ComplexTypeDefinition def = gct.getDefinition();
        System.out.println("Sequence under PurchaseType position: " + def.findPosition());
        assertEquals("sequence under PurchaseType position ", 430, def.findPosition() );
        
        Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
        GlobalSimpleType simpleType = simpleTypes.iterator().next();
        System.out.println("Position of simple Type: " + simpleType.findPosition());
        assertEquals("simple type allNNI position ", 865, simpleType.findPosition());
    }
    
}
