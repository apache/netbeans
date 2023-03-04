/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.text;

import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.loaders.SimpleES;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.AddLoaderManuallyHid;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupportRedirector;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author alsimon
 */
public class XMLCESTest extends NbTestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportTest$Lkp");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    public XMLCESTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testCES() throws Exception {
        AddLoaderManuallyHid.addRemoveLoader(
            XMLDataLoader.getLoader(XMLDataLoader.class), true
        );
        MockLookup.setInstances(new XMLCESTest.Redirector());

        FileObject document = FileUtil.createData(FileUtil.getConfigRoot(), "dummyXMLDocument.xml");

        assertNotNull(document);

        DataObject object = DataObject.find(document);
        assertTrue(object instanceof XMLDataObject);

        XMLDataObject dataObject = (XMLDataObject) object;

        EditorCookie ces = dataObject.getLookup().lookup(EditorCookie.class);
        assertNotNull("CES found", ces);
    }
    
    public static final class Redirector extends CloneableEditorSupportRedirector {
    
        @Override
        protected CloneableEditorSupport redirect(Lookup ces) {
            return ces.lookup(CloneableEditorSupport.class);
        }
    }
    
    public static final class XMLDataLoader extends UniFileLoader {
        public XMLDataLoader() {
            super(XMLDataObject.class.getName());
            getExtensions().addExtension("xml");
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new XMLDataObject(primaryFile, this);
        }
        
    }
    
    private static final class XMLDataObject extends MultiDataObject implements CookieSet.Factory {

        public XMLDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
            getCookieSet().add(SimpleES.class, this);
        }

        @Override
        public <T extends Cookie> T createCookie(Class<T> klass) {
            CloneableEditorSupport sup = DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet());
            sup.setMIMEType("text/xml");
            return klass.cast(sup);
        }
    }
}
