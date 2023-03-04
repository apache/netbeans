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
package org.netbeans.modules.css.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssParserFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.css.lib.nbparser.CssParser;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class TestUtil {
    
    public static final String bodysetPath = "styleSheet/body/bodyItem/";

    public static void setPlainSource() {
        ExtCss3Parser.isScssSource_unit_tests = false;
        ExtCss3Parser.isLessSource_unit_tests = false;
        ExtCss3Lexer.isScssSource_unit_tests = false;
        ExtCss3Lexer.isLessSource_unit_tests = false;
    }
    
    public static void setScssSource() {
        ExtCss3Parser.isScssSource_unit_tests = true;
        ExtCss3Parser.isLessSource_unit_tests = false;
        ExtCss3Lexer.isScssSource_unit_tests = true;
        ExtCss3Lexer.isLessSource_unit_tests = false;
    }
    
    public static void setLessSource() {
        ExtCss3Parser.isScssSource_unit_tests = false;
        ExtCss3Parser.isLessSource_unit_tests = true;
        ExtCss3Lexer.isScssSource_unit_tests = false;
        ExtCss3Lexer.isLessSource_unit_tests = true;
    }
    
    public static CssParserResult parse(String code) {
        try {
            Document doc = new PlainDocument();
            doc.putProperty("mimeType", "text/css");
            doc.insertString(0, code, null);
            Source source = Source.create(doc);
            return parse(source);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static CssParserResult parse(FileObject file) throws ParseException, BadLocationException, IOException {
        //no loader here so we need to create the swing document
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();

        char[] buffer = new char[8096];
        int read;
        while ((read = reader.read(buffer)) > 0) {
            builder.append(buffer, 0, read);
        }
        reader.close();
        return parse(builder.toString());
    }

    public static CssParserResult parse(Source source) throws ParseException, org.netbeans.modules.parsing.spi.ParseException {
        return parse(source, null);
    }
    
    public static CssParserResult parse(Source source, String topLevelSnapshotMimetype) throws ParseException, org.netbeans.modules.parsing.spi.ParseException {
        CssParser parser = new CssParser(topLevelSnapshotMimetype);
        parser.parse(source.createSnapshot(), null, null);
        return parser.getResult(null);
    }

    public static void dumpResult(CssParserResult result) {
        System.out.println("Parse Tree:");
        NodeUtil.dumpTree(result.getParseTree());
        Collection<ProblemDescription> problems = result.getParserDiagnostics();
        if (!problems.isEmpty()) {
            System.out.println(String.format("Found %s problems while parsing:", problems.size()));
            for (ProblemDescription pp : problems) {
                System.out.println(pp);
            }
        }

    }
    
    public static void dumpTokens(CssParserResult result) {
        System.out.println("Tokens:");
        TokenSequence<CssTokenId> ts = result.getSnapshot().getTokenHierarchy().tokenSequence(CssTokenId.language());
        while (ts.moveNext()) {
            System.out.println(ts.offset() + "-" + (ts.token().length() + ts.offset()) + ": " + ts.token().text() + "(" + ts.token().id() + ")");
        }
        System.out.println("-------------");
    }
    
    public static void dumpTokens(Css3Lexer lexer) {
        System.out.println("Tokens:");
        CommonToken t;
        while ((t = (CommonToken)lexer.nextToken()) != null) {
            System.out.println(
                    t.getStartIndex() + "-" + t.getStopIndex() 
                    + ": " + t.getText() + "(" + (t.getType() == -1 ? "" : Css3Parser.tokenNames[t.getType()]) + ")");
            
            if(t.getType() == Css3Lexer.EOF) {
                break;
            }
        }
        System.out.println("-------------");
    }
}
