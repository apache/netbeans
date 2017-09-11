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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssCompletionItem;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssEditorModule.class)
public class MediaQueriesModule extends CssEditorModule {

    private static final String[] MEDIA_TYPES 
            = new String[]{"all", "aural", "braille", "embossed", "handheld", 
                "print", "projection", "screen", "tty", "tv"}; //NOI18N
    
    private static enum MediaFeature {
        
        width(true), 
        height(true), 
        device_width(true), 
        device_height(true),
        orientation(false),
        aspect_ratio(true),
        device_aspect_ratio(true),
        color(true),
        color_index(true),
        monochrome(true),
        resolution(true),
        scan(false),
        grid(false); 
        
        private Collection<String> names;

        private MediaFeature(boolean minMax) {
            String baseName = name().replace('_', '-');
            names = new ArrayList<>(minMax ? 3 : 1);
            names.add(baseName);
            if(minMax) {
                names.add("max-" + baseName);
                names.add("min-" + baseName);
            }
        }
        
        private Collection<String> getNames() {
            return names;
        }
        
    }
    
    private static Collection<String> MEDIA_FEATURE_NAMES;
    
    /* test */ static Collection<String> getMediaFeatures() {
        if(MEDIA_FEATURE_NAMES == null) {
            MEDIA_FEATURE_NAMES = new ArrayList<>();
            for(MediaFeature mf : MediaFeature.values()) {
                MEDIA_FEATURE_NAMES.addAll(mf.getNames());
            }
        }
        return MEDIA_FEATURE_NAMES;
    }
    
    static ElementKind NAMESPACE_ELEMENT_KIND = ElementKind.GLOBAL; //XXX fix CSL

    @Override
    public List<CompletionProposal> getCompletionProposals(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();
        Node node = context.getActiveNode();

        //switch to first non error node
        loop:
        for (;;) {
            switch (node.type()) {
                case error:
                case recovery:
                    node = node.parent();
                default:
                    break loop;
            }
        }

        switch (node.type()) {
            case bodyItem:
                if(context.getActiveTokenId() == CssTokenId.WS) {
                    //no prefix
                    if(null != LexerUtils.followsToken(context.getTokenSequence(), 
                            CssTokenId.MEDIA_SYM, true, true, CssTokenId.WS, CssTokenId.NL)) {
                        //@media |
                        proposals.addAll(getMediaTypes(context));
                    }
                }
            break;
                
            case mediaType:
                //in media type (screen, print,...)
                proposals.addAll(getMediaTypes(context));
                break;
            
            case mediaQueryList:
                if(context.getActiveTokenId() == CssTokenId.COMMA 
                        || LexerUtils.followsToken(context.getTokenSequence(), CssTokenId.COMMA, true, true, CssTokenId.WS) != null) {
                    //caret after comma in mediaQuery: @media screen, |
                    //=> fallback to mediaQuery case
                } else {
                    break;
                }
            case media:
                //check if we are in the mediaQuery section and not in the media body
                if(null != LexerUtils.followsToken(context.getTokenSequence(), CssTokenId.LBRACE, true, true, CssTokenId.WS, CssTokenId.NL)) {
                    //@media xxx { | }
                    break;
                } 
                //fallback to the mediaQuery
            case mediaQuery:
                //no prefix
                proposals.addAll(getMediaTypes(context));
                break;
                
            case mediaFeature:
                //in media feature (max-width,...)
                proposals.addAll(getMediaFeatures(context));
                break;
                
            case mediaExpression:
                //no prefix
                proposals.addAll(getMediaFeatures(context));
                break;
                
        }

        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static List<CompletionProposal> getMediaFeatures(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();
        for(String mtype : getMediaFeatures()) {
            proposals.add(CssCompletionItem.createRAWCompletionItem(null, mtype, ElementKind.FIELD, context.getAnchorOffset(), true));
        }
        return proposals;
    }
    
    private static List<CompletionProposal> getMediaTypes(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();
        for(String mtype : MEDIA_TYPES) {
            proposals.add(CssCompletionItem.createRAWCompletionItem(null, mtype, ElementKind.OTHER, context.getAnchorOffset(), true));
        }
        return proposals;
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case mediaType:
                        //screen, printer, ...
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CUSTOM3_SET);
                        break;
                        
                    case mediaFeature:
                        //max-width, ...
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CUSTOM1_SET);
                        break;
                        
                }
                return false;
            }
        };
    }

}
