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

package org.netbeans.modules.cnd.completion.cplusplus;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.QueryScope;
import org.netbeans.modules.cnd.completion.csm.CompletionResolverImpl;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;

/**
 * Java completion query which is aware of project context.
 *
 */
public class NbCsmCompletionQuery extends CsmCompletionQuery {
    static {
        CsmCompletionQuery.setCsmItemFactory(new NbCsmItemFactory());
    }
    private CsmFile csmFile;
    private Integer offsetInFile;
    private final QueryScope queryScope;
    private final FileReferencesContext fileReferencesContext;
    private final boolean forceCaseSensitiveMode;
    
    protected NbCsmCompletionQuery(CsmFile csmFile, QueryScope localContext, FileReferencesContext fileReferencesContext, boolean forceCaseSensitiveMode) {
        this.csmFile = csmFile;
        this.queryScope = localContext;
        this.fileReferencesContext = fileReferencesContext;
        this.forceCaseSensitiveMode = forceCaseSensitiveMode;
    }    
    
    @Override
    protected CsmFinder getFinder() {
	CsmFinder finder = null; 
        if (getCsmFile() != null) {
            if (fileReferencesContext != null || forceCaseSensitiveMode) {
                finder = new CsmFinderImpl(getCsmFile(), MIMENames.SOURCES_MIME_TYPE, true);
            } else {
                finder = new CsmFinderImpl(getCsmFile(), MIMENames.SOURCES_MIME_TYPE);
            }
        }
        return finder;
    }

    @Override
    protected FileReferencesContext getFileReferencesContext() {
        return fileReferencesContext;
    }
    
    @Override
    public CsmFile getCsmFile() {
        if (this.csmFile == null) {
            BaseDocument bDoc = getBaseDocument();
            if (bDoc != null) {
                this.csmFile = CsmUtilities.getCsmFile(bDoc, true, false);
                String mimeType = DocumentUtilities.getMimeType(bDoc);
                if ("text/x-dialog-binding".equals(mimeType)) { // NOI18N
                    // this is context based code completion
                    InputAttributes inputAttributes = (InputAttributes) bDoc.getProperty(InputAttributes.class);
                    if (inputAttributes != null) {
                        LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                        offsetInFile = (Integer) inputAttributes.getValue(path, "dialogBinding.offset"); //NOI18N
                        Integer length = (Integer) inputAttributes.getValue(path, "dialogBinding.length"); //NOI18N
                        if ((offsetInFile != null) && (offsetInFile.intValue() != -1) && (length != null)) {
                            offsetInFile = Integer.valueOf(offsetInFile.intValue() - length.intValue());
                        }
                        if ((offsetInFile == null) || (offsetInFile.intValue() == -1)) {
                            // try line number info
                            Integer lineInFile = (Integer) inputAttributes.getValue(path, "dialogBinding.line"); //NOI18N
                            if (lineInFile != null && (lineInFile.intValue() >= 0)) {
                                FileObject fo = (FileObject) inputAttributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                                Document aDoc = CsmUtilities.getDocument(fo);
                                if (aDoc instanceof StyledDocument) {
                                    try {
                                        offsetInFile = NbDocument.findLineOffset((StyledDocument)aDoc, lineInFile.intValue());
                                    } catch (IndexOutOfBoundsException ex) {
                                        // skip
                                    }
                                }
                            }
                        }
                    }
                    CompletionSupport sup = CompletionSupport.get(bDoc);
                    if (offsetInFile != null && sup != null) {
                        sup.setContextOffset(offsetInFile.intValue());
                    }
                }
            }            
        }
        return this.csmFile;
    }

    @Override
    protected QueryScope getCompletionQueryScope() {
        return this.queryScope;
    }
    
    @Override
    protected CompletionResolver getCompletionResolver(CsmScope contextScope, boolean openingSource, boolean sort, boolean inIncludeDirective) {
	return getCompletionResolver(getBaseDocument(), getCsmFile(), contextScope, openingSource, sort, queryScope, inIncludeDirective);
    }

    private CompletionResolver getCompletionResolver(BaseDocument bDoc, CsmFile csmFile, CsmScope contextScope,
            boolean openingSource, boolean sort, QueryScope queryScope, boolean inIncludeDirective) {
	CompletionResolver resolver = null; 
        if (csmFile != null) {
            String mimeType = CsmCompletionUtils.getMimeType(bDoc);
            resolver = new CompletionResolverImpl(csmFile, 
                    openingSource || CsmCompletionUtils.isCaseSensitive(mimeType),
                    sort, CsmCompletionUtils.isNaturalSort(mimeType), fileReferencesContext);
            ((CompletionResolverImpl)resolver).setResolveScope(queryScope);
            ((CompletionResolverImpl)resolver).setInIncludeDirective(inIncludeDirective);
            if (offsetInFile != null) {
                ((CompletionResolverImpl)resolver).setContextOffset(offsetInFile.intValue());
            }
            if (contextScope != null) {
                ((CompletionResolverImpl)resolver).setContextScope(contextScope);
            }
        }
        return resolver;
    }    

    @Override
    protected boolean isProjectBeeingParsed(boolean openingSource) {
        if (!openingSource) {
            if (getFinder() != null) {
                CsmFile file = getFinder().getCsmFile();
                if (file != null && file.getProject() != null) {
                    return !file.getProject().isStable(file);
                }
            }
        }
        return false;
    }

    private static final int PRIORITY_SHIFT = 10;
    private static final int LOCAL_VAR_PRIORITY = 0 + PRIORITY_SHIFT;
    private static final int FIELD_PRIORITY = LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int CLASS_ENUMERATOR_PRIORITY = LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int METHOD_PRIORITY = CLASS_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int CONSTRUCTOR_PRIORITY = METHOD_PRIORITY + PRIORITY_SHIFT;
    
    private static final int CLASS_PRIORITY = CONSTRUCTOR_PRIORITY + PRIORITY_SHIFT;
    private static final int ENUM_PRIORITY = CLASS_PRIORITY; // same as class
    private static final int TYPEDEF_PRIORITY = ENUM_PRIORITY; // same as class
    
    private static final int FILE_LOCAL_VAR_PRIORITY = TYPEDEF_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_ENUMERATOR_PRIORITY = FILE_LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_FUNCTION_PRIORITY = FILE_LOCAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_MACRO_PRIORITY = FILE_LOCAL_FUNCTION_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_INCLUDED_PRJ_MACRO_PRIORITY = FILE_LOCAL_MACRO_PRIORITY + PRIORITY_SHIFT;

    private static final int GLOBAL_VAR_PRIORITY = FILE_INCLUDED_PRJ_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_ENUMERATOR_PRIORITY = GLOBAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_MACRO_PRIORITY = GLOBAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_FUN_PRIORITY = GLOBAL_MACRO_PRIORITY + PRIORITY_SHIFT;

    // 650 is priority for abbreviations
    
    private static final int GLOBAL_NAMESPACE_PRIORITY = 1000;
    private static final int GLOBAL_NAMESPACE_ALIAS_PRIORITY = GLOBAL_NAMESPACE_PRIORITY; // same as project ns

    private static final int LIB_CLASS_PRIORITY = GLOBAL_NAMESPACE_ALIAS_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUM_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    private static final int LIB_TYPEDEF_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    
    private static final int FILE_INCLUDED_LIB_MACRO_PRIORITY = LIB_TYPEDEF_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_MACRO_PRIORITY = FILE_INCLUDED_LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_VAR_PRIORITY = LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUMERATOR_PRIORITY = LIB_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_FUN_PRIORITY = LIB_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_NAMESPACE_PRIORITY = LIB_FUN_PRIORITY + PRIORITY_SHIFT;    
    private static final int LIB_NAMESPACE_ALIAS_PRIORITY = LIB_NAMESPACE_PRIORITY; // same as lib ns
       
    public static final int KEYWORDS_PRIORITY = 5000;

    public static final class NbCsmItemFactory implements CsmCompletionQuery.CsmItemFactory {
        public NbCsmItemFactory() {
        }

        @Override
        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbLocalVariableResultItem(var, LOCAL_VAR_PRIORITY);  
        }          

        @Override
	public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld){
            return new NbCsmResultItem.NbFieldResultItem(fld, FIELD_PRIORITY);
        }
        
        @Override
        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, CLASS_ENUMERATOR_PRIORITY);  
        }          
        
        @Override
	public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes){
            return new NbCsmResultItem.NbMethodResultItem(mtd, substituteExp, METHOD_PRIORITY, isDeclaration, instantiateTypes);
        }
        @Override
	public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes){
            return new NbCsmResultItem.NbConstructorResultItem(ctr, substituteExp, CONSTRUCTOR_PRIORITY, isDeclaration, instantiateTypes);
        }

        @Override
        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
        }
        @Override
        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbForwardClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
        }
        @Override
        public CsmResultItem.ForwardEnumResultItem createForwardEnumResultItem(CsmEnumForwardDeclaration cls, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbForwardEnumResultItem(cls, classDisplayOffset, displayFQN, ENUM_PRIORITY);
        }
        @Override
        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumResultItem(enm, enumDisplayOffset, displayFQN, ENUM_PRIORITY);  
        }  
        @Override
        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbTypedefResultItem(def, classDisplayOffset, displayFQN, TYPEDEF_PRIORITY);  
        }
        
        @Override
        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbFileLocalVariableResultItem(var, FILE_LOCAL_VAR_PRIORITY);  
        }          
        
        @Override
        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, FILE_LOCAL_ENUMERATOR_PRIORITY);  
        } 
        
        @Override
        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_LOCAL_MACRO_PRIORITY);  
        }
        
        @Override
        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new NbCsmResultItem.NbFileLocalFunctionResultItem(fun, substituteExp, FILE_LOCAL_FUNCTION_PRIORITY, isDeclaration, instantiateTypes);
        }
        
        @Override
        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_INCLUDED_PRJ_MACRO_PRIORITY);  
        }
        
        @Override
        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbGlobalVariableResultItem(var, GLOBAL_VAR_PRIORITY);  
        }  
        
        @Override
        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, GLOBAL_ENUMERATOR_PRIORITY);  
        }          

        @Override
        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, GLOBAL_MACRO_PRIORITY);  
        }

        @Override
        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, GLOBAL_FUN_PRIORITY, isDeclaration, instantiateTypes);
        }
        
        @Override
        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, GLOBAL_NAMESPACE_PRIORITY);
        }

        @Override
        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new NbCsmResultItem.NbNamespaceAliasResultItem(alias, displayFullNamespacePath, GLOBAL_NAMESPACE_ALIAS_PRIORITY);
        }
        
        @Override
        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, LIB_CLASS_PRIORITY);
        }
        @Override
        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumResultItem(enm, enumDisplayOffset, displayFQN, LIB_ENUM_PRIORITY);  
        }  
        @Override
        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbTypedefResultItem(def, classDisplayOffset, displayFQN, LIB_TYPEDEF_PRIORITY);  
        }
        
        @Override
        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_INCLUDED_LIB_MACRO_PRIORITY);  
        }   
        
        @Override
        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, LIB_MACRO_PRIORITY);  
        }  
        
        @Override
        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbGlobalVariableResultItem(var, LIB_VAR_PRIORITY);  
        }  
        
        @Override
        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, LIB_ENUMERATOR_PRIORITY);  
        }  
        
        @Override
        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, LIB_FUN_PRIORITY, false, instantiateTypes);
        }
        
        @Override
        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, LIB_NAMESPACE_PRIORITY);
        }         

        @Override
        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new NbCsmResultItem.NbNamespaceAliasResultItem(alias, displayFullNamespacePath, LIB_NAMESPACE_ALIAS_PRIORITY);
        }

        @Override
        public CsmResultItem.TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par) {
            return new NbCsmResultItem.NbTemplateParameterResultItem(par, CLASS_ENUMERATOR_PRIORITY);
        }

        @Override
        public CsmResultItem createLabelResultItem(CsmLabel csmStatement) {
            return new NbCsmResultItem.LabelResultItem(csmStatement, LOCAL_VAR_PRIORITY);
        }

    }
}
