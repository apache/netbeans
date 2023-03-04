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
package org.netbeans.spi.editor.highlighting;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Lazy evaluator for attribute values. It is up to each particular attribute
 * to declare if its values can be lazily evaluated. Attributes that declare
 * themselvs as supporting lazy evaluation can have their values specified either
 * directly or through <code>HighlightAttributeValue</code>. All users of such an attribute
 * must check for both the direct value and the lazy evaluator and use them
 * accordingly.
 * 
 * <p class="nonnormative">If an attribute supports
 * lazy evaluation the result of <code>getValue</code> call should have the same
 * type as if the attribute value were specified directly. For example, the
 * <code>EditorStyleConstants.Tooltip</code> attribute supports lazy evaluation
 * and its value can either be <code>String</code> or <code>HighlightAttributeValue&lt;String&gt;</code>.
 * 
 * @author Vita Stejskal
 * @since 1.5
 */
public interface HighlightAttributeValue<T> {

    /**
     * Gets value of an attribute.
     * 
     * @param component The text component, which highlighting layer supplied a highlight
     *   with an attribute using this evaluator as its value.
     * @param document The document, which highlighting layer supplied a highlight
     *   with an attribute using this evaluator as its value.
     * @param attributeKey The key of the attribute.
     * @param startOffset The start offset of the original highlight or any other offset
     *   inside the highlight. Always less than <code>endOffset</code>.
     * @param endOffset The end offset of the original highlight or any other offset
     *   inside the highlight. Always greater than <code>startOffset</code>.
     * 
     * @return The value of the <code>attributeKey</code> attribute.
     */
    T getValue(JTextComponent component, Document document, Object attributeKey, int startOffset, int endOffset);
    
}
