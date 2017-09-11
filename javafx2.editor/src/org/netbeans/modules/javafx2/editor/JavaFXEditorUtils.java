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
package org.netbeans.modules.javafx2.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CompletionProposal;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
public final class JavaFXEditorUtils {
    /**
     * The FXML ns URI
     */
    public static final String FXML_FX_NAMESPACE = "http://javafx.com/fxml"; // NOI18N
    public static final String FXML_FX_NAMESPACE_CURRENT = "http://javafx.com/fxml/1"; // NOI18N
    public static final String FXML_FX_PREFIX = "fx"; // NOI18N
    
    public static final String FXML_MIME_TYPE = "text/x-fxml+xml"; // NOI18N
    public static final String FXML_FILE_EXTENSION = "fxml"; // NOI18N
    public static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    public static final String FXML_NODE_CLASS = "javafx.scene.Node"; // NOI18N
    
    private JavaFXEditorUtils() {
    }

    // Copied from web.common LexerUtils to not have web module dependency
    public static List<CompletionProposal> filterCompletionProposals(List<CompletionProposal> proposals, CharSequence prefix, boolean ignoreCase) {
        List<CompletionProposal> filtered = new ArrayList<CompletionProposal>();
        for (CompletionProposal proposal : proposals) {
            if (startsWith(proposal.getInsertPrefix(), prefix, ignoreCase, false)) {
                filtered.add(proposal);
            }
        }
        return filtered;
    }

    /**
     * @param optimized - first sequence is lowercase, one call to
     * Character.toLowerCase() only
     */
    public static boolean startsWith(CharSequence text1, CharSequence prefix, boolean ignoreCase, boolean optimized) {
        if (text1.length() < prefix.length()) {
            return false;
        } else {
            return equals(text1.subSequence(0, prefix.length()), prefix, ignoreCase, optimized);
        }
    }

    /**
     * @param optimized - first sequence is lowercase, one call to
     * Character.toLowerCase() only
     */
    public static boolean equals(CharSequence text1, CharSequence text2, boolean ignoreCase, boolean optimized) {
        if (text1.length() != text2.length()) {
            return false;
        } else {
            //compare content
            for (int i = 0; i < text1.length(); i++) {
                char ch1 = ignoreCase && !optimized ? Character.toLowerCase(text1.charAt(i)) : text1.charAt(i);
                char ch2 = ignoreCase ? Character.toLowerCase(text2.charAt(i)) : text2.charAt(i);
                if (ch1 != ch2) {
                    return false;
                }
            }
            return true;
        }
    }

    public static Token followsToken(TokenSequence ts, Collection<? extends TokenId> searchedIds, boolean backwards, boolean repositionBack, TokenId... skipIds) {
        Collection<TokenId> skip = Arrays.asList(skipIds);
        int index = ts.index();
        while (backwards ? ts.movePrevious() : ts.moveNext()) {
            Token token = ts.token();
            TokenId id = token.id();
            if (searchedIds.contains(id)) {
                if (repositionBack) {
                    int idx = ts.moveIndex(index);
                    boolean moved = ts.moveNext();

                    assert idx == 0 && moved;

                }
                return token;
            }
            if (!skip.contains(id)) {
                break;
            }
        }
        return null;
    }
}
