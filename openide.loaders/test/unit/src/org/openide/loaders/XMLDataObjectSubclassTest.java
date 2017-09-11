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

import java.util.Enumeration;
import org.openide.filesystems.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import junit.framework.Assert;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.*;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author  Jaroslav Tulach
 */
public class XMLDataObjectSubclassTest extends org.netbeans.junit.NbTestCase {
    private FileObject data;
    private CharSequence log;

    public XMLDataObjectSubclassTest (String name) {
        super (name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();
        MockServices.setServices(Pool.class);
        
        log = Log.enable("org.openide.loaders", Level.WARNING);
        
        super.setUp ();
        String fsstruct [] = new String [] {
        };
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        data = FileUtil.createData (
            fs.getRoot (),
            "kuk/test/my.xml"
        );
        FileLock lock = data.lock ();
        OutputStream os = data.getOutputStream (lock);
        PrintStream p = new PrintStream (os);
        
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println ("<root>");
        p.println ("</root>");
        
        p.close ();
        lock.releaseLock ();

    }

    public void testCheckNoCookies() throws Exception {
        DataObject obj = DataObject.find(data);
        assertEquals("Correct obj type", MyXMLObject.class, obj.getClass());

        assertNull("No open cookie", obj.getLookup().lookup(OpenCookie.class));
        assertNull("No edit cookie", obj.getLookup().lookup(EditCookie.class));
        assertNull("No print cookie", obj.getLookup().lookup(PrintCookie.class));
        assertNull("No close cookie", obj.getLookup().lookup(CloseCookie.class));
    }

    public static final class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(DataLoader.getLoader(MyXMLLoader.class));
        }
    }

    public static final class MyXMLLoader extends UniFileLoader {

        public MyXMLLoader() {
            super(MyXMLObject.class.getName());
            getExtensions().addExtension("xml");
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyXMLObject(primaryFile, this);
        }
    }

    public static final class MyXMLObject extends XMLDataObject {

        public MyXMLObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader, false);
        }

    }
}
