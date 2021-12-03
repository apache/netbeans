package org.netbeans.api.java.source.ui;


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
    private final Pattern markUpPattern = Pattern.compile(JAVA_LANG_SOURCE_LINE_START_PATTERN + "\\Q//\\E"+ MARKUPTAG_START_PATTERN);
    
    List<SourceLineMeta> parse(String snippetComment) {
        List<SourceLineMeta> fullSourceLineInfo = new ArrayList<>();
        Matcher matcher = markUpPattern.matcher("");
        for (String line : computeLines(snippetComment)) {//get all java code lines seperated by new line
            SourceLineMeta sourceLine = new SourceLineMeta();
            matcher.reset(line);
            sourceLine.setActualSourceLine(line);
            if (matcher.matches()) {
                sourceLine.setSourceLineWithoutComment(matcher.group(1));//First group before single line comment
                List<MarkUpTag> extract = extractor.extract(matcher.group(3));// Last group after single line comment, start with @
                sourceLine.setThisLineMarkUpTags(extract);
            } 
            fullSourceLineInfo.add(sourceLine);
        }
        return fullSourceLineInfo;
    }
    
    private List<String> computeLines(String snippetComment) {
        BufferedReader buffReader = new BufferedReader(new StringReader(snippetComment));
        List<String> commentLine = new ArrayList<>();
        String line;
        try {
            while ((line = buffReader.readLine()) != null) {
                commentLine.add(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(SnippetTagCommentParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commentLine;
    }
    
//    Map<String, Integer> computeLinesAndPosition(String snippetComment) {
//        BufferedReader buffReader = new BufferedReader(new StringReader(snippetComment));
//        Map<String, Integer> commentLinePosMap = new LinkedHashMap<>();
//        String line;
//        int startPos = 0;
//        try {
//            while ((line = buffReader.readLine()) != null) {
//                commentLinePosMap.put(line, startPos);
//                startPos = startPos + line.length() + "\n".length();
//                //extractMarkUpTag(line);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(SnippetTagCommentParser.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return commentLinePosMap;
//    }
    
//    List<SourceLineMeta> parse(String snippetComment) {
//        List<SourceLineMeta> fullSourceLineInfo = new ArrayList<>();
//        Matcher matcher = markUpPattern.matcher("");
//        for (Map.Entry<String, Integer> entry : computeLines(snippetComment).entrySet()) {
//            SourceLineMeta sourceLine = new SourceLineMeta();
//            matcher.reset(entry.getKey());
//            sourceLine.setActualSourceLine(entry.getKey());
//            sourceLine.setActualSourceLineStartPos(entry.getValue());
//            if (matcher.matches()) {
//                sourceLine.setSourceLineWithoutComment(matcher.group(1));
//                List<MarkUpTag> extract = extractor.extract(matcher.group(3));
//                sourceLine.setThisLineMarkUpTags(extract);
//            } 
//            fullSourceLineInfo.add(sourceLine);
//        }
//        return fullSourceLineInfo;
//    }
}
