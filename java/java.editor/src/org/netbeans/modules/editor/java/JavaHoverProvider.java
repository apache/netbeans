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
package org.netbeans.modules.editor.java;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.HoverProvider;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = HoverProvider.class)
public class JavaHoverProvider implements HoverProvider {

    @Override
    public CompletableFuture<String> getHoverContent(Document doc, int offset) {
        try {
            JavaDocumentationTask<CompletableFuture<String>> task = JavaDocumentationTask.create(offset, null, new JavaDocumentationTask.DocumentationFactory<CompletableFuture<String>>() {
                @Override
                public CompletableFuture<String> create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                    return (CompletableFuture<String>) ElementJavadoc.create(compilationInfo, element, cancel).getTextAsync();
                }
            }, () -> false);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    task.run(resultIterator);
                }
            });
            CompletableFuture<String> documentation = task.getDocumentation();
            return documentation != null ? documentation : CompletableFuture.completedFuture(null);
        } catch (ParseException parseException) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
