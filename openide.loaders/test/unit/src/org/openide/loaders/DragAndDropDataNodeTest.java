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
