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

/**
 * This interface represents a local element.
 * @author Chris Webster
 */
public interface LocalElement extends Element, SequenceDefinition,
	SchemaComponent, NameableSchemaComponent, TypeContainer, ReferenceableSchemaComponent {
    public static final String MIN_OCCURS_PROPERTY = "minOccurs";
    public static final String MAX_OCCURS_PROPERTY = "maxOccurs";
    public static final String FORM_PROPERTY = "form"; //NOI18N

    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
    Form getForm();
    void setForm(Form form);
    Form getFormDefault();
    Form getFormEffective();
    
    /**
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside 
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model. 
     */
    boolean allowsFullMultiplicity();
    
    String getMaxOccurs();
    void setMaxOccurs(String max);
    String getMaxOccursDefault();
    String getMaxOccursEffective();
    
    Integer getMinOccurs();
    void setMinOccurs(Integer min);
    int getMinOccursDefault();
    int getMinOccursEffective();
    
}
