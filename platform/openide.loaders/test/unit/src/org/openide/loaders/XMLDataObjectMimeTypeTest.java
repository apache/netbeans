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

package org.openide.loaders;

import java.awt.Dialog;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/**
 * the mime type of the resulting xml document shall be the same as is resolved by
 * mimetype resolvers to all fine tuned code templates, hyperlinking etc.
 * The xml data object shall not enforce text/xml on everyone.
 *
 * @author  mkleint
 */
public final class XMLDataObjectMimeTypeTest extends LoggingTestCaseHid {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private FileSystem lfs;
    private DataObject obj;

    public XMLDataObjectMimeTypeTest(String name) {
        super(name);
    }

    protected void setUp(String mime) throws Exception {
        super.setUp();

        String ext = getName();
        
        registerIntoLookup(new DD());

        DD x = Lookup.getDefault().lookup(DD.class);
        assertNotNull("DD is there", x);

        String fsstruct [] = new String [] {
            "AA/a."  + ext
        };

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);
        FileUtil.setMIMEType(ext, mime);
        FileObject fo = lfs.findResource("AA/a." + ext);
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), XMLDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
        DD.options = null;
        DD.disableTest = false;
    }

    private void waitForAWT() throws Exception {
        // just wait till all the stuff from AWT is finished
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                return null;
            }
        });
    }

    protected void tearDown() throws Exception {
        waitForAWT();
        DD.disableTest = true;

        super.tearDown();
        if (obj != null) {
            CloseCookie cc;
            cc = obj.getCookie(CloseCookie.class);
            if (cc != null) {
                DD.toReturn = NotifyDescriptor.NO_OPTION;
                cc.close();
            }
        }
        Repository.getDefault().removeFileSystem(lfs);

        waitForAWT();
    }

    public void testHasOpenCookie() throws Exception {
        setUp("text/x-pom+xml");
        assertNotNull(obj.getCookie(OpenCookie.class));
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
    
    public void testForUnknownMime() throws Exception {
        
        
        X l = X.getLoader(X.class);
        l.getExtensions().addExtension(getName());
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        
        try {
            setUp("content/unknown");
            EditorCookie cook = obj.getCookie(EditorCookie.class);
            StyledDocument doc = cook.openDocument();
            assertEquals("text/xml", doc.getProperty("mimeType"));
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
    }

    public void testForPlainMime() throws Exception {
        
        
        X l = X.getLoader(X.class);
        l.getExtensions().addExtension(getName());
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        
        try {
            setUp("text/plain");
            EditorCookie cook = obj.getCookie(EditorCookie.class);
            StyledDocument doc = cook.openDocument();
            assertEquals("text/xml", doc.getProperty("mimeType"));
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
    }


    /** Our own dialog displayer.
     */
    private static final class DD extends DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        public static boolean disableTest;

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException("Not implemented");
        }

        public Object notify(NotifyDescriptor descriptor) {
            if (disableTest) {
                return toReturn;
            } else {
                assertNull(options);
                assertNotNull(toReturn);
                options = descriptor.getOptions();
                Object r = toReturn;
                toReturn = null;
                return r;
            }
        }

    } // end of DD

    public static class X extends UniFileLoader {
        public X() {
            super(XMLDataObject.class);
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new XMLDataObject(primaryFile, this);
        }
        
    }
}
