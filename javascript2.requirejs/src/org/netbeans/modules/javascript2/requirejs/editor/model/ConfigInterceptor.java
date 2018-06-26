/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.editor.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.requirejs.editor.EditorUtils;
import org.netbeans.modules.javascript2.requirejs.editor.index.RequireJsIndexer;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 351)
public class ConfigInterceptor implements FunctionInterceptor {

    private final static Pattern PATTERN = Pattern.compile("(require|requirejs)\\.config");  //NOI18N
    private static final String PROPERTY_NAME = "name"; //NOI18N
    private static final String PROPERTY_LOCATION = "location"; //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject, DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (!RequireJsIndexer.Factory.isScannerThread()) {
            return Collections.emptyList();

        }
        FunctionArgument fArg = null;
        for (FunctionArgument farg : args) {
            if (farg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                fArg = farg;
                break;
            }
        }

        if (fArg != null && fArg.getValue() instanceof JsObject) {
            // find paths property
            JsObject paths = ((JsObject) fArg.getValue()).getProperty(EditorUtils.PATHS);
            // find baseUrl property
            JsObject baseUrl = ((JsObject) fArg.getValue()).getProperty(EditorUtils.BASE_URL);
            // find packages property
            JsObject packages = ((JsObject) fArg.getValue()).getProperty(EditorUtils.PACKAGES);

            if (paths != null || baseUrl != null || packages != null) {
                FileObject fo = globalObject.getFileObject();
                if (fo == null) {
                    return Collections.emptyList();
                }
                Source source = Source.create(fo);
                TokenHierarchy<?> th = source.createSnapshot().getTokenHierarchy();
                TokenSequence<? extends JsTokenId> ts;
                Token<? extends JsTokenId> token;
                if (paths != null) {
                    // find defined mappings 
                    ts = LexUtilities.getJsTokenSequence(th, paths.getOffset());
                    if (ts == null) {
                        return Collections.emptyList();
                    }
                    Map<String, String> mapping = new HashMap<>();
                    for (JsObject path : paths.getProperties().values()) {
                        String alias = path.getName();
                        String target = null;
                        ts.move(path.getOffset());
                        if (ts.moveNext()) {
                            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.OPERATOR_COLON));
                            if (token.id() == JsTokenId.OPERATOR_COLON) {
                                token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                                        JsTokenId.STRING_BEGIN, JsTokenId.OPERATOR_COLON));
                                if (token.id() == JsTokenId.STRING) {
                                    target = token.text().toString();
                                }
                            }
                        }
                        if (target != null) {
                            mapping.put(alias, target);
                        }
                    }
                    if (!mapping.isEmpty()) {
                        RequireJsIndexer.addPathMapping(fo.toURI(), mapping);
                    }
                }

                if (baseUrl != null) {
                    // find defined mappings 
                    ts = LexUtilities.getJsTokenSequence(th, baseUrl.getOffset());
                    if (ts == null) {
                        return Collections.emptyList();
                    }
                    ts.move(baseUrl.getOffset());
                    String target = null;
                    if (ts.moveNext()) {
                        token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.OPERATOR_COLON));
                        if (token.id() == JsTokenId.OPERATOR_COLON) {
                            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                                    JsTokenId.STRING_BEGIN, JsTokenId.OPERATOR_COLON));
                            if (token.id() == JsTokenId.STRING) {
                                target = token.text().toString();
                            }
                        }
                    }
                    if (target != null) {
                        RequireJsIndexer.addBasePath(fo.toURI(), target);
                    }
                }

                if (packages != null && packages instanceof JsArray) {
                    Map<String, String> packagesMap = loadPackages(th, packages.getOffset());
                    if (packagesMap != null) {
                        // save packages to the index
                        RequireJsIndexer.addPackages(fo.toURI(), packagesMap);
                    }
                }

                // save the name of the source root folder
                final Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    for (FileObject dir : project.getProjectDirectory().getChildren()) {
                        if (dir.isFolder() && FileUtil.isParentOf(dir, fo)) {
                            RequireJsIndexer.addSourceRoot(fo.toURI(), dir.getName());
                            break;
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private Map<String, String> loadPackages(TokenHierarchy<?> th, int offset) {
        Map<String, String> packagesMap = new HashMap<>();
        // find CommonJS packges
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, offset);
        Token<? extends JsTokenId> token;
        if (ts != null) {
            ts.move(offset);
            if (ts.moveNext()) {
                token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_BRACKET));
                while (token != null && token.id() != JsTokenId.BRACKET_RIGHT_BRACKET) {
                    token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_BRACKET));
                    if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                        // we are in the anonymous object defining the package
                        String packageName = null;
                        String packageLocation = null;
                        while (token != null && token.id() != JsTokenId.BRACKET_RIGHT_CURLY) {
                            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.BRACKET_RIGHT_CURLY));
                            if (token.text().equals(PROPERTY_NAME)) {
                                ts.moveNext();
                                token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.OPERATOR_COLON, JsTokenId.WHITESPACE, JsTokenId.STRING_BEGIN));
                                packageName = token != null && token.id() == JsTokenId.STRING ? token.text().toString() : null;
                            } else if (token.text().equals(PROPERTY_LOCATION)) {
                                ts.moveNext();
                                token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.OPERATOR_COLON, JsTokenId.WHITESPACE, JsTokenId.STRING_BEGIN));
                                packageLocation = token != null && token.id() == JsTokenId.STRING ? token.text().toString() : null;
                            } else {
                                ts.moveNext();
                            }
                        }
                        if (packageName != null && !packageName.isEmpty()
                                && packageLocation != null && !packageLocation.isEmpty()) {
                            packagesMap.put(packageName, packageLocation);
                        }
                    } else {
                        if (ts.moveNext()) {
                            token = ts.token();
                        } else {
                            break;
                        }
                    }
                    
                }
            }
        }
        return packagesMap;
    }
}
