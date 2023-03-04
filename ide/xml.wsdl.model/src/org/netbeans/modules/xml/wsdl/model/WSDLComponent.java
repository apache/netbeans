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

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 * Base interface of all WSDL components
 */
public interface WSDLComponent extends DocumentComponent<WSDLComponent> {
    public static final String DOCUMENTATION_PROPERTY = "documentation"; //NOI18N
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement";

    /**
     * @return WSDL model.
     */
    WSDLModel getModel();
    
    void accept(WSDLVisitor visitor);
    
    void setDocumentation(Documentation doc);
    Documentation getDocumentation();
    
    /**
     * Creates a global reference to the given target WSDL component.
     * @param target the target WSDLComponent
     * @param type actual type of the target
     * @return the global reference.
     */
    <T extends ReferenceableWSDLComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type);
    
    /**
     * Creates a GlobalReference to a Schema component
     * @param target The schema component that is being referenced.
     * @param type Class object of the schema component
     */
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> 
            createSchemaReference(T target, Class<T> type);

    void addExtensibilityElement(ExtensibilityElement ee);
    void removeExtensibilityElement(ExtensibilityElement ee);
    List<ExtensibilityElement> getExtensibilityElements();
    
    <T extends ExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);

    /**
     * Returns map of attribute names and string values.
     */
    Map<QName,String> getAttributeMap();
    
}
