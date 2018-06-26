/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
