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
 * Data for a simple selector (these are delimited by commas in a selector list).
 *
 * @author Jan Stola
 */
public class Selector {
    /** Text of the selector. */
    private final String text;
    /** Selector range in the underlying resource (if available). */
    private final SourceRange range;

    /**
     * Creates a new {@code Selector} that corresponds to the given {@code JSONObject}.
     *
     * @param selector JSONObject describing the selector.
     */
    Selector(JSONObject selector) {
        text = (String)selector.get("value"); // NOI18N
        if (selector.containsKey("range")) { // NOI18N
            range = new SourceRange((JSONObject)selector.get("range")); // NOI18N
        } else {
            range = null;
        }
    }

    /**
     * Creates a new {@code Selector} that corresponds to the given {@code String}.
     * 
     * @param selector text of the selector.
     */
    Selector(String selector) {
        text = selector;
        range = null;
    }

    /**
     * Returns the text of the selector.
     * 
     * @return text of the selector.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the range of the selector in the underlying resource.
     * 
     * @return range of the selector if available (returns {@code null} otherwise).
     */
    public SourceRange getRange() {
        return range;
    }

}
