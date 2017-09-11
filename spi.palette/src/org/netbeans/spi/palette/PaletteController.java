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

import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.netbeans.modules.palette.Settings;
import org.openide.util.Lookup;

/**
 * <p>PaletteController provides access to data in the palette. If an instance
 * of this class is in the <code>Lookup</code> of any <code>TopComponent</code> 
 * then the palette window opens and displays a new content when that 
 * <code>TopComponent</code> opens/activates. 
 * <br>
 * Use <code>PaletteFactory</code> to construct a new instance of this class.</p>
 * 
 * <p>There's a number of attributes that can override the default palette behavior.
 * If the palette data are defined in the layers then the palette looks for attributes
 * of the folders and files (<code>FileObject.getAttribute</code>). If the palette
 * data are defined as Nodes then the attributes are extracted using <code>Node.getValue</code>.</p>
 * 
 * <p>User can override attribute values in palette's user interface. Attribute values
 * are persisted and restored after IDE restarts.</p>
 *
 * @author S. Aubrecht
 */

public final class PaletteController {

    /**
     * <code>DataFlavor</code> of palette items dragged from palette to editor. 
     * The trasfer data returned from <code>Transferable</code> for this 
     * <code>DataFlavor</code> is the <code>Lookup</code> of the <code>Node</code>
     * representing the palette item being dragged.
     */
    public static final DataFlavor ITEM_DATA_FLAVOR;
    static {
        try {
            ITEM_DATA_FLAVOR = new DataFlavor("application/x-java-openide-paletteitem;class=org.openide.util.Lookup", // NOI18N
                    "Paste Item", // XXX missing I18N!
                    Lookup.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    // Names of attributes of palette items and categories that the palette supports.
    // User can override their values in palette's user interface. Attribute values
    // are persisted and restored after IDE restarts.
    //
    // Palette clients can override these attributes in their palette model 
    // (folder and file attributes in layers or Node attributes) to change
    // palette's initial state.
    
    /**
     * The width of palette items in pixels, if this attribute is set to -1 or is
     * missing then the item width will be calculated dynamically depending on the length of 
     * item display names and may differ for each category (therefore each category
     * may have a different number of columns).
     * This attribute applies to palette's root only and is read-only.
     * Default value is "-1".
     */
    public static final String ATTR_ITEM_WIDTH = "itemWidth";
    /**
     * "true" to show item names in the palette panel, "false" to show item icons only.
     * This attribute applies to palette's root only.
     * Default value is "true".
     */
    public static final String ATTR_SHOW_ITEM_NAMES = "showItemNames";
    /**
     * Item icon size. 
     * This attribute applies to palette's root only.
     * Default value is java.beans.BeanInfo.ICON_COLOR_16x16
     * @see java.beans.BeanInfo
     */
    public static final String ATTR_ICON_SIZE = "iconSize";
    /**
     * "true" if palette category is expanded, "false" if category is collapsed.
     * Default value is "false".
     */
    public static final String ATTR_IS_EXPANDED = "isExpanded";
    /**
     * "true" if palette category or item is visible in the palette, "false" if 
     * the category or item is visible in palette customizer only.
     * Default value is "true".
     */
    public static final String ATTR_IS_VISIBLE = "isVisible";
    /**
     * "true" if the category or item cannot be removed, "false" otherwise.
     * Users cannot override this attribute's value.
     * Default value is "false".
     */
    public static final String ATTR_IS_READONLY = "isReadonly";
    /**
     * Use this attribut for palette's root, category or item to specify its
     * help id.
     * Default value is "CommonPalette".
     */
    public static final String ATTR_HELP_ID = "helpId";
    /**
     * Boolean attribute to be set on palette's root node or palette's root folder
     * in XML layer to make the palette visible by default. When set to "false"
     * then the palette window won't be visible when a document the palette is associated
     * with is activated and user must open palette window manually for the first time.
     * The default value is "true"
     * @since 1.20
     */
    public static final String ATTR_PALETTE_DEFAULT_VISIBILITY = "paletteDefaultVisible"; //NOI18N

    /**
     * Palette clients should listen to changes of this property if they want to
     * notified when the selected item in palette has changed.
     */
    public static final String PROP_SELECTED_ITEM = Model.PROP_SELECTED_ITEM;
    
    /**
     * Palette data model.
     */
    private Model model;
    /**
     * Palette user settings (expanded/collapsed categories, hidden/visible items etc).
     */
    private Settings settings;
    
    private PropertyChangeSupport support ;
    
    //:::::::::::::::::::::::::::::::::::::
    
    private PaletteController() {
    }
    
    /** Create new instance of PaletteController */
    PaletteController( Model model, Settings settings ) {
        this.model = model;
        this.settings = settings;

        support = new PropertyChangeSupport( this );
        this.model.addModelListener( new ModelListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if( Model.PROP_SELECTED_ITEM.equals( evt.getPropertyName() ) ) {
                    Lookup oldValue = null == evt.getOldValue() ? Lookup.EMPTY : ((Item)evt.getOldValue()).getLookup();
                    Lookup newValue = null == evt.getNewValue() ? Lookup.EMPTY : ((Item)evt.getNewValue()).getLookup();
                    support.firePropertyChange( PROP_SELECTED_ITEM, oldValue, newValue );
                }
            }

            public void categoriesRemoved(Category[] removedCategories) {
                //not interested
            }

            public void categoriesAdded(Category[] addedCategories) {
                //not interested
            }

            public void categoriesReordered() {
                //not interested
            }
        });
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        support.addPropertyChangeListener( listener );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        support.removePropertyChangeListener( listener );
    }
    
    /**
     * @return Lookup representing the item currently selected in the palette. 
     * The lookup is empty if no item is currently selected.
     */
    public Lookup getSelectedItem() {
        Item selItem = model.getSelectedItem();
        return null == selItem ? Lookup.EMPTY : selItem.getLookup();
    }
    
    /**
     * Select a new item in the palette window.
     *
     * @param category Lookup of the category that contains the item to be selected.
     * @param item Item's lookup.
     */
    public void setSelectedItem( Lookup category, Lookup item ) {
        model.setSelectedItem( category, item );
    }
    
    /**
     * @return Lookup representing the category of currently selected item. 
     * The lookup is empty if no item is currently selected.
     */
    public Lookup getSelectedCategory() {
        Category selCategory = model.getSelectedCategory();
        return null == selCategory ? Lookup.EMPTY : selCategory.getLookup();
    }
    
    /**
     * Clear selection in palette (i.e. no item is selected)
     */
    public void clearSelection() {
        model.clearSelection();
    }
    
    /**
     * Refresh the list of categories and items, e.g. when PaletteFilter conditions
     * have changed.
     */
    public void refresh() {
        model.refresh();
    }
    
    /**
     * Open the default Palette Manager window to allow user to customize palette's
     * contents, especially add/import new items to palette.
     */
    public void showCustomizer() {
        model.showCustomizer( this, settings );
    }
    
    /**
     * @return Lookup representing palette's root folder.
     */
    public Lookup getRoot() {
        return model.getRoot();
    }

    Model getModel() {
        return model;
    }
    
    /**
     * For unit-testing only.
     */
    void setModel( Model model ) {
        this.model = model;
    }

    Settings getSettings() {
        return settings;
    }
}
