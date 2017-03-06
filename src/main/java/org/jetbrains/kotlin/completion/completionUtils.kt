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
package org.jetbrains.kotlin.completion

import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.text.Document
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.resolve.KotlinResolutionFacade
import org.jetbrains.kotlin.utils.LineEndUtil
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptorWithResolutionScopes
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.psi.psiUtil.getReceiverExpression
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.utils.getImplicitReceiversHierarchy
import org.jetbrains.kotlin.idea.codeInsight.ReferenceVariantsHelper
import org.jetbrains.kotlin.load.java.descriptors.JavaClassConstructorDescriptor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.netbeans.modules.csl.api.CompletionProposal

fun applicableNameFor(prefix: String, name: Name): Boolean {
    if (!name.isSpecial()) {
        val identifier = name.identifier
        
        return identifier.toLowerCase().startsWith(prefix.toLowerCase())
    }
    return false
}

fun applicableNameFor(prefix: String, completion: String) = completion.startsWith(prefix) || completion.toLowerCase().startsWith(prefix)

fun filterCompletionProposals(descriptors: Collection<DeclarationDescriptor>, prefix: String) = descriptors
        .filter { applicableNameFor(prefix, it.name) }

fun getResolutionScope(psiElement: PsiElement, bindingContext: BindingContext): LexicalScope? {
    psiElement.parentsWithSelf.forEach {
        if (it is KtElement) {
            val scope = bindingContext.get(BindingContext.LEXICAL_SCOPE, it)
            if (scope != null) return scope
        }
        if (it is KtClassBody) {
            val classDescriptor = bindingContext.get(BindingContext.CLASS, it.parent) as? ClassDescriptorWithResolutionScopes
            if (classDescriptor != null) return classDescriptor.scopeForMemberDeclarationResolution
        }
    }
    
    return null
}
 
private fun TypeParameterDescriptor.isVisible(descriptor: DeclarationDescriptor): Boolean {
    val owner = this.containingDeclaration
    var parent: DeclarationDescriptor? = descriptor
    
    while (parent != null) {
        if (parent == owner) return true
        if (parent is ClassDescriptor && !parent.isInner) return false
        
        parent = parent.containingDeclaration
    }
    
    return true
}

private fun DeclarationDescriptorWithVisibility.isVisible(from: DeclarationDescriptor,
                                                          bindingContext: BindingContext?,
                                                          element: KtSimpleNameExpression?): Boolean {
    if (Visibilities.isVisibleWithAnyReceiver(this, from)) return true
    if (bindingContext == null || element == null) return false
    
    val receiverExpression = element.getReceiverExpression()
    if (receiverExpression != null) {
        val receiverType = bindingContext.getType(receiverExpression) ?: return false
        val explicitReceiver = ExpressionReceiver.create(receiverExpression, receiverType, bindingContext)
        
        return Visibilities.isVisible(explicitReceiver, this, from)
    } else {
        val resolutionScope = getResolutionScope(element, bindingContext) ?: return false
        resolutionScope.getImplicitReceiversHierarchy().forEach {
            if (Visibilities.isVisible(it.value, this, from)) return true
        }
        
        return false
    }
}

fun getReferenceVariants(simpleNameExpression: KtSimpleNameExpression,
                         nameFilter: (Name) -> Boolean,
                         file: FileObject,
                         result: AnalysisResultWithProvider? = null): Collection<DeclarationDescriptor> {
    val project = ProjectUtils.getKotlinProjectForFileObject(file) ?: return emptyList()
    val resultWithProvider = if (result == null) KotlinAnalyzer.analyzeFile(project, simpleNameExpression.getContainingKtFile()) else result
    val analysisResult = resultWithProvider.analysisResult
    val container = resultWithProvider.componentProvider
    
    val inDescriptor = getResolutionScope(simpleNameExpression.getReferencedNameElement(), 
            analysisResult.bindingContext)?.ownerDescriptor ?: return emptyList()
    
    val visibilityFilter: (DeclarationDescriptor) -> Boolean = {
        when (it) {
            is TypeParameterDescriptor -> it.isVisible(inDescriptor)
            is DeclarationDescriptorWithVisibility -> it.isVisible(inDescriptor, analysisResult.bindingContext, simpleNameExpression)
            else -> true
        }
    } 
    
    val helper = ReferenceVariantsHelper(analysisResult.bindingContext,
            KotlinResolutionFacade(project, container, analysisResult.moduleDescriptor),
            analysisResult.moduleDescriptor, visibilityFilter)
    return helper.getReferenceVariants(simpleNameExpression, DescriptorKindFilter.ALL, nameFilter, false, false, false, null)
}

fun getSimpleNameExpression(identOffset: Int): KtSimpleNameExpression? {
    val psi = KotlinParser.file?.findElementAt(identOffset) ?: return null
    return PsiTreeUtil.getParentOfType(psi, KtSimpleNameExpression::class.java)
}

fun getSimpleNameExpression(file: FileObject, identOffset: Int, editorText: String): KtSimpleNameExpression? {
    val sourceCodeWithMarker = StringBuilder(editorText).insert(identOffset, "KotlinNetBeans").toString()
    val ktFile = KotlinPsiManager.parseText(StringUtilRt.convertLineSeparators(sourceCodeWithMarker),
                file) ?: return null
    val offsetWithoutCR = LineEndUtil.convertCrToDocumentOffset(sourceCodeWithMarker, identOffset)
    val psiElement = ktFile.findElementAt(offsetWithoutCR)
    
    return PsiTreeUtil.getParentOfType(psiElement, KtSimpleNameExpression::class.java)
}

fun getIdentifierStartOffset(text: String, offset: Int): Int {
    var identStartOffset = offset
    while ((identStartOffset != 0) && Character.isUnicodeIdentifierPart(text[identStartOffset - 1])){
        identStartOffset--;
    }
        
    return identStartOffset
}

private fun generateBasicCompletionProposals(file: FileObject, identifierPart: String,
                                             identOffset: Int, editorText: String,
                                             result: AnalysisResultWithProvider): Collection<DeclarationDescriptor> {
    var simpleNameExpression = getSimpleNameExpression(identOffset)
    if (simpleNameExpression != null) return getReferenceVariants(simpleNameExpression,
            {applicableNameFor(identifierPart, it)}, file, result)
    
    simpleNameExpression = getSimpleNameExpression(file, identOffset, editorText) ?: return emptyList()
    return getReferenceVariants(simpleNameExpression, {applicableNameFor(identifierPart, it)}, file)
}


fun createProposals(doc: Document, caretOffset: Int,
                    result: AnalysisResultWithProvider,
                    prefix: String): List<CompletionProposal> {
    val file = ProjectUtils.getFileObjectForDocument(doc) ?: return emptyList()
    val styledDoc = doc as? StyledDocument ?: return emptyList()
    val editorText = styledDoc.getText(0, styledDoc.length)
    
    val identOffset = getIdentifierStartOffset(editorText, caretOffset)
    val identifierPart = editorText.substring(identOffset, caretOffset)
    val project = ProjectUtils.getKotlinProjectForFileObject(file) ?: return emptyList()
    val descriptors = generateBasicCompletionProposals(file, identifierPart, identOffset, editorText, result)
    
    val proposals: MutableList<CompletionProposal> = descriptors.filter { it !is JavaClassConstructorDescriptor }
            .map { KotlinCompletionProposal(identOffset, it, styledDoc, prefix, project) }
            .toMutableList()
    val ktFile = KotlinParser.file ?: ProjectUtils.getKtFile(editorText, file) ?:  return proposals
    val psiElement = ktFile.findElementAt(identOffset) ?: return proposals
    
    proposals.addAll(generateKeywordProposals(identifierPart, psiElement, identOffset, prefix))
    val simpleNameExpression = PsiTreeUtil.getParentOfType(psiElement, KtSimpleNameExpression::class.java)
    if (simpleNameExpression != null) {
        proposals.addAll(generateNonImportedCompletionProposals(prefix, ktFile, simpleNameExpression, project, identOffset))
    }
    
    return proposals.distinctBy { it.sortText }
}

fun getValueForType(type: String) = when(type) {
    "Int" -> "0"
    "Long" -> "0"
    "Short" -> "0"
    "Double" -> "0.0"
    "Float" -> "0.0"
    "String" -> "\"\""
    "Char" -> "\"\""
    "Boolean" -> "true"
    else -> null
}