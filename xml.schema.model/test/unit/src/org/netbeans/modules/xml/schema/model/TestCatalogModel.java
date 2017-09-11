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
package org.netbeans.modules.xml.schema.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogFileWrapperDOMImpl;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogWriteModelImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author girix
 */
public class TestCatalogModel extends CatalogWriteModelImpl{
    private TestCatalogModel(File file) throws IOException{
        super(file);
    }
    
    static TestCatalogModel singletonCatMod = null;
    public static TestCatalogModel getDefault(){
        if (singletonCatMod == null){
            CatalogFileWrapperDOMImpl.TEST_ENVIRONMENT = true;
            try {
                singletonCatMod = new TestCatalogModel(Util.getTempDir("schematest/catalog"));
                FileObject catalogFO = singletonCatMod.getCatalogFileObject();
                File catFile = FileUtil.toFile(catalogFO);
                catFile.deleteOnExit();
                initCatalogFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return singletonCatMod;
    }
    
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    protected ModelSource createModelSource(final FileObject thisFileObj, boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final CatalogModel catalogModel = createCatalogModel(thisFileObj);
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup proxyLookup = Lookups.proxy(
                new Lookup.Provider() {
            public Lookup getLookup() {
                return Lookups.fixed(new Object[] {
                    FileUtil.toFile(thisFileObj),
                    thisFileObj,
                    getDocument(dobj.getPrimaryFile()),
                    dobj,
                    catalogModel
                });
            }
        }
        );
        return new ModelSource(proxyLookup, editable);
    }
    
    private Document getDocument(FileObject fo){
        Document result = null;
        if (documentPooling) {
            result = documentPool().get(fo);
        }
        if (result != null) return result;
        try {
            
            File file = FileUtil.toFile(fo);
            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
            result = new BaseDocument(true, "text/xml"); //NOI18N
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);
            
        } catch (Exception dObjEx) {
            return null;
        }
        if (documentPooling) {
            documentPool().put(fo, result);
        }
        return result;
    }
    
    protected CatalogModel createCatalogModel(FileObject fo) throws CatalogModelException{
        return getDefault();
    }
    
    public ModelSource createTestModelSource(FileObject fo, boolean editable) throws CatalogModelException{
        final DataObject dobj;
        final CatalogModel catalogModel = createCatalogModel(fo);
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup lookup = Lookups.proxy(new Lookup.Provider() {
            public Lookup getLookup() {
                        return Lookups.fixed(new Object[] {
                            dobj.getPrimaryFile(),
                            getDocument(dobj.getPrimaryFile()),
                            dobj,
                            catalogModel
                        });
            }
        } );
        return new ModelSource(lookup, editable);
    }
    
    private static void initCatalogFile() throws Exception {
        for (NamespaceLocation nl : NamespaceLocation.values()) {
            singletonCatMod.addNamespace(nl);
        }
    }
    
    public void addNamespace(NamespaceLocation nl) throws Exception {
        this.addURI(nl.getLocationURI(), nl.getResourceURI());
    }
    
    public SchemaModel getSchemaModel(NamespaceLocation nl) throws Exception {
        if (nl.getResourceFile().exists()) {
            ModelSource source = singletonCatMod.getModelSource(nl.getLocationURI());
            DataObject dobj = (DataObject) source.getLookup().lookup(DataObject.class);
            SaveCookie save = (SaveCookie) dobj.getCookie(SaveCookie.class);
            if (save != null) save.save();
            FileObject fo = (FileObject) source.getLookup().lookup(FileObject.class);
            fo.delete();
        }
        nl.refreshResourceFile();
        return getSchemaModel(nl.getLocationURI());
    }
    
    public SchemaModel getSchemaModel(URI lcationURI) throws Exception {
        ModelSource source = singletonCatMod.getModelSource(lcationURI);
        SchemaModel model = SchemaModelFactory.getDefault().getModel(source);
        return model;
    }
    
    private Map<FileObject,Document> fileToDocumentMap;
    private Map<FileObject,Document> documentPool() {
        if (fileToDocumentMap == null) {
            fileToDocumentMap = new HashMap<FileObject,Document>();
        }
        return fileToDocumentMap;
    }
    private boolean documentPooling = true;
    
    public void setDocumentPooling(boolean v) {
        documentPooling = v;
        if (! documentPooling) {
            clearDocumentPool();
        }
    }

    public void clearDocumentPool() {
        fileToDocumentMap = null;
    }
    
    /**
     * A JUnit {@link TestRule} that stops tests from interfering with one 
     * another. JUnit will automatically set up/clean up the catalog when this
     * rule is used. <br/>
     * Usage:<br/>
     * {@code @Rule public final TestRule catalogMaintainer = TestCatalogModel.maintainer()}
     * @return the TestRule
     */
    public static TestRule maintainer() {
        return new ExternalResource() {

            @Override
            protected void after() {
                getDefault().clearDocumentPool();
            }
        
        };
    }
}

