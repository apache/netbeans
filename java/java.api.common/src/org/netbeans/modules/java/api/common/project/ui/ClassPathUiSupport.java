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

package org.netbeans.modules.java.api.common.project.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem;
import org.netbeans.spi.java.project.support.ui.EditJarSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.NbCollections;

/**
 * Support for visual classpath customizer.
 * @author Petr Hrebejk, Radko Najman, David Konecny
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathUiSupport {
    
    private ClassPathUiSupport() {
    }
    
    // Methods for working with list models ------------------------------------
    
    public static DefaultListModel createListModel( Iterator it ) {
        
        DefaultListModel model = new DefaultListModel();
        
        while( it.hasNext() ) {
            model.addElement( it.next() );
        }
        
        return model;
    }
    
    public static Iterator<ClassPathSupport.Item> getIterator( DefaultListModel model ) {        
        // XXX Better performing impl. would be nice
        return getList( model ).iterator();        
    }
    
    public static List<ClassPathSupport.Item> getList( DefaultListModel model ) {
        return Collections.list(NbCollections.checkedEnumerationByFilter(model.elements(), ClassPathSupport.Item.class, true));
    }
        
    
    public static boolean canEdit( ListSelectionModel selectionModel, DefaultListModel listModel ) {        
        boolean can =  selectionModel.getMinSelectionIndex() == selectionModel.getMaxSelectionIndex() 
                          && selectionModel.getMinSelectionIndex() != -1;
        if (can) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) listModel.get(selectionModel.getMinSelectionIndex());
            can = item != null && item.canEdit();
        }
        return can;
    }
    
    public static void edit(DefaultListModel listModel, int[] selectedIndices, AntProjectHelper helper) {
        ClassPathSupport.Item item = (ClassPathSupport.Item) listModel.getElementAt(selectedIndices[0]);
        if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
            EditJarSupport.Item eji = new EditJarSupport.Item();
            eji.setJarFile(item.getVariableBasedProperty() != null ? item.getVariableBasedProperty() : item.getFilePath());
            eji.setSourceFile(item.getSourceFilePath());
            eji.setJavadocFile(item.getJavadocFilePath());
            eji = EditJarSupport.showEditDialog(helper, eji);
            if (eji != null) {
                item.setJavadocFilePath(eji.getJavadocFile());
                item.setSourceFilePath(eji.getSourceFile());
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
            if (item.getLibrary() != null) {
                LibrariesCustomizer.showSingleLibraryCustomizer(item.getLibrary());
            }
        }
    }
    
    /** Moves items up in the list. The indices array will contain 
     * indices to be selected after the change was done.
     */
    public static int[] moveUp( DefaultListModel listModel, int indices[]) {
                
        if( indices == null || indices.length == 0 ) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = 0; i < indices.length; i++ ) {
            Object item = listModel.get( indices[i] );
            listModel.remove( indices[i] );
            listModel.add( indices[i] - 1, item ); 
            // Keep the selection a before
            indices[i] -= 1;
        }
        
        return indices;
        
    } 
        
    public static boolean canMoveUp( ListSelectionModel selectionModel ) {        
        return selectionModel.getMinSelectionIndex() > 0;
    }
    
    /** Moves items down in the list. The indices array will contain 
     * indices to be selected after the change was done.
     */
    public static int[] moveDown( DefaultListModel listModel, int indices[]) {
        
        if(  indices == null || indices.length == 0 ) {
            assert false : "MoveDown button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = indices.length -1 ; i >= 0 ; i-- ) {
            Object item = listModel.get( indices[i] );
            listModel.remove( indices[i] );
            listModel.add( indices[i] + 1, item );
            // Keep the selection a before
            indices[i] = listModel.indexOf( item );
        }
        
        return indices;

    }    
        
    public static boolean canMoveDown( ListSelectionModel selectionModel, int modelSize ) {
        int iMax = selectionModel.getMaxSelectionIndex();
        return iMax != -1 && iMax < modelSize - 1;         
    }
    
    /** Removes selected indices from the model. Returns the index to be selected 
     */
    public static int[] remove( DefaultListModel listModel, int[] indices ) {
        
        if(  indices == null || indices.length == 0 ) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        // Remove the items
        for( int i = indices.length - 1 ; i >= 0 ; i-- ) {
            listModel.remove( indices[i] );
        }
                
        if ( !listModel.isEmpty() ) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1; 
            if ( selectedIndex > listModel.size() - 1) {
                selectedIndex = listModel.size() - 1;
            }
            while (selectedIndex >= 0) {
                if (listModel.get(selectedIndex) != null) {
                    return new int[] { selectedIndex };
                }
                selectedIndex--;
            }
        }

        return new int[] {};
    }
    
    public static int[] addLibraries( DefaultListModel listModel, int[] indices, Library[] libraries, 
            Set<Library> alreadyIncludedLibs, Callback callback) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        for (int i = 0, j=1; i < libraries.length; i++) {
            if (!alreadyIncludedLibs.contains(libraries[i])) {
                ClassPathSupport.Item item = ClassPathSupport.Item.create( libraries[i], null);
                if (callback != null) {
                    callback.initItem(item);
                }
                listModel.add( lastIndex + j++, item);
            }
        }
        Set<Library> addedLibs = new HashSet<Library>(Arrays.asList(libraries));
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i=0; i<listModel.getSize(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get(i);
            if (item != null && item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && !item.isBroken()) {
                if (addedLibs.contains(item.getLibrary())) {
                    indexes.add(i);
                }
            }
        }
        int[] indexesArray = new int[indexes.size()];
        int j = 0;
        for (Integer val : indexes) {
            indexesArray[j++] = val.intValue();
        }
        return indexesArray;
    }

    public static int[] addJarFiles( DefaultListModel listModel, int[] indices, String filePaths[], File base, 
            String[] variables, Callback callback) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        int[] indexes = new int[filePaths.length];
        for( int i = 0, delta = 0; i+delta < filePaths.length; ) {            
            int current = lastIndex + 1 + i;
            ClassPathSupport.Item item = ClassPathSupport.Item.create( filePaths[i], base, null, variables != null ? variables[i] : null);
            if (callback != null) {
                callback.initItem(item);
            }
            if ( !listModel.contains( item ) ) {
                listModel.add( current, item );
                indexes[delta + i] = listModel.indexOf( item );
                i++;
            }
            else {
                indexes[i + delta] = listModel.indexOf( item );
                delta++;
            }            
        }
        return indexes;

    }
    
    public static int[] addArtifacts( DefaultListModel listModel, int[] indices, AntArtifactItem artifactItems[],
            Callback callback) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        int[] indexes = new int[artifactItems.length];
        for( int i = 0; i < artifactItems.length; i++ ) {
            int current = lastIndex + 1 + i;
            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI(), null) ;
            if (callback != null) {
                callback.initItem(item);
            }
            if ( !listModel.contains( item ) ) {
                listModel.add( current, item );
            }            
            indexes[i] = listModel.indexOf( item );
        }
        return indexes;
    }
   
    /**
     * Optional callback to perform initialization of item.
     */
    public static interface Callback {

        /** Initialize additional data associated with given Item.*/
        void initItem(ClassPathSupport.Item item);

    }
}
