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
package org.netbeans.api.lsp;

import org.netbeans.api.annotations.common.NonNull;

/**
 * Represents a textual edit applicable to a text document.
 *
 * @author Dusan Balek
 * @since 1.0
 */
public final class TextEdit {

    private final int start;
    private final int end;
    private final String newText;

    public TextEdit(int start, int end, @NonNull String newText) {
        this.start = start;
        this.end = end;
        this.newText = newText;
    }

    /**
     * The start offset of the text document range to be manipulated. To insert
     * text into a document create edit where {@code startOffset == endOffset}.
     *
     * @since 1.0
     */
    public int getStartOffset() {
        return start;
    }

    /**
     * The end offset of the text document range to be manipulated. To insert
     * text into a document create edit where {@code startOffset == endOffset}.
     *
     * @since 1.0
     */
    public int getEndOffset() {
        return end;
    }

    /**
     * The string to be inserted. For delete operations use an empty string.
     *
     * @since 1.0
     */
    @NonNull
    public String getNewText() {
        return newText;
    }
}
