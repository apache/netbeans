/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.nodes.*;


/**
 * Default implementation of PaletteItem interface based on Nodes.
 *
 * @author S. Aubrecht
 */
public class DefaultItem implements Item {

    private Node itemNode;

    /**
     * Creates a new instance of DefaultPaletteItem
     *
     * @param itemNode Node representing the palette item.
     */
    public DefaultItem( Node itemNode ) {
        this.itemNode = itemNode;
    }

    public String getName() {
        return itemNode.getName();
    }
    
    public Image getIcon(int type) {
        return itemNode.getIcon( type );
    }

    public Action[] getActions() {
        return itemNode.getActions( false );
    }

    public String getShortDescription() {
        return itemNode.getShortDescription();
    }

    public String getDisplayName() {
        return itemNode.getDisplayName();
    }

    public void invokePreferredAction( ActionEvent e ) {
        Action action = itemNode.getPreferredAction();
        if( null != action && action.isEnabled() ) {
            action.actionPerformed( e );
        }
    }

    public Lookup getLookup() {
        return itemNode.getLookup();
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof DefaultItem))
            return false;
        
        return itemNode.equals( ((DefaultItem) obj).itemNode );
    }

    public Transferable drag() {
        try {
            return itemNode.drag();
        } catch( IOException ioE ) {
            Logger.getLogger( DefaultItem.class.getName() ).log( Level.INFO, null, ioE );
        }
        return null;
    }

    public Transferable cut() {
        try {
            return itemNode.clipboardCut();
        } catch( IOException ioE ) {
            Logger.getLogger( DefaultItem.class.getName() ).log( Level.INFO, null, ioE );
        }
        return null;
    }
    
    public String toString() {
        return itemNode.getDisplayName();
    }
}
