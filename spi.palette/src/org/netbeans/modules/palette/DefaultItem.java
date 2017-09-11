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
        if( null == obj || !(obj instanceof DefaultItem) )
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
