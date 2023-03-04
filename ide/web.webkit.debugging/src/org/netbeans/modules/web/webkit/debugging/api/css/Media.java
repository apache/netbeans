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
 * Media query descriptor.
 *
 * @author Jan Stola
 */
public class Media {
    /** Media query text. */
    private final String text;
    /** Source of the media query. */
    private final Source source;

    /**
     * Creates a new {@code Media} that corresponds to the given JSONObject.
     *
     * @param media JSONObject describing the media query.
     */
    Media(JSONObject media) {
        text = (String)media.get("text"); // NOI18N
        String codeOfSource = (String)media.get("source"); // NOI18N
        source = Source.forCode(codeOfSource);
    }

    /**
     * Returns the media query text.
     *
     * @return media query text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the source of the media query.
     *
     * @return source of the media query.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Source of the media query.
     */
    public static enum Source {
        /** Specified by a {@code @media} rule. */
        MEDIA_RULE,
        /** Specified by an {@code @import} rule. */
        IMPORT_RULE,
        /** Specified by a {@code media} attribute in a linked stylesheet's {@code LINK} tag. */
        LINKED_SHEET,
        /** Specified by a {@code media} attribute in an inline stylesheet's {@code STYLE} tag. */
        INLINE_SHEET;

        /**
         * Returns the media source for the given code.
         *
         * @param code code of the media source.
         * @return media source matching the given code or {@code null}
         * for an unknown code.
         */
        static Source forCode(String code) {
            Source source = null;
            if ("mediaRule".equals(code)) { // NOI18N
                source = MEDIA_RULE;
            } else if ("importRule".equals(code)) { // NOI18N
                source = IMPORT_RULE;
            } else if ("linkedSheet".equals(code)) { // NOI18N
                source = LINKED_SHEET;
            } else if ("inlineSheet".equals(code)) { // NOI18N
                source = INLINE_SHEET;
            }
            return source;
        }
    }

}
