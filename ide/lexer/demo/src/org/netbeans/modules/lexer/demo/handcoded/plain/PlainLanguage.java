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
package org.netbeans.modules.lexer.demo.handcoded.plain;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;

public class PlainLanguage extends AbstractLanguage {

    /** Lazily initialized singleton instance of this language. */
    private static PlainLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized PlainLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new PlainLanguage();

        return INSTANCE;
    }

    public static final int TEXT_INT = 1;


    public static final TokenId TEXT = new TokenId("text", TEXT_INT); // A line of text

    PlainLanguage() {
    }

    public Lexer createLexer() {
        return new PlainLexer();
    }

}
