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
package org.netbeans.modules.languages.hcl;

import org.antlr.v4.runtime.Vocabulary;
import org.netbeans.api.lexer.Token;
import static org.netbeans.modules.languages.hcl.HCLTokenId.*;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import static org.netbeans.modules.languages.hcl.grammar.HCLLexer.*;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class BasicHCLLexer extends AbstractHCLLexer {

    // At the moment HCLLexer.HEREDOC_END is the last token id;
    private static final HCLTokenId[] convert = new HCLTokenId[HCLLexer.HEREDOC_END + 1 ];

    static {
        Vocabulary v = HCLLexer.VOCABULARY;
        for (int i = 1; i < convert.length; i++) {
            String n = v.getSymbolicName(i);
            try {
                convert[i] = HCLTokenId.valueOf(n);
            } catch (IllegalArgumentException ex) {
                // do nothing leave convert[i] null
            }
        }
    }

    public BasicHCLLexer(LexerRestartInfo<HCLTokenId> info) {
        super(info, HCLLexer::new);
    }

    @Override
    protected Token<HCLTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        HCLTokenId id = fromAntlrToken(antlrToken.getType());
        if (id == null) {
            switch (antlrToken.getType()) {
                case HEREDOC_CONTENT:
                    return groupToken(HEREDOC, HEREDOC_CONTENT);
                case STRING_CONTENT:
                    return groupToken(STRING, STRING_CONTENT);

                case INTERPOLATION_CONTENT:
                    return groupToken(INTERPOLATION, INTERPOLATION_CONTENT);

                case TEMPLATE_CONTENT:
                    return groupToken(INTERPOLATION, TEMPLATE_CONTENT);

                default:
                    return token(ERROR);
            }
        } else {
            return token(id);
        }
    }

    @Override
    protected String flyweightText(HCLTokenId id) {
        return id.getFixedText();
    }

    static HCLTokenId fromAntlrToken(int id) {
        if (id < 0 || id >= convert.length) {
            return null;
        }
        return convert[id];
    }
}
