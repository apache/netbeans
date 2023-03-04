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

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;

public class LinkLanguage extends AbstractLanguage {

    /** Lazily initialized singleton instance of this language. */
    private static LinkLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized LinkLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new LinkLanguage();

        return INSTANCE;
    }

    public static final int TEXT_INT = 1;
    public static final int HTTP_URI_INT = 2;
    public static final int FTP_URI_INT = 3;
    public static final int URI_INT = 4;


    public static final TokenId FTP_URI = new TokenId("ftp-uri", FTP_URI_INT, new String[]{"uri"}); // FTP absolute URI
    public static final TokenId HTTP_URI = new TokenId("http-uri", HTTP_URI_INT, new String[]{"uri"}); // HTTP absolute URI
    public static final TokenId TEXT = new TokenId("text", TEXT_INT); // Either whole line of text or a part of a line if a link is present on it
    public static final TokenId URI = new TokenId("uri", URI_INT, new String[]{"uri"}); // Other URI type

    LinkLanguage() {
    }

    public Lexer createLexer() {
        return new LinkLexer();
    }

}
