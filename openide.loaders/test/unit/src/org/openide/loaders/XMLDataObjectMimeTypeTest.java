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
