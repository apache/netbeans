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
