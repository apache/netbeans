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
package org.netbeans.modules.web.jsps.parserapi;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class TagInfo {
    public static final String BODY_CONTENT_EMPTY = "empty";
    public static final String BODY_CONTENT_JSP = "JSP";
    public static final String BODY_CONTENT_SCRIPTLESS = "scriptless";
    public static final String BODY_CONTENT_TAG_DEPENDENT = "tagdependent";

    private String displayName;
    private String tagName;
    private String infoString;
    private String tagClassName;
    private String bodyContent;
    private TagLibraryInfo tagLibrary;
    private final List<TagAttributeInfo> attributes = new ArrayList<>();
    private final List<TagVariableInfo> variables = new ArrayList<>();
    private final List<VariableInfo> runtimeVariables = new ArrayList<>();

    public TagInfo() {
    }

    public TagInfo(String tagName, String tagClassName, String bodyContent, String infoString, TagLibraryInfo tagLibrary, Object tagExtraInfo, List<TagAttributeInfo> attributes) {
        this(tagName, tagClassName, bodyContent, infoString, tagLibrary, attributes);
    }

    public TagInfo(String tagName, String tagClassName, String bodyContent, String infoString, TagLibraryInfo tagLibrary, Object tagExtraInfo, TagAttributeInfo[] attributes) {
        this(tagName, tagClassName, bodyContent, infoString, tagLibrary, asList(attributes));
    }

    public TagInfo(String tagName, String tagClassName, String bodyContent, String infoString, TagLibraryInfo tagLibrary, List<TagAttributeInfo> attributes) {
        this.tagName = tagName;
        this.infoString = infoString;
        this.tagClassName = tagClassName;
        this.bodyContent = bodyContent;
        this.tagLibrary = tagLibrary;
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getInfoString() {
        return infoString;
    }

    public void setInfoString(String infoString) {
        this.infoString = infoString;
    }

    public String getTagClassName() {
        return tagClassName;
    }

    public void setTagClassName(String tagClassName) {
        this.tagClassName = tagClassName;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public TagLibraryInfo getTagLibrary() {
        return tagLibrary;
    }

    public void setTagLibrary(TagLibraryInfo tagLibrary) {
        this.tagLibrary = tagLibrary;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<TagAttributeInfo> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<TagVariableInfo> getVariables() {
        return variables;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<VariableInfo> getRuntimeVariables() {
        return runtimeVariables;
    }

}
