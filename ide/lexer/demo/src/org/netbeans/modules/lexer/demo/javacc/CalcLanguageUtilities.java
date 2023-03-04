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

package org.netbeans.modules.lexer.demo.javacc;

import org.netbeans.api.lexer.TokenCategory;
import org.netbeans.api.lexer.TokenId;

/**
 * Various utility methods over CalcLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CalcLanguageUtilities {

    private static final CalcLanguage language = CalcLanguage.get();

    private static final TokenCategory errorCategory = language.getCategory("error");
    private static final TokenCategory incompleteCategory = language.getCategory("incomplete");
    private static final TokenCategory operatorCategory = language.getCategory("operator");
    
    private CalcLanguageUtilities() {
        // no instances
    }
 
    /**
     * Does the given tokenId represent an errorneous lexical construction?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "error" category.
     */
    public static boolean isError(TokenId id) {
        return errorCategory.isMember(id);
    }

    /**
     * Does the given tokenId represent an incomplete token?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "incomplete" category.
     */
    public static boolean isIncomplete(TokenId id) {
        return incompleteCategory.isMember(id);
    }

   /**
     * Does the given tokenId represent an operator?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "operator" category.
     */
    public static boolean isOperator(TokenId id) {
        return operatorCategory.isMember(id);
    }
    
}
