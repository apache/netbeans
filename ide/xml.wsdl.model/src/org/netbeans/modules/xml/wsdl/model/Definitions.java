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

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author rico
 * Represents the WSDL definitions section
 */
public interface Definitions extends Nameable<WSDLComponent>, WSDLComponent {
    public static String IMPORT_PROPERTY = "import";
    public static String BINDING_PROPERTY = "binding";
    public static String TYPES_PROPERTY = "types";
    public static String MESSAGE_PROPERTY = "message";
    public static String PORT_TYPE_PROPERTY = "portType";
    public static String SERVICE_PROPERTY = "service";
    public static String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
    
    void addImport(Import importDefinition);
    void removeImport(Import importDefinition);
    Collection<Import> getImports();
    
    void setTypes(Types types);
    Types getTypes();
    
    void addMessage(Message message);
    void removeMessage(Message message);
    Collection<Message> getMessages();
    
    void addPortType(PortType portType);
    void removePortType(PortType portType);
    Collection<PortType> getPortTypes();
    
    void addBinding(Binding binding);
    void removeBinding(Binding binding);
    Collection<Binding> getBindings();
    
    void addService(Service service);
    void removeService(Service service);
    Collection<Service> getServices();
    
    String getTargetNamespace();
    void setTargetNamespace(String uri);
    
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
