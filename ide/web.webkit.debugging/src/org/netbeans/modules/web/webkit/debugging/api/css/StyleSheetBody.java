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
 * Content of CSS stylesheet.
 *
 * @author Jan Stola
 */
public class StyleSheetBody {
    /** Identifier of the stylesheet. */
    private final String styleSheetId;
    /** Rules of the stylesheet. */
    private final List<Rule> rules;
    /** Text of the stylesheet (if available). */
    private final String text;
    /** Header of the style-sheet. */
    private final StyleSheetHeader styleSheetHeader;

    /**
     * Creates a new {@code StyleSheetBody} that corresponds to the given JSONObject.
     *
     * @param header header of the style-sheet.
     * @param body JSONObject describing the body of the stylesheet.
     */
    StyleSheetBody(StyleSheetHeader header, JSONObject body) {
        styleSheetHeader = header;
        styleSheetId = (String)body.get("styleSheetId"); // NOI18N
        JSONArray cssRules = (JSONArray)body.get("rules"); // NOI18N
        rules = new ArrayList<Rule>(cssRules.size());
        for (Object o : cssRules) {
            JSONObject cssRule = (JSONObject)o;
            Rule rule = new Rule(cssRule);
            rule.setParentStyleSheet(this);
            rules.add(rule);
        }
        text = (String)body.get("text"); // NOI18N
    }

    /**
     * Creates a new {@code StyleSheetBody}.
     *
     * @param header header of the style-sheet.
     * @param text text of the style-sheet.
     */
    StyleSheetBody(StyleSheetHeader header, String text) {
        this.styleSheetHeader = header;
        this.styleSheetId = header.getStyleSheetId();
        this.text = text;
        this.rules = Collections.emptyList();
    }

    /**
     * Returns the identifier of the stylesheet.
     *
     * @return identifier of the stylesheet.
     */
    public String getStyleSheetId() {
        return styleSheetId;
    }

    /**
     * Returns the header of the style-sheet.
     * 
     * @return header of the style-sheet.
     */
    public StyleSheetHeader getHeader() {
        return styleSheetHeader;
    }

    /**
     * Returns the rules of the stylesheet.
     *
     * @return
     */
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Returns the text of the stylesheet.
     *
     * @return text of the stylesheet or {@code null} if it is not available.
     */
    public String getText() {
        return text;
    }

}
