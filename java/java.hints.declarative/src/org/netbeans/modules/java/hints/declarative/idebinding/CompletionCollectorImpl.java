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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.Completion.Context;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=CompletionCollector.class)
public class CompletionCollectorImpl implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Context context, Consumer<Completion> consumer) {
        boolean complete = true;

        for (CompletionCollector javaCollector : MimeLookup.getLookup("text/x-java").lookupAll(CompletionCollector.class)) {
            complete &= javaCollector.collectCompletions(doc, offset, context, consumer);
        }

        return complete;
    }

}
