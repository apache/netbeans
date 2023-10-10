/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.CssModule;

/**
 * Represents a category of css properties.
 *
 * For example: font, background, color, ...
 *
 * In theory the {@link CssModule} could serve as the category identificator.
 * Quite often however the css specification (module) contains groups of
 * logically separated properties - for example the specification "CSS Borders
 * and Backgrounds" provides two groups of properties serving to quite different
 * purpose: border and background;
 *
 * @author marekfukala
 */
//TODO I18N
//TODO defined the displaynames and descriptions
public enum PropertyCategory {

    ALIGNMENT,
    ANIMATIONS,
    BOX,
    BACKGROUND,
    COLORS,
    CONTENT, //generated & repl. content
    CONTAIN,
    FLEXIBLE_BOX_LAYOUT,
    FONTS,
    FRAGMENTATION,
    GRID,
    HYPERLINKS,
    IMAGES,
    LINE,
    LISTS_AND_COUNTERS,
    MARQUEE,
    MULTI_COLUMN_LAYOUT,
    PAGED_MEDIA,
    POSITIONING,
    SIZING,
    RUBY,
    SPEECH,
    TEXT,
    TRANSFORMATIONS_2D,
    TRANSFORMATIONS_3D,
    TRANSITIONS,
    USER_INTERFACE,
    WRITING_MODES,

    //browsers
    CHROME,
    FIREFOX,
    INTERNET_EXPLORER,
    OPERA,
    SAFARI,

    //default, the rest
    DEFAULT,

    //unknown
    UNKNOWN;

    private final String displayName;
    private final String shortDescription;
    private final String longDescription;

    private PropertyCategory() {
        displayName = new StringBuilder()
                .append(name().charAt(0))
                .append(name().substring(1).toLowerCase().replace('_', ' '))
                .toString();

        shortDescription = new StringBuilder()
                .append("Provides styling support for ")
                .append(getDisplayName())
                .append('.')
                .toString();

        longDescription = shortDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

}
