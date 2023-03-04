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
package org.netbeans.modules.javascript2.html;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class HtmlCompletionItem implements CompletionProposal {
    private final ParserResult parserInfo;
    private final int astOffset;
    private final ElementHandle element;
    private final HtmlTagAttribute attr;

    public HtmlCompletionItem(ParserResult parserInfo, HtmlTagAttribute attr, int astOffset) {
        this.parserInfo = parserInfo;
        this.attr = attr;
        this.astOffset = astOffset;
        this.element = new HtmlAttrElement(attr);
    }

    @Override
    public String getName() {
        return attr.getName();
    }

    @Override
    public String getInsertPrefix() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @NbBundle.Messages("JsCompletionItem.lbl.html.attribute=HTML Attribute")
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendHtml("<font color=#999999>");
        formatter.appendText(Bundle.JsCompletionItem_lbl_html_attribute());
        formatter.appendHtml("</font>");
        return formatter.getText();
    }

    @Override
    public int getAnchorOffset() {
        return parserInfo.getSnapshot().getOriginalOffset(astOffset);
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getSortText() {
        StringBuilder sb = new StringBuilder();
        if (OffsetRange.NONE.equals(element.getOffsetRange(parserInfo))) {
            sb.append("8");
        } else {
            sb.append("9");     //NOI18N
        }
        sb.append(getName());    
        return sb.toString();
    }
    
    protected boolean isDeprecated() {
        return false;
    }
    
    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isSmart() {
        // TODO implemented properly
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        int order = 100;
        return order;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
}
