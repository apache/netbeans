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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This tree object own a list of its children accessible by getChildNodes().
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeParentNode extends TreeChild {

    /** */
    public static final String PROP_CHILD_LIST = "childList"; // NOI18N

    /** */
    private TreeObjectList childList;


    //
    // init
    //

    /** Creates new TreeParentNode. */
    protected TreeParentNode () {
        super ();
        
        this.childList = new TreeObjectList (createChildListContentManager ());
    }
    
    /** Creates new TreeParentNode -- copy constructor. */
    protected TreeParentNode (TreeParentNode parentNode, boolean deep) {
        super (parentNode);
        
        this.childList = new TreeObjectList (createChildListContentManager ());
        if (deep) {
            this.childList.addAll ((TreeObjectList)parentNode.childList.clone ());
        }
    }
    
    
    //
    // from TreeObject
    //
    
    /** Clone depply tree object.
     * @return deep clone of this node
     */
    public abstract Object clone (boolean deep);
    
    
    /** Call clone (true).
     * @return deep clone of this node.
     */
    public final Object clone () {
        return clone (true);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeParentNode peer = (TreeParentNode) object;
        if (!!! Util.equals (this.childList, peer.childList)) {
            return false;
        }
        
        return true;
    }
    
    /*
     * Merges childlist.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeParentNode peer = (TreeParentNode) treeObject;
        
        childList.merge (peer.childList);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public boolean isAssignableChild (TreeChild child) {
        return childList.isAssignableObject (child);
    }
    
    /**
     * @return <b>reference</b> to kids
     */
    public final TreeObjectList getChildNodes () {
        return childList;
    }
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
        
        childList.setReadOnly (newReadOnly);
    }
    
    
    //
    // Children manipulation
    //
    
    /**
     */
    public final TreeChild getFirstChild () {
        if ( childList.size () == 0 ) {
            return null;
        }
        return (TreeChild)childList.get (0);
    }
    
    /**
     */
    public final TreeChild getLastChild () {
        if ( childList.size () == 0 ) {
            return null;
        }
        return (TreeChild)childList.get (childList.size () - 1);
    }
    
    
    /**
     * @throws ReadOnlyException
     */
    public final void insertBefore (TreeChild newChild, TreeChild refChild) throws ReadOnlyException {
        /*
        if (refChild == null) {
            // For semantic compatibility with DOM.
            appendChild(newChild);
            return;
        }
         */
        childList.checkReadOnly ();
        int index = childList.indexOf (refChild);
        if (index < 0) {
            return;
        }
        childList.add (index, newChild);
    }
    
    /**
     * @throws ReadOnlyException
     */
    public final void replaceChild (TreeChild oldChild, TreeChild newChild) throws ReadOnlyException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeParentNode::replaceChild: oldChild = " + oldChild); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("              ::replaceChild: newChild = " + newChild); // NOI18N
        
        childList.checkReadOnly ();
        int index = childList.indexOf (oldChild);
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("              ::replaceChild: childList [oldChild]  = " + index); // NOI18N
        
        if (index < 0) {
            return;
        }
        childList.set (index, newChild);
    }
    
    /**
     * @throws ReadOnlyException
     */
    public final void removeChild (TreeChild oldChild) throws ReadOnlyException {
        childList.checkReadOnly ();
        childList.remove (oldChild);
    }
    
    /**
     * @throws ReadOnlyException
     */
    public final void appendChild (TreeChild newChild) throws ReadOnlyException {
        childList.checkReadOnly ();
        childList.add (newChild);
    }
    
    
    /**
     * Insert child at specified position and set its parent and owner document.
     * @throws ReadOnlyException
     */
    public final void insertChildAt (TreeChild child, int index) throws ReadOnlyException {
        childList.checkReadOnly ();
        childList.add (index, child);
    }
    
    
    /**
     */
    public final int indexOf (TreeChild node) {
        return childList.indexOf (node);
    }
    
    
    /**
     */
    public final TreeChild item (int index) {
        return (TreeChild)childList.get (index);
    }
    
    /**
     */
    public final int getChildrenNumber () {
        return (childList.size ());
    }
    
    /**
     */
    public final boolean hasChildNodes () {
        return (!!! childList.isEmpty ());
    }
    
    /**
     */
    public final boolean hasChildNodes (Class childClass) {
        return hasChildNodes (childClass, false);
    }
    
    /**
     */
    public boolean hasChildNodes (Class childClass, boolean recursive) {
        Iterator it = getChildNodes ().iterator ();
        while (it.hasNext ()) {
            TreeChild child = (TreeChild)it.next ();
            
            // add matching leaf node
            
            if (childClass == null || childClass.isAssignableFrom (child.getClass ())) {
                return true;
            }
            
            // do recursive descent into kids
            
            if ( recursive && (child instanceof TreeParentNode) ) {
                if ( ((TreeParentNode)child).hasChildNodes (childClass, true) == true ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @return copy collection containing references
     */
    public final Collection getChildNodes (Class childClass) {
        return getChildNodes (childClass, false);
    }
    
    /**
     * @return copy collection containing references
     */
    public Collection getChildNodes (Class childClass, boolean recursive) {
        
        //        new RuntimeException(getClass().toString() + ".getChildNodes(" + childClass.toString() + "," + recursive + ")").printStackTrace(); // NOI18N
        
        Collection allChildNodes = new LinkedList ();
        Iterator it = getChildNodes ().iterator ();
        while (it.hasNext ()) {
            TreeChild child = (TreeChild)it.next ();
            
            // add matching leaf node
            
            if (childClass == null || childClass.isAssignableFrom (child.getClass ())) {
                allChildNodes.add (child);
            }
            
            // do recursive descent into kids
            
            if ( recursive && (child instanceof TreeParentNode) ) {
                allChildNodes.addAll (((TreeParentNode)child).getChildNodes (childClass, true));
            }
        }
        return allChildNodes;
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected abstract TreeObjectList.ContentManager createChildListContentManager ();
    
    /**
     *
     */
    protected abstract class ChildListContentManager extends TreeObjectList.ContentManager {
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (TreeChild.class, obj);
        }
        
        /**
         */
        public void objectInserted (TreeObject obj) {
            ((TreeChild)obj).setParentNode (TreeParentNode.this);
            TreeParentNode.this.firePropertyChange (TreeParentNode.PROP_CHILD_LIST, TreeParentNode.this.childList, obj);
        }
        
        /**
         */
        public void objectRemoved (TreeObject obj) {
            ((TreeChild)obj).setParentNode (null);
            TreeParentNode.this.firePropertyChange (TreeParentNode.PROP_CHILD_LIST, TreeParentNode.this.childList, obj);
        }
        
        /**
         */
        public void orderChanged (int[] permutation) {
            TreeParentNode.this.firePropertyChange (TreeParentNode.PROP_CHILD_LIST, TreeParentNode.this.childList, permutation);
        }
        
    } // end: class ChildListContentManager
    
}
