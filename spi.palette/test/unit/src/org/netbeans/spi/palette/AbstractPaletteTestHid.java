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

package org.netbeans.spi.palette;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.netbeans.modules.palette.Model;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;


/**
 *
 * @author Stanislav Aubrecht
 */
public abstract class AbstractPaletteTestHid extends NbTestCase {

    protected FileObject paletteRootFolder;
    private static final String PALETTE_ROOT_FOLDER_NAME = "test_palette_folder";
    private String rootFolderName;
    
    protected String[] categoryNames;
    protected String[][] itemNames;
    
    private static DataLoader myDummyLoader;

    public AbstractPaletteTestHid( String name ) {
        super( name );
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.netbeans.spi.palette.AbstractPaletteTestHid$Lkp");
//        assertEquals ("Our lookup is installed", Lookup.getDefault ().getClass (), Lkp.class);
        
//        paletteRootFolder = FileUtil.getConfigFile( PALETTE_ROOT_FOLDER_NAME );
//        if( null != paletteRootFolder )
//            paletteRootFolder.delete();
        rootFolderName = PALETTE_ROOT_FOLDER_NAME+System.currentTimeMillis();
        paletteRootFolder = FileUtil.getConfigRoot().createFolder( rootFolderName );
        
//        NbPreferences.forModule( DefaultSettings.class ).node( "CommonPaletteSettings" ).removeNode();
        
        if( null == myDummyLoader )
            myDummyLoader = new DummyItemLoader();
        
        createDefaultPaletteContentInFolder( paletteRootFolder );
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void tearDown() throws Exception {
//        if( null != paletteRootFolder ) {
//            FileLock lock = null;
//            try {
//                if( paletteRootFolder.isValid() ) {
//                    lock = paletteRootFolder.lock();
//                    paletteRootFolder.delete( lock );
//                }
//            } finally {
//                if( null != lock )
//                    lock.releaseLock();
//            }
//        }
    }
    
    protected void createDefaultPaletteContentInFolder( FileObject rootFolder ) throws IOException {
        categoryNames = new String[10];
        itemNames = new String[categoryNames.length][10];
        for( int i=0; i<categoryNames.length; i++ ) {
            categoryNames[i] = "Category_" + i;
            
            FileObject catFolder = rootFolder.createFolder( categoryNames[i] );
            
            for( int j=0; j<itemNames[i].length; j++ ) {
                itemNames[i][j] = categoryNames[i] + "_Item_" + j;
                
                FileObject itemFile = catFolder.createData( itemNames[i][j], DummyItemLoader.ITEM_EXT );
                DataLoaderPool.setPreferredLoader( itemFile, myDummyLoader );
            }
        }
    }
    
    protected String getRootFolderName() {
        return rootFolderName;
    }
    
    protected FileObject getCategoryFile( String catName ) throws DataObjectNotFoundException {
        FileObject fo = paletteRootFolder.getFileObject( catName );
        if( null == fo ) {
            fail( "Category folder '" + catName + "' not found." );
        }
        return fo;
    }
    
    protected Node getCategoryNode( String catName ) throws DataObjectNotFoundException {
        FileObject fo = getCategoryFile( catName );
        DataObject dobj = DataObject.find( fo );
        if( null == dobj ) {
            fail( "Category data object '" + catName + "' not found." );
        }
        return dobj.getNodeDelegate();
    }
    
    protected FileObject getItemFile( String catName, String itemName ) throws DataObjectNotFoundException {
        FileObject fo = getCategoryFile( catName );
        FileObject itemFO = fo.getFileObject( itemName, DummyItemLoader.ITEM_EXT );
        if( null == itemFO ) {
            fail( "Item file '" + itemName + "' not found." );
        }
        return itemFO;
    }
    
    protected Node getItemNode( String catName, String itemName ) throws DataObjectNotFoundException {
        FileObject fo = getItemFile( catName, itemName );
        DataObject dobj = DataObject.find( fo );
        if( null == dobj ) {
            fail( "Item data object '" + itemName + "' not found." );
        }
        return dobj.getNodeDelegate();
    }

    protected Model getModel(PaletteController paletteController) {
        return paletteController.getModel();
    }

    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () throws Exception {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) throws Exception {
            super (ic);
            
            ic.add (new Repository (createLocalFileSystem (Lkp.class.getName()+System.currentTimeMillis(), new String[0])));
            ic.add (new NbMutexEventProvider());
        }
    }

    public static FileSystem createLocalFileSystem(String name, String[] resources) throws IOException {
        File f = File.createTempFile (name, ".tmp");
        f.delete ();
        f = new File (f.getParent (), name);
        f.mkdirs ();
        return createLocalFileSystem (f, resources);
    }

    public static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException iex) {
                    throw new IOException ("While creating " + resources[i] + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath() + " with resource list: " + Arrays.asList(resources));
                }
            }
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    static class StatusFileSystem extends LocalFileSystem {
        StatusDecorator status = new StatusDecorator () {
            public String annotateName (String name, java.util.Set files) {
                return name;
            }

            @Override
            public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                return null;
            }

        };        
        
        @Override
        public StatusDecorator getDecorator() {
            return status;
        }
        
    }
}
