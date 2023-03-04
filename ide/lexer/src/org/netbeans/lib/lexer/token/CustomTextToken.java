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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.WrapTokenId;

/**
 * Token with a custom text and the token length likely different
 * from text's length. It can be used to shrink size of the input chars
 * being referenced from skim token list by referencing some fixed characters.
 * <br/>
 * Token with the custom text cannot be branched by a language embedding.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CustomTextToken<T extends TokenId> extends TextToken<T> {
    
    private int length; // 28 bytes (24-super + 4)
    
    /**
     * @param id non-null identification of the token.
     * @param text non-null text of the token.
     * @param length length of the token.
     */
    public CustomTextToken(WrapTokenId<T> wid, CharSequence text, int length) {
        super(wid, text);
        this.length = length;
    }
    
    @Override
    public boolean isCustomText() {
        return true;
    }

    @Override
    public final int length() {
        return length;
    }
    
    @Override
    protected String dumpInfoTokenType() {
        return "CusT"; // NOI18N "TextToken" or "FlyToken"
    }

}
