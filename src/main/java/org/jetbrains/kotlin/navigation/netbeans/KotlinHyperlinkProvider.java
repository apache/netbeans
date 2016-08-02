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
package org.jetbrains.kotlin.navigation.netbeans;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import kotlin.Pair;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.navigation.NavigationUtil;
import org.jetbrains.kotlin.navigation.references.KotlinReference;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.jvm.JvmClassName;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType = "text/x-kt", service = HyperlinkProvider.class)
public class KotlinHyperlinkProvider implements HyperlinkProvider {

    private KtReferenceExpression referenceExpression;
    private Pair<Document, Integer> navigationCache = null;
    
    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        if (ProjectUtils.getKotlinProjectForFileObject(fo) == null){
            return false;
        }
        try {
            referenceExpression = NavigationUtil.getReferenceExpression(doc, offset);
            return referenceExpression != null;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (isHyperlinkPoint(doc,offset)){
            Pair<Integer, Integer> span = NavigationUtil.getSpan();
            if (span == null){
                return null;
            }
            return new int[]{span.getFirst(),span.getSecond()};
        }
        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset) {
        if (referenceExpression == null){
            return;
        }
        
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null){
            return;
        }
        
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        if (project == null){
            return;
        }
        
        NavigationData navigationData = getNavigationData(referenceExpression, project);
        if (navigationData == null){
            gotoKotlinStdlib(referenceExpression, project);
            return;
        }
        
        navigationCache = NavigationUtil.gotoElement(navigationData.getSourceElement(), navigationData.getDeclarationDescriptor(),
                referenceExpression, project, file);
    }
    
    private String getStdFuncFileName(DeclarationDescriptor desc) {
        String fileName = "";
        JvmPackagePartSource src = (JvmPackagePartSource) ((DeserializedCallableMemberDescriptor) desc).getContainerSource();
        JvmClassName facadeName = src.getFacadeClassName();
        if (facadeName != null) {
            fileName = facadeName.getInternalName();
        } else {
            fileName = src.getClassName().getInternalName();
        }
        if (fileName.endsWith("Kt")){
            fileName = fileName.substring(0, fileName.length()-2) + ".kt";
        }
        return fileName;
    }
    
    private void gotoKotlinStdlib(KtReferenceExpression referenceExpression, Project project) {
        BindingContext context = KotlinAnalyzer.analyzeFile(project, referenceExpression.getContainingKtFile()).
            getAnalysisResult().getBindingContext();
        List<KotlinReference> refs = ReferenceUtils.createReferences(referenceExpression);
        for (KotlinReference ref : refs){
            Collection<? extends DeclarationDescriptor> descriptors = ref.getTargetDescriptors(context);
            for (DeclarationDescriptor desc : descriptors) {
                String fileName = "";
                
                if (desc instanceof DeserializedCallableMemberDescriptor) {
                    if (((DeserializedCallableMemberDescriptor) desc).getContainerSource() == null) {
                        continue;
                    }
                    fileName = getStdFuncFileName(desc);
                } 
                
                VirtualFile virtFile = KotlinEnvironment.getEnvironment(project).
                    getVirtualFileInJar(ProjectUtils.buildLibPath("kotlin-runtime-sources"), fileName);
            
                if (NavigationUtil.gotoKotlinStdlib(virtFile, desc)) {
                    return;
                }
            }
        }
    }
    
    @Nullable
    private NavigationData getNavigationData(KtReferenceExpression referenceExpression,
            Project project) {
        BindingContext context = KotlinAnalyzer.analyzeFile(project, referenceExpression.getContainingKtFile()).
                getAnalysisResult().getBindingContext();
        List<KotlinReference> refs = ReferenceUtils.createReferences(referenceExpression);
        
        for (KotlinReference ref : refs){
            Collection<? extends DeclarationDescriptor> descriptors = ref.getTargetDescriptors(context);
            for (DeclarationDescriptor descriptor : descriptors){
                SourceElement elementWithSource = NavigationUtil.getElementWithSource(descriptor, project);
                if (elementWithSource != null){
                    return new NavigationData(elementWithSource, descriptor);
                }
            }
        }
        
        return null;
        
    }
    
    private class NavigationData {
        
        private final SourceElement sourceElement;
        private final DeclarationDescriptor descriptor;
        
        public NavigationData(SourceElement sourceElement, DeclarationDescriptor descriptor){
            this.sourceElement = sourceElement;
            this.descriptor = descriptor;
        }
        
        public SourceElement getSourceElement(){
            return sourceElement;
        }
        
        public DeclarationDescriptor getDeclarationDescriptor(){
            return descriptor;
        }
    
    }
    
    public Pair<Document, Integer> getNavigationCache() {
        return navigationCache;
    }
    
}
