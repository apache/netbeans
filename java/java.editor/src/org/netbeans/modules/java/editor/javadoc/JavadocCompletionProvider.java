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

package org.netbeans.modules.java.editor.javadoc;

import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.base.javadoc.JavadocCompletionUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Jan Pokorsky
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 200) //NOI18N
public final class JavadocCompletionProvider implements CompletionProvider {
    // complete @TAG, {@TAG}, @param NAME, @see LINK, {@link LINK}

    public CompletionTask createTask(int queryType, JTextComponent component) {
        CompletionTask task = null;
        if (queryType == COMPLETION_QUERY_TYPE || queryType == COMPLETION_ALL_QUERY_TYPE) {
            task = new AsyncCompletionTask(new JavadocCompletionQuery(queryType), component);
        }
        return task;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        char c;
        if (typedText != null && typedText.length() == 1
                && Utilities.getJavadocCompletionAutoPopupTriggers().indexOf(typedText.charAt(0)) >= 0
                && JavadocCompletionUtils.isJavadocContext(component.getDocument(), component.getCaretPosition())) {
            return COMPLETION_QUERY_TYPE;
        }
        return 0;
    }

}
