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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
class ConverterImpl extends IdentifiableDescriptionGroupImpl implements Converter{
    
    protected static final List<String> CONVERTER_SORTED_ELEMENTS = new ArrayList<String>(9);
    static { 
        CONVERTER_SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_ID );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_FOR_CLASS );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_CLASS );
        CONVERTER_SORTED_ELEMENTS.add( ATTRIBUTE );
        CONVERTER_SORTED_ELEMENTS.add( PROPERTY );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_EXTENSION );
    }
    
    /** Creates a new instance of CondverterImpl */
    ConverterImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    ConverterImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.CONVERTER));
    }

    /**
     * Gets converter-class of the faces-config-converterType.
     * @return trimmed converter-class if any, {@code null} otherwise
     */
    public String getConverterClass() {
        String className = getChildElementText(JSFConfigQNames.CONVERTER_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(className);
    }
    
    public void setConverterClass(String value) {
        setChildElementText(CONVERTER_CLASS, value, JSFConfigQNames.CONVERTER_CLASS.getQName(getNamespaceURI()));
    }

    /**
     * Gets converter-for-class of the faces-config-converterType.
     * @return trimmed converter-for-class if any, {@code null} otherwise
     */
    public String getConverterForClass() {
        String className = getChildElementText(JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(className);
    }
    
    public void setConverterForClass(String value) {
        setChildElementText(CONVERTER_FOR_CLASS, value, JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getNamespaceURI()));
    }

    /**
     * Gets converter-id of the faces-config-converterType.
     * @return trimmed converter-id if any, {@code null} otherwise
     */
    public String getConverterId() {
        String converterId = getChildElementText(JSFConfigQNames.CONVERTER_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(converterId);
    }
    
    public void setConverterId(String value) {
        setChildElementText(CONVERTER_ID, value, JSFConfigQNames.CONVERTER_ID.getQName(getNamespaceURI()));
    }
        
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#addConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void addConverterExtension( ConverterExtension extension ) {
        appendChild( CONVERTER_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#addConverterExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void addConverterExtension( int index, ConverterExtension extension )
    {
        insertAtIndex( CONVERTER_EXTENSION, extension, index, ConverterExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#getConverterExtensions()
     */
    public List<ConverterExtension> getConverterExtensions() {
        return getChildren( ConverterExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#removeConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void removeConverterExtension( ConverterExtension extension ) {
        removeChild( CONVERTER_EXTENSION, extension);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return CONVERTER_SORTED_ELEMENTS;
    }

}
