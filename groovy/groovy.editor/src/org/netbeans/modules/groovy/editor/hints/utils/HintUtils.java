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

package org.netbeans.modules.groovy.editor.hints.utils;

import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Janicek
 */
public final class HintUtils {

    private HintUtils() {
    }

    /**
     * Returns {@link OffsetRange} for the whole line where the {@link GroovyError} occurred.
     * 
     * @param context current hint context
     * @param error error for which we want to find {@link OffsetRange}
     * @return offset range for the whole line
     */
    @CheckForNull
    public static OffsetRange getLineOffset(
            @NonNull RuleContext context, 
            @NonNull GroovyError error) {
        
        Parameters.notNull("context", context);
        Parameters.notNull("error", error);
        
        // FIXME: for CLASS_NOT_FOUND errors we mark the whole line.
        // This should be replaced with marking the indentifier only.
        try {

            int lineStart = Utilities.getRowStart(context.doc, error.getStartPosition());
            int lineEnd = Utilities.getRowEnd(context.doc, error.getEndPosition());

            return new OffsetRange(lineStart, lineEnd);
            
        } catch (BadLocationException ex) {
            return null;
        }
    }
}
