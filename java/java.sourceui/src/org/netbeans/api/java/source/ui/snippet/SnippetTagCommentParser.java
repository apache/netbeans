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
package org.netbeans.api.java.source.ui.snippet;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author aksinsin
 */
public class SnippetTagCommentParser {
    private static final String JAVA_LANG_SOURCE_LINE_START_PATTERN = "^(.*)(";
    private static final String MARKUPTAG_START_PATTERN = "(\\s*@\\s*\\w+.+?))$";
    private final SnippetMarkupTagExtractor extractor = new SnippetMarkupTagExtractor();
    private final Pattern markUpPattern;

    public SnippetTagCommentParser() {
        this.markUpPattern = Pattern.compile(JAVA_LANG_SOURCE_LINE_START_PATTERN + "\\Q//\\E" + MARKUPTAG_START_PATTERN);     
    }
    
    public SnippetTagCommentParser(String langCommentPattern) {
        this.markUpPattern = Pattern.compile(JAVA_LANG_SOURCE_LINE_START_PATTERN + langCommentPattern + MARKUPTAG_START_PATTERN);     
    }
  
    public List<SourceLineMeta> parse(String snippetDocComment) {
        List<SourceLineMeta> fullSourceLineInfo = new ArrayList<>();
        Matcher matcher = markUpPattern.matcher("");
        for (String snippetLine : computeLines(snippetDocComment)) {//get all java codne lines seperated by new line
            SourceLineMeta sourceLine = new SourceLineMeta();
            matcher.reset(snippetLine);
            sourceLine.setActualSourceLine(snippetLine);
            if (matcher.matches()) {
                sourceLine.setSourceLineWithoutComment(matcher.group(1));//First group before single line comment
                List<MarkupTag> markUpTags = extractor.extract(matcher.group(3));// Last group after single line comment, start with @
                sourceLine.setThisLineMarkUpTags(markUpTags);
            } 
            fullSourceLineInfo.add(sourceLine);
        }
        return fullSourceLineInfo;
    }
    
    private List<String> computeLines(String snippetComment) {
        BufferedReader buffReader = new BufferedReader(new StringReader(snippetComment));
        List<String> commentLine = new ArrayList<>();
        String line = "";
        try {
            while ((line = buffReader.readLine()) != null) {
                commentLine.add(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(SnippetTagCommentParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commentLine;
    }
    
}
