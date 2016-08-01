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
package org.black.kotlin.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import java.util.Collection;
import java.util.List;
import org.black.kotlin.model.KotlinAnalysisFileCache;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.caches.resolve.KotlinCacheService;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.diagnostics.KotlinSuppressCache;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;
import org.netbeans.api.project.ui.OpenProjects;


/**
 *
 * @author Александр
 */
public class KotlinCacheServiceImpl implements KotlinCacheService {

    @Override
    public ResolutionFacade getResolutionFacade(List<? extends KtElement> list) {
        return new ResolutionFacade(){
            @Override
            public Project getProject() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public BindingContext analyze(KtElement element, BodyResolveMode bodyResolveMode) {
                KtFile ktFile = element.getContainingKtFile();
                org.netbeans.api.project.Project kotlinProject = null;
                for (org.netbeans.api.project.Project project : OpenProjects.getDefault().getOpenProjects()){
                    if (ktFile.getVirtualFile().getUrl().contains(
                            project.getProjectDirectory().toURL().toString())){
                        kotlinProject = project;
                        break;
                    }
                }
                if (kotlinProject == null){
                    return BindingContext.EMPTY;
                }
                
                return KotlinAnalysisFileCache.INSTANCE.getAnalysisResult(ktFile, kotlinProject).
                        getAnalysisResult().getBindingContext();
            }

            @Override
            public AnalysisResult analyzeFullyAndGetResult(Collection<? extends KtElement> clctn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public DeclarationDescriptor resolveToDescriptor(KtDeclaration kd) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ModuleDescriptor getModuleDescriptor() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T getFrontendService(Class<T> type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T getIdeService(Class<T> type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T getFrontendService(PsiElement pe, Class<T> type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T getFrontendService(ModuleDescriptor md, Class<T> type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
    }

    @Override
    public KotlinSuppressCache getSuppressionCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
