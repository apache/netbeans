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
 * DepResolverFactoryImplTest.java
 * JUnit based test
 *
 * Created on January 18, 2006, 7:28 PM
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Formatter;
import java.util.logging.Level;
import javax.swing.text.Document;
import junit.framework.*;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.model.TestUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.cookies.EditorCookie;
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
public class CatalogModelTest extends TestCase {
    
    static {
        TestUtil.registerXMLKit();
    }
    
    public CatalogModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CatalogModelTest.class);
        
        return suite;
    }
    
    
    public void testDepResolver() throws URISyntaxException, CatalogModelException, IOException {
        
        Logger logger = Logger.getLogger(CatalogModelTest.class.getName());
        logger.setLevel(Level.ALL);
        StreamHandler sh = new MyHandler(System.out, new SimpleFormatter());
        sh.setLevel(logger.getLevel());
        //logger.addHandler(sh);
        CatalogFileWrapperDOMImpl.TEST_ENVIRONMENT = true;
        File catFile = new File(System.getProperty("java.io.tmpdir")+File.separator+CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION+".girish");
        catFile.delete();
        catFile.createNewFile();
        FileObject catFO = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
        URL url = getClass().getResource("dummyFile.txt");
        FileObject peerfo = FileUtil.toFileObject(new File(url.toURI()).getAbsoluteFile());
        System.out.println(catFile);
        CatalogWriteModel drz = new MyCatalogWriteModel(catFO);
        //CatalogWriteModel drz = new MyCatalogWriteModel(new File(System.getProperty("java.io.tmpdir")));
        drz.addURI(new URI("dummy/dummy"), peerfo);
        int length = drz.getCatalogEntries().size();
        
        assertEquals(1, length);
        
        //System.out.println("%%%%"+drz.getModelSource(new URI("dummy/dummy")).getFileObject());
        
        //System.out.println("$$$$"+LSResourceResolverFactory.getDefault().resolveResource(null, null, null, "dummy/dummy", url.toURI().toString()).getSystemId());
        
        //assertTrue(LSResourceResolverFactory.getDefault().resolveResource(null, null, null, "dummy/dummy", url.toURI().toString()).getSystemId().endsWith("dummyFile.txt"));
        
        FileObject fob = (FileObject) drz.getModelSource(new URI("dummy/dummy")).getLookup().lookup(FileObject.class);
        
        assertNotNull(fob);
        
        drz.removeURI(new URI("dummy/dummy"));
        
        length = drz.getCatalogEntries().size();
        
        assertEquals(0, length);
    }
    
    class MyCatalogWriteModel extends CatalogWriteModelImpl {
        MyCatalogWriteModel(File file) throws IOException{
            super(file);
        }
        MyCatalogWriteModel(FileObject fo) throws IOException{
            super(fo);
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
                    Document document = null;
                    Logger l = Logger.getLogger(getClass().getName());
                    document = getDocument(thisFileObj);
                    return Lookups.fixed(new Object[] {
                        thisFileObj,
                        document,
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
            try {
                DataObject dObject = DataObject.find(fo);
                EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                if(doc instanceof BaseDocument)
                    return doc;
                result = new org.netbeans.editor.BaseDocument(true, fo.getMIMEType());
                String str = doc.getText(0, doc.getLength());
                result.insertString(0,str,null);
                
            } catch (Exception dObjEx) {
                return null;
            }
            return result;
        }
    }
    
    class MyHandler extends StreamHandler{
        public MyHandler(OutputStream out, Formatter fmt){
            super(out, fmt);
        }
        public void publish(java.util.logging.LogRecord record) {
            super.publish(record);
            flush();
        }
        
    }
    
}
