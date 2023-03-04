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
package org.netbeans.modules.html.custom.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author marek
 */
public abstract class Element {

    private final String name, description, documentation, documentationURL;
    private final Collection<String> contextNames = new ArrayList<>();
    private final Tag parent;
    private final boolean required;

    
    public Element(String name, String description, String documentation, String documentationURL, Tag parent, boolean required, String... contexts) {
        this.name = name;
        this.description = description;
        this.documentation = documentation;
        this.documentationURL = documentationURL;
        this.parent = parent;
        this.contextNames.addAll(Arrays.asList(contexts));
        this.required = required;
        
        //if the element is not context-free then its context is also the parent element
        if(parent != null) {
            contextNames.add(parent.getName());
        }
    }

    public boolean isRequired() {
        return required;
    }
    
    public void addContext(String tagName) {
        contextNames.add(tagName);
    }

    public String getName() {
        return name;
    }

    public Collection<String> getContexts() {
        return contextNames;
    }

    public Tag getParent() {
        return parent;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getDocumentationURL() {
        return documentationURL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=");
        sb.append(name);
        if (parent != null) {
            sb.append(',');

            sb.append("parent=");
            sb.append(parent != null ? parent.getName() : null);
        }
        if (getContexts() != null) {
            sb.append(',');
            sb.append("contexts={");
            for (String ctx : getContexts()) {
                sb.append(ctx);
                sb.append(',');
            }
            sb.append("}");
        }
        if (description != null) {
            sb.append(',');
            sb.append("description=");
            sb.append(description);
        }
        if (documentation != null) {
            sb.append(',');
            sb.append("doc=");
            sb.append(documentation);
        }

        if (documentationURL != null) {
            sb.append(',');
            sb.append("doc_url=");
            sb.append(documentationURL);
        }

        return sb.toString();
    }

}
