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
package org.netbeans.modules.html.parser.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.DefaultHelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class EDHtmlTag implements HtmlTag {

    private ElementDescriptor descriptor;
    private Map<String, HtmlTagAttribute> attrs; //attr name to HtmlTagAttribute instance map
    private HtmlTagType type;
    private Collection<HtmlTag> children;

    public EDHtmlTag(ElementDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    private void initAttributes() {
        attrs = new HashMap<String, HtmlTagAttribute>();
        for (Attribute attr : descriptor.getAttributes()) {
            attrs.put(attr.getName(), HtmlTagProvider.getHtmlTagAttributeInstance(attr));
        }
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 61 * hash + (this.getTagClass() != null ? this.getTagClass().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HtmlTag)) {
            return false;
        }
        HtmlTag other = (HtmlTag)obj;
        
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.getTagClass() != other.getTagClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("ElementName2HtmlTagAdapter{name=%s, type=%s}", getName(), getTagClass());//NOI18N
    }

    @Override
    public synchronized Collection<HtmlTagAttribute> getAttributes() {
        if (attrs == null) {
            initAttributes();
        }
        return attrs.values();
    }

    @Override
    public boolean isEmpty() {
        return descriptor.isEmpty();
    }

    @Override
    public boolean hasOptionalOpenTag() {
        return descriptor.hasOptionalOpenTag();
    }

    @Override
    public boolean hasOptionalEndTag() {
        return descriptor.hasOptionalEndTag();
    }

    @Override
    public synchronized HtmlTagAttribute getAttribute(String name) {
        if (attrs == null) {
            initAttributes();
        }
        return attrs.get(name);
    }

    @Override
    public HtmlTagType getTagClass() {
        return descriptor.getTagType();
    }

    @Override
    public synchronized Collection<HtmlTag> getChildren() {
        if (children == null) {
                //add all directly specified children
                Collection<ElementDescriptor> directChildren = descriptor.getChildrenElements();
                children = new LinkedList<HtmlTag>(HtmlTagProvider.convert(directChildren));
                //add all members of children content types
                for (ContentType ct : descriptor.getChildrenTypes()) {
                    Collection<ElementDescriptor> contentTypeChildren = ElementDescriptorRules.getElementsByContentType(ct);
                    children.addAll(HtmlTagProvider.convert(contentTypeChildren));
                }
        }
        return children;
    }

    @Override
    public HelpItem getHelp() {
        StringBuilder header = new StringBuilder();
        header.append("<h2>");
        header.append(NbBundle.getMessage(HtmlTagProvider.class, "MSG_ElementPrefix"));//NOI18N
        header.append(" '");//NOI18N
        header.append(descriptor.getName());
        header.append("'</h2>");//NOI18N

        return descriptor.getHelpLink() != null
                ? new DefaultHelpItem(
                HtmlDocumentation.getDefault().resolveLink(descriptor.getHelpLink()),
                HtmlDocumentation.getDefault(),
                header.toString())
                : null;

    }
}
