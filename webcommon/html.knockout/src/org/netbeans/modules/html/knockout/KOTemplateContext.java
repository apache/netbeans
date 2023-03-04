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
package org.netbeans.modules.html.knockout;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public class KOTemplateContext {

    private final LinkedList<StackItem> scripts = new LinkedList<>();

    private boolean isScriptStart;

    private boolean isId;

    @SuppressWarnings("fallthrough")
    public Pair<Boolean, String> process(@NonNull Token<HTMLTokenId> token) {
        switch (token.id()) {
            case TAG_OPEN:
                isScriptStart = false;
                if ("script".equals(token.text().toString())) { // NOI18N
                    isScriptStart = true;
                    scripts.push(new StackItem());
                }
                break;
            case TAG_CLOSE_SYMBOL:
                if (isScriptStart && !scripts.isEmpty() && scripts.peek().getId() != null) {
                    return Pair.of(true, scripts.peek().getId());
                }
                isScriptStart = false;
            case TAG_CLOSE:
                if ("script".equals(token.text().toString()) && !scripts.isEmpty()) { // NOI18N
                    StackItem item = scripts.pop();
                    if (item.getId() != null) {
                        return Pair.of(false, item.getId());
                    }
                }
                break;
            case ARGUMENT:
                if (isScriptStart) {
                    isId = false;
                    if ("id".equals(token.text().toString())) { // NOI18N
                        isId = true;
                    }
                }
                break;
            case VALUE:
            case VALUE_CSS:
                if (isScriptStart && isId) {
                    CharSequence text = token.text();
                    // XXX
                    if (text.length() > 2 && !scripts.isEmpty()) {
                        scripts.peek().setId(token.text().subSequence(1, text.length() - 1).toString());
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @CheckForNull
    public String getCurrentScriptId() {
        if (scripts.isEmpty()) {
            return null;
        }
        for (StackItem item : scripts) {
            if (item.getId() != null) {
                return item.getId();
            }
        }
        return null;
    }

    public void clear() {
        scripts.clear();
        isScriptStart = false;
        isId = false;
    }

    public static class TemplateUsage {

        private final KODataBindContext context;

        private final Set<String> parentTemplatesNames = new HashSet<>();

        public TemplateUsage(KODataBindContext context) {
            this.context = context;
        }

        public KODataBindContext getContext() {
            return context;
        }

        public void addParentTemplateName(String name) {
            parentTemplatesNames.add(name);
        }

        public Set<String> getParentTemplatesNames() {
            return Collections.unmodifiableSet(parentTemplatesNames);
        }

        public String getParentTemplateName() {
            if (parentTemplatesNames.isEmpty()) {
                return null;
            }
            return parentTemplatesNames.iterator().next();
        }
    }

    private static class StackItem {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
