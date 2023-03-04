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

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Derivation.Type;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Chris Webster
 */
public interface Element extends SchemaComponent{

    public static final String BLOCK_PROPERTY = "block";
    public static final String DEFAULT_PROPERTY = "default";
    public static final String FIXED_PROPERTY = "fixed";
    public static final String NILLABLE_PROPERTY = "nillable";
    public static final String CONSTRAINT_PROPERTY = "constraint";
    public static final String REF_PROPERTY = "ref";
    
    public enum Block implements Derivation {
        ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION), SUBSTITUTION(Type.SUBSTITUTION), EMPTY(Type.EMPTY);
        private final Type value;
        Block(Type value) { this.value = value; }
        public String toString() { return value.toString(); }
    }
    
    
    String getDefault();
    void setDefault(String defaultValue);
    
    String getFixed();
    void setFixed(String fixed);
    
    Boolean isNillable();
    void setNillable(Boolean nillable);
    boolean getNillableDefault();
    boolean getNillableEffective();
    
    Collection<Constraint> getConstraints();
    void addConstraint(Constraint c);
    void removeConstraint(Constraint c);
}
