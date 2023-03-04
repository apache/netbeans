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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class JadeCompletionItem implements CompletionProposal {

    protected final CompletionRequest request;
    protected final ElementHandle element;

    public static CompletionProposal create(CompletionRequest request, HtmlTag tag) {
        ElementHandle element = new HtmlTagElement(tag);
        return new JadeCompletionItem(request, element);
    }
    
    public static CompletionProposal create(CompletionRequest request, HtmlTagAttribute attribute) {
        ElementHandle element = new HtmlTagAttributeElement(attribute);
        return new JadeCompletionItem(request, element);
    }
    
    public static CompletionProposal createCssItem(CompletionRequest request, String name, String cssPrefix) {
        ElementHandle element = new SimpleElement(name, ElementKind.RULE);
        return new CssItem(request, element, cssPrefix);
    }
    
    public JadeCompletionItem(CompletionRequest request, ElementHandle element) {
        this.request = request;
        this.element = element;
    }
    
    @Override
    public int getAnchorOffset() {
        return request.anchor;
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
        return getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return null;
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
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 200;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
    
    protected static class CompletionRequest {
        public final int anchor;
        public final String prefix;
        public final ParserResult parserResult;

        public CompletionRequest(ParserResult parserResult, int anchor, String prefix) {
            this.anchor = anchor;
            this.prefix = prefix;
            this.parserResult = parserResult;
        }
    }
    
//    public static class HTMLTagCompletionItem extends JadeCompletionItem {
//
//        public HTMLTagCompletionItem(ElementHandle element, CompletionRequest request) {
//            super(request, element);
//        }
//
//        @Override
//        public int getAnchorOffset() {
//            return super.getAnchorOffset();
//        }
//
//
//    }
    
    static class KeywordItem extends JadeCompletionItem {
        private static  ImageIcon keywordIcon = null;
        private String keyword = null;

        public KeywordItem(String keyword, CompletionRequest request) {
            super(request, null);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/jade/resources/jade16.png")); //NOI18N
            }
            return keywordIcon;
        }
        
        @Override
        public String getInsertPrefix() {
            return getName();
        }
        
        @Override
        public String getCustomInsertTemplate() {
            return getName();
        }

        @Override
        public int getSortPrioOverride() {
            return 130;
        }
    }
    
    static class CssItem extends JadeCompletionItem {
        private final String cssPrefix;
        
        public CssItem(CompletionRequest request, ElementHandle element, final String cssPrefix) {
            super(request, element);
            this.cssPrefix = cssPrefix;
        }
        
        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.appendText(cssPrefix);
            formatter.appendText(getName());
            return formatter.getText();
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
        
        public Documentation getDocumentation() {
            return null;
        }
    }
    
    public static class HtmlTagElement extends SimpleElement {
        
        final HtmlTag tag;
        
        public HtmlTagElement(HtmlTag tag) {
            super(tag.getName(), ElementKind.TAG);
            this.tag = tag;
        }

        @Override
        public Documentation getDocumentation() {
            HelpItem help = tag.getHelp();
            if (help != null) {
                if (help.getHelpContent() != null) {
                    if (help.getHelpURL() != null) {
                        return Documentation.create(help.getHelpContent(), help.getHelpURL());
                    }
                    return Documentation.create(help.getHelpContent());
                } else if (help.getHelpResolver() != null && help.getHelpURL() != null) {
                    String content = help.getHelpResolver().getHelpContent(help.getHelpURL());
                    if (content != null) {
                        return Documentation.create(content, help.getHelpURL());
                    }
                }
            }
            return null;
        }
    }
    
    public static class HtmlTagAttributeElement extends SimpleElement {
        
        final HtmlTagAttribute attribute;
        
        public HtmlTagAttributeElement(HtmlTagAttribute attribute) {
            super(attribute.getName(), ElementKind.ATTRIBUTE);
            this.attribute = attribute;
        }

        @Override
        public Documentation getDocumentation() {
            HelpItem help = attribute.getHelp();
            if (help != null) {
                if (help.getHelpContent() != null) {
                    if (help.getHelpURL() != null) {
                        return Documentation.create(help.getHelpContent(), help.getHelpURL());
                    }
                    return Documentation.create(help.getHelpContent());
                } else if (help.getHelpResolver() != null && help.getHelpURL() != null) {
                    String content = help.getHelpResolver().getHelpContent(help.getHelpURL());
                    if (content != null) {
                        return Documentation.create(content, help.getHelpURL());
                    }
                }
            }
            return null;
        }
        
        
    }
    
}
