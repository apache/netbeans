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
