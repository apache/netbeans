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

import java.util.Iterator;

import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.Attribute;

/**
 * TreeAtribute represents attribute name value pair.
 * Value of attribute can be: Text or EntityReference.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttribute extends TreeNode implements Element.Attribute, TreeNamedObjectMap.NamedObject {

    /** */
    public static final String PROP_NAME          = "name"; // NOI18N
    /** */
    public static final String PROP_VALUE         = "value"; // NOI18N
    /** */
    public static final String PROP_OWNER_ELEMENT = "ownerElement"; // NOI18N
    /** */
    public static final String PROP_SPECIFIED     = "specified"; // NOI18N
    
    /** -- can be null. */
    private TreeElement ownerElement;  //my "parent" -- element in which it is attribute // NOI18N
    
    /** */
    private TreeName name;  //attribute qName
    
    /** */
    private TreeObjectList valueList;
    
    /** */
    private boolean specified; //is the attribute specified in document? (or default)
    
    /** */
    private TreeNamedObjectMap.KeyListener mapKeyListener;
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeAttribute.
     * @param qName XML qualified name e.g. "myns:root" or "root".
     * @param value unnormalized attribute value (general refs allowed ???)
     * @param specified true means that the attribute must be represented literaly in document
     * @throws InvalidArgumentException if qName or value contains unacceptable values
     */
    public TreeAttribute (String qName, String value, boolean specified) throws InvalidArgumentException {
        super ();
        
        TreeName treeName = new TreeName (qName);
        checkName (treeName);
        checkValue (value);
        
        this.name      = treeName;
        this.specified = specified;
        this.valueList = new TreeObjectList (createValueListContentManager ());
        setValueImpl (value);
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttribute::INIT : name = " + qName + " : specified = " + specified); // NOI18N
    }
    
    /**
     * Creates new specified TreeAttribute.
     * @param qName XML qualified name e.g. "myns:root" or "root".
     * @param value unnormalized attribute value (no general refs allowed)
     * @throws InvalidArgumentException if qName or value contains unacceptable values
     */
    public TreeAttribute (String qName, String value) throws InvalidArgumentException {
        this (qName, value, true);
    }
    
    
    /**
     * Creates new TreeAttribute -- copy constructor.
     */
    protected TreeAttribute (TreeAttribute attribute) {
        super (attribute);
        
        this.name      = attribute.name;
        this.specified = true; //??? -- copy will be specified
        this.valueList = new TreeObjectList (createValueListContentManager ());
        this.valueList.addAll ((TreeObjectList)attribute.valueList.clone ());
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeAttribute (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeAttribute peer = (TreeAttribute) object;
        if (!!! Util.equals (this.getTreeName (), peer.getTreeName ()))
            return false;
        if (this.specified != peer.isSpecified ())
            return false;
        if (!!! Util.equals (this.valueList, peer.valueList))
            return false;
        
        return true;
    }
    
    /*
     * Merge name and specified (sticky) properties and delegate value list merging.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeAttribute peer = (TreeAttribute) treeObject;
        
        try {
            setTreeNameImpl (peer.getTreeName ());
            setSpecifiedImpl (peer.isSpecified ());
            valueList.merge (peer.valueList);
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
    }
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
        
        valueList.setReadOnly (newReadOnly);
    }
    
    
    //
    // context
    //
    
    /**
     */
    public final boolean isInContext () {
        return ( getOwnerElement () != null );
    }
    
    /**
     */
    public final void removeFromContext () throws ReadOnlyException {
        if ( isInContext () ) {
            getOwnerElement ().removeAttribute (this);
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final String getQName () {
        return name.getQualifiedName ();
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException if given name is not acceptable by constains
     */
    public final void setQName (String name) throws ReadOnlyException, InvalidArgumentException {
        setTreeName (new TreeName (name));
    }
    
    /**
     */
    public final TreeName getTreeName () {
        return name;
    }
    
    /**
     */
    private final void setTreeNameImpl (TreeName newName) {
        TreeName oldName = this.name;
        
        this.name = newName;
        
        fireMapKeyChanged (oldName);
        firePropertyChange (PROP_NAME, oldName, newName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException if passed argument does not pass checks
     */
    public final void setTreeName (TreeName newName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.name, newName) )
            return;
        checkReadOnly ();
        checkName (newName);
        
        //
        // set new value
        //
        setTreeNameImpl (newName);
    }
    
    /**
     */
    protected final void checkName (TreeName name) throws InvalidArgumentException {
        TreeUtilities.checkAttributeName (name);
    }
    
    
    
    public boolean isSpecified () {
        return specified;
    }
    
    /**
     * Set the value and fire a property change event.
     * It may change just during merge operation.
     */
    private void setSpecifiedImpl (boolean newValue) {
        if (this.specified == newValue)
            return;
        
        Boolean oldValue = this.specified ? Boolean.TRUE : Boolean.FALSE;
        
        this.specified = newValue;
        
        firePropertyChange (PROP_SPECIFIED, oldValue, newValue ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /**
     * @return structured representation of attribute value.
     */
    public final TreeObjectList getValueList () {
        return valueList;
    }
    
    /**
     * @return resolved attribute text value
     */
    public final String getValue () {
        StringBuffer value = new StringBuffer (23);
        Iterator it = valueList.iterator ();
        
        while (it.hasNext ()) {
            Object next = it.next ();
            if (next instanceof TreeData) {
                value.append (((TreeData)next).getData ());
            } else if (next instanceof TreeGeneralEntityReference) {
                //!!! resolve it
                value.append ("&" + ((TreeGeneralEntityReference)next).getName () + ";"); // NOI18N
            } else if (next instanceof TreeCharacterReference) {
                value.append (((TreeCharacterReference)next).getData ());
            }
        }
        return value.toString ();
    }
    
    /**
     * @return unresolved attribute value
     */
    public final String getNonNormalizedValue () {
        StringBuffer value = new StringBuffer (23);
        Iterator it = valueList.iterator ();
        
        while (it.hasNext ()) {
            Object next = it.next ();
            if (next instanceof TreeData) {
                value.append (((TreeData)next).getData ());
            } else if (next instanceof TreeGeneralEntityReference) {
                value.append ("&" + ((TreeGeneralEntityReference)next).getName () + ";"); // NOI18N
            } else if (next instanceof TreeCharacterReference) {
                value.append ("&" + ((TreeCharacterReference)next).getName () + ";"); // NOI18N
            }
        }
        return value.toString ();
    }
    
    /**
     * Simplified attribute value setter.
     *
     */
    private final void setValueImpl (String newValue) {
        String oldValue = this.getValue ();
        
        this.valueList.clear ();
        
        if ( newValue.length () != 0) {
            try {
                TreeText newText = new TreeText (newValue);
                this.valueList.add (newText);
            } catch (TreeException exc) {
                // something is wrong -- OK
            }
        }
        
        firePropertyChange (PROP_VALUE, oldValue, newValue);
    }
    
    /**
     * Simplified attribute value setter.
     *
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setValue (String newValue) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.getValue (), newValue) )
            return;
        checkReadOnly ();
        checkValue (newValue);
        
        //
        // set new value
        //
        setValueImpl (newValue);
    }
    
    /**
     * Check value being set by setValue
     */
    protected final void checkValue (String value) throws InvalidArgumentException {
        TreeUtilities.checkAttributeValue (value);
    }
    
    
    
    //
    // Namespaces
    //
    
    /**
     * @return attribute namespace or TreeNamespace.NO_NAMESPACE.
     */
    public final TreeNamespace getNamespace () {
        if (getOwnerElement () != null) {
            TreeElement owner = getOwnerElement ();
            TreeNamespaceContext ctx = owner.getNamespaceContext ();
            String prefix = getNamespacePrefix ();
            String uri = ctx.getURI (prefix);
            if (uri == null) {
                return TreeNamespace.NO_NAMESPACE;
            } else {
                return new TreeNamespace (prefix, uri);
            }
        }
        return TreeNamespace.NO_NAMESPACE;
    }
    
    /**
     */
    public final String getNamespacePrefix () {
        return name.getPrefix ();
    }
    
    
    /**
     */
    public final String getNamespaceURI () {
        return getNamespace ().getURI ();
    }
    
    
    /**
     */
    public final String getLocalName () {
        return name.getName ();
    }
    
    
    //
    // TreeNamedObjectMap.NamedObject
    //
    
    /**
     */
    public Object mapKey () {
        return getTreeName ();
    }
    
    /**
     */
    //    public String mapKeyPropertyName () {
    //	return PROP_NAME;
    //    }
    
    /** Attach NamedObject to NamedObject Map. */
    public void setKeyListener (TreeNamedObjectMap.KeyListener keyListener) {
        mapKeyListener = keyListener;
    }
    
    private void fireMapKeyChanged (Object oldKey) {
        if ( mapKeyListener == null ) {
            return;
        }
        mapKeyListener.mapKeyChanged (oldKey);
    }
    
    
    //
    // from TreeNode
    //
    
    /**
     */
    public final TreeDocumentRoot getOwnerDocument () {
        if ( getOwnerElement () == null )
            return null;
        return getOwnerElement ().getOwnerDocument ();
    }
    
    //
    // ownerElement
    //
    
    /**
     */
    public final TreeElement getOwnerElement () {
        return ownerElement;
    }
    
    /**
     */
    protected final void setOwnerElement (TreeElement newOwnerElement) {
        if (Util.equals (ownerElement, newOwnerElement))
            return;
        
        TreeElement oldOwnerElement = this.ownerElement;
        
        this.ownerElement = newOwnerElement;
        
        firePropertyChange (getEventChangeSupport ().createEvent (PROP_OWNER_ELEMENT, oldOwnerElement, newOwnerElement));
    }
    
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createValueListContentManager () {
        return new ValueListContentManager ();
    }
    
    
    /**
     *
     */
    protected class ValueListContentManager extends TreeObjectList.ContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeAttribute.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (Attribute.Value.class, obj);
        }
        
        /** */
        public void objectInserted (TreeObject obj) {
            TreeAttribute.this.firePropertyChange (PROP_VALUE, TreeAttribute.this.valueList, obj); //!!!
        }
        
        /** */
        public void objectRemoved (TreeObject obj) {
            TreeAttribute.this.firePropertyChange (PROP_VALUE, TreeAttribute.this.valueList, obj); //!!!
        }
        
        /** */
        public void orderChanged (int[] permutation) {
            TreeAttribute.this.firePropertyChange (PROP_VALUE, TreeAttribute.this.valueList, permutation); //!!!
        }
        
    } // end: class ValueListContentManager
    
}
