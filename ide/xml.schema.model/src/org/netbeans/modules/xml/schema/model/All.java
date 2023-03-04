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
 * This interface represents the xml schema all type. The all
 * type describes an unordered group of elements.
 * @author Chris Webster
 */
public interface All extends ComplexExtensionDefinition, ComplexTypeDefinition,
    LocalGroupDefinition,SchemaComponent {

	public static final String ELEMENT_PROPERTY = "element"; //NOI18N
	public static final String ELEMENT_REFERENCE_PROPERTY = "elementReference"; //NOI18N
        public static final String MIN_OCCURS_PROPERTY = "minOccurs"; //NOI18N
        
	
	/**
	 * true if #getMinOccurs() allows optional multiplicity, false otherwise.
	 * This method is only accurate after the element has been inserted into the model.
	 */
	boolean allowsFullMultiplicity();
	
        /**
	 * @return minimum occurrences, must be 0 <= x <= 1
	 */
	Occur.ZeroOne getMinOccurs();
	
	/**
	 * set the minimum number of occurs. 
	 * @param occurs must satisfy 0 <= occurs <= 1
	 */
	void setMinOccurs(Occur.ZeroOne occurs);
        
        /**
         * Returns default values for attribute minOccurs.
         */
        Occur.ZeroOne getMinOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        Occur.ZeroOne getMinOccursEffective();
	
	Collection<LocalElement> getElements();
        void addElement(LocalElement element);
        void removeElement(LocalElement element);
	
	Collection<ElementReference> getElementReferences();
        void addElementReference(ElementReference element);
        void removeElementReference(ElementReference element);
}
