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

package org.netbeans.modules.cnd.modelimpl.parser.spi;

import java.util.Collection;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmParserProvider {
    public static final CsmParserProvider DEFAULT = new Default();
    
    public static CsmParser createParser(final CsmFile file) {
        return createParser(new CsmParserParameters() {
            
            @Override
            public CsmFile getMainFile() { 
                return file; 
            }

            @Override
            public String getLanguage() {
                return Utils.getLanguage(file.getFileType(), file.getAbsolutePath().toString());
            }

            @Override
            public String getLanguageFlavor() {
                if (file instanceof FileImpl) {
                    return ((FileImpl)file).getFileLanguageFlavor();
                }
                return APTLanguageSupport.FLAVOR_UNKNOWN;
            }

        });
    }

    public static CsmParser createParser(CsmParserParameters params) {
        return DEFAULT.create(params);
    }
    
    protected abstract CsmParser create(CsmParserParameters params);

    public interface CsmParseCallback {
        
    }

    public interface CsmParserParameters {
        
        CsmFile getMainFile();
        
        String getLanguage();
        
        String getLanguageFlavor();
        
    }
    
    public interface CsmParser {
        enum ConstructionKind {
            TRANSLATION_UNIT, 
            TRANSLATION_UNIT_WITH_COMPOUND, // do not skip compound statements
            CLASS_BODY,
            ENUM_BODY,
            TRY_BLOCK,
            COMPOUND_STATEMENT,
            INITIALIZER,
            NAMESPACE_DEFINITION_BODY,
            FUNCTION_DEFINITION_AFTER_DECLARATOR
        }
        void init(CsmObject object, TokenStream ts, CsmParseCallback callback);
        CsmParserResult parse(ConstructionKind kind);
        void setErrorDelegate(ParserErrorDelegate delegate);
    }
    
    public interface CsmParserResult {
        void render(Object... context);
        boolean isEmptyAST();
        Object getAST();
        void dumpAST();
        int getErrorCount();
    }
    
    public static void registerParserError(ParserErrorDelegate delegate, String message, CsmFile file, int offset) {
        if (file != null) {
            int[] lineColumn = ((FileImpl)file).getLineColumn(offset);
            String s = file.getAbsolutePath()+":"+lineColumn[0]+":"+lineColumn[1]+": error: "+message; //NOI18N
            delegate.onError(new ParserError(s, lineColumn[0], lineColumn[1], "", offset < 0)); //NOI18N
        } else {
            delegate.onError(new ParserError(message, -1, -1, "", offset < 0)); //NOI18N
        }
    }
    
    public static final class ParserError {
        public String message;
        public String tokenText;
        public int line;
        public int column;
        public boolean eof;
        
        public ParserError(String message, int line, int column, String tokenText, boolean eof) {
            this.message = message;
            this.line = line;
            this.column = column;
            this.tokenText = tokenText;
            this.eof = eof;
        }

        public int getColumn() {
            return column;
        }

        public int getLine() {
            return line;
        }

        public String getMessage() {
            return message;
        }

        public String getTokenText() {
            return tokenText;
        }
        
        public boolean isEof() {
            return eof;
        }        

        @Override
        public String toString() {
            return message;// + " :" + (eof ? "<EOF>" : tokenText); // NOI18N
        }
    }
    
    public interface ParserErrorDelegate {
        void onError(ParserError e);
    }
    
    private static final class Default extends CsmParserProvider {
        private final Collection<? extends CsmParserProvider> parserProviders;

        public Default() {
            parserProviders = Lookup.getDefault().lookupAll(CsmParserProvider.class);
        }
                

        @Override
        protected CsmParser create(CsmParserParameters params) {
            for (CsmParserProvider provider : parserProviders) {
                CsmParser out = provider.create(params);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }
        
    }
}
