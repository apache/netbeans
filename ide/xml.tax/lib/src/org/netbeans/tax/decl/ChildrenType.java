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
package org.netbeans.tax.decl;

import java.util.*;

import org.netbeans.tax.*;

public abstract class ChildrenType extends TreeElementDecl.ContentType implements TypeCollection {

    /** */
    protected List context;         // children collection


    //
    // init
    //

    public ChildrenType (Collection col) {
        super ();

        context = new LinkedList ();
        context.addAll (col);
    }

    public ChildrenType () {
        this (new ArrayList ());
    }

    public ChildrenType (ChildrenType childrenType) {
        super (childrenType);
        
        context = new LinkedList ();
        Iterator it = childrenType.context.iterator ();
        while ( it.hasNext () ) {
            context.add (((TreeElementDecl.ContentType)it.next ()).clone ());
        }
    }
    
    
    //
    // from TreeElementDecl.ContentType
    //
    
    /**
     */
    protected void setNodeDecl (TreeNodeDecl nodeDecl) {
        super.setNodeDecl (nodeDecl);
        
        initNodeDecl ();
    }
    
    protected final void initNodeDecl () {
        //  	Iterator it = context.iterator();
        //  	while ( it.hasNext() ) {
        //  	    TreeElementDecl.ContentType ct = (TreeElementDecl.ContentType)it.next();
        //  	    ct.setNodeDecl (getNodeDecl());
        //  	}
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public void addTypes (Collection types) {
        // remove null members
        Iterator it = types.iterator ();
        while (it.hasNext ()) {
            if (it.next () == null) it.remove ();
        }
        context.addAll (types);
        
        initNodeDecl ();
    }
    
    /** Add new type to collection. */
    public void addType (TreeElementDecl.ContentType type) {
        if (type == null)
            return;
        context.add (type);
        
        initNodeDecl ();
    }
    
    /**
     */
    public Collection getTypes () {
        return context;
    }
    
    /**
     */
    public boolean allowElements () {
        return true;
    }
    
    /**
     */
    public boolean allowText () {
        return false;
    }
    
    /**
     */
    public boolean hasChildren () {
        return context.size () > 0;
    }
    
    /**
     */
    public abstract String getSeparator ();
    
}
