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

import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Information about a supported CSS property.
 *
 * @author Jan Stola
 */
public class PropertyInfo {
    /** Property name. */
    private final String name;
    /** Longhand property names. */
    private final List<String> longhands;

    /**
     * Creates a new {@code PropertyInfo} that corresponds to the given JSONObject.
     *
     * @param propertyInfo JSONObject describing the property.
     */
    PropertyInfo(JSONObject propertyInfo) {
        name = (String)propertyInfo.get("name"); // NOI18N
        JSONArray longHandsArray = (JSONArray)propertyInfo.get("longhands"); // NOI18N
        if (longHandsArray == null) {
            longhands = Collections.emptyList();
        } else {
            longhands = (List<String>)longHandsArray;
        }
    }

    /**
     * Creates a new empty {@code PropertyInfo} for a property
     * with the specified name.
     *
     * @param name name of a CSS property.
     */
    PropertyInfo(String name) {
        this.name = name;
        this.longhands = Collections.emptyList();
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
     * Returns longhand property names.
     *
     * @return longhand property names.
     */
    public List<String> getLonghands() {
        return Collections.unmodifiableList(longhands);
    }

}
