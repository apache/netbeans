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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marek
 */
public class Tag extends Element {
    
    /* attribute name to instance map*/
    private final Map<String, Attribute> attrsMap = new HashMap<>();
    /* tag name to instance map*/
    private final Map<String, Tag> childrenMap = new HashMap<>();

    public Tag(String name) {
        this(name, null, null, null, null, false);
    }
    
    public Tag(String name, String description, String documentation, String documentationURL, Tag parent, boolean required, String... contexts) {
        super(name, description, documentation, documentationURL, parent, required, contexts);
    }

    public void setAttributes(Collection<Attribute> attributes) {
        for(Attribute a : attributes) {
            this.attrsMap.put(a.getName(), a);
        }
    }
    
     /**
     * Gets a collection of the attributes registered to the root context.
     *
     * @return
     */
    public Collection<String> getAttributesNames() {
        return attrsMap.keySet();
    }
    
    public Collection<Attribute> getAttributes() {
        return attrsMap.values();
    }
    
    public Attribute getAttribute(String name) {
        return attrsMap.get(name);
    }

    public void add(Attribute a) {
        attrsMap.put(a.getName(), a);
    }

    public void remove(Attribute a) {
        attrsMap.remove(a.getName());
    }

    public void setChildren(Collection<Tag> children) {
        for(Tag t : children) {
            childrenMap.put(t.getName(), t);
        }
    }

       /**
     * Gets a collection of the tags registered to the root context.
     *
     * @return
     */
    public Collection<String> getTagsNames() {
        return childrenMap.keySet();
    }

    public Collection<Tag> getTags() {
        return childrenMap.values();
    }
    
    public Tag getTag(String tagName) {
        return childrenMap.get(tagName);
    }

    public void add(Tag t) {
        childrenMap.put(t.getName(), t);
    }

    public void remove(Tag t) {
        childrenMap.remove(t.getName());
    }

 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag[");
        sb.append(super.toString());
         sb.append(',');
        sb.append("children={");
        for(Tag t : childrenMap.values()) {
            sb.append(t.toString());
            sb.append(',');
        }
        sb.append('}');
        sb.append(',');
        sb.append("attributes={");
        for(Attribute a : attrsMap.values()) {
            sb.append(a.toString());
            sb.append(',');
        }
        sb.append('}');
        sb.append("]");
        
        return sb.toString();
    }
    
}
