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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * Cleanup the generated lexer and parser files from antlr. Antlr writes
 * timestamps and paths to the generated files. This code attempts to clean
 * this up by removing the timestamp and replacing the full path in the header,
 * comments and the method that report the grammar file name.
 */
public class StripAntlrComments {

    // First line pattern:
    // "// $ANTLR 3.5.3 /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2026-10-13 18:48:07"
    private static final Pattern FIRST_LINE_PATTERN = Pattern.compile("(// \\$ANTLR \\S+ )(.+[\\\\/]Css3\\.g)( \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");

    public static void main(String[] args) throws IOException {
        Path lexerPath = Path.of("src/org/netbeans/modules/css/lib/Css3Lexer.java");
        Path parserPath = Path.of("src/org/netbeans/modules/css/lib/Css3Parser.java");

        List<String> lexerCode = Files.readAllLines(lexerPath);
        List<String> parserCode = Files.readAllLines(parserPath);

        Matcher lexerMatcher = FIRST_LINE_PATTERN.matcher(lexerCode.get(0));
        Matcher parserMatcher = FIRST_LINE_PATTERN.matcher(parserCode.get(0));

        if (!lexerMatcher.find()) {
            System.err.println("First line in " + lexerPath + " not in expected format");
            System.exit(1);
        }

        if (!parserMatcher.find()) {
            System.err.println("First line in " + parserPath + " not in expected format");
            System.exit(1);
        }

        String lexerHeaderPath = lexerMatcher.group(2);
        String parserHeaderPath = parserMatcher.group(2);

        if (!lexerHeaderPath.equals(parserHeaderPath)) {
            System.err.println("Paths in first lines of " + lexerPath + " and " + parserPath + " don't match:" );
            System.err.println("Css3Lexer.java:  " + lexerHeaderPath);
            System.err.println("Css3Parser.java: " + parserHeaderPath);
            System.exit(1);
        }

        lexerCode.set(0, lexerMatcher.group(1) + "Css3.g");
        parserCode.set(0, parserMatcher.group(1) + "Css3.g");

        for(int i = 1; i < lexerCode.size(); i++) {
            String strippedLine = lexerCode.get(i)
                    .replace("// " + lexerHeaderPath, "// Css3.g")
                    .replace("\"" + lexerHeaderPath, "\"Css3.g");
            lexerCode.set(i, strippedLine);
        }

        for(int i = 1; i < parserCode.size(); i++) {
            String strippedLine = parserCode.get(i)
                    .replace("// " + lexerHeaderPath, "// Css3g")
                    .replace("\"" + lexerHeaderPath, "\"Css3.g");
            parserCode.set(i, strippedLine);
        }

        Files.writeString(lexerPath, lexerCode.stream().collect(joining("\n")));
        Files.writeString(parserPath, parserCode.stream().collect(joining("\n")));
    }
}
