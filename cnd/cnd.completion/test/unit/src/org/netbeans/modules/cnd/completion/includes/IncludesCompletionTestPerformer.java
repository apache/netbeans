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
package org.netbeans.modules.cnd.completion.includes;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionTestPerformer;
import java.io.*;
import java.util.Collection;
import javax.swing.JEditorPane;
import org.netbeans.editor.*;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 *
 */
public class IncludesCompletionTestPerformer extends CompletionTestPerformer {
    
    private final int queryType;
    public IncludesCompletionTestPerformer() {
        this.queryType = CompletionProvider.COMPLETION_QUERY_TYPE;
    }
    
    @Override
    protected CompletionItem[] completionQuery(
            PrintWriter  log,
            JEditorPane  editor,
            BaseDocument doc,
            int caretOffset,
            boolean unsorted,
            boolean tooltip
            ) {
        doc = doc == null ? Utilities.getDocument(editor) : doc;
        Collection<CsmIncludeCompletionItem> items = null;
        if (doc != null) {
            items = CsmIncludeCompletionProvider.getFilteredData(doc, caretOffset, this.queryType);
        }
        CompletionItem[] array =  (items == null) ? new CompletionItem[0] : items.toArray(new CompletionItem[items.size()]);
        assert array != null;
        return array;
    }
}
