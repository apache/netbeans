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
package org.netbeans.api.java.source.ui.snippet;

import java.util.List;
import java.util.Objects;

public class MarkupTag {

    private String tagName;
    private List<MarkupTagAttribute> markUpTagAttributes;
    private boolean isTagApplicableToNextLine;


    public MarkupTag(String tagName, List<MarkupTagAttribute> markUpTagAttributes, boolean isTagApplicableToNextLine) {
        this.tagName = tagName;
        this.markUpTagAttributes = markUpTagAttributes;
        this.isTagApplicableToNextLine = isTagApplicableToNextLine;
    }

    public String getTagName() {
        return tagName;
    }

    public List<MarkupTagAttribute> getMarkUpTagAttributes() {
        return markUpTagAttributes;
    }
    public boolean isTagApplicableToNextLine() {
        return isTagApplicableToNextLine;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.tagName);
        hash = 79 * hash + Objects.hashCode(this.markUpTagAttributes);
        hash = 79 * hash + (this.isTagApplicableToNextLine ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarkupTag other = (MarkupTag) obj;
        if (this.isTagApplicableToNextLine != other.isTagApplicableToNextLine) {
            return false;
        }
        if (!Objects.equals(this.tagName, other.tagName)) {
            return false;
        }
        return Objects.equals(this.markUpTagAttributes, other.markUpTagAttributes);
    }
}
