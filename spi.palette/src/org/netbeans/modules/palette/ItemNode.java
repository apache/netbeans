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
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;

/**
 * A Node for palette item.
 *
 * @author S. Aubrecht
 */
class ItemNode extends FilterNode {
    

    private Action[] actions;

    public ItemNode( Node originalNode ) {
        super( originalNode, Children.LEAF );
    }

    public Action[] getActions(boolean context) {
        if (actions == null) {
            Node n = getParentNode();
            if( null == n ) {
                return new Action[0];
            }

            actions = new Action[] {
                new Utils.CutItemAction( this ),
                new Utils.CopyItemAction( this ),
                new Utils.PasteItemAction( n ),
                null,
                new Utils.RemoveItemAction( this ),
                null,
                new Utils.SortItemsAction( n ),
                null,
                new Utils.RefreshPaletteAction()
            };
        }
        PaletteActions customActions = getCustomActions();
        if( null != customActions ) {
            return Utils.mergeActions( actions, customActions.getCustomItemActions( getLookup() ) );
        }
        return actions;
    }

    public Transferable clipboardCut() throws java.io.IOException {
        ExTransferable t = ExTransferable.create( super.clipboardCut() );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }

    public Transferable clipboardCopy() throws IOException {
        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }

    public PasteType getDropType( Transferable t, int action, int index ) {
        return null;
    }

    public Transferable drag() throws IOException {
        ExTransferable t = ExTransferable.create( super.drag() );//NodeTransfer.transferable(this, NodeTransfer.DND_MOVE) );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }
    
    private ExTransferable.Single createTransferable() {
        final Lookup lkp = getLookup();
        return new ExTransferable.Single( PaletteController.ITEM_DATA_FLAVOR ) {
           public Object getData () {
               return lkp;
           }
       };
    }
    
    private void customizeTransferable( ExTransferable t ) {
        DragAndDropHandler tp = getTransferableProvider();
        if( null != tp ) {
            tp.customize( t, getLookup() );
        }
    }

    private PaletteActions getCustomActions() {
        PaletteActions res = null;
        Node category = getParentNode();
        if( null != category ) {
            Node root = category.getParentNode();
            if( null != root ) {
               res = root.getLookup().lookup( PaletteActions.class ); 
            }
        }
        return res;
    }

    private DragAndDropHandler getTransferableProvider() {
        DragAndDropHandler res = null;
        Node category = getParentNode();
        if( null != category ) {
            Node root = category.getParentNode();
            if( null != root ) {
                res = root.getLookup().lookup( DragAndDropHandler.class );
            }
        }
        return res;
    }
    
    public Action getPreferredAction() {

        PaletteActions customActions = getCustomActions();
        
        if( null == customActions )
            return null;;
        
        return customActions.getPreferredAction( getLookup() );
    }

    public boolean canDestroy() {

        return !Utils.isReadonly( getOriginal() );
    }
    
    Node getOriginalNode() {
        return getOriginal();
    }

    public HelpCtx getHelpCtx() {
        return Utils.getHelpCtx( this, super.getHelpCtx() );
    }
}
