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
package org.netbeans.modules.php.blade.syntax.antlr4.html_components;

import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.php.blade.syntax.antlr4.utils.BaseBladeAntlrUtils;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 *
 * @author bogdan
 */
public final class BladeHtmlAntlrUtils extends BaseBladeAntlrUtils {

    public static AntlrTokenSequence getTokens(Document doc) {

        try {
            String text = doc.getText(0, doc.getLength());
            return new AntlrTokenSequence(new BladeHtmlAntlrLexer(CharStreams.fromString(text)));
        } catch (BadLocationException ex) {

        }
        return null;
    }
    
    
    public static Token findBackwardWithStop(
            AntlrTokenSequence tokens, int tokenMatch, Set<Integer> stopTokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            if (pt.getType() == tokenMatch) {
                return pt;
            }

            if (stopTokens.contains(pt.getType())) {
                return null;
            }
        }

        return null;
    }
}
