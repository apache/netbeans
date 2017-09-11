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
 * XAMCatalogWriteModelTest.java
 * JUnit based test
 *
 * Created on December 14, 2006, 4:19 PM
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModelFactory;
import org.netbeans.modules.xml.retriever.catalog.model.TestUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class XAMCatalogWriteModelTest extends TestCase {
    
    public XAMCatalogWriteModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XAMCatalogWriteModelTest.class);
        
        return suite;
    }
    
    public void testSearchURI() {
        URI locationURI = null;
        try {
            locationURI = new URI("sysIDAttr");
        } catch (URISyntaxException ex) {
        }
        
        XAMCatalogWriteModelImpl instance = getTestCatModelInstance();
        
        URI result = instance.searchURI(locationURI);
        assertNotNull(result);
    }
    
    public void testAddAndRemoveURI() throws Exception {
        URI leftURI = new URI("girish");
        URI rightURI = new URI("kumar");
        FileObject fileObj = null;
        MyXAMCatalogWriteModel instance = getTestCatModelInstance();
        
        int start = instance.getCatalogEntries().size();
        
        instance.addURI(leftURI, rightURI);
        
        assertEquals(start+1, instance.getCatalogEntries().size());
        
        instance.removeURI(leftURI);
        
        assertEquals(start, instance.getCatalogEntries().size());
    }
    
    public MyXAMCatalogWriteModel getTestCatModelInstance(){
        FileObject inputFile = null;
        
        try {
            inputFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(XAMCatalogWriteModelTest.class.
                    getResource("catalog.xml").toURI())));
        } catch (URISyntaxException ex) {
            assert false;
            ex.printStackTrace();
            return null;
        }
        
        MyXAMCatalogWriteModel instance = null;
        try {
            instance = new MyXAMCatalogWriteModel(inputFile);
        } catch (IOException ex) {
            assert false;
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
            assert false;
        }
        
        return instance;
    }
    
    public void testAddNextCatalog() throws Exception {
        URI leftURI = new URI("girish");
        FileObject fileObj = null;
        MyXAMCatalogWriteModel instance = getTestCatModelInstance();
        
        int start = instance.getCatalogEntries().size();
        
        instance.addNextCatalog(leftURI, true);
        
        assertEquals(start+1, instance.getCatalogEntries().size());
        
        instance.removeNextCatalog(leftURI);
        
        assertEquals(start, instance.getCatalogEntries().size());
    }
    
    
    
    class MyXAMCatalogWriteModel extends XAMCatalogWriteModelImpl{
        public MyXAMCatalogWriteModel(FileObject catFile) throws IOException, CatalogModelException{
            super(catFile);
        }
        
        protected ModelSource createModelSource(FileObject catFileObject) throws CatalogModelException {
            ModelSource source = null;
            try {
                source = TestUtil.createModelSource(super.catalogFileObject, true);
            } catch (CatalogModelException ex) {
                assert false;
                ex.printStackTrace();
                return null;
            }
            return source;
        }
        
        
        public String getContent(){
            Document doc = (Document) getCatalogModel().getModelSource().getLookup().lookup(Document.class);;
            try {
                return doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
