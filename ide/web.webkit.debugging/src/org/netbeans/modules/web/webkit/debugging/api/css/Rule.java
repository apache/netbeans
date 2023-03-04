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
 * CSS Rule.
 *
 * @author Jan Stola
 */
public class Rule {
    /** Identifier of the rule (absent for user agent stylesheet and user-specified stylesheet rules). */
    private RuleId id;
    /** Selector of the rule. */
    private String selector;
    /** Selectors of the rule. */
    private List<Selector> selectors;
    /** Parent stylesheet resource URL. */
    private final String sourceURL;
    /** Line number of the first character of the selector. */
    private final int sourceLine;
    /** Origin of the parent stylesheet. */
    private final StyleSheetOrigin origin;
    /** Associated style declaration. */
    private final Style style;
    /** Rule selector range in the underlying resource (if available). */
    private SourceRange selectorRange;
    /**
     * Media list (for rules involving media queries). It enumerates media
     * queries starting with the innermost one, going outwards.
     */
    private final List<Media> media;
    /** Parent stylesheet of the rule. */
    private StyleSheetBody parentStyleSheet;
    /** JSON object this rule is based on. */
    private final JSONObject json;

    /**
     * Creates a new {@code Rule} that corresponds to the given JSONObject.
     *
     * @param rule JSONObject describing the rule.
     */
    Rule(JSONObject rule) {
        this.json = rule;
        // Determines whether the given rule object is CSS.RuleMatch or CSS.CSSRule.
        boolean isRuleMatch = rule.containsKey("rule"); // NOI18N
        if (isRuleMatch) {
            rule = (JSONObject)json.get("rule"); // NOI18N
        }
        if (rule.containsKey("ruleId")) { // NOI18N
            id = new RuleId((JSONObject)rule.get("ruleId")); // NOI18N
        } else if (rule.containsKey("styleSheetId")) { // NOI18N
            id = new RuleId((String)rule.get("styleSheetId")); // NOI18N
        }
        sourceURL = (String)rule.get("sourceURL"); // NOI18N
        if (rule.containsKey("sourceLine")) { // NOI18N
            sourceLine = ((Number)rule.get("sourceLine")).intValue(); // NOI18N
        } else {
            sourceLine = -1;
        }
        String originCode = (String)rule.get("origin"); // NOI18N
        origin = StyleSheetOrigin.forCode(originCode);
        style = new Style((JSONObject)rule.get("style"), (String)rule.get("styleSheetId")); // NOI18N
        if (rule.containsKey("media")) { // NOI18N
            JSONArray array = (JSONArray)rule.get("media"); // NOI18N
            media = new ArrayList<Media>(array.size());
            for (Object o : array) {
                media.add(new Media((JSONObject)o));
            }
        } else {
            media = Collections.emptyList();
        }
        if (rule.containsKey("selectorList")) { // NOI18N
            JSONObject selectorList = (JSONObject)rule.get("selectorList"); // NOI18N
            selector = (String)selectorList.get("text"); // NOI18N
            if (selectorList.containsKey("range")) { // NOI18N
                selectorRange = new SourceRange((JSONObject)selectorList.get("range")); // NOI18N
            }
            if (selectorList.containsKey("selectors")) { // NOI18N
                JSONArray array = (JSONArray)selectorList.get("selectors"); // NOI18N
                selectors = new ArrayList<Selector>(array.size());
                for (Object o : array) {
                    Selector nextSelector;
                    if (o instanceof String) {
                        nextSelector = new Selector((String)o);
                    } else {
                        nextSelector = new Selector((JSONObject)o);
                    }
                    selectors.add(nextSelector);
                }
            }
        } else {
            selector = (String)rule.get("selectorText"); // NOI18N
            if (rule.containsKey("selectorRange")) { // NOI18N
                selectorRange = new SourceRange((JSONObject)rule.get("selectorRange")); // NOI18N
            }
        }
    }

    /**
     * Returns the identifier of the rule.
     *
     * @return identifier of the rule or {@code null} for user agent stylesheet
     * and user-specified stylesheet rules.
     */
    public RuleId getId() {
        return id;
    }

    /**
     * Returns the selector of the rule.
     *
     * @return selector of the rule.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Sets the selector of the rule.
     * 
     * @param selector selector of the rule.
     */
    void setSelector(String selector) {
        this.selector = selector;
    }

    /**
     * Returns URL of the parent stylesheet.
     *
     * @return URL of the parent stylesheet.
     */
    public String getSourceURL() {
        String url = sourceURL;
        if (url == null && parentStyleSheet != null) {
            url = parentStyleSheet.getHeader().getSourceURL();
        }
        return url;
    }

    /**
     * Returns the line number of the first character of the selector.
     *
     * @return line number of the first character of the selector.
     */
    public int getSourceLine() {
        int line;
        if ((sourceLine == -1) && (selectors != null)) {
            Selector firstSelector = selectors.get(0);
            SourceRange range = firstSelector.getRange();
            if (range != null) {
                return range.getStartLine();
            }
        }
        if ((sourceLine == -1) && (selectorRange != null)) {
            line = selectorRange.getStartLine();
        } else {
            line = sourceLine;
        }
        return line;
    }

    /**
     * Returns the origin of the parent stylesheet.
     *
     * @return origin of the parent stylesheet.
     */
    public StyleSheetOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns the associated style declaration.
     *
     * @return associated style declaration.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Return the selector range in the underlying resource.
     *
     * @return selector range in the underlying resource or {@code null}
     * if this information is not available.
     */
    public SourceRange getSelectorRange() {
        return selectorRange;
    }

    /**
     * Returns the media list (for rules involving media queries).
     *
     * @return media list that enumerates media queries starting with
     * the innermost one, going outwards.
     */
    public List<Media> getMedia() {
        return Collections.unmodifiableList(media);
    }

    /**
     * Returns the parent style sheet of the rule. Note that this method
     * is supported on instances obtained from {@code StyleSheetBody} only.
     * It returns {@code null} if the {@code Rule} object is obtain by any
     * other mean.
     *
     * @return parent style sheet of the rule or {@code null}.
     */
    public StyleSheetBody getParentStyleSheet() {
        return parentStyleSheet;
    }

    /**
     * Sets the parent style sheet of this rule.
     *
     * @param parentStyleSheet parent style sheet of this rule.
     */
    void setParentStyleSheet(StyleSheetBody parentStyleSheet) {
        this.parentStyleSheet = parentStyleSheet;
    }

    @Override
    public String toString() {
        return json.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Rule)) {
            return false;
        }
        RuleId ruleId = getId();
        if (ruleId == null || ruleId.getOrdinal() == -1) {
            return (this == object);
        } else {
            Rule other = (Rule)object;
            return ruleId.equals(other.getId());
        }
    }

    @Override
    public int hashCode() {
        RuleId ruleId = getId();
        return (ruleId == null || ruleId.getOrdinal() == -1) ? super.hashCode() : ruleId.hashCode();
    }

}
