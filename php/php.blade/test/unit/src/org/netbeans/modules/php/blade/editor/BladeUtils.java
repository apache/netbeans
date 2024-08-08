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
package org.netbeans.modules.php.blade.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.antlr.v4.runtime.*;
import org.netbeans.modules.php.blade.syntax.antlr4.formatter.BladeAntlrFormatterLexer;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrColoringLexer;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer;

/**
 *
 * @author bhaidu
 */
public final class BladeUtils {

    private BladeUtils() {

    }

    public static String getFileContent(File file) throws Exception {
        StringBuffer sb = new StringBuffer();
        String lineSep = "\n";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public static CommonTokenStream getTokenStream(String content) {
        CharStream stream = CharStreams.fromString(content);
        BladeAntlrLexer lexer = new BladeAntlrLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    public static CommonTokenStream getColoringTokenStream(String content) {
        CharStream stream = CharStreams.fromString(content);
        BladeAntlrColoringLexer lexer = new BladeAntlrColoringLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    public static CommonTokenStream getFormatTokenStream(String content) {
        CharStream stream = CharStreams.fromString(content);
        BladeAntlrFormatterLexer lexer = new BladeAntlrFormatterLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    public static String replaceLinesAndTabs(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("\n", "\\\\n"); //NOI18N
        escapedString = escapedString.replaceAll("\r", "\\\\r"); //NOI18N
        escapedString = escapedString.replaceAll("\t", "\\\\t"); //NOI18N
        return escapedString;
    }
}
