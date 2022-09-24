/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.List;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Debug connector arguments.
 *
 * @author Martin Entlicher
 */
public final class DebugConnector {

    /**
     * The identifier of the connector.
     */
    @NonNull
    private String id;

    /**
     * The display name identifier of the connector.
     */
    @NonNull
    private String name;

    /**
     * The type of the connector.
     */
    @NonNull
    private String type;

    /**
     * The debug connector arguments.
     */
    @NonNull
    private List<String> arguments;

    /**
     * Default values of debug connector arguments.
     */
    @NonNull
    private List<String> defaultValues;

    /**
     * Descriptions of debug connector arguments.
     */
    @NonNull
    private List<String> descriptions;

    public DebugConnector() {
    }

    public DebugConnector(String id, String name, String type, List<String> arguments, List<String> defaultValues, List<String> descriptions) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.arguments = arguments;
        this.defaultValues = defaultValues;
        this.descriptions = descriptions;
    }

    @Pure
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Pure
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Pure
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Pure
    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Pure
    public List<String> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(List<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    @Pure
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.type);
        hash = 37 * hash + Objects.hashCode(this.arguments);
        return hash;
    }

    @Pure
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DebugConnector other = (DebugConnector) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Pure
    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("id", id);
        b.add("name", name);
        b.add("type", type);
        b.add("arguments", arguments.toString());
        b.add("defaultValues", defaultValues.toString());
        b.add("descriptions", descriptions.toString());
        return b.toString();
    }

}
