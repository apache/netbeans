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

package org.netbeans.modules.xml.schema.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface represents a common interface shared by all schema elements.
 * @author Chris Webster
 */
public interface SchemaComponent extends DocumentComponent<SchemaComponent> {

    // TODO Should there be a resolve capability and expose uri for references
    public static final String ANNOTATION_PROPERTY = "annotation";
    public static final String ID_PROPERTY = "id";
    
    /**
     * @return the schema model this component belongs to.
     */
    SchemaModel getModel();
    
    /**
     * @return schema component 'id' attribute if presents, null otherwise.
     */
    String getId();
    
    /**
     * Set the schema component 'id' attribute value.
     */
    void setId(String id);
    
    /**
     * Returns value of an attribute defined in a certain namespace.
     */
    String getAnyAttribute(QName attributeName);
    
    /**
     * Sets value of an attribute defined in a certain namespace.
     * Propery change event will be fired with property name using attribute local name.
     */
    void setAnyAttribute(QName attributeName, String value);
    
    /**
     **/
    public Annotation getAnnotation();
    
    /**
     **/
    public void setAnnotation(Annotation annotation);
    
    /**
     * Visitor providing
     */
    void accept(SchemaVisitor visitor);
    
    /**
     * @return true if the elements are from the same schema model.
     */
    boolean fromSameModel(SchemaComponent other);
    
    /**
     * Returns the type of the component in terms of the schema model interfaces
     *
     */
    Class<? extends SchemaComponent> getComponentType();
	
    /**
     * Creates a global reference to the given target Schema component.
     * @param referenced the schema component being referenced.
     * @param type actual type of the target
     * @return the reference.
     */
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> createReferenceTo(T referenced, Class<T> type);
}
