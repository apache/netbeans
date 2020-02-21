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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.ShiftedTokenStream;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.ParserProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.spi.model.TypesProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.TypesProvider.class)
public class TypesProviderImpl implements TypesProvider {
    
    // Now both decltypes and typeofs are handled as decltypes
    private static final CharSequence DECLTYPE_ALIASES[] = {
        CppTokenId.DECLTYPE.fixedText(),
        CppTokenId.__DECLTYPE.fixedText(),
        CppTokenId.TYPEOF.fixedText(),
        CppTokenId.__TYPEOF.fixedText(),
        CppTokenId.__TYPEOF__.fixedText()
    };
    
    @Override
    public CsmType createType(CharSequence sequence, CsmScope scope, CsmTypes.SequenceDescriptor descriptor) {
        AST typeAst = tryParseType(sequence, descriptor.lang, descriptor.langFlavour, descriptor.offsets);
        if (typeAst != null) {
            AST ptrOperator = AstUtil.findSiblingOfType(typeAst, CPPTokenTypes.CSM_PTR_OPERATOR);
            return TypeFactory.createType(
                typeAst, 
                descriptor.offsets.getContainer(), 
                ptrOperator, 
                0, 
                null, 
                scope, 
                descriptor.inFunctionParams | descriptor.inTemplateDescriptor,  // TODO: they should be separated
                descriptor.inTypedef
            );
        }
        return null;
    }    
    
    @Override
    public CsmType createType(CsmClassifier cls, CsmTypes.TypeDescriptor td, CsmTypes.OffsetDescriptor offs) {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }    

    @Override
    public CsmType createType(CsmType orig, CsmTypes.TypeDescriptor newDescriptor) {
        return TypeFactory.createType(
                orig, 
                newDescriptor.getPtrDepth(), 
                CsmTypes.TypeDescriptor.getReferenceType(newDescriptor), 
                newDescriptor.getArrDepth(), 
                newDescriptor.isConst(),
                newDescriptor.isVolatile()
        );
    }

    @Override
    public boolean isDecltype(CharSequence classifierText) {
        for (CharSequence alias : getDecltypeAliases()) {
            if (CharSequenceUtils.contentEquals(alias, classifierText)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public CharSequence[] getDecltypeAliases() {
        return DECLTYPE_ALIASES;
    }
    
    private static AST tryParseType(CharSequence sequence, String lang, String langFlavour, CsmTypes.OffsetDescriptor offs) {
        CPPParserEx parser = createParser(sequence, lang, langFlavour, offs);
        if (parser != null) {
            parser.type_name();
            if (!parser.matchError) {
                return parser.getAST();
            }
        }
        return null;
    }    
    
    private static CPPParserEx createParser(CharSequence sequence, String lang, String langFlavour, CsmTypes.OffsetDescriptor offs) {
        APTFile.Kind aptKind = APTDriver.langFlavorToAPTFileKind(lang, langFlavour);
        TokenStream ts = APTTokenStreamBuilder.buildTokenStream(sequence.toString(), aptKind);
        if (ts != null) {
            if (offs.getStartOffset() != 0) {
                ts = new ShiftedTokenStream(ts, offs.getStartOffset());
            }
            int flags = getParserFlags(lang, langFlavour);        
            APTLanguageFilter langFilter = APTLanguageSupport.getInstance().getFilter(lang, langFlavour);
            return CPPParserEx.getInstance("In_memory_parse", langFilter.getFilteredStream(ts), flags); // NOI18N
        }
        return null;
    }
    
    private static int getParserFlags(String lang, String langFlavour) {
        return ParserProviderImpl.adjustAntlr2ParserFlagsForLanguage(CPPParserEx.CPP_SUPPRESS_ERRORS, lang, langFlavour);
    }
}
