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

/**
 * AnyElement represents a wildcard that allows the insertion of any element belonging
 * to a list of namespaces.
 *
 * @author Chris Webster
 */
public interface AnyElement extends Any, SequenceDefinition, SchemaComponent {
	
	public static final String MIN_OCCURS_PROPERTY = "minOccurs";
	public static final String MAX_OCCURS_PROPERTY = "maxOccurs";
	
	String getMaxOccurs();
	void setMaxOccurs(String occurs);

	Integer getMinOccurs();
	void setMinOccurs(Integer occurs);

        /**
         * Returns default values for attribute minOccurs.
         */
        int getMinOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        int getMinOccursEffective();
	
        /**
         * Returns default values for attribute minOccurs.
         */
        String getMaxOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        String getMaxOccursEffective();
	
}
