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
