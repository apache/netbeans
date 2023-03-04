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
package org.netbeans.modules.web.jsf.hints;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfHintsUtils {

    private static final Logger LOG = Logger.getLogger(JsfHintsUtils.class.getName());
    private static final String CACHED_CONTEXT = "cached-jsfProblemContext"; //NOI18N

    /**
     * Gets problem context used by standard JSF hints.
     * Uses cached value if found, otherwise creates a new one which stores into the CompilationInfo.
     *
     * @param context Hints API context
     * @return {@code JsfHintsContext}
     */
    public static JsfHintsContext getOrCacheContext(HintContext context) {
        Object cached = context.getInfo().getCachedValue(CACHED_CONTEXT);
        if (cached == null) {
            LOG.log(Level.FINEST, "HintContext doesn't contain cached JsfHintsContext which is going to be created.");
            JsfHintsContext newContext = createJsfHintsContext(context);
            context.getInfo().putCachedValue(CACHED_CONTEXT, newContext, CompilationInfo.CacheClearPolicy.ON_SIGNATURE_CHANGE);
            return newContext;
        } else {
            LOG.log(Level.FINEST, "JsfHintsContext cached value used.");
            return (JsfHintsContext) cached;
        }
    }

    private static JsfHintsContext createJsfHintsContext(HintContext context) {
        return new JsfHintsContext(context.getInfo().getFileObject());
    }

    public static ErrorDescription createProblem(Tree tree, CompilationInfo cinfo, String description, Severity severity, List<Fix> fixes) {
        TextSpan underlineSpan = getUnderlineSpan(cinfo, tree);
        return ErrorDescriptionFactory.createErrorDescription(
                severity, description, fixes, cinfo.getFileObject(),
                underlineSpan.getStartOffset(), underlineSpan.getEndOffset());
    }

    /**
     * This method returns the part of the syntax tree to be highlighted.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree) {
        SourcePositions srcPos = info.getTrees().getSourcePositions();
        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);
        return new TextSpan(startOffset, endOffset);
    }

    /**
     * Represents a span of text.
     */
    public static class TextSpan {

        private int startOffset;
        private int endOffset;

        public TextSpan(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }
}
