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

package org.netbeans.api.java.source.gen;

import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;

/**
 * Utility class for tree comparison.
 *
 * @author Pavel Flaska
 */
public class TreeChecker {

    /** Creates a new instance of TreeChecker */
    private TreeChecker() {
    }

    static boolean compareTrees(Tree firstTree, Tree secondTree) {
        return true;
    }
    
    static Map<Object, CharSequence[]> compareTokens(TokenHierarchy hierarchy0, TokenHierarchy hierarchy1) {
        Map result = new HashMap<Integer, String[]>();
        TokenSequence ts0 = hierarchy0.tokenSequence(JavaTokenId.language());
        TokenSequence ts1 = hierarchy1.tokenSequence(JavaTokenId.language());
        while (ts0.moveNext()) {
            if (ts1.moveNext()) {
                if (!TokenUtilities.equals(ts0.token().text(), ts1.token().text())) {
                   result.put(ts0.token().id(), new CharSequence[] { ts0.token().text(), ts1.token().text() });
                }
            }
        }
        if (result.size() > 0) {
           return result;
        } else {
           return Collections.EMPTY_MAP;
        }
    }
}
