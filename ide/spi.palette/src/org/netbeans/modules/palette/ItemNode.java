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
            return null;
        
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
