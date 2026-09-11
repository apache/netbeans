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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ComponentTag implements Tag {

    /*
     * JSF spec 3.1.12 Render-Independent Properties: 
     * Read-Write: id, parent, rendered, rendererType, transient; 
     * Read-Only:  rendersChildren
    */
    private static final Set<String> DEFAULT_ATTRS = Set.of("id", "parent", "rendered", "rendererType", "transient", "class" /* not in the spec */); //NOI18N

    private static final Map<String, Attribute> DEFAULT_ATTRS_MAP;

    static {
        DEFAULT_ATTRS_MAP = DEFAULT_ATTRS.stream().collect(Collectors.toMap(Function.identity(),
                defaultAttributeName -> new Attribute.DefaultAttribute(defaultAttributeName,
                        NbBundle.getMessage(ComponentTag.class, "HELP_" + defaultAttributeName), false))); //NOI18N
    }

    public static ComponentTag wrap(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag instanceof ComponentTag componentTag) {
            return componentTag;
        }
        return new ComponentTag(tag);
    }

    private final Tag delegate;

    private ComponentTag(Tag delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public Collection<Attribute> getAttributes() {
        //merge with default attributes
        // cannot use a Set since Attribute's equals/hashCode use the required attribute, might cause doubles
        Map<String, Attribute> all = new HashMap<>();
        delegate.getAttributes().forEach(att -> all.put(att.getName(), att));
        DEFAULT_ATTRS_MAP.forEach((name, att) -> all.putIfAbsent(name, att));
        return all.values();
    }

    @Override
    public Attribute getAttribute(String name) { 
        Attribute attribute = delegate.getAttribute(name);
        return attribute != null ? attribute : DEFAULT_ATTRS_MAP.get(name);
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return delegate.hasNonGenenericAttributes();
    }
    
}
