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
package org.netbeans.modules.xml.axi;

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * A content model represents various content models in XML Schema
 * language, e.g. ComplexType, Group, AttributeGroup etc.
 * These are few constructs for reusability and extension.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ContentModel extends AXIContainer implements AXIType {
    
    /**
     * Various types of content model.
     */
    public static enum ContentModelType {
        COMPLEX_TYPE,
        GROUP,
        ATTRIBUTE_GROUP
    }
    
    /**
     * Creates a new instance of ContentModel
     */
    public ContentModel(AXIModel model, ContentModelType type) {
        super(model);
        this.type = type;
    }
    
    /**
     * Creates a new instance of ContentModel
     */
    public ContentModel(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
        if(schemaComponent instanceof GlobalGroup)
            type = ContentModelType.GROUP;
        if(schemaComponent instanceof GlobalAttributeGroup)
            type = ContentModelType.ATTRIBUTE_GROUP;
        if(schemaComponent instanceof GlobalComplexType)
            type = ContentModelType.COMPLEX_TYPE;
    }

    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the type of this component.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.SHARED;
    }
    
    /**
     * Returns the type of this content model.
     */
    public ContentModelType getType() {
        return type;
    }
    
    public String toString() {
        return getName();
    }

    private ContentModelType type;
    public static final String PROP_CONTENT_MODEL = "contentModel";
}
