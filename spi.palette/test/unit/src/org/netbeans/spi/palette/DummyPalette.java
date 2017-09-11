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
