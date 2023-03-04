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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.netbeans.junit.*;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Utilities;

public class DragAndDropDataNodeTest extends NbTestCase {

    private LocalFileSystem testFileSystem;

    public DragAndDropDataNodeTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        testFileSystem = new LocalFileSystem();
        testFileSystem.setRootDirectory( getWorkDir() );
    }

    public void testClipboardCopy() throws IOException, ClassNotFoundException, UnsupportedFlavorException {
        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        FileObject fo = FileUtil.createData( testFileSystem.getRoot(), "dndtest.txt" );
        File tmpFile = FileUtil.toFile( fo );

        DataObject dob = DataObject.find( fo );
        DataNode node = new DataNode( dob, Children.LEAF );

        Transferable t = node.clipboardCopy();
        assertTrue( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) );
        List fileList = (List) t.getTransferData( DataFlavor.javaFileListFlavor );
        assertNotNull( fileList );
        assertEquals( 1, fileList.size() );
        assertTrue( fileList.contains( tmpFile ) );

        assertTrue( t.isDataFlavorSupported( uriListFlavor ) );
        String uriList = (String) t.getTransferData( uriListFlavor );
        assertEquals( Utilities.toURI(tmpFile)+"\r\n", uriList );
    }

    public void testClipboardCut() throws ClassNotFoundException, IOException, UnsupportedFlavorException {
        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        FileObject fo = FileUtil.createData( testFileSystem.getRoot(), "dndtest.txt" );
        File tmpFile = FileUtil.toFile( fo );

        DataObject dob = DataObject.find( fo );
        DataNode node = new DataNode( dob, Children.LEAF );

        Transferable t = node.clipboardCopy();
        assertTrue( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) );
        List fileList = (List) t.getTransferData( DataFlavor.javaFileListFlavor );
        assertNotNull( fileList );
        assertEquals( 1, fileList.size() );
        assertTrue( fileList.contains( tmpFile ) );

        assertTrue( t.isDataFlavorSupported( uriListFlavor ) );
        String uriList = (String) t.getTransferData( uriListFlavor );
        assertEquals( Utilities.toURI(tmpFile)+"\r\n", uriList );
    }
}
