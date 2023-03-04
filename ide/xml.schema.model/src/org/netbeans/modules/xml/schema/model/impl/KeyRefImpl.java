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
package org.netbeans.modules.xml.schema.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
/**
 *
 * @author Vidhya Narayanan
 * @author rico
 */
public class KeyRefImpl extends ConstraintImpl implements KeyRef {
    
    public KeyRefImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEYREF,model));
    }
    
    /**
     * Creates a new instance of KeyRefImpl
     */
    public KeyRefImpl(SchemaModelImpl model, org.w3c.dom.Element el) {
        super(model, el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return KeyRef.class;
    }
    
    /**
     *
     */
    public void setReferer(Constraint c) {
        this.setAttribute(REFERER_PROPERTY, SchemaAttributes.REFER,
                c==null?null:new ConstraintWrapper(c));
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    
    /**
     *
     */
    public Constraint getReferer() {
        String referValue = this.getAttribute(SchemaAttributes.REFER);
        if(referValue == null)
             return null;
        //remove prefix, if any
        String localName = getLocalName(referValue);
        SchemaComponent parent = findOutermostParentElement();
        FindReferredConstraintVisitor visitor = 
                new FindReferredConstraintVisitor();
        
        return visitor.findReferredConstraint(parent, localName);
    }
    
    /**
     * Adapter class to enable the use of Constraint in setAttribute()
     */
    private static class ConstraintWrapper{
        private Constraint c;
        
        public ConstraintWrapper(Constraint c){
            this.c = c;
        }
        
        public String toString(){
            return c.getName();
        }
    }
    
    private String getLocalName(String uri) {
        String localName = null;
        try {
            URI u = new URI(uri);
            localName = u.getSchemeSpecificPart();
        } catch (URISyntaxException ex) {
        }
        return localName;
    }
    
    /**
     * Look for the outermost <element> that encloses this keyRef. This is 
     * required to determine the effective scope where valid keys and uniques
     * may be obtained. That is, the refer attribute may only refer to keys or
     * uniques that are contained within the same element scope.
     */
    private SchemaComponent findOutermostParentElement(){
        SchemaComponent element = null;
        //go up the tree and look for the last instance of <element>
	SchemaComponent sc = getParent();
        while(sc != null){
            if(sc instanceof Element){
                element = sc;
            }
	    sc = sc.getParent();
        }
        return element;
    }
}
