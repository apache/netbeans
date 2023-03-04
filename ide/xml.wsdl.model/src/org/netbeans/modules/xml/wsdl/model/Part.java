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

package org.netbeans.modules.xml.wsdl.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Represents a message part in the WSDL document
 * @author rico
 * @author Nam Nguyen
 */
public interface Part extends Nameable<WSDLComponent>, ReferenceableWSDLComponent {
    public static final String ELEMENT_PROPERTY = "element";
    public static final String TYPE_PROPERTY = "type";

    /**
     * Sets the element attribute value to a GlobalReference to a schema component 
     * @param elementRef GlobalReference to a schema component
     */
    void setElement(NamedComponentReference<GlobalElement> elementRef);
    
    /**
     * Retrieves the element attribute value. The attribute value is a GlobalReference to
     * a schema component.
     */
    NamedComponentReference<GlobalElement> getElement();
    
    /**
     * Sets the type attribute value to a GlobalReference to a schema component 
     * @param typeRef GlobalReference to a schema component
     */
    void setType(NamedComponentReference<GlobalType> typeRef);
    
    /**
     * Retrieves the type attribute value. The attribute value is a GlobalReference to
     * a schema component.
     */
    NamedComponentReference<GlobalType> getType();

    /**
     * Returns string value of the attribute from different namespace.
     * If given QName has prefix, it will be ignored.
     * @param attr non-null QName represents the attribute name.
     * @return attribute value
     */
    String getAnyAttribute(QName attr);

    /**
     * Set string value of the attribute identified by given QName.
     * This will fire property change event using attribute local name.
     * @param attr non-null QName represents the attribute name.
     * @param value string value for the attribute.
     */
    void setAnyAttribute(QName attr, String value);
}
