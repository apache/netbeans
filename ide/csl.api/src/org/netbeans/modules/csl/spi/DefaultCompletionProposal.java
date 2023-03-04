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

package org.netbeans.modules.csl.spi;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;

/**
 * Default implementation of a CompletionProposal with some useful base
 * functionality.
 *
 * @author Tor Norbye
 */
public abstract class DefaultCompletionProposal implements CompletionProposal {
    protected int anchorOffset;
    protected boolean smart;
    protected ElementKind elementKind;

    /**
     * Called before the defaultAction method is called, i.e. when user
     * invokes the completion proposal and the text is going to be inserted.
     * One may hook here some UI action like css color chooser which then
     *
     * @return true if the defaultActions should be cancelled, false otherwise
     */
    public boolean beforeDefaultAction() {
        return false;
    }

    public int getSortPrioOverride() {
        return 0;
    }

    public String getName() {
        return "";
    }


    public ImageIcon getIcon() {
        return null;
    }

    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public boolean isSmart() {
        return smart;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    public String getInsertPrefix() {
        return getName();
    }

    public String getSortText() {
        return getName();
    }

    public ElementKind getKind() {
        return elementKind;
    }

    public String getLhsHtml(HtmlFormatter formatter) {
        ElementKind kind = getKind();
        formatter.name(kind, true);
        formatter.appendText(getName());
        formatter.name(kind, false);

        return formatter.getText();
    }

    public String getRhsHtml(HtmlFormatter formatter) {
        return null;
    }

    /**
     * Set the kind of this item. Controls what kind of icon or sorting priority
     * is assigned this item by the infrastructure.
     * 
     * @param kind The kind of completion item, such as "class" or "variable"
     */
    public void setKind(ElementKind kind) {
        this.elementKind = kind;
    }

    /** Set whether this item is "smart", e.g. should bubble to the top of
     * the completion list
     *
     * @param smart True iff item is smart
     */
    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    /**
     * Set the anchor offset for this item. The anchor offset is the position
     * that, when this completion item is inserted, should have all text between
     * the caret and the anchor position removed and the completion text inserted.
     *
     * @param anchorOffset
     */
    public void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    /**
     * Parameters to be inserted for this item, if any. Has no effect
     * if getCustomInsertTemplate() returns non null.
     * This will be used to implement getCustomInsertTemplate on your behalf.
     *
     * @return A list of insert parameters
     */
    public List<String> getInsertParams() {
        return null;
    }

    /** The strings to be inserted to start and end a parameter list. Should be a String of length 2.
     * In Java we would expect {(,)}, and in Ruby it's either {(,)} or { ,}.
     *
     * This will be used to implement getCustomInsertTemplate on your behalf.
     */
    public String[] getParamListDelimiters() {
        return null;
    }

    public String getCustomInsertTemplate() {
        List<String> params = getInsertParams();
        if (params == null || params.size() == 0) {
            return getInsertPrefix();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getInsertPrefix());
        String[] delimiters = getParamListDelimiters();
        assert delimiters.length == 2;
        sb.append(delimiters[0]);
        int id = 1;
        for (Iterator<String> it = params.iterator(); it.hasNext();) {
            String paramDesc = it.next();
            sb.append("${"); //NOI18N
            // Ensure that we don't use one of the "known" logical parameters
            // such that a parameter like "path" gets replaced with the source file
            // path!
            sb.append("gsf-cc-"); // NOI18N
            sb.append(Integer.toString(id++));
            sb.append(" default=\""); // NOI18N
            sb.append(paramDesc);
            sb.append("\""); // NOI18N
            sb.append("}"); //NOI18N
            if (it.hasNext()) {
                sb.append(", "); //NOI18N
            }
        }
        sb.append(delimiters[1]);
        sb.append("${cursor}"); // NOI18N

        return sb.toString();
    }
}
