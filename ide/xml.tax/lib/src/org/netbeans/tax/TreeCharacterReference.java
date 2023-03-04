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

import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.Attribute;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeCharacterReference extends TreeChild implements TreeReference, TreeCharacterData, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, Attribute.Value {
    /** */
    public static final String PROP_NAME = "name"; // NOI18N


    /** */
    private String name;  //literal occuring in document  "#99" // NOI18N
    
    //
    // init
    //
    
    /** Creates new TreeCharacterReference.
     * @throws InvalidArgumentException
     */
    public TreeCharacterReference (String name) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        this.name = name;
    }
    
    /** Creates new TreeCharacterReference -- copy constructor. */
    protected TreeCharacterReference (TreeCharacterReference characterReference) {
        super (characterReference);
        
        this.name = characterReference.name;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeCharacterReference (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeCharacterReference peer = (TreeCharacterReference) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        
        return true;
    }
    
    /*
     * Merge name property.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeCharacterReference peer = (TreeCharacterReference) treeObject;
        setNameImpl (peer.getName ());
    }
    
    //
    // itself
    //
    
    public final String getName () {
        return name;
    }
    
    /**
     */
    private final void setNameImpl (String newName) {
        String oldName = this.name;
        
        this.name = newName;
        
        firePropertyChange (PROP_NAME, oldName, newName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setName (String newName) throws ReadOnlyException, InvalidArgumentException {
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
        setNameImpl (newName);
    }
    
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkCharacterReferenceName (name);
    }
    
    /**
     * @return string representing value (may be a surrogate)
     */
    public final String getData () {
        
        //!!! does not work for surrogates
        
        short val;
        
        if (name.startsWith ("#x")) { // NOI18N
            val = Short.parseShort (name.substring (2), 16);
        } else {
            val = Short.parseShort (name.substring (1));
        }
        return new String (new char[] {(char) val});
    }
    
}
