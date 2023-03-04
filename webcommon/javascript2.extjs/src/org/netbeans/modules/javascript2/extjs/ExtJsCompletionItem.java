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
package org.netbeans.modules.javascript2.extjs;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Petr Pisl
 */
public class ExtJsCompletionItem implements CompletionProposal {
    
    static CompletionProposal createExtJsItem(ExtJsDataItem item, int anchorOffset) {
        ElementHandle element = new ExtJsElement(item.getName(), item.getDocumentation(), ElementKind.PROPERTY);
        return new ExtJsCompletionItem(item, anchorOffset, element);
    }
    
    private final int anchorOffset;
    private final ElementHandle element;
    private final ExtJsDataItem dataItem;

    public ExtJsCompletionItem(ExtJsDataItem item, int anchorOffset, ElementHandle element) {
        this.anchorOffset = anchorOffset;
        this.element = element;
        this.dataItem = item;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendText(getName());
        if (dataItem.getType() != null) {
            formatter.appendText(": "); //NOI18N
            formatter.type(true);
            formatter.appendText(dataItem.getType());
            formatter.type(false);
        }
        return formatter.getText();
    }

    @Messages("ExtJsCompletionItem.lbl.extjs.framework=Ext JS")
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return Bundle.ExtJsCompletionItem_lbl_extjs_framework();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 22;
    }

    @Override
    public String getCustomInsertTemplate() {
        if (dataItem.getTemplate() != null) {
            return getName() + ": " + dataItem.getTemplate().trim(); //NOI18N
        }
        return null;
    }

    
}
