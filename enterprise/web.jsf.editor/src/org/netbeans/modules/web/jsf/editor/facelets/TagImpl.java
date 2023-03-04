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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.web.jsfapi.api.Attribute;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class TagImpl extends GenericTag {

    private String name;
    private String description;
    private Map<String, Attribute> attrs;

    public TagImpl(String name, String description, Map<String, Attribute> attrs) {
        this.name = name;
        this.description = description;
        this.attrs = attrs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return !attrs.isEmpty();
    }

    @Override
    public Collection<Attribute> getAttributes() {
        //merge with default attributes
        Collection<Attribute> all = new ArrayList<>(super.getAttributes());
        all.addAll(attrs.values());
        return all;
    }

    @Override
    public Attribute getAttribute(String name) {
        Attribute superA = super.getAttribute(name);
        return superA != null ? superA : attrs.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag[name=").append(getName()).append(", attributes={"); //NOI18N
        for (Attribute attr : getAttributes()) {
            sb.append(attr.toString()).append(",");
        }
        sb.append("}]");
        return sb.toString();
    }
}
