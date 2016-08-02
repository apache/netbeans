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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import kotlin.Pair;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.resolve.NetBeansDescriptorUtils;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansJavaSourceElement;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElement;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryPackageSourceElement;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinarySourceElement;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor;
import org.netbeans.api.project.Project;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

public class NavigationUtil {
    
    private static PsiElement psiExpression;
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(Document doc, int offset) throws BadLocationException{
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null){
            return null;
        }
        
        KtFile ktFile = ProjectUtils.getKtFile(doc.getText(0, doc.getLength()), file);
        if (ktFile == null){
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
            Element binding = ((NetBeansJavaElement)((NetBeansJavaSourceElement) element).
                    getJavaElement()).getBinding();
            gotoJavaDeclaration(binding, project);
        } else if (element instanceof KotlinSourceElement){
            return gotoKotlinDeclaration(((KotlinSourceElement) element).getPsi(), fromElement, project, currentFile);
        
        } else if (element instanceof KotlinJvmBinarySourceElement){
            gotoElementInBinaryClass(((KotlinJvmBinarySourceElement) element).getBinaryClass(), descriptor, project);
        } else if (element instanceof KotlinJvmBinaryPackageSourceElement){
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
    
    
    
    public static Name getImplClassName(DeserializedCallableMemberDescriptor memberDescriptor) {
        int nameIndex = 0;
        
        try {
            Method getProtoMethod = DeserializedCallableMemberDescriptor.class.getMethod("getProto");
            Object proto = getProtoMethod.invoke(memberDescriptor);
            Field implClassNameField = Class.forName("org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf").getField("implClassName");
            Object implClassName = implClassNameField.get(null);
            Class protobufCallable = Class.forName("org.jetbrains.kotlin.serialization.ProtoBuf\\$Callable");
            Method getExtensionMethod = protobufCallable.getMethod("getExtension", implClassName.getClass());
            Object indexObj = getExtensionMethod.invoke(proto, implClassName);
            
            if (!(indexObj instanceof Integer)) {
                return null;
            }
            
            nameIndex = (Integer) indexObj;
            
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return memberDescriptor.getNameResolver().getName(nameIndex);
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
            Exceptions.printStackTrace(ex);
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
                if (declaration instanceof KtFunction 
                        && desc instanceof KtFunction) {
                    List<KtParameter> parameters1 = ((KtFunction) declaration).getValueParameters();
                    List<KtParameter> parameters2 = ((KtFunction) desc).getValueParameters();
                    
                    if (parameters1.equals(parameters2)) {
                        startOffset = declaration.getTextOffset();
                        break;
                    }
                    
                    
                } else {
                    startOffset = declaration.getTextOffset();
                    break;
                }
            }
        }
        
        StyledDocument document = null;
        try {
            document = ProjectUtils.getDocumentFromFileObject(declarationFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (document == null){
            return false;
        }
        
        
        openFileAtOffset(document, declarationFile, startOffset);
        
        return true;
    }
    
    private static Pair<Document, Integer> gotoKotlinDeclaration(PsiElement element, KtElement fromElement, 
            Project project, FileObject currentFile) {
        FileObject declarationFile = findFileObjectForReferencedElement(
                element, fromElement, project, currentFile);
        if (declarationFile == null){
            return null;
        }
        
        StyledDocument document = null;
        try {
            document = ProjectUtils.getDocumentFromFileObject(declarationFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (document == null){
            return null;
        }
        
        int startOffset = LineEndUtil.convertCrToDocumentOffset(
                element.getContainingFile().getText(), element.getTextOffset());
        openFileAtOffset(document, declarationFile, startOffset);
        return new Pair<Document, Integer>(document, startOffset);
    }

    
    private static void gotoJavaDeclaration(Element binding, Project project) {
        Element javaElement = binding;
        if (binding.getKind() == ElementKind.CONSTRUCTOR){
            javaElement = ((ExecutableElement) binding).getEnclosingElement();
        }
        
        if (javaElement != null){
            NetBeansJavaProjectElementUtils.openElementInEditor(javaElement, project);
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
    
    public static void openFileAtOffset(StyledDocument doc, FileObject file, int offset){
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (dataObject == null){
            return;
        }
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null){
            return;
        }
        
        int lineNumber = NbDocument.findLineNumber(doc, offset);
        int colNumber = NbDocument.findLineColumn(doc, offset);
        
        Line line = lineCookie.getLineSet().getOriginal(lineNumber);
        line.show(Line.ShowOpenType.OPEN,Line.ShowVisibilityType.FRONT, colNumber);
    }
    
}
