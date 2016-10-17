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
package org.jetbrains.kotlin.navigation;

import com.intellij.openapi.util.text.StringUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import kotlin.Pair;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.resolve.NetBeansDescriptorUtils;
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansJavaSourceElement;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryPackageSourceElement;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinarySourceElement;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.navigation.netbeans.KotlinHyperlinkProvider;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElement;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaMember;
import org.jetbrains.kotlin.resolve.lang.java.NbElementUtilsKt;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.text.NbDocument;

public class NavigationUtil {
    
    private static PsiElement psiExpression;
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(Document doc, int offset) throws BadLocationException{
        KtFile ktFile = KotlinParser.getFile();
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        if (ktFile == null || !ktFile.getName().equals(fo.getName())){
            ktFile = KotlinPsiManager.INSTANCE.parseText(doc.getText(0, doc.getLength()), fo);
        }
        if (ktFile == null) {
            return null;
        }
        int documentOffset = LineEndUtil.convertCrToDocumentOffset(ktFile.getText(), offset);
        psiExpression = ktFile.findElementAt(documentOffset);
        if (psiExpression == null){
            return null;
        }
        
        return ReferenceUtils.getReferenceExpression(psiExpression);
    }
    
    @Nullable
    public static Pair<Integer, Integer> getSpan(){
        if (psiExpression == null){
            return null;
        }
        
        int start = psiExpression.getTextRange().getStartOffset();
        int end = psiExpression.getTextRange().getEndOffset();
        
        return new Pair<Integer, Integer>(start, end);
    }
    
    @Nullable
    public static SourceElement getElementWithSource(DeclarationDescriptor descriptor, Project project){
        List<SourceElement> sourceElements = NetBeansDescriptorUtils.descriptorToDeclarations(descriptor, project);
        for (SourceElement element : sourceElements){
            if (element != SourceElement.NO_SOURCE){
                return element;
            }
        }
        
        return null;
    }

    public static Pair<Document, Integer> gotoElement(SourceElement element, DeclarationDescriptor descriptor,
            KtElement fromElement, Project project, FileObject currentFile){
        
        if (element instanceof NetBeansJavaSourceElement){
            ElementHandle binding = ((NetBeansJavaElement)((NetBeansJavaSourceElement) element).getJavaElement()).getElementHandle().getElementHandle();
            if (binding == null) {
                return null;
            }
            
            if (binding.getKind() == ElementKind.CONSTRUCTOR){
                JavaClass containingClass = ((NetBeansJavaMember)((NetBeansJavaSourceElement) element).
                        getJavaElement()).getContainingClass();
                binding = ((NetBeansJavaClass) containingClass).getElementHandle().getElementHandle();
            }
            
            gotoJavaDeclaration(binding, project);
        } else if (element instanceof KotlinSourceElement){
            return gotoKotlinDeclaration(((KotlinSourceElement) element).getPsi(), fromElement, project, currentFile);        
        } else if (element instanceof KotlinJvmBinarySourceElement){
            KotlinHyperlinkProvider.gotoKotlinStdlib(
                    (KtReferenceExpression) fromElement, project);
//            gotoElementInBinaryClass(((KotlinJvmBinarySourceElement) element).getBinaryClass(), descriptor, project);
        } else if (element instanceof KotlinJvmBinaryPackageSourceElement){
            KotlinHyperlinkProvider.gotoKotlinStdlib(
                    (KtReferenceExpression) fromElement, project);
//            NavigationUtilsKt.gotoClassByPackageSourceElement(
//                    (KotlinJvmBinaryPackageSourceElement) element, descriptor, project);
        } 
        return null;
    }

    public static void gotoElementInBinaryClass(KotlinJvmBinaryClass binaryClass,
            DeclarationDescriptor descriptor, Project project) {
        String path = binaryClass.getLocation().replace(".class", ".kt");
        String[] internalPath = path.split("!/");
        if (internalPath.length == 1) {
            return;
        }
        
        VirtualFile virtFile = KotlinEnvironment.getEnvironment(project).
                    getVirtualFileInJar(ProjectUtils.buildLibPath("kotlin-runtime-sources"), internalPath[1]);
        
        gotoKotlinStdlib(virtFile, descriptor);
    }
    
    public static boolean gotoKotlinStdlib(VirtualFile virtFile, DeclarationDescriptor desc) {
        if (virtFile == null) {
            return false;
        }
        
        FileObject declarationFile = JarNavigationUtil.getFileObjectFromJar(virtFile.getPath());
        if (declarationFile == null) {
            return false;
        }
        
        KtFile file = null;
        try {
            file = KotlinPsiManager.INSTANCE.
                    getParsedKtFileForSyntaxHighlighting(StringUtilRt.convertLineSeparators(declarationFile.asText()));
        } catch (IOException ex) {
            KotlinLogger.INSTANCE.logException("", ex);
        }
        
        if (file == null) {
            return false;
        }
        
        Collection<KtDeclaration> declarations = PsiTreeUtil.findChildrenOfType(file, KtDeclaration.class);
        
        int startOffset = 0;
        for (KtDeclaration declaration : declarations) {
            String declarationName = declaration.getName();
            if (declarationName == null) {
                continue;
            }
            String fqNameDesc = DescriptorUtils.getFqNameFromTopLevelClass(desc).asString();
            if (declarationName.equals(fqNameDesc)) {
                if (declaration instanceof KtFunction && desc instanceof CallableMemberDescriptor) {
                    List<KtParameter> parameters1 = ((KtFunction) declaration).getValueParameters();
                    List<ValueParameterDescriptor> parameters2 = ((CallableMemberDescriptor) desc).getValueParameters();
                    
                    if (parameters1.size() == parameters2.size()) {
                        boolean isEqual = true;
                        for (int i = 0; i < parameters1.size(); i++) {
                            if (!parameters1.get(i).getNameAsName().equals(parameters2.get(i).getName())) {
                                isEqual = false;
                            }
                        }
                        if (isEqual) {
                            startOffset = declaration.getTextOffset();
                            break;
                        }
                    }
                    
                } else {
                    startOffset = declaration.getTextOffset();
                }
            }
        }
        
        StyledDocument document = null;
        try {
            document = ProjectUtils.getDocumentFromFileObject(declarationFile);
        } catch (IOException ex) {
            KotlinLogger.INSTANCE.logException("No document for " + declarationFile.getPath(), ex);
        }
        if (document == null){
            return false;
        }
        
        
        openFileAtOffset(document, startOffset);
        
        return true;
    }
    
    private static Pair<Document, Integer> gotoKotlinDeclaration(PsiElement element, KtElement fromElement, 
            Project project, FileObject currentFile) {
        FileObject declarationFile = findFileObjectForReferencedElement(
                element, fromElement, project, currentFile);
        if (declarationFile == null){
            return null;
        }
        String text = null;
        StyledDocument document = null;
        try {
            document = ProjectUtils.getDocumentFromFileObject(declarationFile);
            text = document.getText(0, document.getLength());
        } catch (IOException ex) {
            KotlinLogger.INSTANCE.logException("", ex);
        } catch (BadLocationException ex) {
            KotlinLogger.INSTANCE.logException("", ex);
        }
        if (document == null){
            return null;
        }
        
        int startOffset = LineEndUtil.convertCrToDocumentOffset(
                element.getContainingFile().getText(), element.getTextOffset());
        if (!element.getContainingFile().getText().equals(text)) {
            PsiElement elementToNavigate = element.getContainingFile().findElementAt(startOffset);
            if (elementToNavigate != null && text != null) {
                return openFileDespiteOfEncoding(elementToNavigate, startOffset, text, document);
            }
        }
        
        openFileAtOffset(document, startOffset);
        return new Pair<Document, Integer>(document, startOffset);
    }

    private static Pair<Document, Integer> openFileDespiteOfEncoding(PsiElement elementToNavigate, int start, 
            String text, StyledDocument document) {
        int startOffset = start;
        KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedKtFileForSyntaxHighlighting(text);
        Collection<? extends PsiElement> elements1 = PsiTreeUtil.findChildrenOfType(ktFile, elementToNavigate.getClass());
        for (PsiElement elem : elements1) {
            if (elem.getText().equals(elementToNavigate.getText())) {
                if (elem instanceof KtFunction && elementToNavigate instanceof KtFunction) {
                    List<KtParameter> parameters1 = ((KtFunction) elem).getValueParameters();
                    List<KtParameter> parameters2 = ((KtFunction) elementToNavigate).getValueParameters();
                    if (parameters1.size() == parameters2.size()) {
                        boolean isEqual = true;
                        for (int i = 0; i < parameters1.size(); i++) {
                            if (!parameters1.get(i).getName().equals(parameters2.get(i).getName())) {
                                isEqual = false;
                            }
                        }
                        if (isEqual) {
                            startOffset = elem.getTextOffset();
                            break;
                        }
                    }
                } else {
                    startOffset = elem.getTextOffset();
                    break;
                }
            }
        }
        
        openFileAtOffset(document, startOffset);
        return new Pair<Document, Integer>(document, startOffset);
    }
    
    private static void gotoJavaDeclaration(ElementHandle javaElement, Project project) {
        if (javaElement != null){
            NbElementUtilsKt.openInEditor(javaElement, project);
        }
        
    }
    
    private static FileObject findFileObjectForReferencedElement(PsiElement element, 
            KtElement fromElement, Project project, FileObject currentFile){
        
        if (fromElement.getContainingFile() == element.getContainingFile()){
            return currentFile;
        }
        
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null){
            return null;
        }
        
        String path = virtualFile.getPath();
        
        File file = new File(path);
        currentFile = FileUtil.toFileObject(file);
        if (currentFile != null){
            return currentFile;
        } 
        
        currentFile = JarNavigationUtil.getFileObjectFromJar(path);
        if (currentFile != null){
            return currentFile;
        }
        
        return null;
    }
    
    public static void openFileAtOffset(StyledDocument doc, int offset){
        Line line = NbEditorUtilities.getLine(doc, offset, false);
        int colNumber = NbDocument.findLineColumn(doc, offset);
        line.show(Line.ShowOpenType.OPEN,Line.ShowVisibilityType.FRONT, colNumber);
    }
    
}
