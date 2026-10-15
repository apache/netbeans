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
package org.netbeans.modules.java.openjdk.jtreg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.CodeLens;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Range;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.openjdk.jtreg.TagParser.Result;
import org.netbeans.spi.lsp.CodeLensProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 */
@MimeRegistration(mimeType="text/x-java", service=CodeLensProvider.class)
public class CodeLensProviderImpl implements CodeLensProvider {

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(Document doc) {
        CompletableFuture<List<? extends CodeLens>> result = new CompletableFuture<>();
        result.complete(doComputeCodeLens(doc));
        return result;
    }

    @Messages({
        "DN_RunTest=Run test",
        "DN_DebugTest=Debug test"
    })
    private List<? extends CodeLens> doComputeCodeLens(Document doc) {
        FileObject file = NbEditorUtilities.getFileObject(doc);
        TestRootDescription rootDesc = file != null ? TestRootDescription.findRootDescriptionFor(file) : null;

        if (rootDesc == null) return Collections.emptyList();

        Result tags = TagParser.parseTags(doc);
        List<Tag> testTags = tags.getName2Tag().getOrDefault("test", Collections.emptyList());

        if (testTags.isEmpty()) return Collections.emptyList();

        Tag testTag = testTags.get(0);
        Range lenSpan = new Range(testTag.getTagStart(), testTag.getTagEnd());
        List<Object> params = Collections.singletonList(file.toURI().toString());
        return Collections.unmodifiableList(Arrays.asList(new CodeLens(lenSpan, new Command(Bundle.DN_RunTest(), "nbls.run.test", params), null),
                                                          new CodeLens(lenSpan, new Command(Bundle.DN_DebugTest(), "nbls.debug.test", params), null)));
    }

}
