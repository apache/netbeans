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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class GenericTag implements Tag {

    /*
     * JSF spec 3.1.12 Render-Independent Properties: 
     * Read-Write: id, parent, rendered, rendererType, transient; 
     * Read-Only:  rendersChildren
    */
    private static final String[] DEFAULT_ATTRS = new String[]{"id", "parent", "rendered",
        "rendererType", "transient", "class" /* not in the spec */}; //NOI18N
    
    private final AtomicReference<Map<String, Attribute>> attrs = new AtomicReference<>();

    @Override
    public Collection<Attribute> getAttributes() {
        return getGenericAttributes().values();
    }

    @Override
    public Attribute getAttribute(String name) {
        return getGenericAttributes().get(name);
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return false;
    }

    protected Map<String, Attribute> getAdditionalGenericAttributes() {
        return Collections.emptyMap();
    }

    private Map<String, Attribute> getGenericAttributes() {
        if (attrs.compareAndSet(null, new HashMap<String, Attribute>())) {
            //add the default ID attribute
            for (String defaultAttributeName : DEFAULT_ATTRS) {
                if (getAttribute(defaultAttributeName) == null) {
                    attrs.get().put(defaultAttributeName, 
                            new Attribute.DefaultAttribute(defaultAttributeName, 
                            NbBundle.getMessage(GenericTag.class, new StringBuilder().append("HELP_").append(defaultAttributeName).toString()), false)); //NOI18N
                }
            }
            for (Map.Entry<String, Attribute> entry : getAdditionalGenericAttributes().entrySet()) {
                if (getAttribute(entry.getKey()) == null) {
                    attrs.get().put(entry.getKey(), entry.getValue());
                }
            }
        }

        return attrs.get();
    }
    
}
