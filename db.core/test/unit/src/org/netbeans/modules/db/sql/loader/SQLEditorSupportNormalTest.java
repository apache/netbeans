/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.db.sql.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;

/**
 * Tests the functionality of SQLEditorSupport when serving for a normal DataObject.
 *
 * @author Andrei Badea
 */
public class SQLEditorSupportNormalTest extends NbTestCase {
    
    private FileObject fileObject;
    private DataObject dataObject;
    private MySQLEditorSupport support;
    
    public SQLEditorSupportNormalTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception {
        FileObject folder = FileUtil.toFileObject(getWorkDir()).createFolder("folder" + System.currentTimeMillis());
        fileObject = folder.createData("SQLFile", "sql");
        
        MockServices.setServices(Pool.class);
        assertEquals(Pool.class, Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
        
        dataObject = DataObject.find(fileObject);
        support = (MySQLEditorSupport)dataObject.getCookie(OpenCookie.class);
    }
    
    @Override
    public void tearDown() throws Exception {
        fileObject.getParent().delete();
    }
    
    @Override
    public boolean runInEQ() {
        return true;
    }
    
    public void testMultiViewElement() {
        MySQLEditorSupport es = dataObject.getLookup().lookup(MySQLEditorSupport.class);
        assertNotNull("SQLEditorSupport found in lookup of " + dataObject, es);
        CloneableEditor ce = es.createCloneableEditor();
        assertTrue("SQLEditorSupport.createCloneableEditor() instanceof MultiViewElement", ce instanceof MultiViewElement);
    }
    
    public void testEditorNameIsNodeDisplayName() throws Exception {
        support.open();
        assertEquals(dataObject.getNodeDelegate().getDisplayName(), support.messageName());
        
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);
        
        assertTrue(support.messageName().indexOf(dataObject.getNodeDelegate().getDisplayName()) >= 0);

        // XXX should test support.close() here, but how, since canClose() displays a dialog box?
        //support.close();
    }
    
    public void testDataObjectIsModifiedWhenDocumentChanged() throws Exception {
        support.open();
        
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);
        
        assertTrue(support.isModified());
        assertTrue(dataObject.isModified());
        assertNotNull(dataObject.getCookie(SaveCookie.class));
        // XXX should test support.close() here, but how, since canClose() displays a dialog box?
        //support.close();
    }
    
    public void testDocumentIsNotSavedOnComponentDeactivatedOrWriteExternal() throws Exception {
        support.open();
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);

        CloneableTopComponent ctc = (CloneableTopComponent) support.getAllEditors().getComponents().nextElement();
        SQLCloneableEditor editor = ctc.getLookup().lookup(SQLCloneableEditor.class);
        
        editor.componentDeactivated();
        assertFalse(support.saveDocumentCalled);

        doc.insertString(0, "test", null);
        editor.writeExternal(new ObjectOutputStream(new ByteArrayOutputStream()));
        assertFalse(support.saveDocumentCalled);
        
        // XXX should test support.close() here, but how, since canClose() displays a dialog box?
        //support.close();
    }
    
    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * MySQLDataLoader.
     */
    public static final class Pool extends DataLoaderPool {
        
        @Override
        public Enumeration loaders() {
            return Enumerations.singleton(new MySQLDataLoader());
        }
    }
    
    /**
     * DataLoader for SQL files. Not using SQLDataLoader because we want
     * the loader to return our special MySQLDataObject's.
     */
    private static final class MySQLDataLoader extends UniFileLoader {
    
        public MySQLDataLoader() {
            super("org.netbeans.modules.db.sql.loader.SQLEditorSupportNormalTest$MySQLDataObject");
        }
    
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MySQLDataObject(primaryFile, this);
        }

        @Override
        protected void initialize() {
            super.initialize();
            ExtensionList extensions = new ExtensionList();
            extensions.addExtension("sql");
            setExtensions(extensions);
        }
    }
    
    /**
     * SQLDataObject which has MySQLEditorSupport in its cookie set instead
     * of the cookie added by SQLDataObject.
     */
    private static final class MySQLDataObject extends SQLDataObject {
        
        public MySQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
            super(primaryFile, loader);
            CookieSet cookies = getCookieSet();
            cookies.remove(cookies.getCookie(OpenCookie.class));
            cookies.add(new MySQLEditorSupport(this));
        }

        @Override
        protected Node createNodeDelegate() {
            return new SQLNode(this, getLookup());
        }
    }
    
    /**
     * SQLEditorSupport which allows finding out whether the saveDocument() method was called
     * and calling the componentDeactivated() method.
     */
    private static final class MySQLEditorSupport extends SQLEditorSupport {
        
        boolean saveDocumentCalled = false;
        
        public MySQLEditorSupport(SQLDataObject obj) {
            super(obj);
        }
        
        /**
         * In order to silently close a modified support.
         */
        @Override
        public boolean canClose() {
            return true;
        }
        
        @Override
        public void saveDocument() throws IOException {
            super.saveDocument();
            saveDocumentCalled = true;
        }
        
        public CloneableTopComponent.Ref getAllEditors() {
            return allEditors;
        }
        
        @Override
        public CloneableEditor createCloneableEditor() {
            return new SQLCloneableEditor(Lookups.singleton(this));
        }
        
    }
}
