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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderRenameHandler;
import org.openide.loaders.LoaderTransfer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.test.MockLookup;

public class PackageViewTest extends NbTestCase {
    
    static {
        System.setProperty("org.netbeans.spi.java.project.support.ui.packageView.TRUNCATE_PACKAGE_NAMES", "true");
    }
    
    public PackageViewTest( String name ) {
        super( name );
    }
    
    private FileObject root;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new VQImpl());
        root = TestUtil.makeScratchDir(this);
        System.setProperty("PackageView.unitTest", "true");     //NOI18N
    }

    @Override
    protected int timeOut() {
        return 300000;
    }

    @RandomlyFails
    public void testFolders() throws Exception {
        assertNull( "source folder should not exist yet", root.getFileObject( "src" ) );
        
        
	// Create children
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        
        // Create folder
	FileUtil.createFolder( root, "src/a/b/c" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, }, 
                     true ); // Needs to compute the nodes first
        
        // Testing files/folders in ignored folders
                     
        // Create ignored folder             
        FileUtil.createFolder( root, "src/KRTEK.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
        
        // Create file in ignored folder
        FileUtil.createData( root, "src/KRTEK.folder/nonignored.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );             
                     
        // Create folder in ignored folder             
        FileObject nonignoredFolder = FileUtil.createFolder( root, "src/KRTEK.folder/nonignored.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
                     
        // Create file in NONignored folder which is under ignored folder            
        FileObject nonignoredFile = FileUtil.createData( root, "src/KRTEK.folder/nonignored.folder/nonignored.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
             
        // Rename the file             
        FileLock nfLock = nonignoredFile.lock();
        nonignoredFile.rename( nfLock, "othername.file", null );
        nfLock.releaseLock();     
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );

        // Delete the file and folder
        nonignoredFile.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
        nonignoredFolder.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );

                     
                     
        // Create some other folder
        FileUtil.createFolder( root, "src/e/f/g" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 0 } );
        
        
        // Add some ignored files/folders             
        FileUtil.createFolder( root, "src/e/KRTEK" );
        FileUtil.createFolder( root, "src/e/f/KRTEK.folder" );
        FileUtil.createData( root, "src/e/f/KRTEK.file" );        
        FileUtil.createFolder( root, "src/e/f/g/KRTEK.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 0 } );
                                          
                    
        // Create file
        FileUtil.createData( root, "src/e/f/g/Some.java" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );

        // Create ignored file
        FileUtil.createData( root, "src/e/f/g/KRTEK.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );
                     
        // Create file in ignored folder
        FileUtil.createData( root, "src/e/f/g/KRTEK.folder/Tag" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );
                     
                                  
        // Add empty package and ignored package
        FileUtil.createFolder( root, "src/x/y/z/KRTEK" );        
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                     
        // Add file to folder                       
        FileObject x_y_z_some = FileUtil.createData( root, "src/x/y/z/Some.java" );        
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 1 } );
                     
        // Remove file from folder
        x_y_z_some.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                   
                     
        // Add file to super folder
        FileObject x_y_some = FileUtil.createData( root, "src/x/y/Some.java" );        
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y", "x.y.z" },
                     new int[] { 0, 1, 1, 0 } );
                     
        // Remove file from superfolder
        x_y_some.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
        
        
        // Add subfolder    
        FileObject x_y_z_w = FileUtil.createFolder( root, "src/x/y/z/w" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z.w" },
                     new int[] { 0, 1, 0 } );
                     
        // Remove subfolder
        x_y_z_w.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                     
        // Remove super folder
        FileObject x_y = root.getFileObject( "src/x/y" );
        x_y.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x" },
                     new int[] { 0, 1, 0 } );
        
        // Remove root folder
        FileUtil.createFolder( root, "src/x/v/w" );
                assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.v.w" },
                     new int[] { 0, 1, 0 } );
        FileObject x = root.getFileObject( "src/x" );
        x.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 1 } );

        /*   
         * Sometime fails in Jarda's DataObject container test
         *          
        // Rename ignored file to unignored
        FileObject e_f_g_krtekFile = root.getFileObject( "src/e/f/g/KRTEK.file" );
        FileLock krtekLock = e_f_g_krtekFile.lock();
        e_f_g_krtekFile.rename( krtekLock, "ZIZALA.file", null );
        krtekLock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 2 } );
        
                             
        // Rename unignored to ignored file
        e_f_g_krtekFile = root.getFileObject( "src/e/f/g/ZIZALA.file" );
        krtekLock = e_f_g_krtekFile.lock();
        e_f_g_krtekFile.rename( krtekLock, "KRTEK.file", null );
        krtekLock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 1 } );             
        */ 
                     
        // Rename leaf folder
        FileObject e_f_g = root.getFileObject( "src/e/f/g" );
        FileLock lock = e_f_g.lock();
        e_f_g.rename( lock, "h", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h" },
                     new int[] { 0, 1 } );
        
                     
        // Rename ignored folder to unignored folder
        FileObject e_f_h_krtekFolder = root.getFileObject( "src/e/f/h/KRTEK.folder" );
        lock = e_f_h_krtekFolder.lock();
        e_f_h_krtekFolder.rename( lock, "ZIZALA", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h", "e.f.h.ZIZALA" },
                     new int[] { 0, 1, 1 } );
                     
        // Rename unignored folder back to ignored folder
        e_f_h_krtekFolder = root.getFileObject( "src/e/f/h/ZIZALA" );
        lock = e_f_h_krtekFolder.lock();
        e_f_h_krtekFolder.rename( lock, "KRTEK.folder", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h" },
                     new int[] { 0, 1 } );
                     
                                          
        // Rename super folder
        FileUtil.createFolder( root, "src/e/f/g" );
        FileUtil.createFolder( root, "src/e/f/i" );
        FileObject e_f = root.getFileObject( "src/e/f" );
        lock = e_f.lock();
        e_f.rename( lock, "r", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.r.g", "e.r.h", "e.r.i" },
                     new int[] { 0, 0, 1, 0 } );
                     
        // Rename rootfolder 
        FileObject e = root.getFileObject( "src/e/" );
        final Collection<? extends String> hContentBeforeRename = toFileNameExt(root.getFileObject("src/e/r/h").getChildren());
        lock = e.lock();
        e.rename( lock, "t", null );
        lock.releaseLock();
        final Collection<? extends String> hContentAfterRename = toFileNameExt(root.getFileObject("src/t/r/h").getChildren());
        //Filesystems rename issue
        assertEquals(hContentBeforeRename,hContentAfterRename);
        assertNodes( ch, 
                     new String[] { "a.b.c", "t.r.g", "t.r.h", "t.r.i" },
                     new int[] { 0, 0, 1, 0 } );                
                     
        // Test truncated package names
        FileUtil.createFolder(root, "src/org/foo/something/whatever");
        assertNodes( ch, 
                     new String[] { "a.b.c", "o.foo.som.whatever", "t.r.g", "t.r.h", "t.r.i" },
                     new int[] { 0, 0, 0, 1, 0 } );
        
                     
                     
    }

    @RandomlyFails // NB-Core-Build #3802
    public void testDefaultPackage() throws Exception {
	// Create children        
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        // Default package should be there
        assertNodes( ch, 
                     new String[] { "<default package>" },
                     new int[] { 0 }, 
                     true ); // Needs to compute the nodes first
                     
        // Default package should disappear             
        FileObject a = FileUtil.createFolder( root, "src/a" );
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, } );
                     
        // Default package should appear again
        FileObject someJava = FileUtil.createData( root, "src/Some.java" );
        assertNodes( ch, 
                     new String[] { "<default package>", "a", },
                     new int[] { 1, 0, } );
                     
        // Disappear again             
        someJava.delete();
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, } );             
                     
        // And appear again
        a.delete();
        assertNodes( ch, 
                     new String[] { "<default package>" },
                     new int[] { 0 } );
        
    }
    
    public void testNodeDestroy() throws Exception {
	FileObject srcRoot;
        FileObject toDelete;
        SourceGroup group;
        Node rootNode;
        Node n;
        
        // Empty parent
        srcRoot = FileUtil.createFolder( root, "ep" );
        toDelete = FileUtil.createFolder( srcRoot, "a/aa" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );

        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", },
                     new int[] { 0, }, 
                     true ); // Needs to compute the nodes first
        
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[0] );
        
        // Non-Empty parent
        srcRoot = FileUtil.createFolder( root, "nep" );
        toDelete = FileUtil.createFolder( srcRoot, "a/aa" );
        FileUtil.createData( srcRoot, "a/some.java" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a", "a.aa" },
                     new int[] { 1, 0 }, 
                     true ); // Needs to compute the nodes first
        
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        
               
        // Non empty siblings
        srcRoot = FileUtil.createFolder( root, "es" );
        FileObject a = FileUtil.createFolder( srcRoot, "a" );
        FileUtil.createFolder( a, "aa" );
        FileUtil.createData( srcRoot, "a/aa/some.java" );
        toDelete = FileUtil.createFolder( srcRoot, "a/b" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", "a.b" },
                     new int[] { 1, 0 }, 
                     true ); // Needs to compute the nodes first
        
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );
        
        // Empty siblings
        srcRoot = FileUtil.createFolder( root, "nes" );
        a = FileUtil.createFolder( srcRoot, "a" );
        FileUtil.createFolder( a, "aa" );
        toDelete = FileUtil.createFolder( srcRoot, "a/b" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", "a.b" },
                     new int[] { 0, 0 }, 
                     true ); // Needs to compute the nodes first
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );
        
        
        // Do not delete subfoders        
        srcRoot = FileUtil.createFolder( root, "dds" );
        a = FileUtil.createFolder( srcRoot, "a" );        
        FileUtil.createData( srcRoot, "a/some.java" );        
        FileObject aa = FileUtil.createFolder( a, "aa" );
        FileUtil.createData( srcRoot, "a/aa/some.java" );        
        toDelete = a;
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a", "a.aa", },
                     new int[] { 1, 1 }, 
                     true ); // Needs to compute the nodes first
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );

        //Issue #49075
        srcRoot = FileUtil.createFolder(root, "issue49075");
        a = srcRoot.createFolder("a");
        FileObject b = FileUtil.createFolder( a, "b" );
        FileObject c = FileUtil.createFolder( b, "c" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        assertNodes(rootNode.getChildren(), new String[] { "a.b.c" },true );
        File cFile = FileUtil.toFile(c);
        File bFile = FileUtil.toFile(b);
        cFile.delete();
        bFile.delete();
        a.getFileSystem().refresh(false);
        assertNodes(rootNode.getChildren(), new String[] { "a" },true );
    }

    @RandomlyFails // NB-Core-Build #3190
    public void testFindPath() throws Exception {
	// Create children        
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Node sourceRoot = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( sourceRoot.getChildren(), 
                     new String[] { "<default package>" },
                     new int[] { 0 }, 
                     true ); // Needs to compute the nodes first
        
        FileObject a_b_c = FileUtil.createFolder( root, "src/a/b/c" );
        FileObject a_b = root.getFileObject( "src/a/b" );
        FileObject e_f_g = FileUtil.createFolder( root, "src/e/f/g" );
        
        FileObject dp_java = FileUtil.createData( root, "src/DP" );
        FileObject a_b_c_java = FileUtil.createData( root, "src/a/b/c/ABC" );
        FileObject a_b_java = FileUtil.createData( root, "src/a/b/AB" );
        FileObject e_f_g_java = FileUtil.createData( root, "src/e/f/g/EFG.someext" );
        
        // Try to find standard files
        Node n;
        n = PackageView.findPath( sourceRoot, a_b_c_java );
        assertNode( n, "ABC" );
        // Check also DataObject:
        n = PackageView.findPath(sourceRoot, DataObject.find(a_b_c_java));
        assertNode(n, "ABC");
                
        n = PackageView.findPath( sourceRoot, a_b_java );
        assertNode( n, "AB" );
        
        n = PackageView.findPath( sourceRoot, e_f_g_java );
        assertNode( n, "EFG.someext" );
        
        // Try to find folders
        n = PackageView.findPath( sourceRoot, a_b_c );
        assertNode( n, "a.b.c" );
        
        n = PackageView.findPath( sourceRoot, a_b );
        assertNode( n, "a.b" );
        
        n = PackageView.findPath( sourceRoot, e_f_g );
        assertNode( n, "e.f.g" );
        
        // Try file in default package
        n = PackageView.findPath( sourceRoot, dp_java );
        assertNode( n, "DP" );
        
        n = PackageView.findPath( sourceRoot, group.getRootFolder() );
        assertNode( n, "" );
                
        dp_java.delete(); // Dp will disapear should return root node
        waitForAWT();
        n = PackageView.findPath( sourceRoot, group.getRootFolder() );
        assertNode( n, group.getName() );
        
        
    }

    @RandomlyFails // Default package exists
    public void testCopyPaste () throws Exception {
        //Setup 2 sourcegroups
        FileObject root1 = root.createFolder("src1");
        FileObject tmp = root1.createFolder ("src1test1");
        root1.createFolder ("src1test2");
        createFile(tmp, "src1test1", "File1");
        createFile(tmp, "src1test1", "File2");
        FileObject root2 = root.createFolder("src2");
        SourceGroup group1 = new SimpleSourceGroup(root1);
        SourceGroup group2 = new SimpleSourceGroup(root2);
        Node rn1 = PackageView.createPackageView( group1 );        
        Node rn2 = PackageView.createPackageView( group2 );
        Node[] nodes = rn1.getChildren().getNodes(true);
        
        //Single package into same source root
        Transferable t = nodes[0].clipboardCopy();
        PasteType[] pts = rn1.getPasteTypes(t);
        assertEquals ("Single package into same source root",0, pts.length);        
        
        //Multiple packages into same source root
        t = new ExTransferable.Multi (new Transferable[] {nodes[0].clipboardCopy(),
                                                          nodes[1].clipboardCopy()});
        pts = rn1.getPasteTypes(t);
        assertEquals ("Multiple packages into same source root",0,pts.length);
        
        //Single file into package
        Node[] fileNodes = nodes[0].getChildren().getNodes(true);
        t = fileNodes[0].clipboardCopy();
        pts = nodes[1].getPasteTypes(t);
        assertEquals ("Single file into package",1, pts.length);        
        pts[0].paste();
        Node[] resultNodes = nodes[1].getChildren().getNodes(true);
        assertEquals ("Wrong paste result",1, resultNodes.length);        
        assertEquals ("Wrong paste result",fileNodes[0].getDisplayName(), resultNodes[0].getDisplayName());                
        resultNodes[0].getLookup().lookup(DataObject.class).delete();
        
        //Multiple files into package
        t = new ExTransferable.Multi (new Transferable[] {fileNodes[0].clipboardCopy(),
                                                          fileNodes[1].clipboardCopy()});
        pts = nodes[1].getPasteTypes(t);
        assertEquals ("Multiple files into package",1, pts.length);        
        pts[0].paste();
        /* XXX fails if this test is run first; timing-sensitive:
        //After change - requires optimalResults
        assertNodes (nodes[1].getChildren(), new String[] {
            fileNodes[0].getDisplayName(),
            fileNodes[1].getDisplayName(),
        }, true);
        */
        for (Node n : nodes[1].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null)
                dobj.delete ();
        }
                
        //Single file into source root
        t = fileNodes[0].clipboardCopy();
        pts = rn1.getPasteTypes(t);
        assertEquals ("Single file into package",1, pts.length);        
        pts[0].paste();
        String defaultPackageName = ResourceBundle.getBundle("org/netbeans/modules/java/project/Bundle").getString("LBL_DefaultPackage");
        assertNodes(rn1.getChildren(), new String[] {
            defaultPackageName,
            "src1test1",
            "src1test2",
        }, true);
        for (Node n : rn1.getChildren().getNodes(true)) {
            if (defaultPackageName.equals(n.getDisplayName())) {
                assertNodes(n.getChildren(), new String[] {
                    fileNodes[0].getDisplayName(),
                }, true);
                for (Node n2 : n.getChildren().getNodes(true)) {
                    DataObject dobj = n2.getLookup().lookup(DataObject.class);
                    if (dobj != null) {
                        dobj.delete ();
                    }
                }
                break;
            }
        }        
        //Multiple files into source root
        //Verify preconditions
        FileObject[] files = rn1.getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
        assertEquals("Invalid initial file count",2,files.length);

        t = new ExTransferable.Multi (new Transferable[] {fileNodes[0].clipboardCopy(),
                                                          fileNodes[1].clipboardCopy()});
        pts = rn1.getPasteTypes(t);
        assertEquals ("Multiple files into source root",1, pts.length);        
        pts[0].paste();
        //Verify that the files was added, the used PasteType is DataFolder's PasteType
        files = rn1.getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
        assertEquals("Invalid final file count",4,files.length);
        Set<String> s = new HashSet<String>();
        s.add(fileNodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getNameExt());
        s.add(fileNodes[1].getLookup().lookup(DataObject.class).getPrimaryFile().getNameExt());
        for (FileObject f : files) {
            s.remove(f.getNameExt());
        }
        assertTrue("The following files were not created: "+s.toString(),s.size()==0);
        assertNodes(rn1.getChildren(), new String[] {
            defaultPackageName,
            "src1test1",
            "src1test2",
        }, true);
        for (Node n : rn1.getChildren().getNodes(true)) {
            if (defaultPackageName.equals(n.getDisplayName())) {
                assertNodes(n.getChildren(), new String[] {
                    fileNodes[0].getDisplayName(),
                    fileNodes[1].getDisplayName()
                }, true);
                for (Node n2 : n.getChildren().getNodes(true)) {
                    DataObject dobj = n2.getLookup().lookup(DataObject.class);
                    if (dobj != null) {
                        dobj.delete ();
                    }
                }
                break;
            }
        }
        
        //Single package into different source root
        t = nodes[0].clipboardCopy();
        pts = rn2.getPasteTypes(t);
        assertEquals ("Single package into different source root",1,pts.length);
        pts[0].paste ();
        assertNodes (rn2.getChildren(), new String[] {"src1test1"}, true);
        rn2.getChildren().getNodes(true)[0].getLookup().lookup(DataObject.class).delete();
        
        //Multiple packages into different source root
        t = new ExTransferable.Multi (new Transferable[] {nodes[0].clipboardCopy(),
                                                          nodes[1].clipboardCopy()});
        pts = rn2.getPasteTypes(t);
        assertEquals ("Multiple packages into different source root",1,pts.length);
        pts[0].paste ();
        assertNodes (rn2.getChildren(), new String[] {"src1test1","src1test2"}, true);
        for (Node n : rn2.getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null)
                dobj.delete ();
        }
        
        //One more case (Issue #48246), Copy default pkg test
        FileObject defPkgFileRoot1 = createFile(root1, null, "TestDP1");
        nodes = rn1.getChildren().getNodes(true);
        FileObject defPkgFileRoot2 = createFile(root2, null, "TestDP2");
        
        Node defPkgNode = null;
        for (Node n : nodes) {
            if (n.getDisplayName().equals (defaultPackageName)) {
                defPkgNode = n;
                break;
            }
        }
        assertNotNull("Default package exists",defPkgNode);
        t = defPkgNode.clipboardCopy();
        pts = rn2.getPasteTypes(t);
        assertEquals ("Multiple packages into different source root",1,pts.length);
        pts[0].paste();
        assertNodes (rn2.getChildren(), new String[] {defaultPackageName}, true);
        defPkgFileRoot1.delete();
        for (Node n : rn2.getChildren().getNodes(true)[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }
    }

    @RandomlyFails
    public void testCopyPasteJavaFileFromClipboard() throws Exception {

        //Setup sourcegroups
        FileObject root1 = root.createFolder("paste-src");
        SourceGroup group = new SimpleSourceGroup(root1);
        Node rn = PackageView.createPackageView(group);
        Node[] nodes = rn.getChildren().getNodes(true);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("import java.util.*;"
                + "class C{}"
                + "public class PC{}"
                + "interface I{}"
                + "enum En{}"
        );
        clipboard.setContents(selection, selection);

        Transferable transferable = clipboard.getContents(selection);
        if (nodes.length > 0) {
            PasteType[] pts = nodes[0].getPasteTypes(transferable);
            pts[0].paste();
            FileObject[] files = nodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
            assertEquals("File count", 1, files.length);
            assertEquals("File name", "PC.java", files[0].getName() + "." + files[0].getExt());
            assertEquals("File contents","import java.util.*;"
                    + "class C{}"
                    + "public class PC{}"
                    + "interface I{}"
                    + "enum En{}",
                     files[0].asText());
        }

         for (Node n : nodes[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }

    }
    
     @RandomlyFails
    public void testCopyPasteJavaFileFromClipboard_createJavaFileForEnum() throws Exception {

        //Setup sourcegroups
        FileObject root1 = root.createFolder("paste-src");
        SourceGroup group = new SimpleSourceGroup(root1);
        Node rn = PackageView.createPackageView(group);
        Node[] nodes = rn.getChildren().getNodes(true);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("import java.util.*;"
                + "class C{}"
                + "class PC{}"
                + "interface I{}"
                + "public enum En{}"
        );
        clipboard.setContents(selection, selection);

        Transferable transferable = clipboard.getContents(selection);
        if (nodes.length > 0) {
            PasteType[] pts = nodes[0].getPasteTypes(transferable);
            pts[0].paste();
            FileObject[] files = nodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
            assertEquals("File count", 1, files.length);
            assertEquals("File name", "En.java", files[0].getName() + "." + files[0].getExt());
            assertEquals("File contents","import java.util.*;"
                    + "class C{}"
                    + "class PC{}"
                    + "interface I{}"
                    + "public enum En{}",
                     files[0].asText());
        }

         for (Node n : nodes[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }

    }
    
     @RandomlyFails
    public void testCopyPasteJavaFileFromClipboard_createJavaFileForInterface() throws Exception {

        //Setup sourcegroups
        FileObject root1 = root.createFolder("paste-src");
        SourceGroup group = new SimpleSourceGroup(root1);
        Node rn = PackageView.createPackageView(group);
        Node[] nodes = rn.getChildren().getNodes(true);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("import java.util.*;"
                + "class C{}"
                + "class PC{}"
                + "public interface I{}"
                + "enum En{}"
        );
        clipboard.setContents(selection, selection);

        Transferable transferable = clipboard.getContents(selection);
        if (nodes.length > 0) {
            PasteType[] pts = nodes[0].getPasteTypes(transferable);
            pts[0].paste();
            FileObject[] files = nodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
            assertEquals("File count", 1, files.length);
            assertEquals("File name", "I.java", files[0].getName() + "." + files[0].getExt());
            assertEquals("File contents","import java.util.*;"
                    + "class C{}"
                    + "class PC{}"
                    + "public interface I{}"
                    + "enum En{}",
                     files[0].asText());
        }

         for (Node n : nodes[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }

    }

    @RandomlyFails
    public void testCopyPasteJavaFileFromClipboard_removeExistingPackageName() throws Exception {

        //Setup sourcegroups
        FileObject root1 = root.createFolder("paste-src-rm-package");
        SourceGroup group = new SimpleSourceGroup(root1);
        Node rn = PackageView.createPackageView(group);
        Node[] nodes = rn.getChildren().getNodes(true);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("package copy.paste     ."
                + "java"
                + ";"
                + "import java.util.*;"
                + "public class PC{}"
        );
        clipboard.setContents(selection, selection);

        Transferable transferable = clipboard.getContents(selection);
        if (nodes.length > 0) {
            PasteType[] pts = nodes[0].getPasteTypes(transferable);
            pts[0].paste();
            FileObject[] files = nodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
            assertEquals("File count", 1, files.length);
            assertEquals("File name", "PC.java", files[0].getName() + "." + files[0].getExt());
            assertEquals("File contents","import java.util.*;"
                    + "public class PC{}",
                     files[0].asText());
        }

         for (Node n : nodes[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }

    }

    @RandomlyFails
    public void testCopyPasteJavaFileFromClipboard_CompilationErrorInCode() throws Exception {

        //Setup sourcegroups
        FileObject root1 = root.createFolder("paste-src-error");
        SourceGroup group = new SimpleSourceGroup(root1);
        Node rn = PackageView.createPackageView(group);
        Node[] nodes = rn.getChildren().getNodes(true);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("Not a valid java file content. class Hello java my.sh. this test case is used for testing copy and paste from clipboard");
        clipboard.setContents(selection, selection);

        Transferable transferable = clipboard.getContents(selection);
        if (nodes.length > 0) {
            PasteType[] pts = nodes[0].getPasteTypes(transferable);
            pts[0].paste();
            FileObject[] files = nodes[0].getLookup().lookup(DataObject.class).getPrimaryFile().getChildren();
            assertEquals("File count", 1, files.length);
            assertEquals("File name", "Hello.java", files[0].getName() + "." + files[0].getExt());
            assertEquals("Not a valid java file content. class Hello java my.sh. this test case is used for testing copy and paste from clipboard",
                     files[0].asText());
        }

         for (Node n : nodes[0].getChildren().getNodes(true)) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }
    }

    @RandomlyFails
    public void testRename() throws Exception {
        assertNull( "source folder should not exist yet", root.getFileObject( "src" ) );
        
        
	// Create children
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        // Create folder
	FileUtil.createFolder( root, "src/a" );
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, },
                     true );
        
        Node n = ch.findChild( "a" );                     
        n.setName( "b" );        
        assertNodes( ch, 
                     new String[] { "b", },
                     new int[] { 0, } );
        
        FileUtil.createFolder( root, "src/b/c" );
        assertNodes( ch, 
                     new String[] { "b.c", },
                     new int[] { 0, } );
        
        n = ch.findChild( "b.c" );                     
        n.setName( "b.d" );        
        assertNodes( ch, 
                     new String[] { "b.d", },
                     new int[] { 0, } );
        
        n = ch.findChild( "b.d" );                     
        n.setName( "a.d" );
        assertNodes( ch, 
                     new String[] { "a.d", },
                     new int[] { 0, } );
        
        FileUtil.createFolder( root, "src/a/e" );
        assertNodes( ch, 
                     new String[] { "a.d", "a.e" },
                     new int[] { 0, 0 } );
        
        n = ch.findChild( "a.e" );                     
        n.setName( "a.f" );
        assertNodes( ch, 
                     new String[] { "a.d", "a.f" },
                     new int[] { 0, 0 } );
        
        
        n = ch.findChild( "a.d" );                     
        n.setName( "c.d" );
        assertNodes( ch, 
                     new String[] { "a.f", "c.d"},
                     new int[] { 0, 0 } );
        
        n = ch.findChild( "a.f" );                     
        n.setName( "c.f" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f" },
                     new int[] { 0, 0 } );
                     
        
        FileUtil.createFolder( root, "src/x/y/z" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "x.y.z" },
                     new int[] { 0, 0, 0 } );
        n = ch.findChild( "x.y.z" );                     
        n.setName( "x.y" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "x.y" },
                     new int[] { 0, 0, 0 } );                                          
        n = ch.findChild( "x.y" );                     
        n.setName( "p.me.tools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );                     
        n = ch.findChild( "p.me.tools" );
        n.setName( "p.metools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.metools" },
                     new int[] { 0, 0, 0 } );                                          
        n = ch.findChild( "p.metools" );
        n.setName( "p.me.tools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );                     
        n = ch.findChild( "p.me.tools" );
        n.setName( "p.me.toolsx" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.toolsx" },
                     new int[] { 0, 0, 0 },
                     true);
        n = ch.findChild( "p.me.toolsx" );
        n.setName( "p.me.tools" );
        assertNodes( ch,
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );
    }

    private static void assertNodes( Children children, String[] nodeNames, boolean optimalResult ) throws InterruptedException, InvocationTargetException {
        assertNodes( children, nodeNames, null, optimalResult );
    }

    private static void assertNodes( Children children, String[] nodeNames ) throws InterruptedException, InvocationTargetException {
        assertNodes( children, nodeNames, null, false );
    }

    private static void assertNodes (Children children, String[] nodeNames, int[] childCount) throws InterruptedException, InvocationTargetException {
        assertNodes(children, nodeNames, childCount, false);
    }

    private static void assertNodes( Children children, String[] nodeNames, int[] childCount, boolean optimalResult ) throws InterruptedException, InvocationTargetException {
        waitForAWT();
        Node[] nodes = children.getNodes (optimalResult);
        String[] actualNodeNames = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            actualNodeNames[i] = nodes[i].getDisplayName();
        }
        assertEquals("Wrong # or names of nodes", Arrays.asList(nodeNames), Arrays.asList(actualNodeNames));
        
        for( int i = 0; i < nodeNames.length; i++ ) {
            if ( childCount != null ) {
                if ( childCount[i] == 0 ) {
                    assertEquals( "Node should be leaf", true, nodes[i].isLeaf() );
                }
                else {
                    assertEquals( "Node should not be leaf", false, nodes[i].isLeaf() );
                }
                
                assertEquals( "Wrong number of children. Node: " + nodeNames[i] +".", childCount[i], nodes[i].getChildren().getNodes( true ).length );
                
                
                DataObject.Container cont = nodes[i].getLookup().lookup (DataObject.Container.class);
                if (cont != null) {
                    Node[] arr = nodes[i].getChildren ().getNodes ( true );
                    DataObject[] child = cont.getChildren ();
                    for (int k = 0, l = 0; k < arr.length; k++) {
                        if ( !VisibilityQuery.getDefault().isVisible( child[k].getPrimaryFile() ) ) {
                            continue;
                        }
                        DataObject myObj = arr[l].getLookup().lookup(DataObject.class);
                        assertNotNull ("Data object should be found for " + arr[k], myObj);
                        if (child.length <= k) {
                            fail ("But there is no object for node: " + arr[k]);
                        } else {
                            assertEquals ("child objects are the same", child[k], myObj);
                        }
                        l++;
                    }
                }
                
            }
        }
    }
    
    private static void assertNode( Node n, String name ) {
        
        if ( name != null ) {
            assertNotNull( "Node " + name +" not found", n  );
            assertEquals( "Wrong name", name, n.getName() );             
        }
        else {
            assertNull( "No node should be found", n );
        }
        
    }
    
    private static void assertFileObjects( FileObject folder, String[] names ) {
        
        assertTrue( "Has to be a folder ", folder.isFolder() );
        
        FileObject[] children = folder.getChildren();
        String[] chNames = new String[ children.length ];
        for( int i = 0; i < children.length; i++ ) {            
            chNames[i] = children[i].getNameExt();
        }
        
        Arrays.sort( names );
        Arrays.sort( chNames );
        
        assertTrue( "Arrays have to be equal ", Arrays.equals( names, chNames ) );
        
    }
    
    private static FileObject createFile (FileObject parent, String pkg, String name) throws IOException {
        FileObject fo = parent.createData (name,"java");
        FileLock lock = fo.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (fo.getOutputStream(lock)));            
            try {
                if (pkg != null) {
                    out.println ("package "+pkg+";");
                }
                out.println("public class "+name+" {");            
                out.println("}");
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    
    private static void waitForAWT () throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        } );
    }
    
    private static Collection<? extends String> toFileNameExt(final FileObject... files) {
        final List<String> names = new ArrayList<String>(files.length);
        for (FileObject fo : files) {
            names.add(fo.getNameExt());
        }
        Collections.sort(names);
        return names;
    }
    
    private static class SimpleSourceGroup implements SourceGroup {
        
        private FileObject root;
        
        public SimpleSourceGroup( FileObject root ) {
            this.root = root;
        }
        
        public FileObject getRootFolder() {
            return root;
        }
        
        public String getName() {
            return "TestGroup";
        }
        
        public String getDisplayName() {
            return getName();
        }
        
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override public boolean contains(FileObject file) {
            return FileUtil.isParentOf( root, file );
        }
    
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        public void removePropertyChangeListener(PropertyChangeListener listener) {}
        
    }
    
    private static class VQImpl implements VisibilityQueryImplementation {
        
        public static String IGNORED = "KRTEK"; 
        
        public boolean isVisible(FileObject file) {
            return !file.getNameExt().startsWith( IGNORED );
        }

        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}
        
    }

    @RandomlyFails // NB-Core-Build #988
    public void testFilteredViews() throws Exception {
        final FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        class Grp implements SourceGroup {
            public FileObject getRootFolder() {
                return r;
            }
            public String getName() {
                return "test";
            }
            public String getDisplayName() {
                return "Test";
            }
            public Icon getIcon(boolean opened) {
                return null;
            }
            boolean sense = true;
            @Override public boolean contains(FileObject file) {
                String path = FileUtil.getRelativePath(r, file);
                if (path == null) {
                    return false;
                }
                return !path.matches(".+/(" + (sense ? "bad" : "contemporary") + "(/|$)|" + (sense ? "Ugly" : "Pretty") + ".*\\.java$)");
            }
            PropertyChangeSupport pcs = new PropertyChangeSupport(Grp.this);
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                pcs.addPropertyChangeListener(listener);
            }
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }
            void nonsense() {
                sense = !sense;
                pcs.firePropertyChange(PROP_CONTAINERSHIP, null, null);
            }
        }
        FileUtil.createData(r, "a/good/man/is/hard/to/Find.java");
        FileUtil.createData(r, "museum/of/contemporary/Art.java");
        FileUtil.createData(r, "museum/of/bad/Art.java");
        FileUtil.createData(r, "net/pond/aquafowl/PrettyDuckling.java");
        FileUtil.createData(r, "net/pond/aquafowl/UglyDuckling.java");
        FileUtil.createData(r, "net/pond/aquafowl/KRTEK");
        Grp g = new Grp();
        Node n = PackageView.createPackageView(g);
        assertTree("Test{a.goo.man.is.har.to{Find.java}, m.of.contemporary{Art.java}, n.pon.aquafowl{PrettyDuckling.java}}", n);
        g.nonsense();
        assertTree("Test{a.goo.man.is.har.to{Find.java}, m.of.bad{Art.java}, n.pon.aquafowl{UglyDuckling.java}}", n);
        g = new Grp();
        n = new TreeRootNode(g, false);
        assertTree("Test{a{good{man{is{hard{to{Find.java}}}}}}, museum{of{contemporary{Art.java}}}, net{pond{aquafowl{PrettyDuckling.java}}}}", n);
        g.nonsense();
        assertTree("Test{a{good{man{is{hard{to{Find.java}}}}}}, museum{of{bad{Art.java}}}, net{pond{aquafowl{UglyDuckling.java}}}}", n);
        g = new Grp();
        n = new TreeRootNode(g, true);
        assertTree("Test{a.good.man.is.hard.to{Find.java}, museum.of.contemporary{Art.java}, net.pond.aquafowl{PrettyDuckling.java}}", n);
        g.nonsense();
        assertTree("Test{a.good.man.is.hard.to{Find.java}, museum.of.bad{Art.java}, net.pond.aquafowl{UglyDuckling.java}}", n);
    }

    public void testMemoryLeak() throws Exception { // #99804
        FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        FileUtil.createData(r, "some/pkg/Clazz.java");
        SourceGroup g = new SimpleSourceGroup(r);
        assertTree("TestGroup{s.pkg{Clazz.java}}", PackageView.createPackageView(g));
        Reference<?> ref = new WeakReference<Object>(g);
        r = null;
        g = null;
        assertGC("can collect source group", ref);
    }

    private static void assertTree(String expected, Node n) {
        assertEquals(expected, printTree(n).replace('[', '{').replace(']', '}'));
    }
    private static String printTree(Node n) {
        String name = n.getDisplayName();
        if (n.isLeaf()) {
            return name;
        } else {
            List<String> kidNames = new ArrayList<String>();
            for (Node kid : n.getChildren().getNodes(true)) {
                kidNames.add(printTree(kid));
            }
            return name + kidNames;
        }
    }

    private SourceGroup sampleGroup() throws IOException {
        FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        FileUtil.createData(r, "org/netbeans/api/stuff/Stuff.java");
        FileUtil.createData(r, "org/netbeans/modules/stuff/Bundle.properties");
        FileUtil.createData(r, "org/netbeans/modules/stuff/StuffUtils.java");
        FileUtil.createData(r, "org/netbeans/modules/stuff/resources/stuff.png");
        FileUtil.createData(r, "org/netbeans/spi/stuff/StuffImplementation.java");
        FileUtil.createData(r, "org/netbeans/spi/stuff/support/AbstractStuffImplementation.java");
        SourceGroup g = new SimpleSourceGroup(r);
        return g;
    }

    public void testTreeBasics() throws Exception {
        SourceGroup g = sampleGroup();
        Node n = new TreeRootNode(g, false);
        assertTree("TestGroup{org{netbeans{api{stuff{Stuff.java}}, modules{stuff{resources{stuff.png}, Bundle.properties, StuffUtils.java}}, spi{stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}}}", n);
        // XXX test PathFinder
    }

    @RandomlyFails // NB-Core-Build #7966
    public void testReducedTreeBasics() throws Exception { // #53192
        SourceGroup g = sampleGroup();
        Node n = new TreeRootNode(g, true);
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modules.stuff{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", n);
        FileObject r = g.getRootFolder();
        FileUtil.createData(r, "README.txt");
        DataFolder.findFolder(r).children(); // force refresh
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modules.stuff{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}, README.txt}", n);
        r.getFileObject("org/netbeans/modules/stuff").delete();
        DataFolder.findFolder(r.getFileObject("org/netbeans/modules")).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modules{}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}, README.txt}", n);
        r.getFileObject("org/netbeans/modules").delete();
        DataFolder.findFolder(r.getFileObject("org/netbeans")).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}, README.txt}", n);
        FileUtil.createData(r, "org/netbeans/spi/Oops.java");
        DataFolder.findFolder(r.getFileObject("org/netbeans/spi")).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi{stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}, Oops.java}}, README.txt}", n);
        r.getFileObject("org/netbeans/spi/Oops.java").delete();
        DataFolder.findFolder(r.getFileObject("org/netbeans/spi")).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}, README.txt}", n);
        r.getFileObject("README.txt").delete();
        DataFolder.findFolder(r).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", n);
        r.getFileObject("org/netbeans/spi/stuff/StuffImplementation.java").delete();
        DataFolder.findFolder(r.getFileObject("org/netbeans/spi/stuff")).children();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi.stuff.support{AbstractStuffImplementation.java}}}", n);
    }

    @RandomlyFails // NB-Core-Build #7974
    public void testReducedTreeRename() throws Exception {
        final AtomicReference<Node> node = new AtomicReference<Node>();
        final AtomicReference<DataFolder> folder = new AtomicReference<DataFolder>();
        final AtomicReference<String> newName = new AtomicReference<String>();
        final AtomicReference<Object> message = new AtomicReference<Object>();
        SourceGroup g = sampleGroup();
        final FileObject rootFolder = g.getRootFolder();
        Node r = new TreeRootNode(g, true);
        // First check basic behavior with no refactoring support:
        Node n = NodeOp.findPath(r, new String[] {"org.netbeans", "modules.stuff"});
        n.setName("modules.stuph");
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modules.stuph{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", r);
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "modules.stuph"});
        n.setName("modulez.stuph");
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modulez.stuph{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", r);
        // Now test #210107 monstrosity:
        MockLookup.setInstances(new PackageRenameHandler() {
            @Override public void handleRename(Node _node, String _newName) {
                node.set(_node);
                newName.set(_newName);
            }
        }, new FolderRenameHandler() {
            @Override public void handleRename(DataFolder _folder, String _newName) throws IllegalArgumentException {
                folder.set(_folder);
                newName.set(_newName);
            }
        }, new DialogDisplayer() {
            @Override public Object notify(NotifyDescriptor descriptor) {
                message.set(descriptor.getMessage());
                return NotifyDescriptor.OK_OPTION;
            }
            @Override public Dialog createDialog(DialogDescriptor descriptor) {
                throw new UnsupportedOperationException();
            }
        });
        // Case 1: only last component is renamed. Can use FolderRenameHandler.
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "modulez.stuph"});
        n.setName("modulez.stuff");
        assertNull(node.get());
        assertEquals(DataFolder.findFolder(rootFolder.getFileObject("org/netbeans/modulez/stuph")), folder.get());
        assertEquals("stuff", newName.get());
        assertNull(message.get());
        folder.set(null);
        newName.set(null);
        // Case 2: no subpackages. Can use PackageRenameHandler with a NonRecursiveFolder.
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "api.stuff"});
        n.setName("stuff.api");
        Node phony = node.get();
        assertNotNull(phony);
        assertNotSame(n, phony);
        assertEquals("org.netbeans.api.stuff", phony.getName());
        NonRecursiveFolder nrf = phony.getLookup().lookup(NonRecursiveFolder.class);
        assertNotNull(nrf);
        assertEquals(rootFolder.getFileObject("org/netbeans/api/stuff"), nrf.getFolder());
        assertNull(folder.get());
        assertEquals("org.netbeans.stuff.api", newName.get());
        assertNull(message.get());
        node.set(null);
        newName.set(null);
        // Case 3: multicomponent rename attempted and there are subpackages.
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "modulez.stuph"});
        n.setName("modules.stuff");
        assertNull(node.get());
        assertNull(folder.get());
        assertNull(newName.get());
        assertNotNull(message.get());
        message.set(null);
        // Case 1 variant: starting at root folder.
        n = NodeOp.findPath(r, new String[] {"org.netbeans"});
        n.setName("org.netbeanz");
        assertNull(node.get());
        assertEquals(DataFolder.findFolder(rootFolder.getFileObject("org/netbeans")), folder.get());
        assertEquals("netbeanz", newName.get());
        assertNull(message.get());
        folder.set(null);
        newName.set(null);
        // Case 1 variant: single-component folder.
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "modulez.stuph", "resources"});
        n.setName("resourcez");
        assertNull(node.get());
        assertEquals(DataFolder.findFolder(rootFolder.getFileObject("org/netbeans/modulez/stuph/resources")), folder.get());
        assertEquals("resourcez", newName.get());
        assertNull(message.get());
        folder.set(null);
        newName.set(null);
        // Did not actually change anything in all this (refactoring impl bypasses others even if it does not work):
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modulez.stuph{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", r);
    }

    @RandomlyFails // NB-Core-Build #8123
    public void testReducedTreeDelete() throws Exception {
        SourceGroup g = sampleGroup();
        Node r = new TreeRootNode(g, true);
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, modules.stuff{resources{stuff.png}, Bundle.properties, StuffUtils.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", r);
        Node n = NodeOp.findPath(r, new String[] {"org.netbeans", "modules.stuff"});
        n.destroy();
        assertTree("TestGroup{org.netbeans{api.stuff{Stuff.java}, spi.stuff{support{AbstractStuffImplementation.java}, StuffImplementation.java}}}", r);
    }

    public void testReducedTreeCut() throws Exception { // #210314
        SourceGroup g = sampleGroup();
        Node r = new TreeRootNode(g, true);
        Node n = NodeOp.findPath(r, new String[] {"org.netbeans", "modules.stuff"});
        Transferable t = n.clipboardCut();
        DataObject moving = LoaderTransfer.getDataObject(t, LoaderTransfer.MOVE);
        assertEquals(g.getRootFolder().getFileObject("org/netbeans/modules"), moving.getPrimaryFile());
        n = NodeOp.findPath(r, new String[] {"org.netbeans", "spi.stuff", "support"});
        t = n.clipboardCut();
        moving = LoaderTransfer.getDataObject(t, LoaderTransfer.MOVE);
        assertEquals(g.getRootFolder().getFileObject("org/netbeans/spi/stuff/support"), moving.getPrimaryFile());
    }

    public void testReducedTreePathFinder() throws Exception {
        SourceGroup g = sampleGroup();
        final Node r = new TreeRootNode(g, true);
        final FileObject rootFolder = g.getRootFolder();
        final TreeRootNode.PathFinder pf = new TreeRootNode.PathFinder(g, true);
        class A {
            void assertPath(String expected, String resource) {
                FileObject f = rootFolder.getFileObject(resource);
                assertNotNull(resource, f);
                Node n = pf.findPath(r, f);
                if (expected == null) {
                    assertNull(resource, n);
                } else {
                    assertNotNull(resource, n);
                    assertEquals(expected, Arrays.toString(NodeOp.createPath(n, r)));
                }
            }
        }
        new A().assertPath("[org.netbeans, api.stuff, Stuff]", "org/netbeans/api/stuff/Stuff.java");
        new A().assertPath("[org.netbeans, api.stuff]", "org/netbeans/api/stuff");
        new A().assertPath(null, "org/netbeans/api"); // displayed only in Files
        new A().assertPath("[org.netbeans, spi.stuff, StuffImplementation]", "org/netbeans/spi/stuff/StuffImplementation.java");
        new A().assertPath("[org.netbeans, spi.stuff, support]", "org/netbeans/spi/stuff/support");
        new A().assertPath("[org.netbeans, spi.stuff, support, AbstractStuffImplementation]", "org/netbeans/spi/stuff/support/AbstractStuffImplementation.java");
    }

    // XXX test reduced tree copy & paste, drag & drop

}
