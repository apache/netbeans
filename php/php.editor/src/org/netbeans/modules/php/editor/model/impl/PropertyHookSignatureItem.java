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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PropertyHookElement;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.model.PropertyHookScope;

/**
 * Represents a JSON format object for a property hook.
 *
 * Use "JSON simple". If we use "Jackson", problems(timeout,
 * java.lang.NoClassDefFoundError) occurs in CI for Windows.
 * <pre>
 * e.g.
 * {
 *     "name":"set",
 *     "start":3651,
 *     "end":3690,
 *     "mod":1,
 *     "isRef":false,
 *     "isAttr":false,
 *     "hasBody":true,
 *     "paramSig":"$value::0::1:1:0:0:0:0::"
 * }
 * </pre>
 */
public class PropertyHookSignatureItem implements JSONAware {

    private static final Logger LOGGER = Logger.getLogger(PropertyHookSignatureItem.class.getName());
    private static final String EMPTY_ARRAY = "[]"; // NOI18N

    private final String name;
    private final int start;
    private final int end;
    private final int mod;
    private final boolean isRef;
    private final boolean isAttr;
    private final boolean hasBody;
    private final String paramSig;

    private PropertyHookSignatureItem(String name, int start, int end, int mod, boolean isAttr, boolean isRef, boolean hasBody, String paramSig) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.mod = mod;
        this.isAttr = isAttr;
        this.isRef = isRef;
        this.hasBody = hasBody;
        this.paramSig = paramSig;
    }

    private PropertyHookSignatureItem(PropertyHookScope propertyHook) {
        this(
                propertyHook.getName(),
                propertyHook.getOffsetRange().getStart(),
                propertyHook.getOffsetRange().getEnd(),
                propertyHook.getPhpModifiers().toFlags(),
                propertyHook.isAttributed(),
                propertyHook.isReference(),
                propertyHook.hasBody(),
                getParameterSignature(propertyHook.getParameters())
        );
    }

    private PropertyHookSignatureItem(PropertyHookElement element) {
        this(
                element.getName(),
                element.getOffsetRange().getStart(),
                element.getOffsetRange().getEnd(),
                element.getPhpModifiers().toFlags(),
                element.isAttributed(),
                element.isReference(),
                element.hasBody(),
                getParameterSignature(element.getParameters())
        );
    }

    private static String getParameterSignature(List<? extends ParameterElement> params) {
        StringBuilder sb = new StringBuilder();
        for (ParameterElement param : params) {
            ParameterElementImpl parameter = (ParameterElementImpl) param;
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(parameter.getSignature());
        }
        return sb.toString();
    }

    /**
     * Get a signature(JSON format) for PropetyHookScopes. (Serialize
     * PropertyHookScopes.)
     *
     * e.g.
     * `[{"name":"set","start":3651,"end":3690,"mod":1,"isRef":false,"isAttr":false,"hasBody":true,"paramSig":"$value::0::1:1:0:0:0:0::"}]`
     *
     * @param propertyHookScopes
     * @return a signature for scopes
     */
    public static String getSignatureFromScopes(Collection<? extends PropertyHookScope> propertyHookScopes) {
        final long start = (LOGGER.isLoggable(Level.FINE)) ? System.currentTimeMillis() : 0;
        List<PropertyHookSignatureItem> signatureItems = getSignatureItemsFromScopes(propertyHookScopes);
        String signature = EMPTY_ARRAY;
        if (!signatureItems.isEmpty()) {
            JSONArray items = new JSONArray();
            items.addAll(signatureItems);
            signature = items.toJSONString();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "getSignatureFromScopes() took: {0} ms", (System.currentTimeMillis() - start)); // NOI18N
            }
        }
        return signature;
    }

    private static List<PropertyHookSignatureItem> getSignatureItemsFromScopes(Collection<? extends PropertyHookScope> scopes) {
        List<PropertyHookScope> propertyHooks = new ArrayList<>(scopes);
        propertyHooks.sort((hook1, hook2) -> Integer.compare(hook1.getOffset(), hook2.getOffset()));
        List<PropertyHookSignatureItem> signatureItems = new ArrayList<>();
        for (PropertyHookScope propertyHook : propertyHooks) {
            signatureItems.add(new PropertyHookSignatureItem(propertyHook));
        }
        return signatureItems;
    }

    /**
     * Get a signature(JSON format) for PropetyHookElements. (Serialize
     * PropertyHookElements.)
     *
     * e.g.
     * `[{"name":"set","start":3651,"end":3690,"mod":1,"isRef":false,"isAttr":false,"hasBody":true,"paramSig":"$value::0::1:1:0:0:0:0::"}]`
     *
     * @param propertyHookElements
     * @return a signature for elements
     */
    public static String getSignatureFromElements(Collection<? extends PropertyHookElement> propertyHookElements) {
        final long start = (LOGGER.isLoggable(Level.FINE)) ? System.currentTimeMillis() : 0;
        List<PropertyHookSignatureItem> signatureItems = getSignatureItemsFromElements(propertyHookElements);
        String signature = EMPTY_ARRAY;
        if (!signatureItems.isEmpty()) {
            JSONArray items = new JSONArray();
            items.addAll(signatureItems);
            signature = items.toJSONString();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "getSignatureFromElements() took: {0} ms", (System.currentTimeMillis() - start)); // NOI18N
            }
        }
        return signature;
    }

    private static List<PropertyHookSignatureItem> getSignatureItemsFromElements(Collection<? extends PropertyHookElement> elements) {
        List<PropertyHookElement> propertyHooks = new ArrayList<>(elements);
        propertyHooks.sort((hook1, hook2) -> Integer.compare(hook1.getOffset(), hook2.getOffset()));
        List<PropertyHookSignatureItem> signatureItems = new ArrayList<>();
        for (PropertyHookElement propertyHook : propertyHooks) {
            signatureItems.add(new PropertyHookSignatureItem(propertyHook));
        }
        return signatureItems;
    }

    /**
     * Get property hook items from a JSON format signature. (Deserialize a
     * signature)
     *
     * e.g.
     * `[{"name":"set","start":3651,"end":3690,"mod":1,"isRef":false,"isAttr":false,"hasBody":true,"paramSig":"$value::0::1:1:0:0:0:0::"}]`
     *
     * @param signature JSON formatted signature
     * @return signature items
     */
    public static List<PropertyHookSignatureItem> fromSignature(final String signature) {
        if (StringUtils.isEmpty(signature) || EMPTY_ARRAY.equals(signature)) {
            return List.of();
        }

        final long start = (LOGGER.isLoggable(Level.FINE)) ? System.currentTimeMillis() : 0;
        List<PropertyHookSignatureItem> signatureItems = new ArrayList<>(2);
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(signature);
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                PropertyHookSignatureItem item = new PropertyHookSignatureItem(
                        (String) jsonObject.get("name"), // NOI18N
                        ((Long) jsonObject.get("start")).intValue(), // NOI18N
                        ((Long) jsonObject.get("end")).intValue(), // NOI18N
                        ((Long) jsonObject.get("mod")).intValue(), // NOI18N
                        (Boolean) jsonObject.get("isAttr"), // NOI18N
                        (Boolean) jsonObject.get("isRef"), // NOI18N
                        (Boolean) jsonObject.get("hasBody"), // NOI18N
                        (String) jsonObject.get("paramSig") // NOI18N
                );
                signatureItems.add(item);
            }
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING,
                    "Cannot deserialize: {0}, {1} (please try to delete your cache directory because the signature may be changed.)", // NOI18N
                    new Object[]{signature, ex.getMessage()});
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "fromSignature() took: {0} ms", (System.currentTimeMillis() - start)); // NOI18N
        }
        return signatureItems;
    }

    public String getName() {
        return name;
    }

    public OffsetRange getOffsetRange() {
        return new OffsetRange(start, end);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getModifier() {
        return mod;
    }

    public boolean isAttributed() {
        return isAttr;
    }

    public boolean isReference() {
        return isRef;
    }

    public boolean hasBody() {
        return hasBody;
    }

    public String getParameterSignature() {
        return paramSig;
    }

    @Override
    public String toString() {
        return "PropertyHookSignatureItem{" // NOI18N
                + "name=" + name // NOI18N
                + ", start=" + start // NOI18N
                + ", end=" + end // NOI18N
                + ", mod=" + mod // NOI18N
                + ", isRef=" + isRef // NOI18N
                + ", isAttr=" + isAttr // NOI18N
                + ", hasBody=" + hasBody // NOI18N
                + ", paramSig=" + paramSig // NOI18N
                + '}';
    }

    @Override
    public String toJSONString() {
        return  '{'
                + "\"name\":\"" + name + "\""// NOI18N
                + ",\"start\":" + start // NOI18N
                + ",\"end\":" + end // NOI18N
                + ",\"mod\":" + mod // NOI18N
                + ",\"isRef\":" + (isRef ? "true" : "false") // NOI18N
                + ",\"isAttr\":" + (isAttr ? "true" : "false") // NOI18N
                + ",\"hasBody\":" + (hasBody ? "true" : "false") // NOI18N
                + ",\"paramSig\":\"" + paramSig + "\""// NOI18N
                + '}';
    }
}
