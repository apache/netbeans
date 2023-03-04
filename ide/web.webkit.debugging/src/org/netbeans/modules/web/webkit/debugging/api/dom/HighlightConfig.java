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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Color;
import org.json.simple.JSONObject;

/**
 * Configuration data for the highlighting of page elements.
 * See {@code DOM.HighlightConfig} in WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class HighlightConfig {
    /** Determines whether the info tooltip should be shown. */
    public boolean showInfo;
    /** Color used to paint the content box, can be {@code null}. */
    public Color contentColor;
    /** Color used to paint the padding, can be {@code null}. */
    public Color paddingColor;
    /** Color used to paint the border, can be {@code null}. */
    public Color borderColor;
    /** Color used to paint the margin, can be {@code null}. */
    public Color marginColor;

    /**
     * Converts this highlight configuration into {@code DOM.HighlightConfig} format.
     * 
     * @return this highlight configuration in {@code DOM.HighlightConfig} format.
     */
    JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("showInfo", showInfo); // NOI18N
        if (contentColor != null) {
            object.put("contentColor", colorToRGBA(contentColor)); // NOI18N
        }
        if (paddingColor != null) {
            object.put("paddingColor", colorToRGBA(paddingColor)); // NOI18N
        }
        if (borderColor != null) {
            object.put("borderColor", colorToRGBA(borderColor)); // NOI18N
        }
        if (marginColor != null) {
            object.put("marginColor", colorToRGBA(marginColor)); // NOI18N
        }
        return object;
    }

    /**
     * Converts the given color into a JSONObject in {@code DOM.RGBA} format.
     * 
     * @param color color to convert.
     * @return color in {@code DOM.RGBA} format.
     */
    static JSONObject colorToRGBA(Color color) {
        JSONObject object = new JSONObject();
        object.put("r", color.getRed()); // NOI18N
        object.put("g", color.getGreen()); // NOI18N
        object.put("b", color.getBlue()); // NOI18N
        if (color.getAlpha() != 255) {
            object.put("a", color.getAlpha()/255.0); // NOI18N
        }
        return object;
    }
    
}
