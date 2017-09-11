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
