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
package org.netbeans.modules.lsp.client;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.TextDocumentService;

public interface EnhancedTextDocumentService extends TextDocumentService {
    @JsonRequest("textDocument/inlineCompletion")
    public default CompletableFuture<InlineCompletionItem[]> inlineCompletion(InlineCompletionParams params) {
        throw new UnsupportedOperationException();
    }

    public static class InlineCompletionParams {
        private TextDocumentIdentifier textDocument;
        private Position position;
        private InlineCompletionContext context;

        public InlineCompletionParams() {
        }

        public InlineCompletionParams(TextDocumentIdentifier textDocument, Position position, InlineCompletionContext context) {
            this.textDocument = textDocument;
            this.position = position;
            this.context = context;
        }

        public TextDocumentIdentifier getTextDocument() {
            return textDocument;
        }

        public void setTextDocument(TextDocumentIdentifier textDocument) {
            this.textDocument = textDocument;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        public InlineCompletionContext getContext() {
            return context;
        }

        public void setContext(InlineCompletionContext context) {
            this.context = context;
        }

    }

    public static class InlineCompletionContext {
        private Object triggerKind;
        private Object selectedCompletionInfo;

        public Object getTriggerKind() {
            return triggerKind;
        }

        public void setTriggerKind(Object triggerKind) {
            this.triggerKind = triggerKind;
        }

        public Object getSelectedCompletionInfo() {
            return selectedCompletionInfo;
        }

        public void setSelectedCompletionInfo(Object selectedCompletionInfo) {
            this.selectedCompletionInfo = selectedCompletionInfo;
        }

    }

    public static class InlineCompletionItem {
        private String insertText;

        public InlineCompletionItem() {
        }

        public InlineCompletionItem(String insertText) {
            this.insertText = insertText;
        }

        public String getInsertText() {
            return insertText;
        }

        public void setInsertText(String insertText) {
            this.insertText = insertText;
        }

    }
}
