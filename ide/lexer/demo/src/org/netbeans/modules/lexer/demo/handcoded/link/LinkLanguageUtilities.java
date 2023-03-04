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

package org.netbeans.modules.lexer.demo.handcoded.link;

import org.netbeans.api.lexer.TokenCategory;
import org.netbeans.api.lexer.TokenId;

/**
 * Various utility methods over LinkLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LinkLanguageUtilities {

    private static final LinkLanguage language = LinkLanguage.get();

    private static final TokenCategory linkCategory = language.getCategory("link");


    private LinkLanguageUtilities() {
        // no instances
    }

    /**
     * Does the given tokenId represent a link token?
     * @param id tokenId to check. It must be part of the LinkLanguage.
     * @return true if the id is in link category.
     */
    public static boolean isLink(TokenId id) {
        return linkCategory.isMember(id);
    }

}
