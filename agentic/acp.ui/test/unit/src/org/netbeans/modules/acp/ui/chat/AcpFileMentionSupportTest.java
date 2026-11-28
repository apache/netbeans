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
package org.netbeans.modules.acp.ui.chat;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;

public class AcpFileMentionSupportTest extends TestCase {

    public void testRemovingOneOfTwoIdenticalMentionsKeepsOnlyTheOtherAttachment() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextArea input = new JTextArea();
            AcpFileMentionSupport support = new AcpFileMentionSupport(input);
            int second = support.insertAttachment(0, "/first/Same.java", "Same.java");
            support.insertAttachment(second, "/second/Same.java", "Same.java");
            input.insert("Please review ", 0);
            input.replaceRange("", "Please review ".length(), "Please review ".length() + second);
            assertEquals(List.of(new AcpFileMentionSupport.FileAttachment("/second/Same.java", "Same.java")),
                    support.consumeAttachedFiles());
        });
    }

    public void testReplacingDraftDiscardsAttachmentEvenIfReplacementMentionsTheSameName() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextArea input = new JTextArea();
            AcpFileMentionSupport support = new AcpFileMentionSupport(input);
            support.insertAttachment(0, "/first/Private.java", "Private.java");
            input.setText("What does @Private.java mean?");
            assertTrue(support.consumeAttachedFiles().isEmpty());
        });
    }
}
