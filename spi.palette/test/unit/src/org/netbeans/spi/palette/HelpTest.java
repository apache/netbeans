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

import java.beans.BeanInfo;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.ui.PalettePanel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 *
 * @author S. Aubrecht
 */
public class HelpTest extends AbstractPaletteTestHid {

    public HelpTest(String testName) {
        super(testName);
    }

    public void testItemHelp() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject item2 = getItemFile( categoryNames[0], itemNames[0][1] );
        
        item1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        Item[] items = categories[0].getItems();
        
        Node node1 = items[0].getLookup().lookup( Node.class );
        Node node2 = items[1].getLookup().lookup( Node.class );
        
        HelpCtx help1 = node1.getHelpCtx();
        HelpCtx help2 = node2.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help1.getHelpID() );
        assertEquals( "Default help", HelpCtx.DEFAULT_HELP, help2 );
    }

    public void testCategoryHelp() throws Exception {
        FileObject cat1 = getCategoryFile( categoryNames[0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        
        cat1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        Node node1 = categories[0].getLookup().lookup( Node.class );
        Node node2 = categories[1].getLookup().lookup( Node.class );
        
        HelpCtx help1 = node1.getHelpCtx();
        HelpCtx help2 = node2.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help1.getHelpID() );
        assertNull( "Default help", help2 );
    }

    public void testRootHelpCustom() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Node node = model.getRoot().lookup( Node.class );
        
        HelpCtx help = node.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help.getHelpID() );
    }

    public void testRootHelpDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Node node = model.getRoot().lookup( Node.class );
        
        HelpCtx help = node.getHelpCtx();

        assertNull( "Custom help", help );
    }

    public void testPalettePanelCustom() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        
        item1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyItemHelpId" );
        cat2.setAttribute( PaletteController.ATTR_HELP_ID, "DummyCategoryHelpId" );
        paletteRootFolder.setAttribute( PaletteController.ATTR_HELP_ID, "DummyRootHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        PalettePanel panel = PalettePanel.getDefault();
        panel.setContent( pc, model, pc.getSettings() );
        panel.refresh();
        
        HelpCtx help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No category, no item selected", "DummyRootHelpId", help.getHelpID() );

        //UI is not fully initialized at this point
//        pc.setSelectedItem( model.getCategories()[1].getLookup(), model.getCategories()[1].getItems()[0].getLookup() );
//        help = panel.getHelpCtx();
//        assertNotNull( help );
//        assertEquals( "Category selected, selected item has no custom help", "DummyCategoryHelpId", help.getHelpID() );
        
        pc.setSelectedItem( model.getCategories()[0].getLookup(), model.getCategories()[0].getItems()[0].getLookup() );
        help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "Category selected, selected item has custom help", "DummyItemHelpId", help.getHelpID() );
    }

    public void testPalettePanelDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        PalettePanel panel = PalettePanel.getDefault();
        panel.setContent( pc, model, pc.getSettings() );
        
        HelpCtx help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No custom help defined", "CommonPalette", help.getHelpID() );
        
        pc.setSelectedItem( model.getCategories()[0].getLookup(), model.getCategories()[0].getItems()[0].getLookup() );
        help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No custom help defined", "CommonPalette", help.getHelpID() );
    }
}
