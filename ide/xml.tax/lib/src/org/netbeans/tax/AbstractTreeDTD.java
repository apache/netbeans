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
import java.util.Collection;
import java.util.LinkedList;

/**
 * Introduces utility methods for quering DTD content.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class AbstractTreeDTD extends TreeParentNode {

    //
    // init
    //

    /** Creates new AbstractTreeDTD. */
    protected AbstractTreeDTD () {
        super ();
    }

    /** Creates new AbstractTreeDTD -- copy constructor. */
    protected AbstractTreeDTD (AbstractTreeDTD abstractDTD, boolean deep) {
        super (abstractDTD, deep);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final Collection getElementDeclarations () {
        return getChildNodes (TreeElementDecl.class, true);
    }
    
    /**
     */
    public final Collection getAttlistDeclarations () {
        return getChildNodes (TreeAttlistDecl.class, true);
    }
    
    /**
     */
    public final Collection getAttributeDeclarations (String elementName) {
        Collection attrDefs = new LinkedList ();
        Iterator it = getAttlistDeclarations ().iterator ();
        while (it.hasNext ()) {
            TreeAttlistDecl attlist = (TreeAttlistDecl)it.next ();
            if ( attlist.getElementName ().equals (elementName) ) {
                attrDefs.addAll ((Collection)attlist.getAttributeDefs ());
            }
        }
        return attrDefs;
    }
    
    /**
     */
    public final Collection getEntityDeclarations () {
        return getChildNodes (TreeEntityDecl.class, true);
    }
    
    /**
     */
    public final Collection getNotationDeclarations () {
        return getChildNodes (TreeNotationDecl.class, true);
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     *
     */
    protected abstract class ChildListContentManager extends TreeParentNode.ChildListContentManager {
    } // end: class ChildListContentManager
    
}
