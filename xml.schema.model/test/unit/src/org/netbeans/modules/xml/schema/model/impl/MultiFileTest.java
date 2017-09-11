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
 * MultiFileTest.java
 * JUnit based test
 *
 * Created on December 8, 2005, 12:08 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import junit.framework.*;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Administrator
 */
public class MultiFileTest extends TestCase {
    
    private static String TEST_XSD = "resources/OrgChart.xsd";
    
    public MultiFileTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testGetImportedModelSources() throws Exception {
        if ( ! NamespaceLocation.ADDRESS.getResourceFile().exists() ) {
            NamespaceLocation.ADDRESS.refreshResourceFile();
        }
        SchemaModel sm = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.ORGCHART);
        // get imported model sources
        SchemaImpl schema = (SchemaImpl)sm.getSchema();
        Collection<Import> importedModelSources = new LinkedList<Import>(schema.getImports());
	assertEquals("should be six imports", 6 ,importedModelSources.size());
	Iterator<Import> itr = importedModelSources.iterator();
	while(itr.hasNext()) {
	    Import i = itr.next();
	    try {
		SchemaModel sm2 = i.resolveReferencedModel();
	    } catch (CatalogModelException ex) {
		itr.remove();
	    } 
	}
        assertEquals("only two imports are reachable", 1,importedModelSources.size());
        
        ModelSource importedModelSource = importedModelSources.iterator().next().resolveReferencedModel().getModelSource();
        assertEquals("address.xsd",((FileObject)importedModelSource.getLookup().lookup(FileObject.class)).getNameExt());
        // get imported model
        ModelSource testImportedModelSource = TestCatalogModel.getDefault().createTestModelSource((FileObject) importedModelSource.getLookup().lookup(FileObject.class), false);
        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
        assertNotNull(sm1);
        assertEquals("http://www.altova.com/IPO",sm1.getSchema().getTargetNamespace());
    }
    
    public void testGetIncludedModelSources() throws Exception {
        // get the model for OrgChart.xsd
        URL orgChartUrl = getClass().getResource("../resources/ipo.xsd");
        File orgChartFile = new File(orgChartUrl.toURI());
        FileObject orgChartFileObj = FileUtil.toFileObject(orgChartFile);
        //ModelSource localTestModelSource = new TestModelSource(orgChartFileObj,false);
        ModelSource testModelSource = TestCatalogModel.getDefault().createTestModelSource(orgChartFileObj, false);
        SchemaModel sm = SchemaModelFactory.getDefault().getModel(testModelSource);
        
        //register address.xsd with relative location (this is to be done only once
        URL addressUrl = getClass().getResource("../resources/address.xsd");
        TestCatalogModel.getDefault().addURI(new URI("address.xsd"),addressUrl.toURI());
        
        // get included model sources
        SchemaImpl schema = (SchemaImpl)sm.getSchema();
        Collection<Include> includedModelSources = schema.getIncludes();
        assertEquals(1,includedModelSources.size());
        
        ModelSource importedModelSource = includedModelSources.iterator().next().resolveReferencedModel().getModelSource();
        assertEquals("address.xsd",((FileObject)importedModelSource.getLookup().lookup(FileObject.class)).getNameExt());
        
        // get included model
        ModelSource testImportedModelSource = TestCatalogModel.getDefault().createTestModelSource((FileObject) importedModelSource.getLookup().lookup(FileObject.class), false);
        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
        assertNotNull(sm1);
        assertEquals(schema.getTargetNamespace(),sm1.getSchema().getTargetNamespace());
    }
}
