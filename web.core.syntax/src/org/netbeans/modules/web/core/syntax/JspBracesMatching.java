/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.core.syntax;

import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Super simple implementation of BracesMatcher. The performance is not good since
 * the logic break some rules defined in the SPI - findOrigin() method
 * is quite cost (uses two match searches) and the searches goes beyond the limited area.
 * Needs to be reimplemented later.
 *
 * @author Marek Fukala
 */
public class JspBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    
    public JspBracesMatching() {
        this(null, null);
    }
    
    private JspBracesMatching(MatcherContext context, LanguagePath htmlLanguagePath) {
        this.context = context;
    }
    
    //use two searches to find the original area :-|
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(context.getDocument());
            int searchOffset = context.getSearchOffset();
            int[] found = syntaxSupport.findMatchingBlock(searchOffset, false);
            if(found == null) {
                return null;
            }
            int[] opposite = syntaxSupport.findMatchingBlock(found[0], false);
            return opposite;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(context.getDocument());
            int searchOffset = context.getSearchOffset();
            return syntaxSupport.findMatchingBlock(searchOffset, false);
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    //BracesMatcherFactory implementation
    public BracesMatcher createMatcher(final MatcherContext context) {
        final JspBracesMatching[] ret = { null };
        context.getDocument().render(new Runnable() {
            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());
                List<TokenSequence<?>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
                for (TokenSequence ts : ets) {
                    Language language = ts.language();
                    if (language == JspTokenId.language()) {
                        ret[0] = new JspBracesMatching(context, ts.languagePath());
                    }
                }
                // We might be trying to search at the end or beginning of a document. In which
                // case there is nothing to find and/or search through, so don't create a matcher.
                //        throw new IllegalStateException("No text/x-jsp language found on the MatcherContext's search offset! This should never happen!");
            }
        });
        return ret[0];
    }
    
}
