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
package org.netbeans.modules.textmate.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

public enum TextmateTokenId implements TokenId {

    TEXTMATE,
    UNTOKENIZED;

    @Override
    public String primaryCategory() {
        return "textmate";
    }

    public static class LanguageHierarchyImpl extends LanguageHierarchy<TextmateTokenId> {

        public static final String GRAMMAR_MARK = "textmate-grammar";
        public static final String INJECTION_MARK = "inject-to";
        private static final Map<String, FileObject> scope2File;
        private static final Map<String, Collection<String>> scope2Injections;
        private static final Map<String, String> mimeType2Scope;

        static {
            scope2File = new HashMap<>();
            scope2Injections = new HashMap<>();
            mimeType2Scope = new HashMap<>();
            FileObject editors = FileUtil.getSystemConfigRoot().getFileObject("Editors");
            if (editors != null) {
                Enumeration<? extends FileObject> en = editors.getChildren(true);
                while (en.hasMoreElements()) {
                    FileObject candidate = en.nextElement();
                    Object attr = candidate.getAttribute(GRAMMAR_MARK);
                    if (attr != null && attr instanceof String) {
                        String scope = (String) attr;
                        scope2File.put(scope, candidate);
                        attr = candidate.getAttribute(INJECTION_MARK);
                        if (attr != null && attr instanceof String) {
                            for (String s : ((String)attr).split(",")) {
                                scope2Injections.computeIfAbsent(s, str -> new ArrayList<>()).add(scope);
                            }
                        } else {
                            mimeType2Scope.put(FileUtil.getRelativePath(editors, candidate.getParent()), scope);
                        }
                    }
                }
            }
        }

        private final String mimeType;
        private final IGrammar grammar;

        public LanguageHierarchyImpl(String mimeType, String scope) throws Exception {
            this.mimeType = mimeType;
            IRegistryOptions opts = new IRegistryOptions() {
                @Override
                public String getFilePath(String scopeName) {
                    FileObject file = scope2File.get(scopeName);
                    return file != null ? file.getNameExt() : null;
                }
                @Override
                public InputStream getInputStream(String scopeName) throws IOException {
                    FileObject file = scope2File.get(scopeName);
                    return file != null ? file.getInputStream(): null;
                }
                @Override
                public Collection<String> getInjections(String scopeName) {
                    return scope2Injections.get(scopeName);
                }
            };
            this.grammar = new Registry(opts).loadGrammar(scope);
        }

        @Override
        protected Collection<TextmateTokenId> createTokenIds() {
            return Arrays.asList(TextmateTokenId.values());
        }

        @Override
        protected Lexer<TextmateTokenId> createLexer(LexerRestartInfo<TextmateTokenId> lri) {
            return new TextmateLexer(lri.input(), lri.state(), lri.tokenFactory(), grammar);
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }

    }
    
    @ServiceProvider(service=MimeDataProvider.class, position=Integer.MAX_VALUE)
    public static final class MimeDataProviderImpl implements MimeDataProvider {

        private static final Logger LOG = Logger.getLogger(MimeDataProviderImpl.class.getName());

        @Override
        public Lookup getLookup(MimePath arg0) {
            String scope = LanguageHierarchyImpl.mimeType2Scope.get(arg0.getPath());
            
            if (scope != null) {
                try {
                    return Lookups.singleton(new LanguageHierarchyImpl(arg0.getPath(), scope).language());
                } catch (Exception ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }

            return null;
        }
        
    }
}
