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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.FacesConverter;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "converter" element represents a concrete Converter
 * implementation class that should be registered under the
 * specified converter identifier.  Converter identifiers must
 * be unique within the entire web application.
 * 
 * Nested "attribute" elements identify generic attributes that
 * may be configured on the corresponding UIComponent in order
 * to affect the operation of the Converter.  Nested "property"
 * elements identify JavaBeans properties of the Converter
 * implementation class that may be configured to affect the
 * operation of the Converter.  "attribute" and "property"
 * elements are intended to allow component developers to
 * more completely describe their components to tools and users.
 * These elements have no required runtime semantics.
 * @author Petr Pisl, ads
 */
public interface Converter  extends FacesConfigElement, DescriptionGroup, 
    FacesConverter, IdentifiableElement , AttributeContainer, PropertyContainer
{

    String CONVERTER_CLASS = JSFConfigQNames.CONVERTER_CLASS.getLocalName();
    
    String CONVERTER_FOR_CLASS = JSFConfigQNames.CONVERTER_FOR_CLASS.getLocalName();
    
    String CONVERTER_ID = JSFConfigQNames.CONVERTER_ID.getLocalName();
    
    String CONVERTER_EXTENSION = JSFConfigQNames.CONVERTER_EXTENSION.getLocalName();
    
    void setConverterClass(String value);
    
    void setConverterForClass(String value);
    
    void setConverterId(String value);
    
    List<ConverterExtension> getConverterExtensions();
    void addConverterExtension( ConverterExtension extension );
    void addConverterExtension( int index, ConverterExtension extension );
    void removeConverterExtension( ConverterExtension extension );
}
