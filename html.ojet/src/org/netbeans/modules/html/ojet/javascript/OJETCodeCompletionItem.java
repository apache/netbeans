/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.javascript;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.html.ojet.data.DataItem;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class OJETCodeCompletionItem implements CompletionProposal {

    private final int anchorOffset;
    private final ElementHandle element;

    public OJETCodeCompletionItem(final ElementHandle element, final int anchorOffset) {
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
        OJETUtils.logUsage(null);
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

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return element.getName();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return "";
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

    public static class OJETComponentItem extends OJETCodeCompletionItem {

        private final DataItem component;
        private final CodeCompletionContext context;

        public OJETComponentItem(DataItem component, CodeCompletionContext ccContext) {
            super(new DocSimpleElement(component.getName(), component.getDocumentation(), ElementKind.CLASS), ccContext.getCaretOffset());
            this.component = component;
            this.context = ccContext;
        }

        @Override
        public ImageIcon getIcon() {
            return OJETUtils.OJET_ICON;
        }

        @Override
        public int getAnchorOffset() {
            return context.getCaretOffset() - context.getPrefix().length();
        }

        @Override
        public String getCustomInsertTemplate() {
            String result = component.getName();
            TokenHierarchy th = TokenHierarchy.get(context.getParserResult().getSnapshot().getSource().getDocument(true));
            if (th != null) {
                TokenSequence<JsTokenId> ts = LexerUtils.getTokenSequence(th, context.getCaretOffset(), JsTokenId.javascriptLanguage(), false);
                if (ts != null) {
                    int diff = ts.move(context.getCaretOffset());
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        Token<JsTokenId> token = ts.token();
                        JsTokenId id = token.id();
                        if (id == JsTokenId.UNKNOWN && ts.movePrevious()) {
                            token = ts.token();
                            id = token.id();
                        }

                        boolean isInString = (id == JsTokenId.STRING_BEGIN || id == JsTokenId.STRING);
                        if (!isInString) {
                            result = '\'' + result + '\'';
                        }
                    }
                }
            }
            return result;
        }

    }

    public static class OJETComponentOptionItem extends OJETComponentItem {
        private final DataItem option;
        
        public OJETComponentOptionItem(DataItem option, CodeCompletionContext ccContext) {
            super(option, ccContext);
            this.option = option;
        }
        
        @Override
        public String getCustomInsertTemplate() {
            return option.getName() + ": "; //NOI18N
        }
    }
    
    public static class OJETComponentEventItem extends OJETComponentOptionItem {
        private static  ImageIcon eventIcon = null;
        
        public OJETComponentEventItem(DataItem option, CodeCompletionContext ccContext) {
            super(option, ccContext);
        }

        @Override
        public ImageIcon getIcon() {
            if (eventIcon == null) {
                eventIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/html/ojet/ui/resources/ojetEvent-icon.png")); //NOI18N
            }
            return eventIcon;
        }
        
        
    }
}
