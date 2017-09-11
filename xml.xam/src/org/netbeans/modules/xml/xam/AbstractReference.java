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

package org.netbeans.modules.xml.xam;

/**
 * Represents reference to a component.  On writing, this indirection help serialize
 * the referenced component as an attribute string value.  On reading, the referenced
 * can be resolved on demand.
 * <p>
 * Note: Client code should always check for brokeness before access the referenced.
 *
 * @author rico
 * @author Nam Nguyen
 * @author Chris Webster
 */
public abstract class AbstractReference<T extends Referenceable> implements Reference<T> {

    private T referenced;
    private Class<T> classType;
    private AbstractComponent parent;
    protected String refString;
    
    /**
     * Constructor for writing.
     * @param referenced the component being referenced
     * @param referencedType type of the referenced component
     * @param parent referencing component on which the referenced is serialized 
     * as an attribute string value.
     */
    public AbstractReference(T referenced, Class<T> referencedType, AbstractComponent parent) {
        if (referenced == null) {
            throw new IllegalArgumentException("Referenced component null"); //NOI18N
        }
        checkParentAndType(parent, referencedType);
        this.referenced = referenced;
        this.classType = referencedType;
        this.parent = parent;
    }
    
    /**
     * Constructor for reading.
     * @param referencedType type of the referenced component
     * @param parent referencing component on which the referenced is serialized 
     * as an attribute string value.
     * @param ref the string value used in resolving.
     */
    public AbstractReference(Class<T> referencedType, AbstractComponent parent, String ref){
        checkParentAndType(parent, referencedType);
        this.refString = ref;
        this.classType = referencedType;
        this.parent = parent;
    }

    /**
     * Access method for referenced.
     */
    protected T getReferenced() {
        return referenced;
    }
    /**
     * Accessor method for referenced.
     */
    protected void setReferenced(T referenced) {
        this.referenced = referenced;
    }

    /**
     * Returns type of the referenced component
     */
    public Class<T> getType() {
        return classType;
    }
    
    /**
     * Returns true if the reference cannot be resolved in the current document
     */
    public boolean isBroken() {
        try {
            return get() == null;
        } catch(IllegalStateException ise) {
            referenced = null;
            return false;
        }
    }
    
    /**
     * Returns true if this reference refers to the target component. 
     */
    public boolean references(T target) {
        return get() == target;
    }
    
    /**
     * @return string to use in persiting the reference as attribute value of 
     * the containing component
     */
    public String getRefString() {
        return refString;
    }

    @Override
    public String toString() {
        return getRefString();
    }

    protected AbstractComponent getParent() {
        return parent;
    }
    
    private void checkParentAndType(AbstractComponent parent, Class<T> classType) {
        if (parent == null) {
            throw new IllegalArgumentException("parent == null"); //NOI18N
        }
        if (classType == null)  {
            throw new IllegalArgumentException("classType == null"); //NOI18N
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof AbstractReference) {
            AbstractReference<T> that = (AbstractReference<T>) obj;
            return refString.equals(that.getRefString()) && 
                   parent.equals(that.parent) &&
                   getType().equals(that.getType());
        } else {
            return super.equals(obj);
        }
    }
    
    @Override
    public int hashCode() {
        return parent.hashCode();
    }
    
}
