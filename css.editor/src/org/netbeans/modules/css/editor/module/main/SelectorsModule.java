/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The selectors module functionality is partially implemented in the
 * DefaultCssModule from historical reasons. Newly added features are
 * implemented here.
 *
 * Provides pseudo classes and pseudo elements support. The items themselves are
 * provided by the css modules resp. CssModule.getPseudoElements() and
 * CssModule.getPseudoClasses() methods.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssEditorModule.class)
public class SelectorsModule extends CssEditorModule {

    //NOI18N>>>
    private static final Collection<String> PSEUDO_CLASSES = Arrays.asList(new String[]{
        "not",
        "link", "visited", "hover", "active", "focus", //dynamic

        "target",
        "lang",
        "enabled", "disabled", "checked", "indeterminate", //UI

        "root", "nth-child", "nth-last-child", "nth-of-type", "nth-last-of-type",
        "first-child", "last-child", "first-of-type", "last-of-type", "only-child",
        "only-of-type", "empty", //structural

        //following pseudo elements needs to be supported also in the pseudo class form (:: prefix)
        //to be compatible with CSS2. See http://www.w3.org/TR/selectors/#pseudo-elements
        "first-line", "first-letter", "before", "after"

    }); //NOI18N

    private static final Collection<String> PSEUDO_ELEMENTS = Arrays.asList(new String[]{
        "first-line", "first-letter", "before", "after"
    }); //NOI18N

    //<<< NOI18N
    //XXX fix CSL
    static ElementKind PSEUDO_ELEMENT_KIND = ElementKind.GLOBAL;
    static ElementKind PSEUDO_CLASS_KIND = ElementKind.GLOBAL;

    @Override
    public Collection<String> getPseudoClasses(EditorFeatureContext context) {
        return PSEUDO_CLASSES;
    }

    @Override
    public Collection<String> getPseudoElements(EditorFeatureContext context) {
        return PSEUDO_ELEMENTS;
    }

    @Override
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<>();
        Node activeNode = context.getActiveNode();
        Node errorNode = null;
        if (activeNode.type() == NodeType.recovery) {
            activeNode = activeNode.parent();
        }
        if (activeNode.type() == NodeType.error) {
            errorNode = activeNode;
            activeNode = activeNode.parent();
        }

        switch (activeNode.type()) {
            case declaration:
                if (errorNode != null) {
                //div { &:| } completion
                //SASS/LESS - referencing parent selector
                switch (context.getActiveTokenId()) {
                    case IDENT:
                        //div { &:hov| }
                        if (LexerUtils.followsToken(context.getTokenSequence(),
                                Arrays.asList(CssTokenId.COLON, CssTokenId.DCOLON),
                                true, false /* do NOT reposition back! */) == null) {
                            break;
                        } //else fallback!

                    case COLON:
                    case DCOLON:
                        if (LexerUtils.followsToken(context.getTokenSequence(), CssTokenId.LESS_AND, true, true, CssTokenId.WS, CssTokenId.NL) != null) {
                        //& before colon or double colon
                        switch (context.getActiveTokenId()) {
                            case COLON:
                                proposals.addAll(getPseudoClasses(context));
                                break;
                            case DCOLON:
                                proposals.addAll(getPseudoElements(context));
                                break;
                        }
                    }
                        break;
                }
            }
                break;

            case simpleSelectorSequence:
                if (errorNode != null) {
                //test if the previous node is typeSelector:  html:|
                Node siblingBefore = errorNode;
                //possibly skip all elementSubsequent nodes 
                for (;;) {
                    siblingBefore = NodeUtil.getSibling(siblingBefore, true);
                    if (siblingBefore == null) {
                        break;
                    }
                    if (siblingBefore.type() != NodeType.elementSubsequent) {
                        break;
                    }
                }

                if (siblingBefore != null && siblingBefore.type() == NodeType.typeSelector) {
                    switch (context.getTokenSequence().token().id()) {
                        case COLON:
                            proposals.addAll(getPseudoClasses(context));
                            break;
                        case DCOLON:
                            proposals.addAll(getPseudoElements(context));
                            break;
                    }
                }
            }
                break;

            case pseudo:
                switch (context.getTokenSequence().token().id()) {
                case COLON:
                    proposals.addAll(getPseudoClasses(context));
                    break;
                case DCOLON:
                    proposals.addAll(getPseudoElements(context));
                    break;
                case IDENT:
                    if (context.getTokenSequence().movePrevious()) {
                    switch (context.getTokenSequence().token().id()) {
                        case COLON:
                            proposals.addAll(getPseudoClasses(context));
                            break;
                        case DCOLON:
                            proposals.addAll(getPseudoElements(context));
                            break;
                    }
                }
            }
                break;
        }

        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static List<CompletionProposal> getPseudoClasses(CompletionContext context) {
        return Utilities.createRAWCompletionProposals(CssModuleSupport.getPseudoClasses(context), ElementKind.FIELD, context.getAnchorOffset());
    }

    private static List<CompletionProposal> getPseudoElements(CompletionContext context) {
        return Utilities.createRAWCompletionProposals(CssModuleSupport.getPseudoElements(context), ElementKind.FIELD, context.getAnchorOffset());
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case pseudo:
                        getResult().put(Css3Utils.getDocumentOffsetRange(node, snapshot), ColoringAttributes.CLASS_SET);
                        break;
                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return Utilities.createMarkOccurrencesNodeVisitor(context, result, NodeType.pseudo);
    }
}
