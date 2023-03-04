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
package org.netbeans.modules.openide.loaders;

import java.awt.Dialog;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class AskEditorQuestionsTest {

    public AskEditorQuestionsTest() {
    }

    @Before
    public void setUp() {
        Locale.setDefault(Locale.ENGLISH);
        MockServices.setServices(MockDialogDisplayer.class);
    }

    public void testNotifyChangedToReadOnly() {
        try {
            AskEditorQuestions.notifyChangedToReadOnly("any.txt");
            fail("Expecting dialog to be shown");
        } catch (DisplayerException ex) {
            assertTrue(ex.descriptor.getMessage().toString().contains("any.txt"));
        }
    }

    @Test
    public void testAskUserQuestionExceptionOnSave() {
        try {
            boolean result = AskEditorQuestions.askUserQuestionExceptionOnSave("any.txt");
            fail("Expecting dialog not a result: " + result);
        } catch (DisplayerException ex) {
            assertTrue(ex.descriptor.getMessage().toString().contains("any.txt"));
        }
    }

    @Test
    public void testYesUserQuestionExceptionOnSave() {
        Locale.setDefault(new Locale("DA"));
        boolean result = AskEditorQuestions.askUserQuestionExceptionOnSave("any.txt");
        assertTrue("Default answer is yes", result);
    }

    @Test
    public void testNoUserQuestionExceptionOnSave() {
        Locale.setDefault(new Locale("NO"));
        boolean result = AskEditorQuestions.askUserQuestionExceptionOnSave("any.txt");
        assertFalse("Default answer is no", result);
    }

    @Test
    public void testAskFileReadOnlyOnClose() {
        try {
            boolean result = AskEditorQuestions.askFileReadOnlyOnClose("any.txt");
            fail("Expecting dialog not a result: " + result);
        } catch (DisplayerException ex) {
            assertTrue(ex.descriptor.getMessage().toString().contains("any.txt"));
        }
    }

    @Test
    public void testYesFileReadOnlyOnClose() {
        Locale.setDefault(new Locale("DA"));
        boolean result = AskEditorQuestions.askFileReadOnlyOnClose("any.txt");
        assertTrue("Default answer is yes", result);
    }

    @Test
    public void testNoFileReadOnlyOnClose() {
        Locale.setDefault(new Locale("NO"));
        boolean result = AskEditorQuestions.askFileReadOnlyOnClose("any.txt");
        assertFalse("Default answer is no", result);
    }

    private static final class DisplayerException extends RuntimeException {
        final NotifyDescriptor descriptor;

        public DisplayerException(NotifyDescriptor descriptor) {
            this.descriptor = descriptor;
        }
    }

    public static final class MockDialogDisplayer extends DialogDisplayer {
        public MockDialogDisplayer() {
        }

        @Override
        public Object notify(NotifyDescriptor nd) {
            throw new DisplayerException(nd);
        }

        @Override
        public Dialog createDialog(DialogDescriptor dd) {
            throw new DisplayerException(dd);
        }
    }

}
