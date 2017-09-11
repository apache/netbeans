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
package org.netbeans.tax.traversal;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.InvalidStateException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeIterator {
    /** Root node of this iterator. */
    private TreeNode root;

    /** Determines which node types are presented. */
    private int whatToShow;

    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** State of iterator. */
    private boolean valid;


    /** Creates new TreeNodeIterator.
     * @param node root node
     * @param wTS what to show
     * @param f used filter
     */
    public TreeNodeIterator (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;
        
        valid = true;
    }
    
    
    /**
     */
    public TreeNode getRoot () {
        return root;
    }
    
    /**
     */
    public int getWhatToShow () {
        return whatToShow;
    }
    
    /**
     */
    public TreeNodeFilter getFilter () {
        return filter;
    }
    
    /**
     */
    public TreeNode nextNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.nextNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.previousNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public void detach () {
        valid = false;
    }
    
}
