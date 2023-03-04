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

import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 * The SchemaModelReference interface is implemented by classes which reference
 * other schema models (Import, Include, and Redefine). This interface provides
 * a uniform way of obtaining the referenced model.
 * @author Chris Webster
 */
public interface SchemaModelReference extends SchemaComponent {
	public static final String SCHEMA_LOCATION_PROPERTY = "schemaLocation";

        // TODO maybe use reference pattern Reference<SchemaModel> getModelReference()
        // issue is Reference.get() cannot throw CatalogModelException, but that fall
        // into the pattern Reference.isBroken(). 
        // Maybe the pattern need Reference.getProblemDescription.
        
        /**
	 * obtain the model for the referenced schema. 
	 * 
	 * @throws CatalogModelException if the referenced model cannot
	 * be created.
	 */
	SchemaModel resolveReferencedModel() throws CatalogModelException;

	
	String getSchemaLocation();
	void setSchemaLocation(String uri);
}
