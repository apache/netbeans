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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * CSS style.
 *
 * @author Jan Stola
 */
public class Style {
    /** Identifier of the style. */
    private final StyleId id;
    /** Properties in the style. */
    private final List<Property> properties;
    /** Style declaration text (if available). */
    private final String text;
    /** Style declaration range in the enclosing stylesheet (if available). */
    private SourceRange range;

    /**
     * Creates a new {@code Style} that corresponds to the given JSONObject.
     *
     * @param style JSONObject describing the style.
     */
    Style(JSONObject style) {
        this(style, null);
    }

    Style(JSONObject style, String preferredId) {
        if (preferredId != null) {
            id = new StyleId(preferredId);
        } else {
            if (style.containsKey("styleId")) { // NOI18N
                id = new StyleId((JSONObject)style.get("styleId")); // NOI18N
            } else if (style.containsKey("styleSheetId")) { // NOI18N
                id = new StyleId((String)style.get("styleSheetId")); // NOI18N
            } else {
                id = null;
            }
        }
        JSONArray cssProperties = (JSONArray)style.get("cssProperties"); // NOI18N
        properties = new ArrayList<Property>(cssProperties.size());
        for (Object o : cssProperties) {
            JSONObject cssProperty = (JSONObject)o;
            Property property = new Property(cssProperty);
            properties.add(property);
        }
        text = (String)style.get("cssText"); // NOI18N
        if (style.containsKey("range")) { // NOI18N
            range = new SourceRange((JSONObject)style.get("range")); // NOI18N
        }
    }

    /**
     * Returns the identifier of the style.
     *
     * @return identifier of the style.
     */
    public StyleId getId() {
        return id;
    }

    /**
     * Returns the properties in the style.
     *
     * @return properties in the style.
     */
    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns the style declaration text.
     *
     * @return style declaration text or {@code null} if it is not available.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the style declaration range in the enclosing stylesheet.
     *
     * @return style declaration range in the enclosing stylesheet or
     * {@code null} if this information is not available.
     */
    public SourceRange getRange() {
        return range;
    }

}
