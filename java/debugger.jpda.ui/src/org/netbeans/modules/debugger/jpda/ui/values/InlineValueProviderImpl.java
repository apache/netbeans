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
package org.netbeans.modules.debugger.jpda.ui.values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lsp.InlineValue;
import org.netbeans.api.lsp.Range;
import org.netbeans.spi.lsp.InlineValuesProvider;
import org.openide.filesystems.FileObject;

@MimeRegistration(mimeType = "text/x-java", service = InlineValuesProvider.class)
public final class InlineValueProviderImpl implements InlineValuesProvider {

    @Override
    public CompletableFuture<List<? extends InlineValue>> inlineValues(FileObject file, int currentExecutionPosition) {
        //TODO: proper cancellability
        JavaSource js = JavaSource.forFileObject(file);
        CompletableFuture<List<? extends InlineValue>> result = new CompletableFuture<>();
        List<InlineValue> resultValues = new ArrayList<>();
        if (js != null) {
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    int stackLine = (int) cc.getCompilationUnit().getLineMap().getLineNumber(currentExecutionPosition);
                    int stackCol = (int) cc.getCompilationUnit().getLineMap().getColumnNumber(currentExecutionPosition);
                    for (ComputeInlineValues.InlineVariable var : ComputeInlineValues.computeVariables(cc, stackLine, stackCol, new AtomicBoolean())) {
                        resultValues.add(InlineValue.createInlineVariable(new Range(var.start(), var.end()), var.expression()));
                    }
                }, true);
            } catch (IOException ex) {
                result.completeExceptionally(ex);
                return result;
            }
        }
        result.complete(resultValues);
        return result;
    }

}
