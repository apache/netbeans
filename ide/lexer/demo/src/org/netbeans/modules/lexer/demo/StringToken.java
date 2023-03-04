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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.inc.RawOffsetToken;
import org.netbeans.spi.lexer.util.IntegerCache;

/**
 * Simple token implementation for demo purposes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class StringToken implements Token {

    private final TokenId id;

    private final String text;

    StringToken(TokenId id, String text) {
        if (id == null) {
            throw new NullPointerException();
        }
        
        if (text == null) {
            throw new NullPointerException();
        }
        
        this.id = id;
        this.text = text;
    }
    
    public TokenId getId() {
        return id;
    }

    public CharSequence getText() {
        return (CharSequence)(Object)text; // jdk1.3 compilability
    }
    
    public String toString() {
        return text;
    }

}

