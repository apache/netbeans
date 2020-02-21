/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
