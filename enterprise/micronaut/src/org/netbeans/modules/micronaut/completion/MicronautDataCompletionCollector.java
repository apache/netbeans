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
package org.netbeans.modules.micronaut.completion;

import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
public class MicronautDataCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        new MicronautDataCompletionTask().query(doc, offset, new MicronautDataCompletionTask.ItemFactory<Completion>() {
            @Override
            public Completion createFinderMethodItem(String name, String returnType, int offset) {
                Builder builder = CompletionCollector.newBuilder(name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name));
                if (returnType != null) {
                    builder.insertText(new StringBuilder("${1:").append(returnType).append("} ").append(name).append("$0()").toString());
                    builder.insertTextFormat(Completion.TextFormat.Snippet);
                }
                return builder.build();
            }
            @Override
            public Completion createFinderMethodNameItem(String prefix, String name, int offset) {
                return CompletionCollector.newBuilder(prefix + name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name)).build();
            }
            @Override
            public Completion createSQLItem(CompletionItem item) {
                return CompletionCollector.newBuilder(item.getInsertPrefix().toString())
                        .insertText(item.getInsertPrefix().toString().replace("\"", "\\\""))
                        .kind(Completion.Kind.Property)
                        .build();
            }
        }).stream().forEach(consumer);
        return true;
    }
}
