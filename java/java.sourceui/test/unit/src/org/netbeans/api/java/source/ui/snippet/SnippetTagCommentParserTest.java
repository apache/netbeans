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
import junit.framework.TestCase;

/**
 *
 * @author aksinsin
 */
public class SnippetTagCommentParserTest extends TestCase{

    public SnippetTagCommentParserTest(String testName) {
      super(testName);
    }

    public void testNoMarkupTag(){
        String testData = "class HelloWorld {}// This is test class";
        List<SourceLineMeta> parse = new SnippetTagCommentParser().parse(testData);
        SourceLineMeta slm = parse.get(0);
        assertEquals("class HelloWorld {}// This is test class", slm.getActualSourceLine());
        assertNull(slm.getSourceLineWithoutComment());
        assertEquals(Collections.emptyList(), slm.getThisLineMarkUpTags());
    }
    public void testMarkupTag(){
        String testData ="        System.out.println(\"Hello println\");  // @highlight substring    =   \"println\"     type=\"italic\"";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser().parse(testData);
        SourceLineMeta slm = parseResult.get(0);

        assertEquals("        System.out.println(\"Hello println\");  // @highlight substring    =   \"println\"     type=\"italic\"", slm.getActualSourceLine());
        assertEquals("        System.out.println(\"Hello println\");  ",slm.getSourceLineWithoutComment());

        MarkupTag actual = slm.getThisLineMarkUpTags().get(0);

        SourceLineMeta obj = new SourceLineMeta();
        obj.setActualSourceLine("        System.out.println(\"Hello println\");  // @highlight substring    =   \"println\"     type=\"italic\"");
        obj.setSourceLineWithoutComment("        System.out.println(\"Hello println\");  ");

        MarkupTagAttribute markupTagAttribute1 = new MarkupTagAttribute("substring",12,"println",30);
        MarkupTagAttribute markupTagAttribute2 = new MarkupTagAttribute("type",43,"italic",49);
        List<MarkupTagAttribute> mtaList = new ArrayList<>();
        mtaList.add(markupTagAttribute1);
        mtaList.add(markupTagAttribute2);
        MarkupTag expected = new MarkupTag("highlight", mtaList, false);
       
        assertEquals(expected, actual);
    }

    public void testMultipleMarkupTag(){
        String testData = "System.out.println(\"Hello println\");"
                + "// @highlight substring=\"println\" "
                + "@link substring=\"System.out\" target = \"java.lang.System#out\""
                + "@replace regex=\\bHello\\b replacement=\"Hi\"";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser().parse(testData);
        SourceLineMeta slm = parseResult.get(0);
        assertEquals("System.out.println(\"Hello println\");// @highlight substring=\"println\" @link substring=\"System.out\" target = \"java.lang.System#out\"@replace regex=\\bHello\\b replacement=\"Hi\"", slm.getActualSourceLine());
        assertEquals("System.out.println(\"Hello println\");", slm.getSourceLineWithoutComment());

        List<MarkupTag> actualMarkUpTags = slm.getThisLineMarkUpTags();

        MarkupTagAttribute highlightTagAttribute = new MarkupTagAttribute("substring", 12, "println", 23);
        List<MarkupTagAttribute> mtaList1 = new ArrayList<>();
        mtaList1.add(highlightTagAttribute);

        MarkupTagAttribute linkTagAttributeSubstring = new MarkupTagAttribute("substring", 38, "System.out", 49);
        MarkupTagAttribute linkTagAttributeTarget = new MarkupTagAttribute("target", 61, "java.lang.System#out", 71);
        List<MarkupTagAttribute> mtaList2 = new ArrayList<>();
        mtaList2.add(linkTagAttributeSubstring);
        mtaList2.add(linkTagAttributeTarget);

        MarkupTagAttribute replaceTagAttributeRegex = new MarkupTagAttribute("regex", 101, "\\bHello\\b", 107);
        MarkupTagAttribute replaceTagAttributeReplacement = new MarkupTagAttribute("replacement", 117, "Hi", 130);
        List<MarkupTagAttribute> mtaList3 = new ArrayList<>();
        mtaList3.add(replaceTagAttributeRegex);
        mtaList3.add(replaceTagAttributeReplacement);

        MarkupTag markupTag1 = new MarkupTag("highlight", mtaList1, false);
        MarkupTag markupTag2 = new MarkupTag("link", mtaList2, false);
        MarkupTag markupTag3 = new MarkupTag("replace", mtaList3, false);

        List<MarkupTag> expectedMarkupTags = new ArrayList<>();
        expectedMarkupTags.add(markupTag1);
        expectedMarkupTags.add(markupTag2);
        expectedMarkupTags.add(markupTag3);

        assertEquals(expectedMarkupTags, actualMarkUpTags);
    }

    public void testMarkupTagAppliesToNextLine(){
        
        String testData = "System.out.println(\"Hello println\");"
                + "// @highlight substring=\"println\" :"
                + "@link substring=\"System.out\" target = \"java.lang.System#out\"\n"
                + "System.out.println(\"Hi println\");";
        List<SourceLineMeta> parseResult = new SnippetTagCommentParser().parse(testData);
        SourceLineMeta slm = parseResult.get(0);
        assertEquals("System.out.println(\"Hello println\");// @highlight substring=\"println\" :@link substring=\"System.out\" target = \"java.lang.System#out\"", slm.getActualSourceLine());
        assertEquals("System.out.println(\"Hello println\");", slm.getSourceLineWithoutComment());

        SourceLineMeta slm1 = parseResult.get(1);
        assertEquals("System.out.println(\"Hi println\");", slm1.getActualSourceLine());
        assertNull(slm1.getSourceLineWithoutComment());
        assertEquals(Collections.emptyList(), slm1.getThisLineMarkUpTags());

        List<MarkupTag> actualMarkUpTags = slm.getThisLineMarkUpTags();

        MarkupTagAttribute highlightTagAttribute = new MarkupTagAttribute("substring", 12, "println", 23);
        List<MarkupTagAttribute> mtaList1 = new ArrayList<>();
        mtaList1.add(highlightTagAttribute);

        MarkupTagAttribute linkTagAttributeSubstring = new MarkupTagAttribute("substring", 39, "System.out", 50);
        MarkupTagAttribute linkTagAttributeTarget = new MarkupTagAttribute("target", 62, "java.lang.System#out", 72);
        List<MarkupTagAttribute> mtaList2 = new ArrayList<>();
        mtaList2.add(linkTagAttributeSubstring);
        mtaList2.add(linkTagAttributeTarget);

        MarkupTag markupTag1 = new MarkupTag("highlight", mtaList1, true);
        MarkupTag markupTag2 = new MarkupTag("link", mtaList2, false);

        List<MarkupTag> expectedMarkupTags = new ArrayList<>();
        expectedMarkupTags.add(markupTag1);
        expectedMarkupTags.add(markupTag2);

        assertEquals(expectedMarkupTags, actualMarkUpTags);

    }

    public void testMarkupTagRegion(){
        String testData = "System.out.println(\"Hello println\");"
                + "// @highlight region substring=\"println\" \n"
                + "System.out.println(\"Hi println\");\n"
                + "System.out.println(\"How are you println\");\\@end";

        List<SourceLineMeta> parseResult = new SnippetTagCommentParser().parse(testData);
        SourceLineMeta slm = parseResult.get(0);
        assertEquals("System.out.println(\"Hello println\");// @highlight region substring=\"println\" ", slm.getActualSourceLine());
        assertEquals("System.out.println(\"Hello println\");", slm.getSourceLineWithoutComment());

        SourceLineMeta slm1 = parseResult.get(1);
        assertEquals("System.out.println(\"Hi println\");", slm1.getActualSourceLine());
        assertNull(slm1.getSourceLineWithoutComment());
        assertEquals(Collections.emptyList(), slm1.getThisLineMarkUpTags());

        SourceLineMeta slm2 = parseResult.get(2);
        assertEquals("System.out.println(\"How are you println\");\\@end", slm2.getActualSourceLine());
        assertNull(slm2.getSourceLineWithoutComment());
        assertEquals(Collections.emptyList(), slm2.getThisLineMarkUpTags());

        List<MarkupTag> actualMarkUpTags = slm.getThisLineMarkUpTags();

        MarkupTagAttribute highlightTagAttributeRegion = new MarkupTagAttribute("region", 12, "println", -1);
        MarkupTagAttribute highlightTagAttributeSubstring = new MarkupTagAttribute("substring", 19, "println", 30);
        List<MarkupTagAttribute> mtaList1 = new ArrayList<>();
        mtaList1.add(highlightTagAttributeRegion);
        mtaList1.add(highlightTagAttributeSubstring);

        MarkupTag markupTag1 = new MarkupTag("highlight", mtaList1, false);

        List<MarkupTag> expectedMarkupTags = new ArrayList<>();
        expectedMarkupTags.add(markupTag1);
        assertEquals(expectedMarkupTags, actualMarkUpTags);

    }
}
