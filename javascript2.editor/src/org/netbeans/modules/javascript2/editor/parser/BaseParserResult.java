/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.javascript2.doc.spi.DocumentationContainer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.spi.ModelContainer;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public abstract class BaseParserResult extends ParserResult {
    private static final Set<String> MIME_TYPES;
    static {
        final Set<String> s = new HashSet<>();
        Collections.addAll(s,
            JsTokenId.JAVASCRIPT_MIME_TYPE,
            JsTokenId.GULP_MIME_TYPE,
            JsTokenId.GRUNT_MIME_TYPE,
            JsTokenId.KARMACONF_MIME_TYPE,
            JsTokenId.JSON_MIME_TYPE,
            JsTokenId.PACKAGE_JSON_MIME_TYPE,
            JsTokenId.BOWER_JSON_MIME_TYPE,
            JsTokenId.BOWERRC_JSON_MIME_TYPE,
            JsTokenId.JSHINTRC_JSON_MIME_TYPE
        );
        MIME_TYPES = Collections.unmodifiableSet(s);
    }

    private final ModelContainer modelContainer = new ModelContainer();
    private final DocumentationContainer documentationContainer = new DocumentationContainer();
    private final boolean embedded;
    private final boolean success;
    private final Lookup lookup;
    private List<? extends FilterableError> errors;

    BaseParserResult(
            Snapshot snapshot,
            boolean success,
            @NonNull final Lookup additionalLkp) {
        super(snapshot);
        errors = Collections.emptyList();
        this.success = success;
        embedded = isEmbedded(snapshot);
        final Lookup baseLkp = Lookups.fixed(this, modelContainer, documentationContainer);
        lookup = new ProxyLookup(baseLkp, additionalLkp);
    }

    @Override
    public final Lookup getLookup() {
        return lookup;
    }

    @Override
    @NonNull
    public final List<? extends FilterableError> getDiagnostics() {
        return getErrors(false);
    }

    @NonNull
    public final List<? extends FilterableError> getErrors(boolean includeFiltered) {
        if (includeFiltered) {
            return Collections.unmodifiableList(errors);
        } else {
            //remove filtered issues
            final List<FilterableError> result = new ArrayList<>(errors.size());
            for(FilterableError e : errors) {
                if(!e.isFiltered()) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    public final void setErrors(@NonNull final List<? extends FilterableError> errors) {
        Parameters.notNull("errors", errors);   //NOI18N
        this.errors = errors;
    }

    public final boolean isEmbedded() {
        return embedded;
    }

    @Override
    protected void invalidate() {
    }

    public static boolean isEmbedded(@NonNull Snapshot snapshot) {
        return !MIME_TYPES.contains(snapshot.getMimePath().getPath());
    }

    boolean success() {
        return this.success;
    }

}
