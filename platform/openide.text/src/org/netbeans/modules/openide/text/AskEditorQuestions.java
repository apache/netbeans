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
package org.netbeans.modules.openide.text;

import java.util.MissingResourceException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

public final class AskEditorQuestions {
    public enum QuestionResult {
        /**
         * The implementation should ask the user. The default, if no
         * value is present in the resource bundle.
         */
        ASK_USER,
        
        /**
         * Assume yes, do not ask the user, proceed immediately.
         */
        YES,
        
        /**
         * Assume no, do not ask the user, proceed immediately.
         */
        NO,
    }
    private AskEditorQuestions() {
    }
    
    public static QuestionResult askUserQuestion(UserQuestionException uqe) {
        String key = "UserQuestionAnswer_" + uqe.getClass().getName();
        try {
            String ask = NbBundle.getMessage(AskEditorQuestions.class, key); // NOI18N
            if ("yes".equals(ask)) {
                return QuestionResult.YES;
            }
            if ("no".equals(ask)) {
                return QuestionResult.NO;
            }
        } catch (MissingResourceException ex) {
            // expected
        }
        return QuestionResult.ASK_USER;
    }

    public static boolean askReloadDocument(String localizedMessage) {
        String ask = NbBundle.getMessage(AskEditorQuestions.class, "ASK_OnReload"); // NOI18N
        if ("yes".equals(ask)) { // NOI18N
            return true;
        }
        if ("no".equals(ask)) { // NOI18N
            return false;
        }
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(localizedMessage, NotifyDescriptor.YES_NO_OPTION);
        Object res = DialogDisplayer.getDefault().notify(nd);
        if (NotifyDescriptor.OK_OPTION.equals(res)) {
            return true;
        } else {
            return false;
        }
    }
}
