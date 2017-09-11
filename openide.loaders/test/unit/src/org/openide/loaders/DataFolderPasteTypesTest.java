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
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;

public class DataFolderPasteTypesTest extends NbTestCase {

    private File dir;
    private Node folderNode;
    private LocalFileSystem testFileSystem;

    
    public DataFolderPasteTypesTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        testFileSystem = new LocalFileSystem();
        testFileSystem.setRootDirectory( getWorkDir() );
        Repository.getDefault().addFileSystem( testFileSystem );
        
        FileObject fo = FileUtil.createFolder( testFileSystem.getRoot(), "testDir");
        DataObject dob = DataObject.find( fo );
        folderNode = dob.getNodeDelegate();
    }

    protected void tearDown() throws Exception {

        clearWorkDir();
        Repository.getDefault().removeFileSystem( testFileSystem );
    }

    public void testNoPasteTypes() throws ClassNotFoundException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( new MockTransferable( new DataFlavor[] {flavor}, null ), list );
        assertEquals( 0, list.size() );
    }

    public void testJavaFileListPasteTypes() throws ClassNotFoundException, IOException {
        FileObject testFO = FileUtil.createData( testFileSystem.getRoot(), "testFile.txt" );
        File testFile = FileUtil.toFile( testFO );
        ArrayList fileList = new ArrayList(1);
        fileList.add( testFile );
        Transferable t = new MockTransferable( new DataFlavor[] {DataFlavor.javaFileListFlavor}, fileList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        FileObject[] children = testFileSystem.getRoot().getFileObject( "testDir" ).getChildren();
        assertEquals( 1, children.length );
        assertEquals( children[0].getNameExt(), "testFile.txt" );
    }

    public void testUriFileListPasteTypes() throws ClassNotFoundException, IOException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );
        FileObject testFO = FileUtil.createData( testFileSystem.getRoot(), "testFile.txt" );
        File testFile = FileUtil.toFile( testFO );
        String uriList = Utilities.toURI(testFile) + "\r\n";
        Transferable t = new MockTransferable( new DataFlavor[] {new DataFlavor("text/uri-list;class=java.lang.String")}, uriList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        FileObject[] children = testFileSystem.getRoot().getFileObject( "testDir" ).getChildren();
        assertEquals( 1, children.length );
        assertEquals( children[0].getNameExt(), "testFile.txt" );
    }

    /**
     * Test for bug 233673.
     *
     * @throws java.io.IOException
     */
    public void testJavaFileListWithRelativePaths() throws IOException {

        FileObject testFO = FileUtil.createData(testFileSystem.getRoot(),
                "absoluteTestFile.txt");
        File absoluteTestFile = FileUtil.toFile(testFO);
        File relativeTestFile = new File("relativeFile.txt");

        ArrayList fileList = new ArrayList(2);
        fileList.add(relativeTestFile);
        fileList.add(absoluteTestFile);
        Transferable t = new MockTransferable(
                new DataFlavor[]{DataFlavor.javaFileListFlavor}, fileList);

        DataFolder.FolderNode node = (DataFolder.FolderNode) folderNode;
        ArrayList<PasteType> list = new ArrayList<PasteType>();
        node.createPasteTypes(t, list);
        assertEquals("Relative path should be skipped", 1, list.size());
        PasteType paste = (PasteType) list.get(0);
        paste.paste();

        FileObject[] children = testFileSystem.getRoot().getFileObject(
                "testDir").getChildren();
        assertEquals(1, children.length);
        assertEquals(children[0].getNameExt(), "absoluteTestFile.txt");
    }

    private static class MockTransferable implements Transferable {
        private DataFlavor[] flavors;
        private Object data;
        public MockTransferable( DataFlavor[] flavors, Object data ) {
            this.flavors = flavors;
            this.data = data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for( int i=0; i<flavors.length; i++ ) {
                if( flavors[i].equals( flavor ) ) {
                    return true;
                }
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported( flavor ) ) {
                throw new UnsupportedFlavorException( flavor );
            }
            return data;
        }

    }
}
