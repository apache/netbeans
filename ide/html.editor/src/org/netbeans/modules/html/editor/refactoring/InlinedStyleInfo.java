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
package org.netbeans.modules.html.editor.refactoring;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.csl.api.OffsetRange;

public class InlinedStyleInfo {

    private String tag;
    private String tagsClass;
    private String tagsId;
    private String attr;
    private String inlinedCssValue;
    private OffsetRange valueRange;
    private int attributeStartOffset; //<div style=|"color:red"/>
    private int classValueAppendOffset; // <div class="my|" />

    public InlinedStyleInfo(String tag, String tagsClass, String tagId, String attr, 
            int attributeStartOffset, int classValueAppendOffset, OffsetRange range,
            String inlinedCssValue) {
        this.tag = tag;
        this.tagsClass = tagsClass;
        this.tagsId = tagId;
        this.attr = attr;
        this.valueRange = range;
        this.attributeStartOffset = attributeStartOffset;
        this.inlinedCssValue = inlinedCssValue;
        this.classValueAppendOffset = classValueAppendOffset;
    }

    public boolean isValueQuoted() {
        return true; //maybe implement later if nonquoted inlined styles are allowed???
    }

    public String getAttr() {
        return attr;
    }

    public int getAttributeStartOffset() {
        return attributeStartOffset;
    }

    public int getClassValueAppendOffset() {
        return classValueAppendOffset;
    }
    
    public OffsetRange getRange() {
        return valueRange;
    }

    public String getTag() {
        return tag;
    }

    public String getTagsClass() {
        return tagsClass;
    }

    public String getTagsId() {
        return tagsId;
    }

    public String getInlinedCssValue() {
        return inlinedCssValue;
    }

    public List<String> getParsedDeclarations() {
        StringTokenizer st = new StringTokenizer(getInlinedCssValue(), ";"); //NOI18N
        List<String> declarations = new LinkedList<>();
        while(st.hasMoreTokens()) {
            declarations.add(st.nextToken().trim());
        }
        return declarations;
    }

}
