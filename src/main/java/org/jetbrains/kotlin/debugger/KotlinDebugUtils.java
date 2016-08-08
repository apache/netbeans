/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.text.StyledDocument;
import kotlin.Pair;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import static org.jetbrains.kotlin.debugger.KotlinEditorContextBridge.getContext;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinDebugUtils {

    private static KtDeclaration findDeclarationBeforeEndOffset(KtFile ktFile, StyledDocument doc, int startOffset, int endOffset){
        PsiElement element = null;
        if (startOffset > endOffset) {
            return null;
        }
        
        element = ktFile.findElementAt(startOffset);
        if (element == null) {
            return null;
        }
        
        KtDeclaration declaration = PsiTreeUtil.getNonStrictParentOfType(element, KtDeclaration.class);
        
        if (declaration != null && declaration instanceof KtFunction) {
            return declaration;
        } 
        
        return findDeclarationBeforeEndOffset(ktFile, doc, element.getTextRange().getEndOffset() + 1, endOffset);
    }
    
    private static KtDeclaration findDeclarationAtLine(KtFile ktFile, StyledDocument doc, int line) {
        int startOffset = NbDocument.findLineOffset(doc, line - 1);
        int endOffset = NbDocument.findLineOffset(doc, line);
        KtDeclaration declaration = null;
        
        PsiElement psi = ktFile.findElementAt(startOffset);
        if (psi == null) {
            return null;
        }
        
        declaration = PsiTreeUtil.getNonStrictParentOfType(psi, KtDeclaration.class);
        if (declaration != null && declaration instanceof KtFunction) {
            return declaration;
        }
        
        int psiEndOffset = psi.getTextRange().getEndOffset() + 1;
        
        declaration = findDeclarationBeforeEndOffset(ktFile, doc, psiEndOffset, endOffset);
        
        return declaration;
    }
    
    public static Pair<String, String> getFunctionNameAndContainingClass(String urlStr, int lineNumber) {
        String name = null;
        String classFqName = null;
        
        try {
            URL url = new URL(urlStr);
            File file = Utilities.toFile(url.toURI());
            FileObject fo = FileUtil.toFileObject(file);
            KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedFile(fo);
            StyledDocument doc = ProjectUtils.getDocumentFromFileObject(fo);
            KtDeclaration declaration = findDeclarationAtLine(ktFile, doc, lineNumber);
            if (declaration == null) {
                return null;
            }
            name = declaration.getName();
            KtClass containingClass = KtPsiUtilKt.containingClass(declaration);
            if (containingClass != null){
                classFqName = containingClass.getFqName().asString();
            } else {
                classFqName = NoResolveFileClassesProvider.INSTANCE.getFileClassInfo(ktFile).getFacadeClassFqName().toString();
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return new Pair<String, String>(classFqName, name);
    }
    
    public static void annotate(JPDABreakpoint b, String url, int line) {
        boolean isConditional = false;
        String annotationType;
        if (b instanceof LineBreakpoint) {
            annotationType = b.isEnabled () ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        } else if (b instanceof FieldBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof MethodBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE;
        } else {
            return;
        }
        
        getContext().annotate(url, line, annotationType, null);
    }
    
}
