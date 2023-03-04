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

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface represents a reference to a global attribute definition or
 * local definition (cannot be referenced ).
 * @author Chris Webster
 */
public interface LocalAttribute extends Attribute, NameableSchemaComponent, ReferenceableSchemaComponent  {
        public static final String REF_PROPERTY         = "ref";
        public static final String FORM_PROPERTY        = "form";
        public static final String USE_PROPERTY         = "use";
        
	Form getForm();
	void setForm(Form form);
	Form getFormDefault();
	Form getFormEffective();
	
	NamedComponentReference<GlobalSimpleType> getType();
	void setType(NamedComponentReference<GlobalSimpleType> type);
	
	LocalSimpleType getInlineType();
	void setInlineType(LocalSimpleType type);
	
	Use getUse();
	void setUse(Use use);
        Use getUseDefault();
        Use getUseEffective();
	
}
