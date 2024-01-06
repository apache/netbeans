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
package org.netbeans.spi.lsp;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lsp.CodeAction;
import org.netbeans.api.lsp.Range;
import org.openide.util.Lookup;

/**
 * A provider for code actions for a given document and range.
 *
 * @author Dusan Balek
 * @since 1.23
 */
public interface CodeActionProvider {

    /**
     * Computes code actions for a given document and range
     * @param doc a text document
     * @param range a range inside the text document
     * @param context a context carrying additional information.
     * @return a list of {@link CodeAction} instances
     * @since 1.23
     */
    public List<CodeAction> getCodeActions(@NonNull Document doc, @NonNull Range range, @NonNull Lookup context);
}
