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

package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;


/** Maintains current category in the customizer.
 *
 * @author Petr Hrebejk
 */
public class CategoryModel {

    public static final String PROP_CURRENT_CATEGORY = "propCurrentCategory";

    private ProjectCustomizer.Category[] categories;

    private ProjectCustomizer.Category currentCategory;

    // Might be vetoable later
    private PropertyChangeSupport pcs;
    
    public CategoryModel( ProjectCustomizer.Category[] categories ) {
        
        if ( categories == null || categories.length == 0 ) {
            throw new IllegalArgumentException( "Must provide at least one category" ); // NOI18N
        }
        
        this.categories = categories;
        this.currentCategory = categories[0];
        this.pcs = new PropertyChangeSupport( this );
    }
    
    
    public ProjectCustomizer.Category getCurrentCategory() {
        return this.currentCategory;
    }
    
    public ProjectCustomizer.Category getCategory( String name ) {
        return findCategoryByName( name, categories );
    }
    
    public void setCurrentCategory( ProjectCustomizer.Category category ) {
        
        if ( currentCategory != category ) {
            ProjectCustomizer.Category oldValue = currentCategory;
            this.currentCategory = category;
            firePropertyChange( PROP_CURRENT_CATEGORY, oldValue, category );
        }
        
    }
    
    public ProjectCustomizer.Category[] getCategories() {
        return this.categories;
    }
    
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        pcs.addPropertyChangeListener( propertyName, l );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        pcs.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        pcs.removePropertyChangeListener( propertyName, l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        pcs.removePropertyChangeListener( l );
    }
    
    public void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
        pcs.firePropertyChange( propertyName, oldValue, newValue );
    }
    
    // Private methods ---------------------------------------------------------
    
    private static ProjectCustomizer.Category findCategoryByName( String name, ProjectCustomizer.Category[] categories ) {
        
        for( int i = 0; i < categories.length; i++ ) {
            if ( name.equals( categories[i].getName() ) ) {
                return categories[i];
            }
            
            ProjectCustomizer.Category[] subcategories = categories[i].getSubcategories();
            if ( subcategories != null ) {
                ProjectCustomizer.Category category = findCategoryByName( name, subcategories );
                if ( category != null ) {
                    return category;
                }
            }
            
        }
        
        return null;
    }
    
}
