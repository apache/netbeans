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
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractAttribute extends AXIComponent {
    
    /**
     * Creates a new instance of Attribute
     */
    public AbstractAttribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Attribute
     */
    public AbstractAttribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Attribute.
     */
    public AbstractAttribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Attribute.
     */
    public abstract void accept(AXIVisitor visitor);
    
    /**
     * Returns the name.
     */
    public abstract String getName();
    
    public static final String PROP_ATTRIBUTE       = "attribute"; //NOI18N
}
