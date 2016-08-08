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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander.Baratynski
 */
@ActionsProvider.Registrations({
    @ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" }),
    @ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" })
})
public class KotlinToggleBreakpointActionProvider extends ActionsProviderSupport 
    implements PropertyChangeListener {

    private JPDADebugger debugger;
    
    public KotlinToggleBreakpointActionProvider() {
        KotlinEditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }
    
    public KotlinToggleBreakpointActionProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, 
                KotlinToggleBreakpointActionProvider.this);
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        KotlinEditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
    }
    
    private void destroy() {
        debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
        KotlinEditorContextBridge.getContext().removePropertyChangeListener(this);
    }
    
    private KtDeclaration findDeclarationBeforeEndOffset(KtFile ktFile, StyledDocument doc, int startOffset, int endOffset){
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
    
    private KtDeclaration findDeclarationAtLine(KtFile ktFile, StyledDocument doc, int line) {
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
        
//        KtPsiUtilKt.
        
        return declaration;
    }
    
    @Override
    public void doAction(Object o) {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        
        int lineNumber = KotlinEditorContextBridge.getContext().getCurrentLineNumber();
        String urlStr = KotlinEditorContextBridge.getContext().getCurrentURL();
        
        if ("".equals(urlStr.trim())) {
            return;
        }
        
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
                return;
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
        
        if (name == null || classFqName == null) {
            return;
        }
        
//        if (classFqName.endsWith("Kt")) {
//            classFqName = classFqName.substring(0, classFqName.length()-2);
//        }
        
        JPDABreakpoint lineBreakpoint = MethodBreakpoint.create(classFqName, name);
        
//        KotlinLineBreakpoint lineBreakpoint = findBreakpoint(url, lineNumber);
//        if (lineBreakpoint != null) {
//            manager.removeBreakpoint(lineBreakpoint);
//            return;
//        }
//        
//        lineBreakpoint = KotlinLineBreakpoint.create(url, lineNumber);
//        lineBreakpoint.setPrintText("breakpoint");
        manager.addBreakpoint(lineBreakpoint);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String url = KotlinEditorContextBridge.getContext().getCurrentURL();
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            fo = null;
        }
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT,
                (KotlinEditorContextBridge.getContext().getCurrentLineNumber() >= 0) &&
                        (fo != null && "text/x-kt".equals(fo.getMIMEType())));
        if (debugger != null && debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            destroy();
        }
    }
    
    private static KotlinLineBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof KotlinLineBreakpoint)) {
                continue;
            }
            KotlinLineBreakpoint lineBreakpoint = (KotlinLineBreakpoint) breakpoint;
            if (!lineBreakpoint.getURL().equals(url)) {
                continue;
            }
            if (lineBreakpoint.getLineNumber() == lineNumber) {
                return lineBreakpoint;
            }
        }
        
        return null;
    }
    
}