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

package org.netbeans.spi.palette;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.modules.palette.DefaultModel;
import org.netbeans.modules.palette.DefaultSettings;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.RootNode;
import org.netbeans.modules.palette.Settings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;


/**
 * <p>PaletteFactory creating new PaletteController instances.</p>
 *
 * @author S. Aubrecht
 */
public final class PaletteFactory {
    
    /** 
     * Do not allow instances of this class.
     */
    private PaletteFactory() {
    }

    /**
     * Create a new palette from the given folder.
     *
     * @param rootFolderName Name of palette's root folder, its sub-folders are categories.
     * Cannot be null.
     * @param customActions Import actions for palette customizer.
     *
     * @throws IOException If the given folder cannot be found.
     */
    public static PaletteController createPalette( String rootFolderName, PaletteActions customActions ) 
            throws IOException {
        return createPalette( rootFolderName, customActions, null, DragAndDropHandler.getDefault() );
    }
    
    
    /**
     * Create a new palette from the given folder.
     *
     * @param rootFolderName Name of palette's root folder, its sub-folders are categories. 
     * Cannot be null.
     * @param customActions Import actions for palette customizer.
     * @param filter A filter that can dynamically hide some categories and items.
     * @param dndHandler Handle drop of new items into palette window and add 
     * custom DataFlavors to the Transferable of items being dragged from
     * the palette to editor window. Can be null.
     *
     * @throws IOException If the given folder cannot be found.
     */
    public static PaletteController createPalette( String rootFolderName, 
                                                   PaletteActions customActions,
                                                   PaletteFilter filter,
                                                   DragAndDropHandler dndHandler ) 
            throws IOException {
        
        if( null == rootFolderName ) {
            throw new IllegalArgumentException( "Folder name cannot be null." );
        }
        
        DataFolder paletteFolder = DataFolder.findFolder( getPaletteFolder( rootFolderName ) );
        return createPalette( paletteFolder.getNodeDelegate(), customActions, filter, dndHandler );
    }
    
    
    /**
     * Create a new palette from the given root Node.
     *
     * @param paletteRoot Palette's root <code>Node</code>, its children are categories, 
     * their children are palette items.
     * @param customActions Import actions for palette customizer.
     */
    public static PaletteController createPalette( Node paletteRoot, PaletteActions customActions ) {
        return createPalette( paletteRoot, customActions, null, DragAndDropHandler.getDefault() );
    }
    
    /**
     * Create a new palette from the given root Node.
     *
     * @param paletteRoot Palette's root <code>Node</code>, its children are categories, 
     * their children are palette items. Cannot be null.
     * @param customActions Import actions for palette customizer. Cannot be null.
     * @param filter A filter that can dynamically hide some categories and items. Can be null.
     * @param dndHandler Handle drop of new items into palette window and add 
     * custom DataFlavors to the Transferable of items being dragged from
     * the palette to editor window. Can be null.
     */
    public static PaletteController createPalette( Node paletteRoot, 
                                                   PaletteActions customActions,
                                                   PaletteFilter filter,
                                                   DragAndDropHandler dndHandler ) {
        
        if( null == paletteRoot ) {
            throw new IllegalArgumentException( "Palette root Node cannot be null." );
        }
        if( null == customActions ) {
            throw new IllegalArgumentException( "Palette custom actions must be provided." );
        }
        
        ArrayList<Object> lookupObjects = new ArrayList<Object>(3);
        lookupObjects.add( customActions );
        if( null != filter )
            lookupObjects.add( filter );
        
        if( null == dndHandler )
            dndHandler = DragAndDropHandler.getDefault();
        lookupObjects.add( dndHandler );
        

        RootNode root = new RootNode( paletteRoot, Lookups.fixed( lookupObjects.toArray() ) );
        Model model = createModel( root );
        Settings settings = new DefaultSettings( model );
        
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                PaletteSwitch.getDefault().startListening();
            }
        });
        
        return new PaletteController( model, settings );
    }
    
    private static Model createModel( RootNode root ) {
        return new DefaultModel( root );
    }
    
    private static FileObject getPaletteFolder( String folderName ) throws IOException {
        FileObject paletteFolder;
        paletteFolder = FileUtil.getConfigFile( folderName );
        if (paletteFolder == null) { // not found, cannot continue
            throw new FileNotFoundException( folderName );
        }
        return paletteFolder;
    }
}
