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
package org.netbeans.modules.rust.grammar.antlr4;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.rust.grammar.RustTokenID;
import org.netbeans.modules.rust.grammar.antlr4.RustParser.CrateContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Rust Lexer/Parser utilities.
 */
public final class RustTestUtils {

    private static File getTestFile(File dataDir, String name) throws FileNotFoundException {
        File file = new File(dataDir, name);
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException("Cannot find test file " + file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Visits a test file using a visitor.
     *
     * @param dataDir the result of "getDataDir()" in "NbTestCase".
     * @param fileName The name of the test file under test/unit/data
     * @param dumpTokens True to dump tokens to System.out for debugging.
     * @param visitor An optional visitor to visit each token. Return false to
     * stop visiting.
     * @throws Exception On error
     */
    static void lexFile(File dataDir, String fileName, boolean dumpTokens, Function<Token, Boolean> visitor) throws Exception {
        File testFile = getTestFile(dataDir, fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), StandardCharsets.UTF_8))) {
            lexStream(CharStreams.fromReader(reader), dumpTokens, visitor);
        }
    }

    /**
     * Visits a string using a visitor.
     *
     * @param string The string to visit
     * @param dumpTokens True to dump tokens to System.out for debugging.
     * @param visitor An optional visitor to visit each token. Return false to
     * stop visiting.
     * @throws Exception On error
     */
    static void lexString(String string, boolean dumpTokens, Function<Token, Boolean> visitor) throws Exception {
        lexStream(CharStreams.fromString(string), dumpTokens, visitor);
    }

    private static void lexStream(CharStream charStream, boolean dumpTokens, Function<Token, Boolean> visitor) {
        RustLexer lexer = new RustLexer(charStream);
        lexer.addErrorListener(new RustANTLRTestErrorListener());
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (dumpTokens) {
                System.out.format("Token: %3d:%-3d (%s) = '%s'%n", token.getLine(), token.getCharPositionInLine(), tokenID.name(), token.getText().replace("\n", "\\n"));
            }
            if (visitor != null && !visitor.apply(token)) {
                break;
            }
        }
    }

    /**
     * Parses a Rust file and visits the grammar.
     *
     * @param dataDir The getDataDir() in a NbTestCase.
     * @param testFileName The name of the test file.
     * @param visitor an ParseTreeVisitor<?> to visit the grammar, or null.
     * @return The "CrateContext" corresponding to the "crate" being visited.
     * @throws Exception on error.
     */
    static CrateContext parseFile(File dataDir, String testFileName, ParseTreeVisitor<?> visitor) throws Exception {
        File testFile = getTestFile(dataDir, testFileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), StandardCharsets.UTF_8))) {
            RustANTLRTestErrorListener errorListener = new RustANTLRTestErrorListener();
            RustLexer lexer = new RustLexer(CharStreams.fromReader(reader));
            lexer.addErrorListener(errorListener);
            RustParser parser = new RustParser(new CommonTokenStream(lexer));
            parser.addErrorListener(errorListener);
            CrateContext crate = parser.crate();
            if (visitor != null) {
                crate.accept(visitor);
            }
            return crate;
        }
    }

    public static FileObject getFileObject(File dataDir, String fileName) throws IOException {
        File testFile = new File(dataDir, fileName);
        return FileUtil.toFileObject(testFile);
    }

    public static String getErrorMessages(Collection<DefaultError> errors) {
        return errors.stream().map((e) -> {
            return String.format("%d:%d:%s%n", 
                    e.getStartPosition(), 
                    e.getEndPosition(), 
                    e.getDescription());
        }).collect(Collectors.joining());
    }

    public static String getFileText(File dataDir, String fileName) throws IOException {
        File testFile = new File(dataDir, fileName);
        // When NB is on JDK 11 let's use return Files.readString(testFile.toPath());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(32 * 1024); //
                 FileInputStream input = new FileInputStream(testFile)) {
            byte [] buffer = new byte[16*1024];
            do {
                int n = input.read(buffer);
                if (n < 0) {
                    byte [] bytes = output.toByteArray();
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                output.write(buffer, 0, n);
            } while(true);
        }
    }

}
