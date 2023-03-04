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

import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation.Type;

/**
 * This interface represents a global complex type.
 * @author Chris Webster
 */
public interface GlobalComplexType extends ComplexType, GlobalType,
        SchemaComponent  {

    public static final String ABSTRACT_PROPERTY = "abstract";
    public static final String FINAL_PROPERTY = "final";
    public static final String BLOCK_PROPERTY = "block";

    Boolean isAbstract();
    void setAbstract(Boolean isAbstract);
    boolean getAbstractDefault();
    boolean getAbstractEffective();
    
    public enum Block implements Derivation {
        ALL(Type.ALL), EXTENSION(Type.EXTENSION), RESTRICTION(Type.RESTRICTION), EMPTY(Type.EMPTY);
        private Type value;
        Block(Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
    public enum Final implements Derivation {
        ALL(Type.ALL), EXTENSION(Type.EXTENSION), RESTRICTION(Type.RESTRICTION), EMPTY(Type.EMPTY);
        private Type value;
        Final(Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    
    Set<Final> getFinal();
    void setFinal(Set<Final> finalValue);
    Set<Final> getFinalDefault();
    Set<Final> getFinalEffective();
}
