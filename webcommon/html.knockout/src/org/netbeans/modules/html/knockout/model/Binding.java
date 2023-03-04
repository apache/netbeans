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
package org.netbeans.modules.html.knockout.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.knockout.KOHelpItem;

/**
 * KO binding types.
 *
 * http://knockoutjs.com/documentation/introduction.html
 *
 * TODO add metadata for the bindings
 *
 * @author marekfukala
 */
public enum Binding implements KOHelpItem {

    //text and appearance
    visible,
    text,
    textInput,
    html,
    css,
    style,
    attr,
    //control flow
    foreach,
    _if, //real name is "IF"
    ifnot,
    with,
    //fields
    click,
    event,
    submit,
    enable,
    disable,
    value,
    hasfocus,
    checked,
    options,
    selectedOptions,
    uniqueName,
    //rendering
    component,
    template;

    public static final String DOC_CHARSET = "UTF-8"; //NOI18N
    
    private static final String DOC_URL_BASE = "http://knockoutjs.com/documentation/"; //NOI18N
    private static final String DOC_URL_POSTFIX = "-binding.html"; //NOI18N

    private static final Map<String, Binding> NAMES2BINDINGS = new HashMap<>();
    static {
        for(Binding d : values()) {
            NAMES2BINDINGS.put(d.getName(), d);
        }
    }
    
    public static Binding getBinding(String name) {
        return NAMES2BINDINGS.get(name);
    }
    
    /**
     * Gets the binding name.
     *
     * Use this instead of {@link #name()}.
     *
     * @return name of the KO binding.
     */
    @NonNull
    @Override
    public String getName() {
        return name().charAt(0) == '_' ? name().substring(1) : name();
    }

    @Override
    public String getExternalDocumentationURL() {
        return new StringBuilder()
                .append(DOC_URL_BASE)
                .append(getName().equals(Binding.textInput.name()) ? getName().toLowerCase() : getName()) // workaround for issue #246945
                .append(DOC_URL_POSTFIX)
                .toString();
    }
}
