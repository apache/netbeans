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
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the CSS domain of WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class CSS {

    private static final Logger LOG = Logger.getLogger(CSS.class.getName());

    /** Transport used by this instance. */
    private final TransportHelper transport;
    /** Callback for CSS event notifications. */
    private final ResponseCallback callback;
    /** Registered listeners. */
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    /** Cache of style-sheets. */
    private final Map<String, StyleSheetBody> styleSheets = new HashMap<String, StyleSheetBody>();
    /** Style-sheet headers. */
    private final List<StyleSheetHeader> styleSheetHeaders = new CopyOnWriteArrayList<StyleSheetHeader>();

    /**
     * Creates a new wrapper for the CSS domain of WebKit Remote Debugging Protocol.
     *
     * @param transport transport to use.
     */
    public CSS(TransportHelper transport) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    /**
     * Enables the CSS agent. Clients should not assume that the CSS agent
     * has been enabled until this method returns.
     */
    public void enable() {
        transport.sendBlockingCommand(new Command("CSS.enable")); // NOI18N
    }

    /**
     * Disables the CSS agent.
     */
    public void disable() {
        transport.sendCommand(new Command("CSS.disable")); // NOI18N
    }

    /**
     * Returns meta-information of all stylesheets.
     *
     * @return meta-information of all stylesheets.
     */
    public List<StyleSheetHeader> getAllStyleSheets() {
        List<StyleSheetHeader> sheets = new ArrayList<StyleSheetHeader>();
        Response response = transport.sendBlockingCommand(new Command("CSS.getAllStyleSheets")); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result == null) {
                // CSS.getAllStyleSheets is not in the latest versions of the protocol
                sheets = Collections.unmodifiableList(styleSheetHeaders);
            } else {
                JSONArray headers = (JSONArray)result.get("headers"); // NOI18N
                for (Object o : headers) {
                    JSONObject header = (JSONObject)o;
                    sheets.add(new StyleSheetHeader(header));
                }
            }
        }
        return sheets;
    }

    /**
     * Returns (the content of) the specified stylesheet.
     *
     * @param styleSheetId identifier of the requested stylesheet.
     * @return specified stylesheet.
     */
    public StyleSheetBody getStyleSheet(String styleSheetId) {
        StyleSheetBody body;
        synchronized (this) {
            body = styleSheets.get(styleSheetId);
            if (body != null) {
                return body;
            }
        }
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getStyleSheet", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            StyleSheetHeader header = getStyleSheetHeader(styleSheetId);
            if (result == null) {
                // CSS.getStyleSheet has been removed from the CSS domain
                if (header == null) { // Issue 246143
                    body = null;
                } else {
                    String styleSheetText = getStyleSheetText(styleSheetId);
                    body = new StyleSheetBody(header, styleSheetText);
                }
            } else {
                JSONObject sheetInfo = (JSONObject) result.get("styleSheet"); // NOI18N
                body = new StyleSheetBody(header, sheetInfo);
            }
            synchronized (this) {
                styleSheets.put(styleSheetId, body);
            }
        }
        return body;
    }

    /**
     * Returns header of the style-sheet with the specified ID.
     * 
     * @param styleSheetId style-sheet ID.
     * @return header of the style-sheet with the specified ID
     * or {@code null} if there is no such style-sheet.
     */
    private StyleSheetHeader getStyleSheetHeader(String styleSheetId) {
        for (StyleSheetHeader header : styleSheetHeaders) {
            String id = header.getStyleSheetId();
            if (id.equals(styleSheetId)) {
                return header;
            }
        }
        return null;
    }

    /**
     * Returns the content of the specified stylesheet.
     * 
     * @param styleSheetId identifier of a stylesheet.
     * @return content of the specified stylesheet.
     */
    public String getStyleSheetText(String styleSheetId) {
        String text = null;
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getStyleSheetText", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                text = (String)result.get("text"); // NOI18N
            }
        }
        return text;
    }

    /**
     * Sets the text of the specified stylesheet. Invocation of this method
     * invalidates all {@code StyleId}s and {@code RuleId}s attached
     * to the stylesheet before.
     *
     * @param styleSheetId identifier of the stylesheet.
     * @param styleSheetText new text of the stylesheet.
     */
    public void setStyleSheetText(String styleSheetId, String styleSheetText) {
        styleSheetText = replaceHoverInStyleSheetText(styleSheetText);
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        params.put("text", styleSheetText); // NOI18N
        transport.sendBlockingCommand(new Command("CSS.setStyleSheetText", params)); // NOI18N
        if (!styleSheetChanged.getAndSet(false)) {
            // Workaround for a bug - if a styleSheetChanged event is not fired
            // as a result of invocation of CSS.setStyleSheetText then we fire
            // this event manually.
            notifyStyleSheetChanged(styleSheetId);
        }
    }

    /** Determines whether styleSheetChanged event was fired. */
    private final AtomicBoolean styleSheetChanged = new AtomicBoolean();
    /**
     * Supported CSS properties. A mapping from a property name
     * to information about the property.
     */
    private Map<String,PropertyInfo> supportedProperties;

    /**
     * Returns information about the supported CSS properties.
     *
     * @return a mapping from a property name to information about the property.
     */
    public Map<String,PropertyInfo> getSupportedCSSProperties() {
        if (supportedProperties == null) {
            Response response = transport.sendBlockingCommand(new Command("CSS.getSupportedCSSProperties")); // NOI18N
            if (response != null) {
                JSONObject result = response.getResult();
                if (result == null) {
                    supportedProperties = loadSupportedProperties();
                } else {
                    Map<String,PropertyInfo> map = new HashMap<String,PropertyInfo>();
                    JSONArray properties = (JSONArray)result.get("cssProperties"); // NOI18N
                    for (Object o : properties) {
                        PropertyInfo info;
                        if (o instanceof String) {
                            info = new PropertyInfo((String)o);
                        } else {
                            info = new PropertyInfo((JSONObject)o);
                        }
                        map.put(info.getName(), info);
                    }
                    supportedProperties = map;
                }
            }
        }
        return supportedProperties;
    }

    // Chrome 35 replaces CSS.getSupportedCSSProperties by a hardcoded list,
    // see https://chromium.googlesource.com/chromium/blink/+/master/Source/core/css/CSSShorthands.in
    private Map<String,PropertyInfo> loadSupportedProperties() {
        Map<String,PropertyInfo> map = new HashMap<>();
        try (InputStream stream = getClass().getResourceAsStream("Longhands.properties"); ) { // NOI18N
            Properties properties = new Properties();
            properties.load(stream);
            for(String name: properties.stringPropertyNames()) {
                StringTokenizer tokenizer = new StringTokenizer(properties.getProperty(name), ","); // NOI18N
                JSONArray longhands = new JSONArray();
                while (tokenizer.hasMoreTokens()) {
                    String longhand = tokenizer.nextToken();
                    longhands.add(longhand);
                }
                JSONObject json = new JSONObject();
                json.put("name", name); // NOI18N
                json.put("longhands", longhands); // NOI18N
                map.put(name, new PropertyInfo(json));
            }
        } catch (IOException ioex) {
            Logger.getLogger(CSS.class.getName()).log(Level.INFO, null, ioex);
        }
        return map;
    }

    /**
     * Ensures that the given node will have the specified pseudo-classes
     * whenever its style is computed by the browser.
     *
     * @param node node for which to force the pseudo-classes.
     * @param forcedPseudoClasses pseudo-classes to force.
     * @since 1.5
     */
    public void forcePseudoState(Node node, PseudoClass[] forcedPseudoClasses) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        JSONArray pseudoClasses = new JSONArray();
        if (forcedPseudoClasses != null) {
            for (PseudoClass pseudoClass : forcedPseudoClasses) {
                pseudoClasses.add(pseudoClass.getCode());
            }
        }
        params.put("forcedPseudoClasses", pseudoClasses); // NOI18N
        transport.sendCommand(new Command("CSS.forcePseudoState", params)); // NOI18N
    }

    /**
     * Returns CSS rules matching the specified node.
     *
     * @param node node whose matching style should be returned.
     * @param forcedPseudoClasses element pseudo classes to force when
     * computing the applicable style rules.
     * @param includePseudo determines whether to include pseudo styles.
     * @param includeInherited determines whether to include inherited styles.
     * @return CSS rules matching the specified node.
     */
    public MatchedStyles getMatchedStyles(Node node,
            PseudoClass[] forcedPseudoClasses, boolean includePseudo, boolean includeInherited) {
        MatchedStyles matchedStyles = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        if (forcedPseudoClasses != null && forcedPseudoClasses.length != 0) {
            JSONArray pseudoClasses = new JSONArray();
            for (PseudoClass pseudoClass : forcedPseudoClasses) {
                pseudoClasses.add(pseudoClass.getCode());
            }
            params.put("forcedPseudoClasses", pseudoClasses); // NOI18N
        }
        params.put("includePseudo", includePseudo); // NOI18N
        params.put("includeInherited", includeInherited); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getMatchedStylesForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                matchedStyles = new MatchedStyles(result);
                processMatchedRule(matchedStyles.getMatchedRules());
                for (InheritedStyleEntry entry : matchedStyles.getInheritedRules()) {
                    processMatchedRule(entry.getMatchedRules());
                }
            }
        }
        return matchedStyles;
    }

    private void processMatchedRule(List<Rule> rules) {
        for (Rule rule : rules) {
            returnHoverToSelector(rule);
            if (rule.getOrigin() != StyleSheetOrigin.USER_AGENT) { // Issue 234848
                StyleId styleId = rule.getStyle().getId();
                if (styleId == null) {
                    LOG.info("null styleId for rule "+rule);
                    continue;
                }
                String styleSheetId = styleId.getStyleSheetId();
                StyleSheetBody body = getStyleSheet(styleSheetId);
                rule.setParentStyleSheet(body);
            }
        }
    }

    /**
     * Returns styles defined by DOM attributes (like {@code style},
     * {@code witdth}, {@code height}, etc.)
     *
     * @param node node whose inline styles should be returned.
     * @return styles defined by DOM attributes.
     */
    public InlineStyles getInlineStyles(Node node) {
        InlineStyles inlineStyles = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getInlineStylesForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                inlineStyles = new InlineStyles(result);
            }
        }
        return inlineStyles;
    }

    /**
     * Returns the computed style for the specified node.
     *
     * @param node node whose computed style should be returned.
     * @return computed style for the specified node.
     */
    public List<ComputedStyleProperty> getComputedStyle(Node node) {
        List<ComputedStyleProperty> list = Collections.emptyList();
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getComputedStyleForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONArray properties = (JSONArray)result.get("computedStyle"); // NOI18N
                list = new ArrayList<ComputedStyleProperty>(properties.size());
                for (Object o : properties) {
                    list.add(new ComputedStyleProperty((JSONObject)o));
                }
            }
        }
        return list;
    }

    /**
     * Sets a new text of the specified property.
     *
     * @param styleId ID of the style to modify.
     * @param propertyIndex index of the property in the style.
     * @param propertyText text of the property in the form {@code name:value;}.
     * @param overwrite if {@code true} then the property at the given position
     * is overwritten, otherwise it is inserted.
     * @return the resulting style after the property text modification.
     */
    public Style setPropertyText(StyleId styleId, int propertyIndex, String propertyText, boolean overwrite) {
        Style resultingStyle = null;
        JSONObject params = new JSONObject();
        params.put("styleId", styleId.toJSONObject()); // NOI18N
        params.put("propertyIndex", propertyIndex); // NOI18N
        params.put("text", propertyText); // NOI18N
        params.put("overwrite", overwrite); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.setPropertyText", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject style = (JSONObject)result.get("style"); // NOI18N
                resultingStyle = new Style(style);
            }
        }
        return resultingStyle;
    }

    /**
     * Toggles a property in a style.
     *
     * @param styleId ID of the style to modify.
     * @param propertyIndex index of the property in the style.
     * @param disable detemines whether the property should be disabled
     * (i.e. removed from the style declaration). If {@code disable} is
     * {@code false} then the property is returned back into the style declaration.
     * @return the resulting style after the property toggling.
     */
    public Style toggleProperty(StyleId styleId, int propertyIndex, boolean disable) {
        Style resultingStyle = null;
        JSONObject params = new JSONObject();
        params.put("styleId", styleId.toJSONObject()); // NOI18N
        params.put("propertyIndex", propertyIndex); // NOI18N
        params.put("disable", disable); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.toggleProperty", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject style = (JSONObject)result.get("style"); // NOI18N
                resultingStyle = new Style(style);
            }
        }
        return resultingStyle;
    }

    /**
     * Sets the selector of a rule.
     *
     * @param ruleId ID of the rule to modify.
     * @param selector new selector of the rule.
     * @return the resulting rule after the selector modification.
     */
    public Rule setRuleSelector(RuleId ruleId, String selector) {
        Rule resultingRule = null;
        JSONObject params = new JSONObject();
        params.put("ruleId", ruleId.toJSONObject()); // NOI18N
        params.put("selector", selector); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.setRuleSelector", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject rule = (JSONObject)result.get("rule"); // NOI18N
                resultingRule = new Rule(rule);
            }
        }
        return resultingRule;
    }

    /**
     * Registers CSS domain listener.
     * 
     * @param listener listener to register.
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters CSS domain listener.
     * 
     * @param listener listener to unregister.
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify listeners about {@code mediaQueryResultChanged} event.
     */
    private void notifyMediaQuertResultChanged() {
        for (Listener listener : listeners) {
            listener.mediaQueryResultChanged();
        }
    }

    /**
     * Notify listeners about {@code styleSheetChanged} event.
     *
     * @param styleSheetId identifier of the modified stylesheet.
     */
    private void notifyStyleSheetChanged(String styleSheetId) {
        for (Listener listener : listeners) {
            listener.styleSheetChanged(styleSheetId);
        }
    }

    /**
     * Notify listeners about {@code styleSheetAdded} event.
     * 
     * @param header meta-information of the added style-sheet.
     */
    private void notifyStyleSheetAdded(StyleSheetHeader header) {
        for (Listener listener : listeners) {
            listener.styleSheetAdded(header);
        }
    }

    /**
     * Notify listeners about {@code styleSheetRemoved} event.
     * 
     * @param styleSheetId identifier of the modified style-sheet.
     */
    private void notifyStyleSheetRemoved(String styleSheetId) {
        for (Listener listener : listeners) {
            listener.styleSheetRemoved(styleSheetId);
        }
    }

    void handleMediaQuertResultChanged(JSONObject params) {
        notifyMediaQuertResultChanged();
    }

    void handleStyleSheetChanged(JSONObject params) {
        styleSheetChanged.set(true);
        String styleSheetId = (String)params.get("styleSheetId"); // NOI18N
        synchronized (this) {
            styleSheets.remove(styleSheetId);
        }
        notifyStyleSheetChanged(styleSheetId);
    }

    void handleStyleSheetAdded(JSONObject params) {
        JSONObject headerInJSON = (JSONObject)params.get("header"); // NOI18N
        StyleSheetHeader header = new StyleSheetHeader(headerInJSON);
        styleSheetHeaders.add(header);
        notifyStyleSheetAdded(header);
    }

    void handleStyleSheetRemoved(JSONObject params) {
        String styleSheetId = (String)params.get("styleSheetId"); // NOI18N
        for (StyleSheetHeader header : styleSheetHeaders) {
            if (styleSheetId.equals(header.getStyleSheetId())) {
                styleSheetHeaders.remove(header);
                break;
            }
        }
        notifyStyleSheetRemoved(styleSheetId);
    }

    /**
     * Resets cached data.
     */
    public synchronized void reset() {
        styleSheets.clear();
        styleSheetHeaders.clear();
        classForHover = null;
    }

    /** CSS class used to simulate hovering. */
    private String classForHover;

    /**
     * Sets the CSS class that should be used to simulate hovering.
     * 
     * @param classForHover class to simulate hovering.
     */
    public void setClassForHover(String classForHover) {
        this.classForHover = classForHover;
    }

    /**
     * Returns the class used to simulate hovering.
     * 
     * @return class to simulate hovering.
     */
    private String getClassForHover() {
        return classForHover;
    }

    /**
     * Returns {@code :hover} pseudo-class into the selector (if it was
     * replaced by a class that simulates hovering).
     * 
     * @param rule rule whose selector should be updated.
     */
    private void returnHoverToSelector(Rule rule) {
        String selector = rule.getSelector();
        String clazz = getClassForHover();
        if (clazz != null) {
            selector = Pattern.compile(Pattern.quote("." + clazz)).matcher(selector).replaceAll(":hover"); // NOI18N
            rule.setSelector(selector);
        }
    }

    /**
     * Replaces {@code :hover} pseudo-classes by a class
     * that should simulate hovering.
     * 
     * @param styleSheetText text of the style-sheet to update.
     * @return updated text of the style-sheet.
     */
    private String replaceHoverInStyleSheetText(String styleSheetText) {
        String clazz = getClassForHover();
        if (clazz != null) {
            styleSheetText = styleSheetText.replaceAll(":hover", "." + clazz); // NOI18N
        }
        return styleSheetText;
    }

    /**
     * CSS domain listener.
     */
    public static interface Listener {

        /**
         * Fired whenever media query result changes (for example, when
         * a browser window is resized). The current implementation
         * considers viewport-dependent media features only.
         */
        void mediaQueryResultChanged();

        /**
         * Fired whenever a stylesheet is changed as a result
         * of the client operation.
         *
         * @param styleSheetId identifier od the modified stylesheet.
         */
        void styleSheetChanged(String styleSheetId);

        /**
         * Fired whenever an active document's style-sheet is added.
         * 
         * @param header meta-information of the added style-sheet.
         */
        void styleSheetAdded(StyleSheetHeader header);

        /**
         * Fired whenever an active document's style-sheet is removed.
         * 
         * @param styleSheetId identifier of the removed style-sheet.
         */
        void styleSheetRemoved(String styleSheetId);

    }

    /**
     * Callback for CSS domain events.
     */
    class Callback implements ResponseCallback {

        /**
         * Handles CSS domain events.
         *
         * @param response event description.
         */
        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            JSONObject params = response.getParams();
            if ("CSS.mediaQueryResultChanged".equals(method)) { // NOI18N
                handleMediaQuertResultChanged(params);
            } else if ("CSS.styleSheetChanged".equals(method)) { // NOI18N
                handleStyleSheetChanged(params);
            } else if ("CSS.styleSheetAdded".equals(method)) { // NOI18N
                handleStyleSheetAdded(params);
            } else if ("CSS.styleSheetRemoved".equals(method)) { // NOI18N
                handleStyleSheetRemoved(params);
            } else if ("Page.frameNavigated".equals(method)) { // NOI18N
                // We cannot reset styleSheetHeaders on DOM.documentUpdated because
                // CSS.styleSheetAdded events are fired before DOM.documentUpdated.
                Object frame = response.getParams().get("frame"); // NOI18N
                if (frame instanceof JSONObject) {
                    Object parentFrame = ((JSONObject)frame).get("parentId"); // NOI18N
                    if (parentFrame == null) {
                        // 248911: Clear the headers when the root frame is navigated only.
                        styleSheetHeaders.clear();
                    }
                }
            } else if ("DOM.documentUpdated".equals(method)) { // NOI18N
                synchronized (this) {
                    styleSheets.clear();
                }
            }
        }

    }

    /**
     * Pseudo class (used by {@code getMatchedStylesForNode()}).
     */
    public static enum PseudoClass {
        ACTIVE("active"), FOCUS("focus"), HOVER("hover"), VISITED("visited"); // NOI18N
        /** Code of the pseudo class. */
        private final String code;

        /**
         * Creates a new {@code PseudoClass} with the specified code.
         *
         * @param code code of the pseudo class.
         */
        private PseudoClass(String code) {
            this.code = code;
        }

        /**
         * Returns the code of this pseudo class.
         *
         * @return code of this pseudo class.
         */
        String getCode() {
            return code;
        }
    }

}
