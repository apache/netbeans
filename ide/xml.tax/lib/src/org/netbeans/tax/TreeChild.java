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
