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

import org.netbeans.tax.*;

/** Reference to other declared element.
 * It links itself to context that element.
 */
public class NameType extends LeafType {

    /** */
    public static final String PROP_TYPE_NAME = "nt-name"; // NOI18N

    /** */
    private String name;


    //
    // init
    //

    public NameType (String name, String mul) {
        super ();

        this.name = name;
        setMultiplicity (mul);
    }

    public NameType (String name) {
        this (name, ""); // NOI18N
    }
    
    public NameType (NameType nameType) {
        super (nameType);
        
        this.name = nameType.name;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new NameType (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        NameType peer = (NameType) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        
        return true;
    }
    
    /*
     * Merges changes from passed object to actual object.
     * @param node merge peer (TreeAttributeDecl)
     * @throws CannotMergeException if can not merge with given node (invalid class)
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        NameType peer = (NameType) treeObject;
        
        // just become peer
        
        setName (peer.getName ());
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public String getName () {
        return name;
    }
    
    /**
     */
    public void setName (String name) {
        if (Util.equals (this.name, name))
            return;
        this.name = name;
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[NameType] firePropertyChange(PROP_TYPE_NAME, name);"); // NOI18N
    }
    
    /**
     */
    public String toString () {
        return name + getMultiplicity ();
    }
    
    
    /**
     */
    public boolean allowElements () {
        return false; //??? should it report TreeElementDecl.forName(name);
    }
    
    /**
     */
    public boolean allowText () {
        return false; //??? should it report TreeElementDecl.forName(name);
    }
    
}
