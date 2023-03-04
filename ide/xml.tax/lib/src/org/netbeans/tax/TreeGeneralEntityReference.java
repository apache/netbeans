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
public class TreeGeneralEntityReference extends TreeEntityReference implements DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, Attribute.Value {

    //
    // init
    //

    /** Creates new TreeGeneralEntityReference.
     * @throws InvalidArgumentException
     */
    public TreeGeneralEntityReference (String name) throws InvalidArgumentException {
        super (name);
    }
    
    /** Creates new TreeGeneralEntityReference -- copy constructor. */
    protected TreeGeneralEntityReference (TreeGeneralEntityReference generalEntityReference, boolean deep) {
        super (generalEntityReference, deep);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeGeneralEntityReference (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        return true;
    }
    
    /*
     * Checks instance and delegate to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkGeneralEntityReferenceName (name);
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
     *
     */
    protected class ChildListContentManager extends TreeEntityReference.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeGeneralEntityReference.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (GeneralEntityReference.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
}
