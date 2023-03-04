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
 * This interface represents commanlity between global and local attributes.
 * @author Chris Webster
 */
public interface Attribute extends SchemaComponent {
        public static final String DEFAULT_PROPERTY = "default";
        public static final String FIXED_PROPERTY = "fixed";
        public static final String TYPE_PROPERTY = "type";
        public static final String INLINE_TYPE_PROPERTY = "inlineType";

	String getDefault();
	void setDefault(String defaultValue);
	
	String getFixed();
	void setFixed(String fixedValue);
	
	enum Use {
            PROHIBITED("prohibited"), OPTIONAL("optional"), REQUIRED("required");
            String value;
            Use(String s) {
                value = s;
            }
            public String toString() { return value; }
	}
}
