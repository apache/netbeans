/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
        final public int anchor;
        final public String prefix;
        final public ParserResult parserResult;

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
