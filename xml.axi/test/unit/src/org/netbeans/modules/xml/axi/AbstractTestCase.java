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

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.openide.filesystems.FileUtil;
        
/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractTestCase extends TestCase {

    //make it true if you want to see System.out.println messages.
    public static final boolean printUnitTestResults = false;    
    
    protected String schemaFileName;
    protected String globalElementName;    
    protected AXIModel axiModel;
    protected Element globalElement;
    protected URL referenceXML;
    protected boolean canCompareExpectedResultWithActual = true;
    
    
    /**
     * AbstractTestCase
     */
    public AbstractTestCase(String testName, 
            String schemaFileName, String globalElementName) {
        super(testName);
        this.schemaFileName = schemaFileName;
        this.globalElementName = globalElementName;
    }

    protected void setUp() throws Exception {
        loadModel(this.schemaFileName);
    }
	
    protected void loadModel(String schemaFileName) throws Exception {
        this.schemaFileName = schemaFileName;
        this.axiModel = getModel(schemaFileName);
        this.globalElement = findAXIGlobalElement(globalElementName);        
        String compareAgainst = schemaFileName.substring(0, schemaFileName.indexOf(".xsd")) + ".xml";
        referenceXML = AbstractTestCase.class.getResource(compareAgainst);
        if(referenceXML == null) {
            canCompareExpectedResultWithActual = false;
            return;
        }
    }

    protected AXIModel getModel(String schemaFileName) throws Exception {
        URL url = AbstractTestCase.class.getResource(schemaFileName);
        File file = new File(url.toURI());
        file = FileUtil.normalizeFile(file);
        return TestCatalogModel.getDefault().
                getAXIModel(FileUtil.toFileObject(file));                
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
            
    protected AXIModel getAXIModel() {
        return axiModel;
    }
    
    protected SchemaModel getSchemaModel() {
        return getAXIModel().getSchemaModel();
    }
    
    protected Element findAXIGlobalElement(String name) {
        if(name == null)
            return null;
        
        for(Element e : axiModel.getRoot().getElements()) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        
        return null;
    }
    
    protected ContentModel findContentModel(String name) {
        for(ContentModel cm : axiModel.getRoot().getContentModels()) {
            if(cm.getName().equals(name)) {
                return cm;
            }
        }
        
        return null;
    }
    
    protected void validateSchema(SchemaModel sm) {
        boolean status = 
			((AXIModelImpl)getAXIModel()).getState()==Model.State.VALID;//((AXIModelImpl)getAXIModel()).validate();
        assertTrue("Schema Validation failed", status);
    }
    
    public final void print(String message) {
        if(printUnitTestResults) {        
            System.out.println(message);
        }
    }
}
