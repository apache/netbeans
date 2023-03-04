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

import java.util.Objects;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor2;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;

/**
 * Represents a xs:include or xs:import declaration, a reference to another
 * XML Schema.
 * @author sdedic
 * @since 1.33
 */
public class SchemaReference extends AXIComponent {
    public static final String PROP_TARGET_NAMESPACE = "targetNamespace";   // NOI18N
    public static final String PROP_SCHEMA_LOCATION = "schemaLocation";   // NOI18N
    
    private final boolean include;
    
    /**
     * Target namespace. Valid only for imports, must be null for imports
     */
    private String  targetNamespace;
    
    /**
     * Optional schema location
     */
    private String  schemaLocation;
    
    public SchemaReference(AXIModel model, SchemaModelReference schemaComponent) {
        super(model, schemaComponent);
        this.include = schemaComponent instanceof Include;
    }

    public SchemaReference(AXIModel model, boolean include) {
        super(model);
        this.include = include;
    }

    public SchemaReference(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
        assert sharedComponent instanceof SchemaReference;
        include = ((SchemaReference)sharedComponent).isInclude();
    }
    
    public boolean isInclude() {
        return include;
    }
    
    public boolean isImport() {
        return !include;
    }

    @Override
    public void accept(AXIVisitor visitor) {
        if (visitor instanceof AXIVisitor2) {
            ((AXIVisitor2)visitor).visit(this);
        }
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (isInclude() && targetNamespace != null) {
            throw new IllegalArgumentException("xs:include does not support targetNamespace");
        }
        String old = getTargetNamespace();
        if (Objects.equals(old, targetNamespace)) {
            return;
        }
        this.targetNamespace = targetNamespace;
        firePropertyChange(PROP_TARGET_NAMESPACE, old, targetNamespace);
    }

    public void setSchemaLocation(String schemaLocation) {
        String old = getSchemaLocation();
        if (Objects.equals(old, schemaLocation)) {
            return;
        }
        this.schemaLocation = schemaLocation;
        firePropertyChange(PROP_SCHEMA_LOCATION, old, targetNamespace);
    }
}
