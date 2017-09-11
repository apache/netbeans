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

package org.netbeans.modules.palette;

import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Palette settings to be remembered over IDE restarts.
 * There's an instance of these settings for each palette model instance.
 *
 * @author S. Aubrecht
 */
public final class DefaultSettings implements Settings, ModelListener, CategoryListener {

    private static final RequestProcessor RP = new RequestProcessor( "PaletteSettings", 1 ); //NOI18N
    
    private static final String NODE_ATTR_PREFIX = "psa_";
    
    private static final String NULL_VALUE = "null";
    
    private static final String[] KNOWN_PROPERTIES = new String[] {
        NODE_ATTR_PREFIX + PaletteController.ATTR_ICON_SIZE,
        NODE_ATTR_PREFIX + PaletteController.ATTR_IS_EXPANDED,
        NODE_ATTR_PREFIX + PaletteController.ATTR_IS_VISIBLE,
        NODE_ATTR_PREFIX + PaletteController.ATTR_SHOW_ITEM_NAMES
    };
    
    private Model model;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private String prefsName;
    
    private static final Logger ERR = Logger.getLogger("org.netbeans.modules.palette"); // NOI18N
    
    public DefaultSettings( Model model ) {
        this.model = model;
        prefsName = constructPrefsFileName( model );
        if( Utilities.isWindows() )
            prefsName = prefsName.toLowerCase();
        model.addModelListener( this );
        Category[] categories = model.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            categories[i].addCategoryListener( this );
        }
        load();
    }
    
    private String constructPrefsFileName( Model model ) {
        DataFolder dof = (DataFolder)model.getRoot().lookup( DataFolder.class );
        if( null != dof ) {
            FileObject fo = dof.getPrimaryFile();
            if( null != fo ) {
                return fo.getPath();
            }
        }
        return model.getName();
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( DefaultSettings.class ).node( "CommonPaletteSettings" ).node( prefsName ); //NOI18N
    }

    @Override
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    @Override
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }

    @Override
    public boolean isVisible(Item item) {
        return _isVisible( item );
    }

    private static boolean _isVisible(Item item) {
        Node node = getNode( item.getLookup() );
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }

    @Override
    public void setVisible(Item item, boolean visible ) {
        Node node = getNode( item.getLookup() );
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }

    @Override
    public boolean isVisible( Category category ) {
        return _isVisible( category );
    }
    
    private static boolean _isVisible( Category category ) {
        Node node = getNode( category.getLookup() );
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }

    @Override
    public void setVisible( Category category, boolean visible ) {
        Node node = getNode( category.getLookup() );
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }
    
    public boolean isNodeVisible( Node node ) {
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }
    
    public void setNodeVisible( Node node, boolean visible ) {
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }

    @Override
    public boolean isExpanded( Category category ) {
        return _isExpanded( category );
    }
    
    private static boolean _isExpanded( Category category ) {
        Node node = getNode( category.getLookup() );
        return get( node, PaletteController.ATTR_IS_EXPANDED, false );
    }

    @Override
    public void setExpanded( Category category, boolean expanded ) {
        Node node = getNode( category.getLookup() );
        set( node, PaletteController.ATTR_IS_EXPANDED, expanded, false );
    }

    @Override
    public int getIconSize() {
        return _getIconSize( model );
    }

    private static int _getIconSize( Model model ) {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_ICON_SIZE, BeanInfo.ICON_COLOR_16x16 );
    }

    @Override
    public void setIconSize( int iconSize ) {
        Node node = getNode( model.getRoot() );
        set( node, PaletteController.ATTR_ICON_SIZE, iconSize, BeanInfo.ICON_COLOR_16x16 );
    }

    @Override
    public void setShowItemNames( boolean showNames ) {
        Node node = getNode( model.getRoot() );
        set( node, PaletteController.ATTR_SHOW_ITEM_NAMES, showNames, true );
    }

    @Override
    public boolean getShowItemNames() {
        return _getShowItemNames( model );
    }

    private static boolean _getShowItemNames( Model model ) {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_SHOW_ITEM_NAMES, true );
    }
    
    private static Node getNode( Lookup lkp ) {
        return (Node)lkp.lookup( Node.class );
    }
    
    private static boolean get( Node node, String attrName, boolean defaultValue ) {
        Object value = get( node, attrName, Boolean.valueOf( defaultValue ) );
        return null == value ? defaultValue : Boolean.valueOf( value.toString() ).booleanValue();
    }

    private static int get( Node node, String attrName, int defaultValue ) {
        Object value = get( node, attrName, Integer.valueOf( defaultValue ) );
        try {
            if( null != value )
                return Integer.parseInt( value.toString() );
        } catch( NumberFormatException nfE ) {
            //ignore
        }
        return defaultValue;
    }
    
    private static Object get( Node node, String attrName, Object defaultValue ) {
        Object res = null;
        if( null != node ) {
            res = node.getValue( NODE_ATTR_PREFIX+attrName );
            if( null == res || NULL_VALUE.equals( res ) ) {
                res = getNodeDefaultValue( node, attrName );
            }
        }
        if( null == res ) {
            res = defaultValue;
        }
        return res;
    }
    
    private static Object getNodeDefaultValue( Node node, String attrName ) {
        Object res = node.getValue( attrName );
        if( null == res ) {
            DataObject dobj = (DataObject)node.getCookie( DataObject.class );
            if( null != dobj ) {
                res = dobj.getPrimaryFile().getAttribute( attrName );
            }
        }
        return res;
    }
    
    private void set( Node node, String attrName, boolean newValue, boolean defaultValue ) {
        set( node, attrName, Boolean.valueOf( newValue ), Boolean.valueOf( defaultValue ) );
    }
    
    private void set( Node node, String attrName, int newValue, int defaultValue ) {
        set( node, attrName, Integer.valueOf( newValue ), Integer.valueOf( defaultValue ) );
    }
    
    private void set( Node node, String attrName, Object newValue, Object defaultValue ) {
        if( null == node )
            return;
        Object oldValue = get( node, attrName, defaultValue );
        if( oldValue.equals( newValue ) ) {
            return;
        }
        node.setValue( NODE_ATTR_PREFIX+attrName, newValue );
        store();
        propertySupport.firePropertyChange( attrName, oldValue, newValue );
    }

    @Override
    public void categoryModified( Category src ) {
        store();
    }
    
    @Override
    public void categoriesRemoved( Category[] removedCategories ) {
        for( int i=0; i<removedCategories.length; i++ ) {
            removedCategories[i].removeCategoryListener( this );
        }
        store();
    }

    @Override
    public void categoriesAdded( Category[] addedCategories ) {
        for( int i=0; i<addedCategories.length; i++ ) {
            addedCategories[i].addCategoryListener( this );
        }
        store();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        //not interested
    }

    @Override
    public void categoriesReordered() {
        //not interested
    }

    private boolean isLoading = false;
    private void load() {
        try {
            isLoading = true;
            Preferences pref = getPreferences();
            setIconSize( pref.getInt( PaletteController.ATTR_ICON_SIZE, getIconSize() ) );
            setShowItemNames( pref.getBoolean( PaletteController.ATTR_SHOW_ITEM_NAMES, getShowItemNames() ) );

            for( Category category : model.getCategories() ) {
                setVisible( category, pref.getBoolean( category.getName()+'-'+PaletteController.ATTR_IS_VISIBLE, isVisible( category ) ) );
                setExpanded( category, pref.getBoolean( category.getName()+'-'+PaletteController.ATTR_IS_EXPANDED, isExpanded( category ) ) );

                for( Item item : category.getItems() ) {
                    setVisible( item, pref.getBoolean( category.getName()+'-'+item.getName()+'-'+PaletteController.ATTR_IS_VISIBLE, isVisible( item ) ) );
                }
            }
        } finally {
            isLoading = false;
        }
    }
    
    private void store() {
        if( isLoading )
            return;
        Preferences pref = getPreferences();
        
        _store( pref, model );
    }

    private static void _store( final Preferences pref, final Model model ) {
        RP.post( new Runnable() {

            @Override
            public void run() {
                try {
                    pref.clear();
                } catch( BackingStoreException bsE ) {
                    ERR.log( Level.INFO, Utils.getBundleString("Err_StoreSettings"), bsE ); //NOI18N
                }
                pref.putInt( PaletteController.ATTR_ICON_SIZE, _getIconSize(model) );
                pref.putBoolean( PaletteController.ATTR_SHOW_ITEM_NAMES, _getShowItemNames(model) );

                for( Category category : model.getCategories() ) {
                    pref.putBoolean( category.getName()+'-'+PaletteController.ATTR_IS_VISIBLE, _isVisible( category ) );
                    pref.putBoolean( category.getName()+'-'+PaletteController.ATTR_IS_EXPANDED, _isExpanded( category ) );

                    for( Item item : category.getItems() ) {
                        pref.putBoolean( category.getName()+'-'+item.getName()+'-'+PaletteController.ATTR_IS_VISIBLE, _isVisible( item ) );
                    }
                }
            }
        });
    }

    @Override
    public int getItemWidth() {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_ITEM_WIDTH, -1 );
    }

    @Override
    public void reset() {
        Node root = (Node)model.getRoot().lookup( Node.class );
        clearAttributes( root );
        Category[] categories = model.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            Node cat = (Node)categories[i].getLookup().lookup( Node.class );
            clearAttributes( cat );
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                Node it = (Node)items[j].getLookup().lookup( Node.class );
                clearAttributes( it );
            }
        }
        try {
            getPreferences().removeNode();
        } catch( BackingStoreException bsE ) {
            ERR.log( Level.INFO, Utils.getBundleString("Err_StoreSettings"), bsE ); //NOI18N
        }
    }

    private void clearAttributes( Node node ) {
        for( int i=0; i<KNOWN_PROPERTIES.length; i++ ) {
            node.setValue( KNOWN_PROPERTIES[i], NULL_VALUE );
        }
    }
}
