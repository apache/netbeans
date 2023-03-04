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
 * This interface represents the xs:redefine element.
 * @author Chris Webster
 */
public interface Redefine extends SchemaModelReference  {
	
	public static final String COMPLEX_TYPE_PROPERTY = "complexType";
	public static final String ATTRIBUTE_GROUP_PROPERTY = "attributeGroup";
	public static final String SIMPLE_TYPE_PROPERTY = "simpleType";
	public static final String GROUP_DEFINITION_PROPERTY = "groupDefinition";
	
	Collection<GlobalSimpleType> getSimpleTypes();
	void addSimpleType(GlobalSimpleType type);
	void removeSimpleType(GlobalSimpleType type);
	
	Collection<GlobalComplexType> getComplexTypes();
	void addComplexType(GlobalComplexType type);
	void removeComplexType(GlobalComplexType type);
	
	Collection<GlobalGroup> getGroupDefinitions();
	void addGroupDefinition(GlobalGroup def);
	void removeGroupDefinition(GlobalGroup def);
	
	Collection<GlobalAttributeGroup> getAttributeGroups();
	void addAttributeGroup(GlobalAttributeGroup group);
	void removeAttributeGroup(GlobalAttributeGroup group);
}
