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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rico
 * @author Vidhya Narayanan
 */
public abstract class SchemaComponentImpl
        extends AbstractDocumentComponent<SchemaComponent>
        implements SchemaComponent, DocumentModelAccess.NodeUpdater {
    
    public SchemaComponentImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

    @Override
    public SchemaModelImpl getModel() {
        return (SchemaModelImpl) super.getModel();
    }

    @Override
    public abstract Class<? extends SchemaComponent> getComponentType();

    @Override
    protected String getNamespaceURI() {
        return XMLConstants.W3C_XML_SCHEMA_NS_URI;
    }
    
    /**
     * Leave this method as abstract
     */
    @Override
    public abstract void accept(SchemaVisitor v);
    
    protected static Element createNewComponent(SchemaElements type, SchemaModelImpl model) {
        String qualified = "xsd:" + type.getName(); //NOI18N
        return model.getDocument().createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, qualified);
    }

    @Override
    protected void populateChildren(List<SchemaComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    SchemaComponent comp = (SchemaComponent)getModel().getFactory().create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    
    /**
     * @return true if the elements are from the same schema model.
     */
    @Override
    public final boolean fromSameModel(SchemaComponent other) {
        return getModel().equals(other.getModel());
    }
    
    /**
     * Annotation always gets added as the first child.
     */
    @Override
    public void setAnnotation(Annotation annotation) {
        List<Class<? extends SchemaComponent>> types = new ArrayList<Class<? extends SchemaComponent>>(1);
        types.add(SchemaComponent.class);
        setChildBefore(Annotation.class, ANNOTATION_PROPERTY, annotation, types);
    }

    @Override
    public Annotation getAnnotation() {
        List<Annotation> annotations = getChildren(Annotation.class);
        return annotations.isEmpty() ? null : annotations.iterator().next();
    }
    
    /**
     * Returns type of the given attribute.
     * The type should either be:
     * 1. String or wrappers for primitive types (Boolean, Integer,...)
     * 2. An enum with toString() overridden to return string value by XSD specs.
     * 3. java.util.Set
     *
     * @param attribute the attribute enum name
     */
    protected Class getAttributeType(Attribute attribute) {
        return attribute.getType();
    }
    
    /**
     * Returns type of member in cases attribute type is collections.
     */
    protected Class getAttributeMemberType(Attribute attribute) {
        return attribute.getMemberType();
    }

    @Override
    protected Object getAttributeValueOf(Attribute attr, String s) {
        if (s == null) {
            return null;
        }
        Class c = getAttributeType(attr);
        if (String.class.isAssignableFrom(c)) {
            return s;
        } else if (Boolean.class.isAssignableFrom(c)) {
            return Boolean.valueOf(s);
        } else if (Integer.class.isAssignableFrom(c)) {
            return Integer.valueOf(s);
        } else if (Enum.class.isAssignableFrom(c)) {
            Class<Enum> enumClass = (Class<Enum>) c;
            return Util.parse(enumClass, s);
        } else if (Set.class.isAssignableFrom(c)) {
            return Util.valuesOf(getAttributeMemberType(attr), s);
        }
        
        assert(false); // should never reached within this model implementation
        return null;
    }
    
    protected <T extends ReferenceableSchemaComponent> GlobalReferenceImpl<T> resolveGlobalReference(Class<T>c, SchemaAttributes attrName){
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    protected Element checkNodeRef() {
        Element e = (Element)getPeer();
        if (e == null) {
            throw new IllegalArgumentException("Valid Node reference must exist"); // NOI18N
        }
        return e;
    }

    @Override
    public <T extends ReferenceableSchemaComponent> NamedComponentReference<T> 
            createReferenceTo(T referenced, Class<T> type) {
        return getModel().getFactory().createGlobalReference(referenced, type, this);
    }

    @Override
    public void setId(String id) {
        setAttribute(ID_PROPERTY, SchemaAttributes.ID, id);
    }

    @Override
    public String getId() {
        return getAttribute(SchemaAttributes.ID);
    }
    
    protected String getAttributeValue(SchemaAttributes attr) {
        return getAttribute(attr);
    }

    @Override
    public boolean canPaste(Component child) {
        if (! (child instanceof DocumentComponent)) return false;
        return new SyncUpdateVisitor().canAdd(this, (DocumentComponent) child);
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return lookupNamespaceURI(prefix, true);
    }
}

