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

package org.netbeans.modules.bugzilla.issue;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugzilla.issue.AttachmentHyperlinkSupport.Attachement;

/**
 *
 * @author Marian Petras
 */
public class AttachmentHyperlinkSupportTest {

    public AttachmentHyperlinkSupportTest() {
    }

    @Test
    public void test() {
        checkBoundaries("", null, null);
        checkBoundaries("C", null, null);
        checkBoundaries("(id=123)", null, null);
        
        checkBoundaries("Created an attachment", null, null);
        checkBoundaries("Created an attachment (id=", null, null);
        checkBoundaries("Created an attachment (id=1", null, null);
        checkBoundaries("Created an attachment (id=12", null, null);
        checkBoundaries("Created an attachment (id=123", null, null);
        checkBoundaries("Created an attachment (id=)", null, null);
        checkBoundaries("Created an attachment (id=1)", "attachment (id=1)", "1");
        checkBoundaries("Created an attachment (id=12)", "attachment (id=12)", "12");
        checkBoundaries("Created an attachment (id=123)", "attachment (id=123)", "123");
        checkBoundaries("Created an atmachment (id=123)", null, null);
        checkBoundaries("Created an attachment (id=1a5)", null, null);
        checkBoundaries("Created an attachment (id=123) [details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)  [details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)\t[details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)\t\t[details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)\t [details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) \t[details]", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details] ", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]  ", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t ", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details] \t", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details] \n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]  \n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t \n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details] \t\n", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details] \n ", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\n ", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n \tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n\t foo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\t\n\t\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\t\n\t\tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo bar\nbaz", "foo bar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n \tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\t foo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n\t\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n\t\tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo bar\nbaz", "foo bar", "123");

        checkBoundaries("Created an attachment (id=123)\nScreenshot", "Screenshot", "123");
        checkBoundaries("Created an attachment (id=123)\n\nScreenshot", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123) [details]\nScreenshot", "Screenshot", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\nScreenshot", "attachment (id=123)", "123");

        checkBoundaries("Created an attachment (id=92562)\n"
                            + "Screenshot\n"
                            + '\n'
                            + "I used NetBeans without connection to internet and when I tried to generate javadoc for openide.util project, strange dialog appeared. I suspect it is warning from Kenai about inability to connect to network.\n"
                            + '\n'
                            + "The dialog is shown when I right-click a node. This is not the right time to display dialogs (from UI point of view) nor to check internet connectivity (from performance point of view).\n"
                            + '\n'
                            + "Please eliminate such checks at this time.",
                        "Screenshot",
                        "92562");

        checkBoundaries("Created an attachment (id=123)", "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)", new int[] {}, null, null);
        checkBoundaries("Created an attachment (id=123)", new int[] {123}, "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=123)", new int[] {123, 789}, "attachment (id=123)", "123");
        checkBoundaries("Created an attachment (id=789)", new int[] {123, 789}, "attachment (id=789)", "789");
        checkBoundaries("Created an attachment (id=456)", new int[] {123, 456, 789}, "attachment (id=456)", "456");
        checkBoundaries("Created an attachment (id=456)", new int[] {123, 473, 789}, null, null);
        
        checkBoundaries("Created attachment", null, null);
        checkBoundaries("Created attachment (id=", null, null);
        checkBoundaries("Created attachment 1", null, null);
        checkBoundaries("Created attachment 12", null, null);
        checkBoundaries("Created attachment 123", null, null);
        checkBoundaries("Created attachment )", null, null);
        checkBoundaries("Created attachment 1", new int[] {1}, "attachment 1", "1");
        checkBoundaries("Created attachment 12", new int[] {12}, "attachment 12", "12");
        checkBoundaries("Created attachment 123", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created atmachment 123", null, null);
        checkBoundaries("Created attachment 1a5", null, null);
        checkBoundaries("Created attachment 123 [details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123  [details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123\t[details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123\t\t[details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123\t [details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 \t[details]", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details] ", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]  ", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t\t", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t ", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details] \t", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details] \n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]  \n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t\n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t\t\n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t \n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details] \t\n", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details] \n ", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\t\n ", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123\nfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\n\tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\n \tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\n\t foo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\t\nfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\t\t\n\t\tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123\t\t\n\t\tfoo\tbar", new int[] {123}, "foo\tbar", "123");
        checkBoundaries("Created attachment 123\t  \n  \tfoo\tbar", new int[] {123}, "foo\tbar", "123");
        checkBoundaries("Created attachment 123\t  \n  \tfoo\tbar baz", new int[] {123}, "foo\tbar baz", "123");
        checkBoundaries("Created attachment 123\t  \n  \tfoo\tbar baz", new int[] {123}, "foo\tbar baz", "123");
        checkBoundaries("Created attachment 123\t  \n  \tfoo bar\nbaz", new int[] {123}, "foo bar", "123");
        checkBoundaries("Created attachment 123 [details]\nfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\n\tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\n \tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\n\t foo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\t\nfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\t\t\n\t\tfoo", new int[] {123}, "foo", "123");
        checkBoundaries("Created attachment 123 [details]\t\t\n\t\tfoo\tbar", new int[] {123}, "foo\tbar", "123");
        checkBoundaries("Created attachment 123 [details]\t  \n  \tfoo\tbar", new int[] {123}, "foo\tbar", "123");
        checkBoundaries("Created attachment 123 [details]\t  \n  \tfoo\tbar baz", new int[] {123}, "foo\tbar baz", "123");
        checkBoundaries("Created attachment 123 [details]\t  \n  \tfoo\tbar baz", new int[] {123}, "foo\tbar baz", "123");
        checkBoundaries("Created attachment 123 [details]\t  \n  \tfoo bar\nbaz", new int[] {123}, "foo bar", "123");

        checkBoundaries("Created attachment 123\nScreenshot", new int[] {123}, "Screenshot", "123");
        checkBoundaries("Created attachment 123\n\nattachment", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123 [details]\nScreenshot", new int[] {123}, "Screenshot", "123");
        checkBoundaries("Created attachment 123 [details]\n\nattachment", new int[] {123}, "attachment 123", "123");

        checkBoundaries("Created attachment 92562\n"
                            + "Screenshot\n"
                            + '\n'
                            + "I used NetBeans without connection to internet and when I tried to generate javadoc for openide.util project, strange dialog appeared. I suspect it is warning from Kenai about inability to connect to network.\n"
                            + '\n'
                            + "The dialog is shown when I right-click a node. This is not the right time to display dialogs (from UI point of view) nor to check internet connectivity (from performance point of view).\n"
                            + '\n'
                            + "Please eliminate such checks at this time.",
                        new int[] {92562}, 
                        "Screenshot",
                        "92562");

        checkBoundaries("Created attachment 123", new int[] {}, null, null);
        checkBoundaries("Created attachment 123", new int[] {123}, "attachment 123", "123");
        checkBoundaries("Created attachment 123", new int[] {123, 789}, "attachment 123", "123");
        checkBoundaries("Created attachment 789", new int[] {123, 789}, "attachment 789", "789");
        checkBoundaries("Created attachment 456", new int[] {123, 456, 789}, "attachment 456", "456");
        checkBoundaries("Created attachment 456", new int[] {123, 473, 789}, null, null);        
    }

    private void checkBoundaries(String stringToParse,
                                 String expectedHyperlinkText,
                                 String expectedId) {
        checkBoundaries(stringToParse, null, expectedHyperlinkText, expectedId);
    }

    private void checkBoundaries(String stringToParse,
                                 int[] knownIds,
                                 String expectedHyperlinkText,
                                 String expectedId) {
        int[] expected;
        if (expectedHyperlinkText == null) {
            expected = null;
        } else {
            int index = stringToParse.indexOf(expectedHyperlinkText);
            assert index != -1;
            expected = new int[] {index, index + expectedHyperlinkText.length()}; 
        }

        Collection<String> knownIdsColl;
        if (knownIds != null) {
            knownIdsColl = new ArrayList<String>(knownIds.length);
            for (int knownId : knownIds) {
                knownIdsColl.add(Integer.toString(knownId));
            }
        } else {
            knownIdsColl = null;
        }
        
        Attachement attachment = AttachmentHyperlinkSupport.findAttachment(stringToParse, knownIdsColl);

        if (expected != null) {
            assertNotNull(attachment);
            assertEquals(expected[0], attachment.idx1);
            assertEquals(expected[1], attachment.idx2);
            assertEquals(expectedId, attachment.id);
        } else {
            assertNull(attachment);
        }
    }

}