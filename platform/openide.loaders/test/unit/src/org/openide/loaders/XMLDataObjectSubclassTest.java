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

import java.util.Enumeration;
import org.openide.filesystems.*;
import java.io.*;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.openide.cookies.*;
import org.openide.util.Enumerations;

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
