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
package org.netbeans.api.java.source.ui.snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import junit.framework.TestCase;
import org.netbeans.api.java.source.ui.snippet.MarkupTagProcessor.ApplicableMarkupTag;
import org.netbeans.api.java.source.ui.snippet.MarkupTagProcessor.Region;

/**
 *
 * @author mjayan
 */
public class MarkupTagProcessorTest extends TestCase {
    public MarkupTagProcessorTest(String testName) {
        super(testName);
    }
    
    public void testNoMarkupTag() {
        String testData = "class HelloWorld {}// This is test class";
        String langCommentPattern = "\\Q//\\E";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser(langCommentPattern).parse(testData);
        MarkupTagProcessor.ProcessedTags tags = new MarkupTagProcessor().process(parseResult);
        assertEquals(Collections.emptyList(),tags.getErrorList());
        assertEquals(Collections.emptyMap(),tags.getMarkUpTagLineMapper());
        assertEquals(Collections.emptyMap(),tags.getRegionTagLineMapper());
    }
    
    public void testMarkupTag() {
        String testData = "        System.out.println(\"Hello println\"); //@highlight substring  =  \"println\"    type=\"italic\" ";
        String langCommentPattern = "\\Q//\\E";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser(langCommentPattern).parse(testData);
        
        MarkupTagProcessor tagProcessor = new MarkupTagProcessor();
        MarkupTagProcessor.ProcessedTags tags = tagProcessor.process(parseResult);
        
        List<ApplicableMarkupTag> actual = tags.getMarkUpTagLineMapper().get(1);
        
        List<ApplicableMarkupTag> expected = new ArrayList<>();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("substring", "println");
        attributes.put("type", "italic");
        MarkupTagProcessor.ApplicableMarkupTag ap = new MarkupTagProcessor().new ApplicableMarkupTag(attributes, "highlight");
        expected.add(ap);
        assertEquals(actual, expected);
    }
    
    public void testMultipleMarkupTag() {
        String langCommentPattern = "\\Q//\\E";
        String testData = "System.out.println(\"Hello println\");"
                + "//@highlight substring=\"println\" "
                + "@link substring=\"System.out\" target=\"java.lang.System#out\""
                + "@replace regex=\\bHello\\b replacement=\"Hi\"";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser(langCommentPattern).parse(testData);
        MarkupTagProcessor tagProcessor = new MarkupTagProcessor();
        MarkupTagProcessor.ProcessedTags tags = tagProcessor.process(parseResult);
        List<ApplicableMarkupTag> actual = tags.getMarkUpTagLineMapper().get(1);
        
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("substring", "println");
        MarkupTagProcessor.ApplicableMarkupTag highlightTag = new MarkupTagProcessor().new ApplicableMarkupTag(attributes1, "highlight");
        
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("substring", "System.out");
        attributes2.put("target", "java.lang.System#out");
        MarkupTagProcessor.ApplicableMarkupTag linkTag = new MarkupTagProcessor().new ApplicableMarkupTag(attributes2, "link");
        
        Map<String, String> attributes3 = new HashMap<>();
        attributes3.put("regex", "\\bHello\\b");
        attributes3.put("replacement", "Hi");
        MarkupTagProcessor.ApplicableMarkupTag replaceTag = new MarkupTagProcessor().new ApplicableMarkupTag(attributes3, "replace");
        
        List<ApplicableMarkupTag> expected = new ArrayList<>();
        expected.add(highlightTag);
        expected.add(linkTag);
        expected.add(replaceTag);
        
        assertEquals(actual, expected);
    }
    
    public void testMarkupTagRegion(){
        String testData = "System.out.println(\"Hello println\");"
                + "// @highlight region substring=\"println\" \n"
                + "System.out.println(\"Hi println\");\n"
                + "System.out.println(\"How are you println\");\\@end";
        String langCommentPattern = "\\Q//\\E";
        
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser(langCommentPattern).parse(testData);
        MarkupTagProcessor tagProcessor = new MarkupTagProcessor();
        MarkupTagProcessor.ProcessedTags tags = tagProcessor.process(parseResult);
        
        Map<Integer, List<MarkupTagProcessor.Region>> actual = tags.getRegionTagLineMapper();
        
        Map<Integer, List<MarkupTagProcessor.Region>> expected = new HashMap<>();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("substring", "println");
        List<MarkupTagProcessor.Region> line1 = new ArrayList<>();
        MarkupTagProcessor.Region region = new MarkupTagProcessor().new Region("anonymous",attributes, "highlight");
        line1.add(region);
        expected.put(1,line1);
        expected.put(2,line1);
        expected.put(3,line1);
        assertEquals(actual, expected);
    }
    
    public void testMarkupTagAppliesToNextLine(){

        String testData = "System.out.println(\"Hello println\");"
                + "// @highlight substring=\"println\" :"
                + "@link substring=\"System.out\" target = \"java.lang.System#out\"\n"
                + "System.out.println(\"Hi println\");";
        String langCommentPattern = "\\Q//\\E";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser(langCommentPattern).parse(testData);
        
        MarkupTagProcessor.ProcessedTags tags = new MarkupTagProcessor().process(parseResult);
        Map<Integer,List<ApplicableMarkupTag>> actual = tags.getMarkUpTagLineMapper();
        
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("substring", "System.out");
        attributes1.put("target", "java.lang.System#out");
        MarkupTagProcessor.ApplicableMarkupTag linkTag = new MarkupTagProcessor().new ApplicableMarkupTag(attributes1, "link");
        
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("substring", "println");
        MarkupTagProcessor.ApplicableMarkupTag highlightTag = new MarkupTagProcessor().new ApplicableMarkupTag(attributes2, "highlight");
        
        List<ApplicableMarkupTag> list1 = new ArrayList<>();
        list1.add(linkTag);
        List<ApplicableMarkupTag> list2 = new ArrayList<>();
        list2.add(highlightTag);
        
        Map<Integer,List<ApplicableMarkupTag>> expected = new HashMap<>();
        expected.put(1,list1);
        expected.put(2,list2);
        assertEquals(actual,expected);
    }
}
