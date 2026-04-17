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
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
class CssIdCompletionItem implements CompletionProposal {
    private final ElementHandle element;
    private final ParserResult parserInfo;
    private final int astOffset;
    private final String name;

    private static ImageIcon cssIcon = null;

    public CssIdCompletionItem(String name, ParserResult info, int astOffset) {
        this.parserInfo = info;
        this.name = name;
        this.astOffset = astOffset;
        this.element = null;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> modifiers;

        if (getElement() == null || getElement().getModifiers().isEmpty()) {
            modifiers = Collections.EMPTY_SET;
        } else {
            modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(getElement().getModifiers());
        }

        if (modifiers.contains(Modifier.PRIVATE) && (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED))) {
            modifiers.remove(Modifier.PUBLIC);
            modifiers.remove(Modifier.PROTECTED);
        }
        return modifiers;
    }

    @Override
    public int getAnchorOffset() {
        return parserInfo.getSnapshot().getOriginalOffset(astOffset);
    }

    @Override
    public ImageIcon getIcon() {
        if (cssIcon == null) {
            cssIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/jquery/resources/style_sheet_16.png", false); //NOI18N
        }
        return cssIcon;
    }

    @Override
    public String getName() {
        return name;
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
        return ElementKind.RULE;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return null;
    }
    
    @Override
    public String getSortText() {
        StringBuilder sb = new StringBuilder();
        if (element != null) {
            FileObject sourceFo = parserInfo.getSnapshot().getSource().getFileObject();
            FileObject elementFo = element.getFileObject();
            if (elementFo != null && sourceFo != null && sourceFo.equals(elementFo)) {
                sb.append("1");     //NOI18N
            } else {
                if (OffsetRange.NONE.equals(element.getOffsetRange(parserInfo))) {
                    sb.append("8");
                } else {
                    sb.append("9");     //NOI18N
                }
            }
        }
        sb.append(getName());    
        return sb.toString();
    }
    
    protected boolean isDeprecated() {
        return element.getModifiers().contains(Modifier.DEPRECATED);
    }
    
    protected void formatName(HtmlFormatter formatter) {
        if (isDeprecated()) {
            formatter.deprecated(true);
            formatter.appendText(getName());
            formatter.deprecated(false);
        } else {
            formatter.appendText(getName());
        }
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

    public String getCustomInsertTemplate() {
        return null;
    }

    public final String getFileNameURL() {
        ElementHandle elem = getElement();
        if (elem == null) {
            return null;
        }
        FileObject fo = elem.getFileObject();
        if (fo != null) {
            return fo.getNameExt();
        }
        return getName();
     }
    
}
