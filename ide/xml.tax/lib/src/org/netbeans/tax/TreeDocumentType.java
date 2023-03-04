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

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.DTD;

import java.util.*;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentType extends AbstractTreeDTD implements TreeDTDRoot, Document.Child {
    /** */
    public static final String PROP_ELEMENT_NAME = "elementName"; // NOI18N
    /** */
    public static final String PROP_PUBLIC_ID    = "publicId"; // NOI18N
    /** */
    public static final String PROP_SYSTEM_ID    = "systemId"; // NOI18N
    
    
    /** */
    private String elementName;
    
    /** -- can be null. */
    private String publicId;
    
    /** -- can be null. */
    private String systemId;

    // strong reference to keep a key in bellow map
    private DTDIdentity dtdIdentity;

    // holds DTD-ID -> TreeDocumentFragment mapping
    private static final WeakHashMap externalEntities = new WeakHashMap();

    private String internalDTDText;  //!!! it is accesed by introspection, it a hack
    
    //
    // init
    //
    
    /**
     * Creates new TreeDocumentType.
     * @throws InvalidArgumentException
     */
    public TreeDocumentType (String elementName, String publicId, String systemId) throws InvalidArgumentException {
        super ();
        
        checkElementName (elementName);
        checkPublicId (publicId);
        checkSystemId (systemId);
        
        this.elementName = elementName;
        this.publicId    = publicId;
        this.systemId    = systemId;
        this.dtdIdentity = new DTDIdentity();

    }
    
    
    /** Creates new TreeDocumentType.
     * @throws InvalidArgumentException
     */
    public TreeDocumentType (String elementName) throws InvalidArgumentException {
        this (elementName, null, null);
    }
    
    /** Creates new TreeDocumentType -- copy constructor. */
    protected TreeDocumentType (TreeDocumentType documentType, boolean deep) {
        super (documentType, deep);
        
        this.elementName = documentType.elementName;
        this.publicId    = documentType.publicId;
        this.systemId    = documentType.systemId;
        this.internalDTDText = documentType.internalDTDText;
        this.dtdIdentity = documentType.dtdIdentity;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeDocumentType (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeDocumentType peer = (TreeDocumentType) object;
        if (!!! Util.equals (this.getElementName (), peer.getElementName ()))
            return false;
        if (!!! Util.equals (this.getPublicId (), peer.getPublicId ()))
            return false;
        if (!!! Util.equals (this.getSystemId (), peer.getSystemId ()))
            return false;
        if (!!! Util.equals (this.dtdIdentity, peer.dtdIdentity))
            return false;
        
        return true;
    }
    
    /*
     * Merges documet root name, publicId and system ID properties.
     * External DTD list merging is delegated.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeDocumentType peer = (TreeDocumentType) treeObject;
        
        setElementNameImpl (peer.getElementName ());
        setPublicIdImpl (peer.getPublicId ());
        setSystemIdImpl (peer.getSystemId ());
        internalDTDText = peer.internalDTDText;
        dtdIdentity = peer.dtdIdentity;
    }
    
    
    //
    // read only
    //
    
    
    /**
     * It's not propagated to exteranl entity. It's always read only.
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
    }
    
    
    //
    // parent
    //
    
    
    /**
     */
    public boolean hasChildNodes (Class childClass, boolean recursive) {
        TreeObjectList external = getExternalDTD();
        Iterator externalIterator = external != null ?
                external.iterator() : Collections.EMPTY_SET.iterator();
        Iterator[] its = new Iterator[] {
            getChildNodes ().iterator (),
            externalIterator
        };
        
        for (int i = 0; i<its.length; i++) {
            Iterator it = its[i];
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
        }
        return false;
    }
    
    /**
     * @return copy collection containing references from internal and
     * optionally external part of DTD
     */
    public Collection getChildNodes (Class childClass, boolean recursive) {
        Collection allChildNodes = new LinkedList ();
        TreeObjectList external = getExternalDTD();
        Iterator externalIterator = external != null ?
                external.iterator() : Collections.EMPTY_SET.iterator();

        Iterator[] its = new Iterator[] {
            getChildNodes ().iterator (),
            externalIterator
        };
        
        for (int i = 0; i<its.length; i++) {
            Iterator it = its[i];
            while (it.hasNext ()) {
                TreeChild child = (TreeChild)it.next ();
                if (childClass == null || childClass.isAssignableFrom (child.getClass ())) {
                    allChildNodes.add (child);
                }
                
                if ( recursive && (child instanceof TreeParentNode) ) {
                    allChildNodes.addAll (((TreeParentNode)child).getChildNodes (childClass, true));
                }
            }
        }
        
        return allChildNodes;
    }
    
    
    //
    // itself
    //
    
    /**
     * Return read only child list representing external DTD content
     * or <code>null</code> if unknown.
     */
    public final TreeObjectList getExternalDTD () {
        TreeDTDFragment fragment = (TreeDTDFragment) externalEntities.get(dtdIdentity);
        if (fragment == null) {
            return null;
        } else {
            return fragment.getChildNodes();
        }
    }
    
    /**
     */
    public final String getElementName () {
        return elementName;
    }
    
    /**
     */
    private final void setElementNameImpl (String newElementName) {
        String oldElementName = this.elementName;
        
        this.elementName = newElementName;
        
        firePropertyChange (PROP_ELEMENT_NAME, oldElementName, newElementName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setElementName (String newElementName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.elementName, newElementName) )
            return;
        checkReadOnly ();
        checkElementName (newElementName);
        
        //
        // set new value
        //
        setElementNameImpl (newElementName);
    }
    
    /**
     */
    protected final void checkElementName (String elementName) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypeElementName (elementName);
    }
    
    /**
     */
    public final String getPublicId () {
        return publicId;
    }
    
    /**
     */
    private final void setPublicIdImpl (String newPublicId) {
        String oldPublicId = this.publicId;
        
        this.publicId = newPublicId;

        firePropertyChange (PROP_PUBLIC_ID, oldPublicId, newPublicId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setPublicId (String newPublicId) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.publicId, newPublicId) )
            return;
        checkReadOnly ();
        checkPublicId (newPublicId);
        
        //
        // set new value
        //
        setPublicIdImpl (newPublicId);
    }
    
    /**
     */
    protected final void checkPublicId (String publicId) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypePublicId (publicId);
    }
    
    
    /**
     */
    public final String getSystemId () {
        return systemId;
    }
    
    /**
     */
    private final void setSystemIdImpl (String newSystemId) {
        String oldSystemId = this.systemId;
        
        this.systemId = newSystemId;
        
        firePropertyChange (PROP_SYSTEM_ID, oldSystemId, newSystemId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setSystemId (String newSystemId) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.systemId, newSystemId) )
            return;
        checkReadOnly ();
        checkSystemId (newSystemId);
        
        //
        // set new value
        //
        setSystemIdImpl (newSystemId);
    }
    
    /**
     */
    protected final void checkSystemId (String systemId) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypeSystemId (systemId);
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
     * Internal DTD content manager.
     * All kids use as parent node wrapping TreeDocumentType.
     * All kids must be DocumentType.Child instances.
     */
    protected class ChildListContentManager extends AbstractTreeDTD.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDocumentType.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DocumentType.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
    

    /**
     * Get DTDIdentity proxy for this class. It's a live object.
     */
    public final DTDIdentity getDTDIdentity() {
        return dtdIdentity;
    }

    /**
     * Set new external DTD model. Note that it can be shared by
     * several TreeDocumentType instances.
     */
    public final void setExternalDTD(TreeDocumentFragment externalDTD) {
        externalEntities.put(getDTDIdentity(), externalDTD);
    }

    /**
     * Defines doctype identity based on its public ID and system ID pairs.
     * Can be used as key if such equalince/identity is required.
     * @see #getDTDIdentity
     */
    public final class DTDIdentity {

        private DTDIdentity() {
        }

        private String getPublicId() {
            return publicId;
        }

        private String getSystemId() {
            return systemId;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof DTDIdentity) {
                DTDIdentity peer = (DTDIdentity) o;
                if (Util.equals(peer.getPublicId(), publicId) == false) return false;
                if (Util.equals(peer.getSystemId(), systemId) == false) return false;
                return true;
            }
            return false;
        }

        public int hashCode() {
            int h1 = publicId != null ? publicId.hashCode() : 13;
            int h2 = systemId != null ? systemId.hashCode() : 37;
            return h1 ^ h2;
        }
    }
}
