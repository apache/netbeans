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
