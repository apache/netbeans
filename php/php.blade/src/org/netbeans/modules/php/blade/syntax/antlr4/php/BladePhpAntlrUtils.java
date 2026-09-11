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
package org.netbeans.modules.php.blade.syntax.antlr4.php;

import org.netbeans.modules.php.blade.syntax.antlr4.v10.*;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser.*;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 *
 * @author bogdan
 */
public class BladePhpAntlrUtils {

    public static AntlrTokenSequence lexerStringScan(String text) {
        CharStream cs = CharStreams.fromString(text);
        BladePhpAntlrLexer lexer = new BladePhpAntlrLexer(cs);
        AntlrTokenSequence tokens = new AntlrTokenSequence(lexer);
        return tokens;
    }

    public static Token getToken(String text, int offset) {
        AntlrTokenSequence tokens = lexerStringScan(text);
        if (offset > text.length()){
            return null;
        }
        
        if (tokens.isEmpty()){
            return null;
        }
        
        tokens.seekTo(offset);

        if (!tokens.hasNext()){
            return null;
        }

        Token token = tokens.next().get();
        return token;
    }
}
