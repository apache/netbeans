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
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;

/**
 *
 * @author marekfukala
 */
public class UnknownHtmlTag implements HtmlTag {

    private String elementName;

    UnknownHtmlTag(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public String getName() {
        return elementName;
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
        HtmlTag other = (HtmlTag) obj;

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
        return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean hasOptionalOpenTag() {
        return false;
    }

    @Override
    public boolean hasOptionalEndTag() {
        return false;
    }

    @Override
    public synchronized HtmlTagAttribute getAttribute(String name) {
        return null;
    }

    @Override
    public HtmlTagType getTagClass() {
        return HtmlTagType.UNKNOWN;
    }

    @Override
    public synchronized Collection<HtmlTag> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public HelpItem getHelp() {
        return null;
    }
}
