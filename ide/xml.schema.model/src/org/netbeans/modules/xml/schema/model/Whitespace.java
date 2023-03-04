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
 * This interface represents the whitespace facet.
 * @author Chris Webster
 */
public interface Whitespace extends SchemaComponent  {
        public static final String FIXED_PROPERTY = "fixed";
        public static final String VALUE_PROPERTY = "value";

	Boolean isFixed();
	void setFixed(Boolean iFixed);
	boolean getFixedDefault();
        boolean getFixedEffective();

	enum Treatment {
		PRESERVE("preserve"), REPLACE("replace"), COLLAPSE("collapse");
                Treatment(String value) {
                    this.value = value;
                }
                public String toString() {
                    return value;
                }
                private String value;
	}
	
	Treatment getValue();
	void setValue(Treatment whitespaceTreatment);
}
