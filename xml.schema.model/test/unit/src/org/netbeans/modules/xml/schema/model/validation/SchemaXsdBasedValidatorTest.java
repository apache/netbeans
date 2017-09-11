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

package org.netbeans.modules.xml.schema.model.validation;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author nn136682
 */
public class SchemaXsdBasedValidatorTest extends TestCase {
    
    public SchemaXsdBasedValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaXsdBasedValidatorTest.class);
        
        return suite;
    }
    
    public void testResolveResource() throws Exception {
        Validation validation = new Validation();
        SchemaModel model = Util.loadSchemaModel("validation/SynchronousSample.xsd");
        SchemaModelReference imported = model.getSchema().getSchemaReferences().iterator().next();
        SchemaModel importedModel = imported.resolveReferencedModel();
        String expected1 = "s4s-att-not-allowed: Attribute 'nameXXXX' cannot appear in element 'attribute'.";
        String expected2 = "s4s-att-must-appear: Attribute 'name' must appear in element 'attribute'.";

        validation.validate(importedModel, Validation.ValidationType.COMPLETE);
        List<ResultItem> results0 = validation.getValidationResult();
        assertEquals(2, results0.size());
        assertEquals("from imported model", importedModel, results0.get(0).getModel());
        assertEquals(expected1, results0.get(0).getDescription());
        assertEquals("from imported model", importedModel, results0.get(1).getModel());
        assertEquals(expected2, results0.get(1).getDescription()); 
        
        Validation validation2 = new Validation();
        validation2.validate(model, Validation.ValidationType.COMPLETE);
        List<ResultItem> results = validation2.getValidationResult();
        assertEquals(2, results.size());
        assertEquals("from imported model", importedModel, results.get(0).getModel());
        assertEquals(expected1, results.get(0).getDescription());
        assertEquals("from imported model", importedModel, results.get(1).getModel());
        assertEquals(expected2, results.get(1).getDescription()); 
    }
    
}
