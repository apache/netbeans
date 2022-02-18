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

package org.netbeans.modules.profiler.oql.language;

import org.netbeans.api.lexer.TokenId;


/**
 *
 * @author Jan Jancura
 */
public enum OQLTokenId implements TokenId {
    WHITESPACE("whitespace"),
    BRACE("brace"),
    COMMA("comma"),
    DOT("dot"),
    SELECT("select"),
    FROM("from"),
    WHERE("where"),
    INSTANCEOF("instanceof"),
    IDENTIFIER("identifier"),
    JSBLOCK("js-block"),
    UNKNOWN("unknown"),
    CLAZZ("clazz"),
    CLAZZ_E("clazz-typo"),
    ERROR("error");

    private final String primaryCategory;

    OQLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
}
