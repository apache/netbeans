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
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author Chris Webster
 */
public interface GlobalElement extends Element, ReferenceableSchemaComponent,
	NameableSchemaComponent, TypeContainer  {
    public static final String FINAL_PROPERTY = "final";
    public static final String ABSTRACT_PROPERTY = "abstract";
    public static final String SUBSTITUTION_GROUP_PROPERTY = "substitutionGroup";
    
    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
   
    
    Boolean isAbstract();
    void setAbstract(Boolean abstr);
    boolean getAbstractDefault();
    boolean getAbstractEffective();
    
    public enum Final implements Derivation {
        ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION),
        EMPTY(Type.EMPTY);
        private Derivation.Type value;
        Final(Derivation.Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    
    Set<Final> getFinal();
    void setFinal(Set<Final> finalValue);
    /**
     * @return either the value of #Schema.getFinalDefaultEffective or empty set.
     */
    Set<Final> getFinalDefault();
    Set<Final> getFinalEffective();
    
    /**
     * The substitution group to which this element belongs
     * @return the substitution group to which this element belongs, or null if
     * the element does not belong to a substitution group
     */
    NamedComponentReference<GlobalElement> getSubstitutionGroup();
    void setSubstitutionGroup(NamedComponentReference<GlobalElement> element);
    
}
