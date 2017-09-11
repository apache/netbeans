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

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author S. Aubrecht
 */
public class ItemTest extends AbstractPaletteTestHid {

    public ItemTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        for( int i=0; i<itemNames.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                Node node = getItemNode( categoryNames[i], itemNames[i][j] );
                assertEquals( node.getName(), items[j].getName() );
            }
        }
    }

    /**
     * Test of getDisplayName method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetDisplayName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        for( int i=0; i<itemNames.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                Node node = getItemNode( categoryNames[i], itemNames[i][j] );
                assertEquals( node.getDisplayName(), items[j].getDisplayName() );
            }
        }
    }

    /**
     * Test of getShortDescription method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetShortDescription() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        for( int i=0; i<itemNames.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                Node node = getItemNode( categoryNames[i], itemNames[i][j] );
                assertEquals( node.getShortDescription(), items[j].getShortDescription() );
            }
        }
    }

    /**
     * Test of getIcon method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetIcon() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        for( int i=0; i<itemNames.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                Node node = getItemNode( categoryNames[i], itemNames[i][j] );
                assertEquals( node.getIcon( BeanInfo.ICON_COLOR_16x16 ), items[i].getIcon( BeanInfo.ICON_COLOR_16x16 ) );
                assertEquals( node.getIcon( BeanInfo.ICON_COLOR_32x32 ), items[i].getIcon( BeanInfo.ICON_COLOR_32x32 ) );
            }
        }
    }

    /**
     * Test of getActions method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetActions() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        for( int i=0; i<categories.length; i++ ) {
            Item[] items = categories[i].getItems();
            for( int m=0; m<items.length; m++ ) {
                Action[] itemActions = items[m].getActions();

                Action[] providedActions = actions.getCustomItemActions( items[m].getLookup() );

                for( int k=0; k<providedActions.length; k++ ) {
                    if( null == providedActions[k] )
                        continue;
                    boolean found = false;
                    for( int j=0; j<itemActions.length; j++ ) {
                        if( null == itemActions[j] )
                            continue;
                        if( itemActions[j].equals( providedActions[k] ) ) {
                            found = true;
                            break;
                        }
                    }
                    assertTrue( "Action " + providedActions[k].getValue( Action.NAME ) + " not found in palette actions.", found );
                }
            }
        }
    }

    /**
     * Test of invokePreferredAction method, of class org.netbeans.modules.palette.Item.
     */
    public void testInvokePreferredAction() throws IOException {
        DummyActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        for( int i=0; i<itemNames.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                MyPreferredAction a = new MyPreferredAction();
                actions.setPreferredAction( a );
                items[j].invokePreferredAction( new ActionEvent( new JPanel(), 0, "junittest") );
                assertEquals( 1, a.getActionInvocations() );
            }
        }
    }
    
    private static class MyPreferredAction extends AbstractAction {
        private int actionInvocations = 0;
        
        public MyPreferredAction() {
            super( "JunitAction" );
        }

        public void actionPerformed(ActionEvent e) {
            actionInvocations++;
        }
        
        public int getActionInvocations() {
            return actionInvocations;
        }
    }

    /**
     * Test of getLookup method, of class org.netbeans.modules.palette.Item.
     */
    public void testGetLookup() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        for( int i=0; i<categories.length; i++ ) {
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                Lookup lkp = items[j].getLookup();
                assertNotNull( lkp );
                Node node = (Node)lkp.lookup( Node.class );
                assertEquals( itemNames[i][j], node.getName() );
            }
        }
    }

    /**
     * Test of drag method, of class org.netbeans.modules.palette.Item.
     */
    public void testDrag() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        for( int i=0; i<categories.length; i++ ) {
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                Transferable t = items[j].drag();
                assertNotNull( t );
                assertTrue( t.isDataFlavorSupported( PaletteController.ITEM_DATA_FLAVOR ) );
                Lookup lookup = (Lookup)t.getTransferData( PaletteController.ITEM_DATA_FLAVOR );
                assertNotNull( lookup );
                Node node = (Node)lookup.lookup( Node.class );
                assertEquals( itemNames[i][j], node.getName() );
            }
        }
    }

    /**
     * Test of cut method, of class org.netbeans.modules.palette.Item.
     */
    public void testCut() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        for( int i=0; i<categories.length; i++ ) {
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                Transferable t = items[j].cut();
                assertNotNull( t );
                assertTrue( t.isDataFlavorSupported( PaletteController.ITEM_DATA_FLAVOR ) );
                Lookup lookup = (Lookup)t.getTransferData( PaletteController.ITEM_DATA_FLAVOR );
                assertNotNull( lookup );
                Node node = (Node)lookup.lookup( Node.class );
                assertEquals( itemNames[i][j], node.getName() );
            }
        }
    }
}
