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

package org.netbeans.modules.xml.xam.dom;

import org.netbeans.modules.xml.xam.NamedReferenceable;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.AbstractReference;

/**
 * Abstract implementation of reference by name to a component.  
 * On writing, this indirection help serialize the referenced component as an 
 * attribute string value.  On reading, the referenced can be resolved on demand.
 * <p>
 * Note: Client code should always check for brokeness before access the referenced.
 *
 * @author rico
 * @author Nam Nguyen
 * @author Chris Webster
 */
public abstract class AbstractNamedComponentReference<T extends NamedReferenceable>
        extends AbstractReference<T> implements NamedComponentReference<T> {

    protected String prefix;
    protected String localName;
    protected QName qname;
    
    /**
     * Constructor for writing.
     * @param referenced the component being referenced
     * @param referencedType type of the referenced component
     * @param parent referencing component on which the referenced is serialized 
     * as an attribute string value.
     */
    public AbstractNamedComponentReference(T referenced, Class<T> referencedType, AbstractDocumentComponent parent) {
        super(referenced, referencedType, parent);
    }
    
    /**
     * Constructor for reading.
     * @param referencedType type of the referenced component
     * @param parent referencing component on which the referenced is serialized 
     * as an attribute string value.
     * @param ref the string value used in resolving.
     */
    public AbstractNamedComponentReference(Class<T> referencedType, AbstractDocumentComponent parent, String ref){
        super(referencedType, parent, ref);
        initReferenceString(ref);
    }
    
    /**
     * Return true if this reference refers to target. This method is more
     * efficient than invoking get
     */
    public boolean references(T target) {
        return target.getName() != null &&
                target.getName().equals(getLocalName()) &&
                ! isBroken() &&
                get() == target;
    }
    
    /**
     * @return string to use in persiting the reference as attribute value of 
     * the containing component
     */
    public synchronized String getRefString() {
        if (refString == null) {
            T referencedT = super.getReferenced();
            assert referencedT != null;
            String namespace = getEffectiveNamespace();
            prefix = getParent().lookupPrefix(namespace);
            if (namespace != null && prefix == null && referencedT instanceof AbstractDocumentComponent) {
                AbstractDocumentComponent target = (AbstractDocumentComponent) referencedT;
                prefix = target.lookupPrefix(namespace);
                if (prefix != null) {
                    getParent().addPrefix(prefix, namespace);
                } else {
                    //TODO investigate; this happen when apply design pattern case
                    //Thread.dumpStack();
                }
            }
            localName = super.getReferenced().getName();
            if (prefix == null || prefix.length() == 0) {
                refString = localName;
            } else {
                refString = prefix + ":" + localName; //NOI18N
            }
        }
        return refString;
    }
    
    private void initReferenceString(String ref) {
        if (ref == null) {
            throw new IllegalArgumentException("Reference string null"); //NOI18N
        }
        refString = ref;
        String[] parts = refString.split(":"); //NOI18N
        if (parts.length == 2) {
            prefix = parts[0];
            localName = parts[1];
        } else {
            prefix = null;
            localName = parts[0];
        }
    }
    
    protected String getPrefix() {
        getRefString();
        return prefix;
    }
    
    protected String getLocalName() {
        getRefString();
        return localName;
    }
    
    protected T getReferenced() {
        if (super.getReferenced() == null) {
            checkParentPartOfModel();
        } else {
            if (super.getParent().getModel() == null) {
                throw new IllegalStateException("Referencing component has been removed from model."); //NOI18N
            }
            if (super.getReferenced().getModel() == null) {
                throw new IllegalStateException("Referenced component has been removed from model."); //NOI18N
            }
        }
        return super.getReferenced();
    }
    
    /**
     * Note this method will first attempt to build the QName base on local lookup
     * before trying to resolve the referenced.  Subclasses need to override
     * if local calculation is not desirable.
     * @exception IllegalStateException if referencing component is discarded from
     * the model.
     */
    public synchronized QName getQName() {
        checkParentNotRemovedFromModel();
        if (qname == null) {
           if (super.getReferenced() == null) {
               qname = calculateQNameLocally();
           } 
           
           if (qname == null && ! isBroken()) {
               qname = new QName(getEffectiveNamespace(), get().getName());
           } 
           
           if (qname == null) {
               return new QName("");
           }
        }
        return qname;
    }
    
    /**
     * Returns parent referencing component.
     */
    protected AbstractDocumentComponent getParent() {
        return (AbstractDocumentComponent) super.getParent();
    }
    
    /**
     * @exception IllegalStateException if parent is not part of a model.
     */
    protected void checkParentPartOfModel() {
        if (! getParent().isInDocumentModel()) {
            throw new IllegalStateException("Referencing component is not part of model."); //NOI18N
        }
    }
    
    /**
     * @exception IllegalStateException if parent is already removed from a model.
     */
    protected void checkParentNotRemovedFromModel() {
        if (getParent().getModel() == null) {
            throw new IllegalStateException("Referencing component has been removed from model."); //NOI18N
        }
    }
    
   /**
    * Calculate the QName based on the local information
    * without loading the referenced object.
    */
    protected QName calculateQNameLocally() {
        assert localName != null;
        String namespace = null;
            namespace = getParent().lookupNamespaceURI(prefix);
            if (namespace == null) {
               //prefix part is namespace name, which could be the namespace uri itself
               String temp = getParent().lookupPrefix(prefix);
               if (temp != null) {
                   prefix = temp;
                   namespace = prefix;
               }
           }

       if (prefix == null) {
           return new QName(namespace, localName);
       } else {
           return new QName(namespace, localName, prefix);
       }
    }
    
    @Override
            public boolean equals(Object obj) {
        return (this == obj) || (obj != null &&
                getClass().equals(obj.getClass()) &&
                getParent().equals(((AbstractNamedComponentReference) obj).getParent()) &&
                getQName().equals(((AbstractNamedComponentReference)obj).getQName()));
    }
    
    @Override
            public int hashCode() {
        return getParent().hashCode();
    }
    
    public synchronized void refresh() {
        getRefString();
        setReferenced(null);
    }
}
