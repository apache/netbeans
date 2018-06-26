/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jquery.editor;

import org.netbeans.modules.javascript2.jquery.PropertyNameDataItem;
import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public abstract class JQueryCompletionItem implements CompletionProposal {

    private final int anchorOffset;
    private final ElementHandle element;

    public static CompletionProposal create(HtmlTag tag, int anchorOffset, String surround) {
        ElementHandle element = new SimpleElement(tag.getName(), ElementKind.TAG);
        return new HTMLTagCompletionItem(element, anchorOffset, surround);
    }
    
    public static CompletionProposal create(HtmlTagAttribute attribute, int anchorOffset, String surround) {
        ElementHandle element = new SimpleElement(attribute.getName(), ElementKind.ATTRIBUTE);
        return new HTMLTagCompletionItem(element, anchorOffset, surround);
    }
    
    public static CompletionProposal createCSSItem(String name, int anchorOffset, String surround) {
        ElementHandle element = new SimpleElement(name, ElementKind.RULE);
        return new CssCompletionItem(element, anchorOffset, surround);
    }

    static CompletionProposal createJQueryItem(String name, int anchorOffset, String surround, String codeTemplate) {
        ElementHandle element = new SimpleElement(name, ElementKind.CALL);
        return new JQuerySimpleItem(element, anchorOffset, surround, codeTemplate);
    }
    
    static CompletionProposal createPropertyNameItem(PropertyNameDataItem item, int anchorOffset, boolean addComma) {
        ElementHandle element = new DocSimpleElement(item.getName(), item.getDocumentation(), ElementKind.PROPERTY);
        return new PropertyNameCompletionItem(item, anchorOffset, element, addComma);
    }

    public JQueryCompletionItem(final ElementHandle element, final int anchorOffset) {
        this.anchorOffset = anchorOffset;
        this.element = element;
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
        return 20;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
    
    public static class HTMLTagCompletionItem extends JQueryCompletionItem {

        private final String surround;
        
        public HTMLTagCompletionItem(ElementHandle element, int anchorOffset, String surround) {
            super(element, anchorOffset);
            this.surround = surround;
        }

        @Override
        public int getAnchorOffset() {
            return super.getAnchorOffset();
        }

        
        public String getSurround() {
            return surround;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getName());
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (surround.isEmpty()) {
                return super.getCustomInsertTemplate();
            } else {
                return surround + getName() + surround;
            }
        }
    }
    
    public static class CssCompletionItem extends HTMLTagCompletionItem {
        
        private static ImageIcon cssIcon = null;
        
        public CssCompletionItem(ElementHandle element, int anchorOffset, String surround) {
            super(element, anchorOffset, surround);
        }

        @Override
        public ImageIcon getIcon() {
            if (cssIcon == null) {
                cssIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/jquery/resources/style_sheet_16.png")); //NOI18N
            }
            return cssIcon;
        }
        
        @Override
        public String getInsertPrefix() {
            String name = super.getInsertPrefix();
            if (name.charAt(0) == '#' || name.charAt(0) == '.') {
                name = name.substring(1);
            }
            return name;
        }
        
        @Override
        public String getCustomInsertTemplate() {
            ElementHandle handle = getElement();
            if (handle != null) {
                return handle.getName();
            }
            return null;
        }

    }
    
    public static class JQuerySimpleItem extends HTMLTagCompletionItem {
        
        private static ImageIcon jQIcon = null;
        private final String template;
        
        public JQuerySimpleItem(ElementHandle element, int anchorOffset, String surround) {
            this(element, anchorOffset, surround, null);
        }
        
        public JQuerySimpleItem(ElementHandle element, int anchorOffset, String surround, String template) {
            super(element, anchorOffset, surround);
            this.template = (template != null && !template.isEmpty() ? ":" + template : element.getName() + "${cursor}");
        }

        @Override
        public String getCustomInsertTemplate() {
            if (getSurround().isEmpty()) {
                return template;
            } else {
                return getSurround() + template + getSurround();
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (jQIcon == null) {
                jQIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/jquery/resources/jquery_16_2.png")); //NOI18N
            }
            return jQIcon;
        }

        @Override
        public String getInsertPrefix() {
            String name = super.getInsertPrefix();
            if (name.charAt(0) == ':') {
                name = name.substring(1);
            }
            return name;
        }
        
        
    }
    
    public static class PropertyNameCompletionItem implements CompletionProposal {

        private final int anchorOffset;
        private final ElementHandle element;
        private final PropertyNameDataItem dataItem;
        private final boolean addComma;

        public PropertyNameCompletionItem(PropertyNameDataItem item, int anchorOffset, ElementHandle element, boolean addComma) {
            this.anchorOffset = anchorOffset;
            this.element = element;
            this.dataItem = item;
            this.addComma = addComma;
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

        @NbBundle.Messages("JQueryCompletionItem.lbl.jquery=jQuery")
        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.JQueryCompletionItem_lbl_jquery();
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
            StringBuilder sb = new StringBuilder();
            sb.append(getName()).append(": "); //NOI18N
            if (dataItem.getTemplate() != null) {
                 sb.append(dataItem.getTemplate().trim()); 
            } else {
                sb.append("${cursor}"); //NOI18N
            }
            if (addComma) {
                sb.append(",");
            }
            return sb.toString();
        }
    }
    
    public static class SimpleElement implements ElementHandle {

        private final String name;
        private final ElementKind kind;

        public SimpleElement(String name, ElementKind kind) {
            this.name = name;
            this.kind = kind;
        }
        
        
        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getIn() {
            return "";
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
    
    public static class DocSimpleElement extends SimpleElement {
        
        private final String documentation;
        
        public DocSimpleElement(String name, String documentation, ElementKind kind) {
            super(name, kind);
            this.documentation = documentation;
        }

        public String getDocumentation() {
            return documentation;
        }
    }
}
