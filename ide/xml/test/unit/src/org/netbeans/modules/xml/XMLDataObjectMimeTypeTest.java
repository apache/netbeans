/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml;

import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Enumerations;
import org.openide.util.Mutex;

/** 
 * the mime type of the resulting xml document shall be the same as is resolved by
 * mimetype resolvers to all fine tuned code templates, hyperlinking etc.
 * The xml data object shall not enforce text/xml on everyone.
 *
 * @author  mkleint
 */
public final class XMLDataObjectMimeTypeTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private LocalFileSystem lfs;
    private DataObject obj;

    public XMLDataObjectMimeTypeTest(String name) {
        super(name);
    }

    protected void setUp(String mime) throws Exception {
        super.setUp();
        MockServices.setServices(Pool.class);
        String ext = getName();
        
        clearWorkDir();
        lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileUtil.createData(lfs.getRoot(), "AA/a."  + ext);
        Repository.getDefault().addFileSystem(lfs);
        FileUtil.setMIMEType(ext, mime);
        FileObject fo = lfs.findResource("AA/a." + ext);
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), XMLDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
    }

    private void waitForAWT() throws Exception {
        // just wait till all the stuff from AWT is finished
        Mutex.EVENT.readAccess((Mutex.Action) () -> null);
    }

    protected void tearDown() throws Exception {
        waitForAWT();

        super.tearDown();
        if (obj != null) {
            CloseCookie cc;
            cc = obj.getCookie(CloseCookie.class);
            if (cc != null) {
                cc.close();
            }
        }
        Repository.getDefault().removeFileSystem(lfs);

        waitForAWT();
    }

    public void testHasEditorCookie() throws Exception {
        setUp("text/x-pom+xml");
        assertNotNull(obj.getCookie(EditorCookie.class));
    }
    
    public void testHasCorrectMimetype() throws Exception {
        setUp("text/x-pom+xml");
        EditorCookie cook = obj.getCookie(EditorCookie.class);
        StyledDocument doc = cook.openDocument();
        assertEquals("text/x-pom+xml", doc.getProperty("mimeType"));
    }
    
    public void testForPlainMime() throws Exception {
        XMLDataLoader l = XMLDataLoader.getLoader(XMLDataLoader.class);
        l.getExtensions().addExtension(getName());
        
        setUp("text/plain");
        EditorCookie cook = obj.getCookie(EditorCookie.class);
        StyledDocument doc = cook.openDocument();
        assertEquals("text/plain+xml", doc.getProperty("mimeType"));
    }
    
    public void testForUnknownContent() throws Exception {
        XMLDataLoader l = XMLDataLoader.getLoader(XMLDataLoader.class);
        l.getExtensions().addExtension(getName());
        
        setUp("content/unknown");
        EditorCookie cook = obj.getCookie(EditorCookie.class);
        StyledDocument doc = cook.openDocument();
        assertEquals("text/plain+xml", doc.getProperty("mimeType"));
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(XMLDataLoader.getLoader(XMLDataLoader.class));
        }
    }
    
}
