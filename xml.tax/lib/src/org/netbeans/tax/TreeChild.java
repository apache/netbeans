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
package org.netbeans.tax;

/**
 * Child adds notion of parent node.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeChild extends TreeNode {

    /** */
    public static final String PROP_PARENT_NODE = "parentNode"; // NOI18N


    /** -- can be null. */
    private TreeParentNode parentNode;


    //
    // init
    //

    /** Creates new TreeChild. */
    protected TreeChild () {
    }


    /**
     * Creates new TreeChild -- copy constructor.
     * (parentNode information is lost)
     */
    protected TreeChild (TreeChild child) {
        super (child);
    }
    
    
    //
    // from TreeNode
    //
    
    /**
     */
    public final TreeDocumentRoot getOwnerDocument () {
        if ( this instanceof TreeDocumentRoot ) {
            return (TreeDocumentRoot)this;
        }
        if ( getParentNode () == null ) {
            return null;
        }
        return getParentNode ().getOwnerDocument ();
    }
    
    
    //
    // context
    //
    
    /**
     */
    public final boolean isInContext () {
        return ( getParentNode () != null );
    }
    
    /**
     */
    public final void removeFromContext () throws ReadOnlyException {
        if ( isInContext () ) {
            getParentNode ().removeChild (this);
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeParentNode getParentNode () {
        return parentNode;
    }
    
    /**
     */
    protected final void setParentNode (TreeParentNode newParentNode) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeChild::setParentNode [ " + this + " ] : newParentNode = " + newParentNode); // NOI18N
        
        //
        // check new value
        //
        if ( Util.equals (this.parentNode, newParentNode) )
            return;
        
        //
        // set new value
        //
        TreeParentNode oldParentNode = this.parentNode;
        
        this.parentNode = newParentNode;
        
        firePropertyChange (PROP_PARENT_NODE, oldParentNode, newParentNode);
    }
    
    
    //
    // Children manipulation
    //
    
    /**
     */
    public final TreeChild getPreviousSibling () {
        int index = index ();
        if ( index == -1 ) { // does not have parent node
            return null;
        }
        if ( index == 0 ) { // it is first node of parent node
            return null;
        }
        return (TreeChild)getParentNode ().getChildNodes ().get (index - 1);
    }
    
    /**
     */
    public final TreeChild getNextSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeChild [ " + this + " ] ::getNextSibling: parentNode = " + getParentNode ()); // NOI18N
        
        int index = index ();
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    index : " + index); // NOI18N
        
        if ( index == -1 ) { // does not have parent node
            return null;
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    parentNode.childNodes.size : " + getParentNode ().getChildNodes ().size ()); // NOI18N
        
        if ( (index + 1) == getParentNode ().getChildNodes ().size () ) { // it is last node of parent node
            return null;
        }
        return (TreeChild)getParentNode ().getChildNodes ().get (index + 1);
    }
    
    /**
     * @return index of this node in parent node child list or -1 if it does not have parent node.
     */
    public final int index () {
        if ( getParentNode () == null ) {
            return -1;
        }
        return getParentNode ().indexOf (this);
    }
    
    
    //
    // util
    //
    
    /**
     */
    public final boolean isDescendantOf (TreeParentNode testParentNode) {
        TreeParentNode ancestor = getParentNode ();
        
        while ( ancestor != null ) {
            if ( ancestor == testParentNode )
                return true;
            ancestor = ancestor.getParentNode ();
        }
        
        return false;
    }
    
}
