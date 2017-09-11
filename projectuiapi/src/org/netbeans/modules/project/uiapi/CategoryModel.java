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
