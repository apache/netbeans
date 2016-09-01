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
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
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

    private static KtDeclaration findDeclarationBeforeEndOffset(KtFile ktFile, StyledDocument doc, int startOffset, int endOffset, int initialStartOffset) {
        PsiElement element = null;
        if (startOffset > endOffset) {
            return null;
        }

        element = ktFile.findElementAt(startOffset);
        if (element == null) {
            return null;
        }

        KtDeclaration declaration = PsiTreeUtil.getNonStrictParentOfType(element, KtDeclaration.class);

        if (declaration != null && declaration instanceof KtFunction
                && declaration.getTextRange().getStartOffset() < endOffset
                && declaration.getTextRange().getStartOffset() > initialStartOffset) {
            return declaration;
        }

        return findDeclarationBeforeEndOffset(ktFile, doc, element.getTextRange().getEndOffset() + 1, endOffset, initialStartOffset);
    }

    private static KtDeclaration findDeclarationAtLine(KtFile ktFile, StyledDocument doc, int line) {
        int startOffset = NbDocument.findLineOffset(doc, line - 1);
        int endOffset = NbDocument.findLineOffset(doc, line);
        KtDeclaration declaration = findDeclarationBeforeEndOffset(ktFile, doc, startOffset, endOffset, startOffset);

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
            if (containingClass != null) {
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

    public static String getClassFqName(String urlStr, int line) {
        String classFqName = null;
        try {
            URL url = new URL(urlStr);
            File file = Utilities.toFile(url.toURI());
            FileObject fo = FileUtil.toFileObject(file);
            KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedFile(fo);
            classFqName = NoResolveFileClassesProvider.INSTANCE.getFileClassInfo(ktFile).getFacadeClassFqName().toString();
            StyledDocument doc = ProjectUtils.getDocumentFromFileObject(fo);
            int offset = NbDocument.findLineOffset(doc, line - 1);
            PsiElement psi = ktFile.findElementAt(offset);
            if (psi == null) {
                return classFqName;
            }
            KtDeclaration declaration = PsiTreeUtil.getNonStrictParentOfType(psi, KtDeclaration.class);
            if (declaration == null) {
                return classFqName;
            }
            KtClass containingClass = KtPsiUtilKt.containingClass(declaration);
            if (containingClass != null) {
                classFqName = containingClass.getFqName().asString();
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return classFqName;
    }

    public static Object annotate(JPDABreakpoint b, String url, int line) {
        String annotationType;
        if (b instanceof FieldBreakpoint) {
            annotationType = b.isEnabled()
                    ? EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE
                    : EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof MethodBreakpoint) {
            annotationType = b.isEnabled()
                    ? EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE
                    : EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE;
        } else {
            return null;
        }

        return EditorContextBridge.getContext().annotate(url, line, annotationType, null);
    }

    public static MethodBreakpoint findMethodBreakpoint(DebuggerManager manager, String className, String functionName) {
        Breakpoint[] breakpoints = manager.getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof MethodBreakpoint)) {
                continue;
            }
            MethodBreakpoint methodBreakpoint = (MethodBreakpoint) breakpoint;
            if (methodBreakpoint.getMethodName().equals(functionName)) {
                String[] classFilters = methodBreakpoint.getClassFilters();
                for (String classFilter : classFilters) {
                    if (match(classFilter, className)) {
                        return methodBreakpoint;
                    }
                }
            }
        }

        return null;
    }

    private static boolean match(String name, String pattern) {
        if (pattern.startsWith("*")) {
            return name.endsWith(pattern.substring(1));
        } else if (pattern.endsWith("*")) {
            return name.startsWith(
                    pattern.substring(0, pattern.length() - 1)
            );
        }

        return name.equals(pattern);
    }

    public static LineBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            if (!lb.getURL().equals(url)) {
                continue;
            }
            if (lb.getLineNumber() == lineNumber) {
                return lb;
            }
        }
        return null;
    }

}
