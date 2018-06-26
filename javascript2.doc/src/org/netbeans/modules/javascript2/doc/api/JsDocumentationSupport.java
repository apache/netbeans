/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.doc.api;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.doc.JsDocumentationFallbackSyntaxProvider;
import org.netbeans.modules.javascript2.doc.JsDocumentationResolver;
import org.netbeans.modules.javascript2.doc.spi.DocumentationContainer;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 * Contains support methods for obtaining {@link JsDocumentationProvider}.
 *
 * @author Martin Fousek, Petr Pisl
 */
public final class JsDocumentationSupport {

    /** Path of the documentation providers in the layer. */
    public static final String DOCUMENTATION_PROVIDER_PATH = "javascript/doc/providers"; //NOI18N

    private JsDocumentationSupport() {
    }

    /**
     * Gets {@code JsDocumentationProvider} for given {@code JsParserResult}.
     * @param result {@code ParserResult}
     * @return {@code JsDocumentationHolder} for given {@code ParserResult}, never {@code null}
     */
    @NonNull
    public static JsDocumentationHolder getDocumentationHolder(ParserResult result) {
        DocumentationContainer c = result.getLookup().lookup(DocumentationContainer.class);
        if (c != null) {
            return c.getHolder(result);
        } else {
            JsDocumentationProvider p = JsDocumentationResolver.getDefault().getDocumentationProvider(result.getSnapshot());
            return p.createDocumentationHolder(result.getSnapshot());
        }
    }

    /**
     * Gets the documentation provider for given parser result.
     * @param result JsParserResult
     * @return JsDocumentationProvider
     */
    @NonNull
    public static JsDocumentationProvider getDocumentationProvider(ParserResult result) {
        // XXX - complete caching of documentation tool provider
        return JsDocumentationResolver.getDefault().getDocumentationProvider(result.getSnapshot());
    }

    /**
     * Gets SyntaxProvider of appropriate documentation support.
     * @param parserResult JsParserResult
     * @return documentation support specific or default {@code SyntaxProvider}
     */
    @NonNull
    public static SyntaxProvider getSyntaxProvider(ParserResult parserResult) {
        SyntaxProvider syntaxProvider = getDocumentationHolder(parserResult).getProvider().getSyntaxProvider();
        return syntaxProvider != null ? syntaxProvider : new JsDocumentationFallbackSyntaxProvider();
    }

    /**
     * Gets JsComment for given offset in the snapshot.
     * @param result JsParserResult
     * @param offset snapshot offset
     * @return found {@code JsComment} or {@code null} otherwise
     */
    @CheckForNull
    public static JsComment getCommentForOffset(ParserResult result, int offset) {
        JsDocumentationHolder holder = getDocumentationHolder(result);
        return holder.getCommentForOffset(offset, holder.getCommentBlocks());
    }

}
