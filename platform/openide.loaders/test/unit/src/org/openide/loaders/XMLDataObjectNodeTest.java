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

import org.openide.filesystems.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.openide.cookies.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/** Tests that the XML DataObject takes the node as provided by its registered
 * Environment.Provider
 *
 * @author  Jaroslav Tulach
 */
public class XMLDataObjectNodeTest extends org.netbeans.junit.NbTestCase {
    private FileObject data;
    private CharSequence log;

    /** Creates new MultiFileLoaderHid */
    public XMLDataObjectNodeTest (String name) {
        super (name);
    }

    protected void setUp () throws Exception {
        log = Log.enable("org.openide.loaders", Level.WARNING);
        
        super.setUp ();
        String fsstruct [] = new String [] {
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        data = FileUtil.createData (
            fs.getRoot (),
            "kuk/test/my.xml"
        );
        FileLock lock = data.lock ();
        OutputStream os = data.getOutputStream (lock);
        PrintStream p = new PrintStream (os);
        
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem " +
            "1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n");
        p.println ("<filesystem>");
        p.println ("</filesystem>");
        
        p.close ();
        lock.releaseLock ();
        
        MockServices.setServices(Env.class);
    }
    
    protected void tearDown () throws Exception {
        super.tearDown ();
        TestUtilHid.destroyLocalFileSystem (getName());
    }
    
    public void testGetNodeBehaviour () throws Exception {
        Env.which = data;
        
        DataObject obj = DataObject.find (data);
        
        if (obj instanceof XMLDataObject) {
            // ok
        } else {
            fail("Expecting XMLDataObject: " + obj);
        }
        
        
        Node n = obj.getNodeDelegate();
        assertEquals("Node is taken from Env Provider", "ENV", n.getName());
    }

    public static final class Env implements Environment.Provider {
        static FileObject which;

        public Lookup getEnvironment(DataObject obj) {
            if (obj.getPrimaryFile().equals(which)) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName("ENV");
                return an.getLookup();
            }
            return null;
        }
    }
}
