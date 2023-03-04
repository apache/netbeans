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

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
 *
 * @author Stanislav Aubrecht
 */
class DummyPalette {

    private static final int CATEGORY_COUNT = 9;
    private static final int ITEM_COUNT = 9;

    /** Creates a new instance of DummyPalette */
    private DummyPalette() {
    }

    public static Node createPaletteRoot() {
        Children categories = new Children.Array();
        categories.add( createCategories() );
        return new RootNode( categories );
    }
    
    private static Node[] createCategories() {
        Node[] categories = new Node[ CATEGORY_COUNT ];
        
        for( int i=0; i<categories.length; i++ ) {
            Children items = new Children.Array();
            items.add( createItems() );
            categories[i] = new CategoryNode( items, i );
        }
        return categories;
    }
    
    private static Node[] createItems() {
        Node[] items = new Node[ ITEM_COUNT ];
        
        for( int i=0; i<items.length; i++ ) {
            items[i] = new ItemNode( i );
        }
        return items;
    }

    private static class RootNode extends AbstractNode {
        public RootNode( Children children ) {
            super( children );
            setName( "DummyPalette" );
        }
    }
    
    private static class CategoryNode extends AbstractNode {
        public CategoryNode( Children children, int index ) {
            super( children );
            setName( "Category_" + index );
            setDisplayName( "CategoryName_" + index );
            setShortDescription( "Short category description " + index );
        }

        public Image getIcon(int type) {

            Image icon = null;
            try {
                URL url = new URL("nbres:/javax/swing/beaninfo/images/JTabbedPaneColor16.gif");
                icon = java.awt.Toolkit.getDefaultToolkit().getImage(url);
            } catch( MalformedURLException murlE ) {
            }
            return icon;
        }
    }
    
    private static class ItemNode extends AbstractNode {
        
        public ItemNode( int index ) {
            super( Children.LEAF );
            setName( "Item_" + index );
            setDisplayName( "ItemName_" + index );
            setShortDescription( "Short item description " + index );
        }

        public Image getIcon(int type) {

            Image icon = null;
            try {
                URL url = new URL("nbres:/javax/swing/beaninfo/images/JTabbedPaneColor16.gif");
                icon = java.awt.Toolkit.getDefaultToolkit().getImage(url);
            } catch( MalformedURLException murlE ) {
            }
            return icon;
        }
    }
}
