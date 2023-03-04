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

import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class AskEditorQuestions {
    private AskEditorQuestions() {
    }

    public static IOException throwableIsReadOnly(FileObject fo) {
        IOException e = new IOException("File is read-only: " + fo); // NOI18N
        UIException.annotateUser(e, null, NbBundle.getMessage(AskEditorQuestions.class, "MSG_FileReadOnlySaving", new Object[]{fo.getNameExt()}), null, null);
        return e;
    }

    public static void notifyChangedToReadOnly(String fileName) {
        // notify user if the object is modified and externally changed to read-only
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(AskEditorQuestions.class, "MSG_FileReadOnlyChanging",
            new Object[]{fileName}), 
            NotifyDescriptor.WARNING_MESSAGE
        ));
    }

    public static boolean askUserQuestionExceptionOnSave(String localizedMessage) {
        String ask = NbBundle.getMessage(AskEditorQuestions.class, "ASK_OnSaving"); // NOI18N
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

    public static boolean askFileReadOnlyOnClose(String fileName) {
        String ask = NbBundle.getMessage(AskEditorQuestions.class, "ASK_OnClosing"); // NOI18N
        if ("yes".equals(ask)) { // NOI18N
            return true;
        }
        if ("no".equals(ask)) { // NOI18N
            return false;
        }
        Object result = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(AskEditorQuestions.class, "MSG_FileReadOnlyClosing", 
            new Object[]{fileName}), 
            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE
        ));
        return result == NotifyDescriptor.OK_OPTION;
    }
}
