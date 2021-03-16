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
package org.netbeans.spi.editor.completion;

/**
 * Factory for completion items. Used in {@link CompletionProvider#getCompletionItems(javax.swing.text.Document, int, org.netbeans.spi.editor.completion.ItemFactory)}.
 *
 * @author Dusan Balek
 * @since 1.57
 */
public interface ItemFactory<T> {

    /**
     * Creates completion item.
     *
     * @param label
     * @param kind
     * @param tags
     * @param sortText
     * @param insertText
     * @param insertTextFormat
     * @param documentation
     * @return created completion item
     */
    T create(String label, int kind, int[] tags, String sortText, String insertText, int insertTextFormat, String documentation);
}
