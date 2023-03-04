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

/**
 * This interface represents a choice outside a definition of a group.
 * @author Chris Webster
 */
public interface Choice extends ComplexExtensionDefinition,
ComplexTypeDefinition, SequenceDefinition, LocalGroupDefinition, SchemaComponent {
        public static final String MAX_OCCURS_PROPERTY  = "maxOccurs"; // NOI18N
	public static final String MIN_OCCURS_PROPERTY  = "minOccurs"; // NOI18N
	public static final String CHOICE_PROPERTY          = "choice"; // NOI18N
        public static final String GROUP_REF_PROPERTY       = "groupReference"; // NOI18N
        public static final String SEQUENCE_PROPERTY        = "sequence"; // NOI18N
        public static final String ANY_PROPERTY             = "any"; // NOI18N
        public static final String LOCAL_ELEMENT_PROPERTY   = "localElememnt"; // NOI18N
	public static final String ELEMENT_REFERENCE_PROPERTY = "elementReference"; // NOI18N
              
	Collection<Choice> getChoices();
	void addChoice(Choice choice);
	void removeChoice(Choice choice);
	
	Collection<GroupReference> getGroupReferences();
	void addGroupReference(GroupReference ref);
	void removeGroupReference(GroupReference ref);
	
	Collection<Sequence> getSequences();
	void addSequence(Sequence seq);
	void removeSequence(Sequence seq);
	
	Collection<AnyElement> getAnys();
	void addAny(AnyElement any);
	void removeAny(AnyElement any);
	
	Collection<LocalElement> getLocalElements();
	void addLocalElement(LocalElement element);
	void removeLocalElement(LocalElement element);
	
	Collection<ElementReference> getElementReferences();
	void addElementReference(ElementReference element);
	void removeElementReference(ElementReference element);
	
	/**
	 * return ability to set min and max occurs if appropriate, null 
	 * otherwise. This method
	 * should only be used after insertion into the model. 
	 */ 
	public Cardinality getCardinality();
    
}
