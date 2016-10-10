/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.completion;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.text.StringUtilRt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.resolve.KotlinResolutionFacade;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.ClassDescriptorWithResolutionScopes;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility;
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor;
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtClassBody;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.scopes.LexicalScope;
import org.openide.filesystems.FileObject;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;
import org.jetbrains.kotlin.psi.psiUtil.PsiUtilsKt;
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.kotlin.resolve.scopes.utils.ScopeUtilsKt;
import org.jetbrains.kotlin.idea.codeInsight.ReferenceVariantsHelper;
import org.jetbrains.kotlin.load.java.descriptors.JavaConstructorDescriptor;
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CompletionProposal;

/**
 *
 * @author Александр
 */
public class KotlinCompletionUtils {

    private final String KOTLIN_DUMMY_IDENTIFIER = "KotlinNetBeans";
    
    public static final KotlinCompletionUtils INSTANCE = 
            new KotlinCompletionUtils();
    
    private KotlinCompletionUtils(){
        createTypesToValuesMap();
    }
    
    private final Map<String, String> typeToValues = new HashMap<String, String>();
    
    public boolean applicableNameFor(String prefix, Name name){
        if (!name.isSpecial()){
            String identifier = name.getIdentifier();
            
            return identifier.startsWith(prefix) ||
                    identifier.toLowerCase().startsWith(prefix);
                    
            
        } else return false;
        
    }
    
    public Collection<DeclarationDescriptor> filterCompletionProposals(Collection<DeclarationDescriptor> descriptors,
            String prefix){
        Collection<DeclarationDescriptor> filteredDescriptors = Lists.newArrayList();
        
        for (DeclarationDescriptor descriptor : descriptors){
            if (applicableNameFor(prefix, descriptor.getName())){
                filteredDescriptors.add(descriptor);
            }
        }
        
        return filteredDescriptors;
    }
    
    public Collection<DeclarationDescriptor> getReferenceVariants(final KtSimpleNameExpression simpleNameExpression,
            Function1<Name, Boolean> nameFilter, FileObject file, AnalysisResultWithProvider resultWithProvider){
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        
        if (project == null){
            return Collections.emptyList();
        }
        
        final AnalysisResult analysisResult = resultWithProvider.getAnalysisResult();
        ComponentProvider container = resultWithProvider.getComponentProvider();
        
        final DeclarationDescriptor inDescriptor = getResolutionScope(simpleNameExpression.getReferencedNameElement(),
                analysisResult.getBindingContext()).getOwnerDescriptor();
    
        Function1<DeclarationDescriptor, Boolean> visibilityFilter = 
                new Function1<DeclarationDescriptor, Boolean>(){
            @Override
            public Boolean invoke(DeclarationDescriptor descriptor) {
                if (descriptor instanceof TypeParameterDescriptor){
                    return isVisible((TypeParameterDescriptor) descriptor, inDescriptor);
                } 
                else if (descriptor instanceof DeclarationDescriptorWithVisibility){
                    return isVisible((DeclarationDescriptorWithVisibility) descriptor,
                            inDescriptor, analysisResult.getBindingContext(),
                            simpleNameExpression);
                } else return true;
            }
        };
        
        ReferenceVariantsHelper helper = new ReferenceVariantsHelper(
            analysisResult.getBindingContext(),
            new KotlinResolutionFacade(project, container, analysisResult.getModuleDescriptor()),
            analysisResult.getModuleDescriptor(),
            visibilityFilter);
        
        return helper.getReferenceVariants(simpleNameExpression, DescriptorKindFilter.ALL, nameFilter,
                false,false,false,null);
    }
    
    public Collection<DeclarationDescriptor> getReferenceVariants(final KtSimpleNameExpression simpleNameExpression,
            Function1<Name, Boolean> nameFilter, FileObject file){
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        
        if (project == null){
            return Collections.emptyList();
        }
        
        AnalysisResultWithProvider resultWithProvider = 
                KotlinAnalyzer.analyzeFile(project, simpleNameExpression.getContainingKtFile());
        
        final AnalysisResult analysisResult = resultWithProvider.getAnalysisResult();
        ComponentProvider container = resultWithProvider.getComponentProvider();
        
        final DeclarationDescriptor inDescriptor = getResolutionScope(simpleNameExpression.getReferencedNameElement(),
                analysisResult.getBindingContext()).getOwnerDescriptor();
    
        Function1<DeclarationDescriptor, Boolean> visibilityFilter = 
                new Function1<DeclarationDescriptor, Boolean>(){
            @Override
            public Boolean invoke(DeclarationDescriptor descriptor) {
                if (descriptor instanceof TypeParameterDescriptor){
                    return isVisible((TypeParameterDescriptor) descriptor, inDescriptor);
                } 
                else if (descriptor instanceof DeclarationDescriptorWithVisibility){
                    return isVisible((DeclarationDescriptorWithVisibility) descriptor,
                            inDescriptor, analysisResult.getBindingContext(),
                            simpleNameExpression);
                } else return true;
            }
        };
        
        ReferenceVariantsHelper helper = new ReferenceVariantsHelper(
            analysisResult.getBindingContext(),
            new KotlinResolutionFacade(project, container, analysisResult.getModuleDescriptor()),
            analysisResult.getModuleDescriptor(),
            visibilityFilter);
        
        return helper.getReferenceVariants(simpleNameExpression, DescriptorKindFilter.ALL, nameFilter,
                false,false,false,null);
    }
    
    public KtSimpleNameExpression getSimpleNameExpression(int identOffset) {
        KtFile ktFile = KotlinParser.getFile();
        PsiElement psi = ktFile.findElementAt(identOffset);
        
        return PsiTreeUtil.getParentOfType(psi, KtSimpleNameExpression.class);
    }
    
    public KtSimpleNameExpression getSimpleNameExpression(FileObject file, int identOffset, String editorText) throws IOException{
        String sourceCodeWithMarker = new StringBuilder(editorText).
                insert(identOffset, KOTLIN_DUMMY_IDENTIFIER).toString();
    
        KtFile ktFile = KotlinPsiManager.INSTANCE.parseText(StringUtilRt.convertLineSeparators(sourceCodeWithMarker),
                file);
        
        
        if (ktFile == null){
            return null;
        }
        
        int offsetWithoutCR = LineEndUtil.convertCrToDocumentOffset(sourceCodeWithMarker, identOffset);
        PsiElement psiElement = ktFile.findElementAt(offsetWithoutCR);
        
        return PsiTreeUtil.getParentOfType(psiElement, KtSimpleNameExpression.class);
    }
    
    private LexicalScope getResolutionScope(PsiElement psiElement, BindingContext bindingContext){
        Iterator<PsiElement> it = PsiUtilsKt.getParentsWithSelf(psiElement).iterator();
        while (it.hasNext()){
            PsiElement parent = it.next();
            if (parent instanceof KtElement){
                LexicalScope scope = 
                        bindingContext.get(
                                BindingContext.LEXICAL_SCOPE, 
                                (KtElement) parent);
                if (scope != null){
                    return scope;
                }
            }
            
            if (parent instanceof KtClassBody){
                ClassDescriptorWithResolutionScopes classDescriptor = 
                        (ClassDescriptorWithResolutionScopes) bindingContext.get(
                                BindingContext.CLASS, parent.getParent());
                if (classDescriptor != null){
                    return classDescriptor.getScopeForMemberDeclarationResolution();
                }
                
            }
        }
        return null;
    }
    
    
    private boolean isVisible(TypeParameterDescriptor typeParameterDescriptor,
            DeclarationDescriptor declarationDescriptor){
        DeclarationDescriptor owner = typeParameterDescriptor.getContainingDeclaration();
        DeclarationDescriptor parent = declarationDescriptor;
        
        while (parent != null){
            if (parent == owner){
                return true;
            }
            if (parent instanceof ClassDescriptor && !((ClassDescriptor)parent).isInner()){
                return false;
            }
            parent = parent.getContainingDeclaration();
        }
        
        return true;
    }
    
    private boolean isVisible(DeclarationDescriptorWithVisibility declarationDescriptorWithVisibility,
            DeclarationDescriptor from, BindingContext bindingContext, 
            KtSimpleNameExpression element){
        if (Visibilities.isVisibleWithAnyReceiver(declarationDescriptorWithVisibility, from)){
            return true;
        }
        
        if (bindingContext == null || element == null){
            return false;
        }
        
        KtExpression receiverExpression = KtPsiUtilKt.getReceiverExpression(element);
        if (receiverExpression != null){
            KotlinType receiverType = bindingContext.getType(receiverExpression);
            if (receiverType == null){
                return false;
            }
            
            ExpressionReceiver explicitReceiver = 
                    ExpressionReceiver.Companion.create(receiverExpression, receiverType, bindingContext);
            
            return Visibilities.isVisible(explicitReceiver, declarationDescriptorWithVisibility, from);
        }
        else {
            LexicalScope resolutionScope = getResolutionScope(element, bindingContext);
            for (ReceiverParameterDescriptor desc : 
                    ScopeUtilsKt.getImplicitReceiversHierarchy(resolutionScope)){
                if (Visibilities.isVisible(desc.getValue(),declarationDescriptorWithVisibility, from)){
                    return true;
                }
            }
            return false;
        }
        
    }
    
    public List<CompletionProposal> createProposals(Document doc, int caretOffset,
            AnalysisResultWithProvider analysisResultWithProvider, String prefix) throws BadLocationException, IOException {
        List<CompletionProposal> proposals = Lists.newArrayList();
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        StyledDocument styledDoc = (StyledDocument) doc;
        String editorText = styledDoc.getText(0, styledDoc.getLength());
        
        int identOffset = getIdentifierStartOffset(editorText, caretOffset);
        
        String identifierPart = editorText.substring(identOffset, caretOffset);
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        Collection<DeclarationDescriptor> descriptors = 
                generateBasicCompletionProposals(file, identifierPart, 
                        identOffset, editorText, analysisResultWithProvider);
        
        for (DeclarationDescriptor descriptor : descriptors){
            if (descriptor instanceof JavaConstructorDescriptor) {
                continue;
            }
            proposals.add(new KotlinCompletionProposal(identOffset, caretOffset, 
                    descriptor, styledDoc, prefix, project));
        }
    
        return proposals; 
    }
    
    @NotNull
    private Collection<DeclarationDescriptor> generateBasicCompletionProposals(
        final FileObject file, final String identifierPart, 
            int identOffset, String editorText, AnalysisResultWithProvider analysisResultWithProvider) throws IOException{
        Function1<Name, Boolean> nameFilter = new Function1<Name, Boolean>(){
            @Override
            public Boolean invoke(Name name) {
                return applicableNameFor(identifierPart, name);
            }
        };
        
        KtSimpleNameExpression simpleNameExpression = getSimpleNameExpression(identOffset);
        if (simpleNameExpression != null) {
            return getReferenceVariants(simpleNameExpression,
                nameFilter, file, analysisResultWithProvider);
        }
        
        simpleNameExpression = 
                getSimpleNameExpression(file, identOffset, editorText);
        if (simpleNameExpression == null){
            return Collections.emptyList();
        }
        
        return getReferenceVariants(simpleNameExpression,
                nameFilter, file);
    }
    
    public int getIdentifierStartOffset(String text, int offset){
        int identStartOffset = offset;
        
        while ((identStartOffset != 0) && Character.isUnicodeIdentifierPart(text.charAt(identStartOffset - 1))){
            identStartOffset--;
        }
        
        return identStartOffset;
    }
    
    private void createTypesToValuesMap() {
        typeToValues.put("Int", "0");
        typeToValues.put("Long", "0");
        typeToValues.put("Short", "0");
        typeToValues.put("Double", "0.0");
        typeToValues.put("Float", "0.0");
        typeToValues.put("String", "\"\"");
        typeToValues.put("Char", "\"\"");
        typeToValues.put("Boolean", "true");
    }
    
    public String getValueForType(String type) {
        return typeToValues.get(type);
    }
    
}
