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
package org.netbeans.modules.css.lib.api;

/**
 * Representation of an error in css source.
 *
 * @author marekfukala
 */
public final class ProblemDescription {
    
    public enum Type {
        INFO, WARNING, ERROR, FATAL;
    }
    
    public enum Keys {
        LEXING, PARSING, AST;
    }
    
    private final int from, to;
    private final String description;
    private final String key;
    private Type type;
    
    public ProblemDescription(int from, int to, String description, String key, Type type) {
        this.from = from;
        this.to = to;
        this.description = description;
        this.key = key;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public int getFrom() {
        return from;
    }

    public String getKey() {
        return key;
    }

    public int getTo() {
        return to;
    }

    public Type getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "ProblemDescription{" + "from=" + from + ", to=" + to + ", description=" + description + ", key=" + key + ", type=" + type + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProblemDescription other = (ProblemDescription) obj;
        if (this.from != other.from) {
            return false;
        }
        if (this.to != other.to) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.from;
        hash = 19 * hash + this.to;
        hash = 19 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 19 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 19 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
    
    
    
}
