/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
