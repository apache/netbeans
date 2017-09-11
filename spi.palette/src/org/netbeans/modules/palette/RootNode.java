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

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFilter;

import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * The root node representing the Component Palette content.
 *
 * @author S. Aubrecht
 */
public final class RootNode extends FilterNode {
    
    static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private Action[] actions;
    

    // --------

    public RootNode( Node originalRoot, Lookup lkp ) {
        this( originalRoot, new InstanceContent(), lkp );
    }

    private RootNode( Node originalRoot, InstanceContent content, Lookup lkp ) {
        super( originalRoot, 
                new Children( originalRoot, lkp ),
                new ProxyLookup( new Lookup[] { lkp, new AbstractLookup( content ), originalRoot.getLookup() } ) );
        DataFolder df = (DataFolder)getOriginal().getCookie( DataFolder.class );
        if( null != df ) {
            content.add( new DataFolder.Index( df, this ) );
        }
        content.add( this );
        setDisplayName(Utils.getBundleString("CTL_Component_palette")); // NOI18N
    }
    
    // --------

    @Override
    public NewType[] getNewTypes() {
        NewType[] res = super.getNewTypes();
        if( null == res || res.length == 0 ) {
            DataFolder paletteFolder = (DataFolder)getCookie( DataFolder.class );
            if( null != paletteFolder )
                res = new NewType[] { new NewCategory() };
        }
        return res;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) {
            List<Action> actionList = new ArrayList<Action>(5);
            Action a = new Utils.NewCategoryAction( this );
            if( a.isEnabled() ) {
                actionList.add( a );
                actionList.add( null );
            }
            actionList.add( new Utils.SortCategoriesAction( this ) );
            actionList.add( null );
            actionList.add( new Utils.RefreshPaletteAction() );
            actions = actionList.toArray( new Action[actionList.size()] );
        }
        PaletteActions customActions = (PaletteActions)getLookup().lookup( PaletteActions.class );
        if( null != customActions ) {
            return Utils.mergeActions( actions, customActions.getCustomPaletteActions() );
        }
        return actions;
    }

    @Override
    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        //no drop is accepted in palette's root node
        return null;
    }


    public void refreshChildren() {
        ((Children)getChildren()).refreshNodes();
    }

    // ------------

    void createNewCategory() throws java.io.IOException {
        java.util.ResourceBundle bundle = Utils.getBundle();
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
            bundle.getString("CTL_NewCategoryName"), // NOI18N
            bundle.getString("CTL_NewCategoryTitle")); // NOI18N
        input.setInputText(bundle.getString("CTL_NewCategoryValue")); // NOI18N

        while (DialogDisplayer.getDefault().notify(input)
                                              == NotifyDescriptor.OK_OPTION)
        {
            String categoryName = input.getInputText();
            if( CategoryNode.checkCategoryName( this, categoryName, null ) ) {
                DataFolder paletteFolder = (DataFolder)getCookie( DataFolder.class );
                FileObject parentFolder = paletteFolder.getPrimaryFile();
                String folderName = CategoryNode.convertCategoryToFolderName( parentFolder, categoryName, null );
                FileObject folder = parentFolder.createFolder(folderName);
                if (!folderName.equals(categoryName))
                    folder.setAttribute( CategoryNode.CAT_NAME, categoryName );
                break;
            }
        }
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return Utils.getHelpCtx( this, super.getHelpCtx() );
    }

    // --------------

    /** Children for the PaletteNode. Creates PaletteCategoryNode instances
     * as filter subnodes. */
    private static class Children extends FilterNode.Children {

        private PaletteFilter filter;
        private Lookup lkp;
        
        public Children(Node original, Lookup lkp) {
            super(original);
            this.lkp = lkp;
            filter = (PaletteFilter)lkp.lookup( PaletteFilter.class );
        }

        @Override
        protected Node copyNode(Node node) {
            return new CategoryNode( node, lkp );
        }

        @Override
        public int getNodesCount(boolean optimalResult) {
            return getNodes(optimalResult).length;
        }
        
        @Override
        protected Node[] createNodes(Node key) {
            if( null == filter || filter.isValidCategory( key.getLookup() ) ) {
                return new Node[] { copyNode(key) };
            }

            return null;
        }
        
        public void refreshNodes() {
            Node[] nodes = original.getChildren().getNodes();
            List<Node> empty = Collections.emptyList();
            setKeys( empty );
            setKeys( nodes );
        }
    }

    // -------


    // -------
    /**
     * New type for creation of new palette category.
     */
    final class NewCategory extends NewType {

        @Override
        public String getName() {
            return Utils.getBundleString("CTL_NewCategory"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(NewCategory.class);
        }

        public void create() throws java.io.IOException {
            RootNode.this.createNewCategory();
        }
    }
}
