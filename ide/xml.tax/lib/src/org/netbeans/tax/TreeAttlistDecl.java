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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;
import org.netbeans.tax.spec.AttlistDecl;

/**
 * Holds DTD attribute declarations for some element.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDecl extends TreeNodeDecl implements DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    /** */
    public static final String PROP_ELEMENT_NAME              = "elementName"; // NOI18N
    /** */
    public static final String PROP_ATTRIBUTE_DEF_MAP_ADD     = "map.add"; // NOI18N
    /** */
    public static final String PROP_ATTRIBUTE_DEF_MAP_REMOVE  = "map.remove"; // NOI18N
    /** */
    public static final String PROP_ATTRIBUTE_DEF_MAP_CONTENT = "map.content"; // NOI18N
    
    /** */
    private String elementName;
    
    /** */
    private TreeNamedObjectMap attributeDefs;
    
    
    //
    // init
    //
    
    /** Creates new TreeAttlistDecl.
     * @throws InvalidArgumentException
     */
    public TreeAttlistDecl (String elementName) throws InvalidArgumentException {
        super ();
        
        checkElementName (elementName);
        this.elementName   = elementName;
        this.attributeDefs = new TreeNamedObjectMap (createAttlistContentManager ());
    }
    
    /** Creates new TreeAttlistDecl -- copy constructor. */
    protected TreeAttlistDecl (TreeAttlistDecl attlistDecl) {
        super (attlistDecl);
        
        this.elementName   = attlistDecl.elementName;
        this.attributeDefs = new TreeNamedObjectMap (createAttlistContentManager ());
        this.attributeDefs.addAll ((TreeNamedObjectMap)attlistDecl.attributeDefs.clone ());
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeAttlistDecl (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeAttlistDecl peer = (TreeAttlistDecl) object;
        if (!!! Util.equals (this.getElementName (), peer.getElementName ()))
            return false;
        if (!!! Util.equals (this.attributeDefs, peer.attributeDefs))
            return false;
        
        return true;
    }
    
    /*
     * Merge element name property and delegate attributeDefs merging.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeAttlistDecl peer = (TreeAttlistDecl) treeObject;
        
        setElementNameImpl (peer.getElementName ());
        attributeDefs.merge (peer.attributeDefs);
    }
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        //if (newReadOnly) Util.saveContext("TreeAttlistDecl.setReadOnly(true)"); // NOI18N
        
        super.setReadOnly (newReadOnly);
        
        attributeDefs.setReadOnly (newReadOnly);
    }
    
    
    //
    // itself
    //
    
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
        TreeUtilities.checkAttlistDeclElementName (elementName);
    }
    
    /**
     */
    public final TreeAttlistDeclAttributeDef getAttributeDef (String attributeDefName) {
        return (TreeAttlistDeclAttributeDef)attributeDefs.get (attributeDefName);
    }
    
    /**
     */
    private final void setAttributeDefImpl (TreeAttlistDeclAttributeDef newAttributeDef) {
        TreeAttlistDeclAttributeDef oldAttributeDef = (TreeAttlistDeclAttributeDef)attributeDefs.get (newAttributeDef.getName ());
        
        attributeDefs.add (newAttributeDef);
        
        firePropertyChange (PROP_ATTRIBUTE_DEF_MAP_ADD, oldAttributeDef, newAttributeDef);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setAttributeDef (TreeAttlistDeclAttributeDef newAttributeDef) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        TreeAttlistDeclAttributeDef oldAttributeDef = (TreeAttlistDeclAttributeDef)attributeDefs.get (newAttributeDef.getName ());
        if ( Util.equals (oldAttributeDef, newAttributeDef) )
            return;
        checkReadOnly ();
//         checkAttributeDef (newAttributeDef);
        
        //
        // set new value
        //
        setAttributeDefImpl (newAttributeDef);
    }
    
    /**
     */
    private final TreeAttlistDeclAttributeDef removeAttributeDefImpl (String attributeDefName) {
        TreeAttlistDeclAttributeDef oldAttributeDef = (TreeAttlistDeclAttributeDef)attributeDefs.get (attributeDefName);
        
        attributeDefs.remove (oldAttributeDef);
        
        firePropertyChange (PROP_ATTRIBUTE_DEF_MAP_REMOVE, oldAttributeDef, null);
        
        return oldAttributeDef;
    }
    
    /**
     * @throws ReadOnlyException
     */
    public final TreeAttlistDeclAttributeDef removeAttributeDef (String attributeDefName) throws ReadOnlyException {
        //
        // check new value
        //
//         if ( Util.equals (this.???, new???) )
//             return;
        checkReadOnly ();
        
        //
        // set new value
        //
        return removeAttributeDefImpl (attributeDefName);
    }
    
    /**
     */
    public final TreeNamedObjectMap getAttributeDefs () {
        return attributeDefs;
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeNamedObjectMap.ContentManager createAttlistContentManager () {
        return new AttlistContentManager ();
    }
    
    
    /**
     *
     */
    protected class AttlistContentManager extends TreeNamedObjectMap.ContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeAttlistDecl.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (TreeAttlistDeclAttributeDef.class, obj);
        }
        
        /**
         */
        public void objectInserted (TreeObject obj) {
            ((TreeAttlistDeclAttributeDef)obj).setNodeDecl (TreeAttlistDecl.this);
        }
        
        /**
         */
        public void objectRemoved (TreeObject obj) {
            ((TreeAttlistDeclAttributeDef)obj).setNodeDecl (null);
        }
        
        /**
         */
        public void orderChanged (int[] permutation) {
        }
        
    } // end: class ChildListContentManager
    
}
