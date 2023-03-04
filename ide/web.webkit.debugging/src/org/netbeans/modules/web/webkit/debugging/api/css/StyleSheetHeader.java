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
 * CSS stylesheet meta-information.
 *
 * @author Jan Stola
 */
public class StyleSheetHeader {
    /** Stylesheet identifier. */
    private final String styleSheetId;
    /** Owner frame identifier. */
    private final String frameId;
    /** Stylesheet resource URL. */
    private final String sourceURL;
    /** Stylesheet origin. */
    private final StyleSheetOrigin origin;
    /** Stylesheet title. */
    private final String title;
    /** Determines whether the stylesheet is disabled. */
    private final boolean disabled;

    /**
     * Creates a new {@code StyleSheetHeader} that corresponds to the given JSONObject.
     *
     * @param header JSONObject describing the stylesheet header.
     */
    StyleSheetHeader(JSONObject header) {
        styleSheetId = (String)header.get("styleSheetId"); // NOI18N
        frameId = (String)header.get("frameId"); // NOI18N
        sourceURL = (String)header.get("sourceURL"); // NOI18N
        String originCode = (String)header.get("origin"); // NOI18N
        origin = StyleSheetOrigin.forCode(originCode); // NOI18N
        title = (String)header.get("title"); // NOI18N
        disabled = (Boolean)header.get("disabled"); // NOI18N
    }

    /**
     * Returns identifier of the stylesheet.
     *
     * @return identifier of the stylesheet.
     */
    public String getStyleSheetId() {
        return styleSheetId;
    }

    /**
     * Returns identifier of the owner frame.
     *
     * @return identifier of the owner frame.
     */
    public String getFrameId() {
        return frameId;
    }

    /**
     * Returns resource URL of the stylesheet.
     *
     * @return resource URL of the stylesheet.
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * Returns origin of the stylesheet.
     *
     * @return origin of the stylesheet.
     */
    public StyleSheetOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns title of the stylesheet.
     *
     * @return title of the stylesheet.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Determines whether the stylesheet is disabled or not.
     *
     * @return {@code true} when the stylesheet is disabled, returns
     * {@code false} otherwise.
     */
    public boolean isDisabled() {
        return disabled;
    }

}
