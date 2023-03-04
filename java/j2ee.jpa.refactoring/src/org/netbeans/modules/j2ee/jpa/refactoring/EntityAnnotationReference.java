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

package org.netbeans.modules.j2ee.jpa.refactoring;

import org.netbeans.api.java.source.TreePathHandle;

/**
 * This class represents an annotation reference to an entity.
 *
 * @author Erno Mononen
 */
public class EntityAnnotationReference {
    
    /**
     * The entity that has the feature with the referencing annotation.
     */
    private final String entity;
    /**
     * The FQN of the referencing annotation.
     */
    private final String annotation;
    /**
     * The referencing annotation attribute.
     */ 
    private final String attribute;
    /**
     * The value for the referencing annotation attribute.
     */ 
    private final String attributeValue;
    /**
     * The handle for the property that has the referencing annotation.
     */ 
    private final TreePathHandle handle;
    /**
     * Creates a new instance of EntityAssociation
     * @param referenced the entity that is referenced.
     * @param referring the entity that has the property with referencing annotation.
     * @param property the property that hat the referencing annotation.
     * @param annotation the referencing annotation
     * @param attributeValue the attribute value of the annotation that references other entity
     */
    public EntityAnnotationReference(String entity, String annotation, 
            String attribute, String attributeValue, TreePathHandle handle) {
        this.entity = entity;
        this.annotation = annotation;
        this.attribute = attribute;
        this.attributeValue = attributeValue;
        this.handle = handle;
    }

    /**
     *@see #entity
     */ 
    public String getEntity() {
        return entity;
    }

    /**
     *@see #annotation
     */ 
    public String getAnnotation() {
        return annotation;
    }

    /**
     *@see #attribute
     */ 
    public String getAttribute() {
        return attribute;
    }

    /**
     *@see #attributeValue
     */ 
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     *@see #handle
     */ 
    public TreePathHandle getHandle() {
        return handle;
    }
    
}
