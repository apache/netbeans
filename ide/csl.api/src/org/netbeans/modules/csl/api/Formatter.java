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
package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;

/**
 * Implementations of this interface can be registered such that the formatter
 * helps indent or reformat source code, or even determine where the caret should
 * be placed on a newly created line.
 * 
 * @author Tor Norbye
 */
public interface Formatter {
    /**
     * Reformat the given portion of source code from startOffset to endOffset in the document.
     * You may use the provided parse tree information, if available, to guide formatting decisions.
     * The caret (if any) should be updated to the corresponding position that it was at before formatting.     * 
     */
    void reformat(@NonNull Context context, @NullAllowed ParserResult compilationInfo);

    /**
     * Reindent the source code. Adjusts indentation and strips trailing whitespace but
     * does not otherwise change the code. The caret (if any) should be updated to the corresponding
     * position that it was at before formatting.
     */
    void reindent(@NonNull Context context);

    /**
     * Return true if the reformat() task in this implementation utilizes the parse information.
     * If it doesn't, the infrastructure can skip producing a parse tree before calling reformat() which
     * has some performance benefits when the info isn't needed.
     */
    boolean needsParserResult();

    /**
     * Return the preferred size in characters of each indentation level for this language.
     * This is not necessarily going to mean spaces since the IDE may use tabs to perform
     * part of the indentation, but the number should reflect the number of spaces it would
     * visually correspond to. For example, the Sun JDK Java style guidelines would return
     * "4" here, and Ruby would return "2".
     *
     * @return The size in characters of each indentation level.
     */
    int indentSize();
    
    /**
     * Return the preferred "hanging indent" size, the amount of space to indent a continued
     * line such as the second line here:
     * <pre>
     *   foo = bar +
     *       baz
     * </pre>
     * The hanging indent is the indentation level difference between "baz" and "foo".
     */
    int hangingIndentSize();
}
