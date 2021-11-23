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
    final Pattern markUpPattern;
    SnippetMarkupTagExtractor extractor = new SnippetMarkupTagExtractor();
    public SnippetTagCommentParser() {
        markUpPattern = Pattern.compile("^(.*)(" + Pattern.quote("//")
                    + "(\\s*@\\s*\\w+.+?))$");
    }
    
    
    public static void main(String[] args) {
        SnippetTagCommentParser parser = new SnippetTagCommentParser();
        List<SourceLineMeta> parseResult = parser.parse(parser.getCommentCode());
        processParseResult(parseResult);
    }

    private static void processParseResult(List<SourceLineMeta> parseResult) {
        for(SourceLineMeta fullLineInfo : parseResult){
            String uncommentLine = fullLineInfo.getUncommentSourceLine();
            if(fullLineInfo.getThisLineMarkUpTags() == null) {
                System.out.println(fullLineInfo.getActualSourceLine());
                continue;
            }
            for(MarkUpTag markUpTag : fullLineInfo.getThisLineMarkUpTags()) {
                if(markUpTag.tagName.equals("highlight")){
                    for(MarkUpTagAttribute markUpTagAttribute : markUpTag.markUpTagAttributes){
                        if(markUpTagAttribute.getName().equals("regex")){
                            Pattern pattern = Pattern.compile(markUpTagAttribute.getValue());
                            Matcher matcher = pattern.matcher(uncommentLine);
                            while (matcher.find()) {
                                String replace = "";
                                if (markUpTagAttribute.getValue().contains("\\")) {
                                    replace = markUpTagAttribute.getValue().replace("\\", "\\\\");
                                }
                                String s = matcher.replaceAll(replace);
                                System.out.println(s);
                            }
                        } else if(markUpTagAttribute.getName().equals("substring")){

                        }

                    }
                }
            }
        }
    }

    List<SourceLineMeta> parse(String snippetComment) {
        List<SourceLineMeta> fullSourceLineInfo = new ArrayList<>();
        Matcher matcher = markUpPattern.matcher("");
        for (Map.Entry<String, Integer> entry : computeLinesAndPosition(snippetComment).entrySet()) {
            SourceLineMeta sourceLine = new SourceLineMeta();
            matcher.reset(entry.getKey());
            sourceLine.setActualSourceLine(entry.getKey());
            sourceLine.setActualSourceLineStartPos(entry.getValue());
            if (matcher.matches()) {
                sourceLine.setSourceLineWithoutComment(matcher.group(1));
                List<MarkUpTag> extract = extractor.extract(matcher.group(3));
                sourceLine.setThisLineMarkUpTags(extract);
            } else{
                //System.out.println("unmatched : "+entry.getKey());
            }
            fullSourceLineInfo.add(sourceLine);
        }
        return fullSourceLineInfo;
    }
    
    Map<String, Integer> computeLinesAndPosition(String snippetComment) {
        BufferedReader buffReader = new BufferedReader(new StringReader(snippetComment));
        Map<String, Integer> commentLinePosMap = new LinkedHashMap<>();
        String line;
        int startPos = 0;
        try {
            while ((line = buffReader.readLine()) != null) {
                commentLinePosMap.put(line, startPos);
                startPos = startPos + line.length() + "\n".length();
                //extractMarkUpTag(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(SnippetTagCommentParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commentLinePosMap;
    }
    void extractMarkUpTag(String tagLine){
        List<MarkUpTag> extract = extractor.extract(tagLine);
        for(MarkUpTag markUpTag : extract) {
            System.out.println("TagName = " + markUpTag.tagName);
            for (MarkUpTagAttribute attribute : markUpTag.markUpTagAttributes) {
                System.out.println("attribute name = " + attribute.getName());
                System.out.println("attribute value = " + attribute.getValue());
            }
        }
    }
    private String getCommentCode() {
        return "public static void main(String... args) {\n"
                + "                                            for (var arg : args) {                 // @highlight regex = \"\\barg\\b\"\n"
                + "                                                if (!arg.isBlank()) {\n"
                + "                                                    System.out.println(arg);\n"
                + "                                                }\n"
                + "                                            }"
                + "                                        }";
    }
    
    

}
