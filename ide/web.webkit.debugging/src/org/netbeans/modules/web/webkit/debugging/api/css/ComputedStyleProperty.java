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
package org.netbeans.modules.web.webkit.debugging.api.css;

import org.json.simple.JSONObject;

/**
 * Computed CSS property.
 *
 * @author Jan Stola
 */
public class ComputedStyleProperty {
    /** Name of the property. */
    private final String name;
    /** Value of the property. */
    private final String value;

    /**
     * Creates a new {@code ComputedStyleProperty} that corresponds to the given JSONObject.
     *
     * @param property JSONObject describing the property.
     */
    ComputedStyleProperty(JSONObject property) {
        name = (String)property.get("name"); // NOI18N
        value = (String)property.get("value"); // NOI18N
    }

    /**
     * Returns the name of the property.
     *
     * @return name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     *
     * @return value of the property.
     */
    public String getValue() {
        return value;
    }

}
