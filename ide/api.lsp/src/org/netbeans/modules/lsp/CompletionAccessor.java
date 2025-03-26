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
package org.netbeans.modules.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;


public abstract class CompletionAccessor {

    private static volatile CompletionAccessor DEFAULT;

    public static synchronized CompletionAccessor getDefault() {
        CompletionAccessor instance = DEFAULT;
        if (instance == null) {
            Class<?> c = Completion.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
                instance = DEFAULT;
                assert instance != null;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }

    public static void setDefault(@NonNull final CompletionAccessor accessor) {
        Parameters.notNull("accessor", accessor);   //NOI18N
        if (DEFAULT != null) {
            throw new IllegalStateException("Accessor already initialized");
        }
        DEFAULT = accessor;
    }

    public abstract Completion createCompletion(String label, String labelDetail, String labelDescription, Completion.Kind kind, List<Completion.Tag> tags, CompletableFuture<String> detail, CompletableFuture<String> documentation,
            boolean preselect, String sortText, String filterText, String insertText, Completion.TextFormat insertTextFormat,
            TextEdit textEdit, Command command, CompletableFuture<List<TextEdit>> additionalTextEdits, List<Character> commitCharacters);
}
