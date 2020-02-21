/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.editor.parser.impl;

import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 */
public interface TokenFilter {

    /**
     * Visits the token if this filter handles tokens of specified type. Creates
     * folders if needed.
     *
     * @param token token to visit.
     */
    void visit(Token<CppTokenId> token);
    
    /**
     * Visits dummy EOF token.
     */
    void visitEof();

    /**
     * Checks if a token of this type will be consumed by filter
     *
     * @param id token id
     * @return true if consumed, false otherwise
     */
    boolean consumes(CppTokenId id);

    /**
     * Get accumulated folders.
     *
     * @return list of folders.
     */
    List<CppFoldRecord> getFolders();

//    /**
//     * Resets this filter state to initial condition and removes accumulated
//     * folders.
//     */
//    void reset();
}
