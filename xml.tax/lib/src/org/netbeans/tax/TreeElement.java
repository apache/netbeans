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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.Attribute;

import org.netbeans.tax.event.TreeEventManager;

/**
 * It represents startTag, endTag and emptyTag markup and holds element content.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeElement extends TreeParentNode implements Document.Child, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child {
    
    /** */
    public static final String PROP_TAG_NAME   = "tagName"; // NOI18N
    /** */
    public static final String PROP_ATTRIBUTES = "attributes"; // NOI18N
    
    /** */
    private TreeName tagName;  //QName as arrears in output
    
    /** */
    //     private String baseURI;  //??? => xml:base="baseURI" // NOI18N
    
    /** */
    private TreeNamespaceContext namespaceContext;
    
    /** */
    private TreeNamedObjectMap attributes;
    
    /** */
    private boolean empty;  //signals that it represents empty element <empty/>
    //as opposite to <nonempty></nonempty>
    //it is a sticky flag
    
    /** */
    private boolean containsCharacterData;
    
    
    //
    // init
    //
    
    /** Creates new TreeElement.
     * @throws InvalidArgumentException
     */
    public TreeElement (String tagName, boolean empty) throws InvalidArgumentException {
        super ();
        
        TreeName treeName = new TreeName (tagName);
        checkTagName (treeName);
        this.tagName = treeName;
        this.empty   = empty;
        this.containsCharacterData = false;
        
        this.namespaceContext = new TreeNamespaceContext (this);
        this.attributes       = new TreeNamedObjectMap (createAttributesContentManager ());
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeElement:: : name = " + tagName + " : empty = " + empty); // NOI18N
    }
    
    
    /** Creates new TreeElement.
     * @throws InvalidArgumentException
     */
    public TreeElement (String qName) throws InvalidArgumentException {
        this (qName, false);
    }
    
    /**
     * Creates new TreeElement -- copy constructor.
     */
    protected TreeElement (TreeElement element, boolean deep) {
        super (element, deep);
        
        this.tagName          = element.tagName;
        this.empty            = element.empty;
        //  	this.baseURI          = element.baseURI;
        this.namespaceContext = new TreeNamespaceContext (this);
        this.attributes       = new TreeNamedObjectMap (createAttributesContentManager ());
        this.attributes.addAll ((TreeNamedObjectMap)element.attributes.clone ());
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeElement (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeElement peer = (TreeElement) object;
        if (this.empty != peer.empty)
            return false;
        if (!!! Util.equals (this.getTreeName (), peer.getTreeName ()))
            return false;
        if (!!! Util.equals (this.getAttributes (), peer.getAttributes ()))
            return false;
        
        return true;
    }
    
    /*
     * Merge name and empty (sticky) properties and delagte merging attributes.
     * Ignore peer's Namespace context.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeElement peer = (TreeElement) treeObject;
        this.empty = empty || peer.empty;  //sticky tag
        
        try {
            setTreeName (peer.getTreeName ());
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
        
        attributes.merge (peer.getAttributes ());
        //         attributes.setContentManager (this);
    }
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
        
        attributes.setReadOnly (newReadOnly);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final String getQName () {
        return tagName.getQualifiedName ();
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setQName (String newTagName) throws ReadOnlyException, InvalidArgumentException {
        setTreeName (new TreeName (newTagName));
    }
    
    /**
     */
    public final TreeName getTreeName () {
        return tagName;
    }
    
    /**
     */
    private final void setTreeNameImpl (TreeName newTagName) {
        TreeName oldTagName = this.tagName;
        
        this.tagName = newTagName;
        
        firePropertyChange (PROP_TAG_NAME, oldTagName, newTagName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setTreeName (TreeName newTagName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.tagName, newTagName) )
            return;
        checkReadOnly ();
        checkTagName (newTagName);
        
        //
        // set new value
        //
        setTreeNameImpl (newTagName);
    }
    
    /**
     * Treat the empty flag as "sticky" is means that is someone
     * adds some conetent and then remove it this flag will survive
     * @return true it this element represents empty one
     */
    public boolean isEmpty () {
        if (empty == false)
            return false;
        return getChildNodes ().size () == 0;
    }
    
    /**
     */
    protected final void checkTagName (TreeName tagName) throws InvalidArgumentException {
        TreeUtilities.checkElementTagName (tagName);
    }
    
    
    //
    // Namespaces
    //
    
    
    /**
     */
    protected final TreeNamespaceContext getNamespaceContext () {
        return namespaceContext;
    }
    
    /**
     * @return element namespce or TreeNamespace.NO_NAMESPACE
     */
    public final TreeNamespace getNamespace () {
        String prefix = getNamespacePrefix ();
        String uri = namespaceContext.getURI (prefix);
        
        if (uri == null) {
            return TreeNamespace.NO_NAMESPACE;
        } else {
            return new TreeNamespace (prefix, uri);
        }
    }
    
    /**
     */
    public final String getNamespacePrefix () {
        return tagName.getPrefix ();
    }
    
    
    /**
     */
    public final String getNamespaceURI () {
        return getNamespace ().getURI ();
    }
    
    
    /**
     */
    public final String getLocalName () {
        return tagName.getName ();
    }
    
    
    
    //
    // Attributes
    //
    
    
    /**
     */
    public final int getAttributesNumber () {
        return (attributes.size ());
    }
    
    /**
     */
    public final boolean hasAttributes () {
        return ( attributes.size () != 0 );
    }
    
    /**
     */
    public final boolean hasAttribute (String name) {
        return ( getAttribute (name) != null );
    }
    
    
    /**
     */
    public final TreeNamedObjectMap getAttributes () {
        return attributes;
    }
    
    /**
     */
    public final TreeAttribute getAttribute (String name) {
        try {
            TreeName treeName = new TreeName (name);
            return (TreeAttribute)attributes.get (treeName);
        } catch (InvalidArgumentException exc) {
            return null;
        }
    }
    
    /**
     * @throws InvalidArgumentException
     * @throws ReadOnlyException
     */
    public final TreeAttribute addAttribute (String name, String value) throws ReadOnlyException, InvalidArgumentException {
//// Will be uncommented after NB 3.3.1 (http://www.netbeans.org/issues/show_bug.cgi?id=17699)
//         TreeAttribute attr = getAttribute (name);
//         if ( attr != null ) {
//             throw new InvalidArgumentException
//             (attr, Util.THIS.getString ("EXC_attribute_exists", name));
//         }
        
        checkReadOnly ();
        TreeAttribute newAttr = new TreeAttribute (name, value);
        TreeAttribute oldAttr = removeAttribute (name);
        attributes.add (newAttr);
        return oldAttr;
    }
    
    
    /**
     * @throws InvalidArgumentException
     * @throws ReadOnlyException
     */
    public final void addAttribute (TreeAttribute newAttr) throws ReadOnlyException, InvalidArgumentException {
        String qName = newAttr.getQName ();

//// Will be uncommented after NB 3.3.1 (http://www.netbeans.org/issues/show_bug.cgi?id=17699)
//         TreeAttribute attr = getAttribute (qName);
//         if ( attr != null ) {
//             throw new InvalidArgumentException
//             (attr, Util.THIS.getString ("EXC_attribute_exists", qName));
//         }
        
        checkReadOnly ();
        TreeAttribute oldAttr = removeAttribute (qName);
        attributes.add (newAttr);
        //         return oldAttr;
    }
    
    
    /**
     * @throws ReadOnlyException
     */
    public final TreeAttribute removeAttribute (String name) throws ReadOnlyException {
        return removeAttribute (getAttribute (name));
    }
    
    
    /**
     * @throws ReadOnlyException
     */
    public final TreeAttribute removeAttribute (TreeAttribute oldAttr) throws ReadOnlyException {
        checkReadOnly ();
        attributes.remove (oldAttr);
        return oldAttr;
    }
    
    /**
     * @throws ReadOnlyException
     */
    public final void removeAttributes () throws ReadOnlyException {
        checkReadOnly ();
        attributes.clear ();
    }
    
    
    //
    // Utilities
    //
    
    /**
     * @throws ReadOnlyException
     */
    public final void normalize () throws ReadOnlyException {
        checkReadOnly ();
        try {
            getChildNodes ().getEventManager ().setFirePolicy (TreeEventManager.FIRE_LATER);
            for (int i = 0; true; i++) {
                TreeChild child = item (i);
                
                if (child instanceof TreeElement) {
                    ((TreeElement)child).normalize ();
                } else if (child instanceof TreeText) {
                    while (true) {
                        TreeChild child2 = item (i + 1);
                        if (child2 instanceof TreeText) {
                            try {
                                ((TreeText)child).appendData (((TreeText)child2).getData ());
                                removeChild (child2);
                            } catch (InvalidArgumentException exc) { // from TreeText.appendChild : impossible because TreeText.getData
                                break; // get out from 'while (true)'
                            }
                        } else {
                            break; // get out from 'while (true)'
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            /* OK */
        } finally {
            getChildNodes ().getEventManager ().setFirePolicy (TreeEventManager.FIRE_NOW);
        }
    }
    
    
    /**
     */
    public final boolean containsCharacterData () {
        return containsCharacterData;
    }
    
    /**
     */
    private void updateContainsCharacterData () {
        Iterator it = getChildNodes ().iterator ();
        while (it.hasNext ()) {
            Object obj = it.next ();
            if ( obj instanceof TreeCharacterData ) {
                TreeCharacterData charData = (TreeCharacterData)obj;
                if ( charData instanceof TreeData ) {
                    if ( ((TreeData)charData).onlyWhiteSpaces () == false ) {
                        containsCharacterData = true;
                    }
                } else {
                    containsCharacterData = true;
                }
                if ( containsCharacterData ) {
                    return;
                }
            }
        }
        containsCharacterData = false;
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    
    /**
     */
    protected TreeNamedObjectMap.ContentManager createAttributesContentManager () {
        return new AttributesContentManager ();
    }
    
    
    /**
     *
     */
    protected class ChildListContentManager extends TreeParentNode.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeElement.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (Element.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
    
    /**
     *
     */
    protected class AttributesContentManager extends TreeNamedObjectMap.ContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeElement.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            checkAssignableClass (Element.Attribute.class, obj);
        }
        
        /**
         */
        public void objectInserted (TreeObject obj) {
            ((TreeAttribute)obj).setOwnerElement (TreeElement.this);
            TreeElement.this.firePropertyChange (TreeElement.PROP_ATTRIBUTES, TreeElement.this.attributes, null);
        }
        
        /**
         */
        public void objectRemoved (TreeObject obj) {
            ((TreeAttribute)obj).setOwnerElement (null);
            TreeElement.this.firePropertyChange (TreeElement.PROP_ATTRIBUTES, TreeElement.this.attributes, null);
        }
        
        /**
         */
        public void orderChanged (int[] permutation) {
            TreeElement.this.firePropertyChange (TreeElement.PROP_ATTRIBUTES, TreeElement.this.attributes, null);
        }
        
    } // end: class AttributesContentManager
    
}
