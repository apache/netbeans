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
package org.jetbrains.kotlin.filesystem.lightclasses;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.kotlin.filesystem.KotlinLightClassManager;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.codegen.CompilationErrorHandler;
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtScript;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class KotlinLightClassGeneration {

    public static KotlinLightClassGeneration INSTANCE = new KotlinLightClassGeneration();

    private KotlinLightClassGeneration() {
    }

    public void updateLightClasses(Project kotlinProject, Set<FileObject> affectedFiles) {
        KotlinLightClassManager.getInstance(kotlinProject).computeLightClassesSources();
        KotlinLightClassManager.getInstance(kotlinProject).updateLightClasses(affectedFiles);
    }

    public GenerationState buildLightClasses(AnalysisResult analysisResult, Project project,
            List<KtFile> ktFiles, final String requestedClassName) {

        GenerationState.GenerateClassFilter generateDeclaredClassFilter
                = new GenerationState.GenerateClassFilter() {
            @Override
            public boolean shouldAnnotateClass(KtClassOrObject classOrObject) {
                return true;
            }

            @Override
            public boolean shouldGenerateClass(KtClassOrObject classOrObject) {
//                String internalName = KotlinLightClassManager.getInternalName(classOrObject);
//                return checkByInternalName(internalName, requestedClassName);
                return true;
            }

            @Override
            public boolean shouldGeneratePackagePart(KtFile ktFile) {
//                String internalName = FileClasses.getFileClassInternalName(NoResolveFileClassesProvider.INSTANCE, ktFile);
//                return checkByInternalName(internalName, requestedClassName);
                return true;
            }

            @Override
            public boolean shouldGenerateScript(KtScript script) {
                return false;
            }
        };

        GenerationState state = new GenerationState(
                KotlinEnvironment.getEnvironment(project).getProject(),
                new LightClassBuilderFactory(),
                analysisResult.getModuleDescriptor(),
                analysisResult.getBindingContext(),
                ktFiles,
                true,
                true,
                generateDeclaredClassFilter
        );
        
        for (KtFile ktFile : ktFiles) {
            PackageFragmentDescriptor fragment = state.getBindingContext().get(BindingContext.FILE_TO_PACKAGE_FRAGMENT, ktFile);
            if (fragment == null) {
                return null;
            }
        }

        KotlinCodegenFacade.compileCorrectFiles(state, new CompilationErrorHandler() {
            @Override
            public void reportException(Throwable thrwbl, String string) {
            }

        });

        return state;
    }

    private boolean checkByInternalName(String internalName, String requestedClassFileName) {
        if (internalName == null) {
            return false;
        }
        String classFileName = getLastSegment(internalName);
        String requestedInternalName = requestedClassFileName.substring(0, requestedClassFileName.length() - ".class".length());
        
        if (requestedInternalName.startsWith(classFileName)) {
            if (requestedInternalName.length() == classFileName.length()) {
                return true;
            }
            
            if (requestedInternalName.charAt(classFileName.length()) == '$') {
                return true;
            }
        }
        
        return false;
    }
    
    private String getLastSegment(String path) {
        String[] parts = path.split("/");
        
        return parts[parts.length - 1];
    }

    public void generate(FileObject file, Project project) {
        if (project == null) {
            return;
        }
        
        KotlinLightClassManager manager = KotlinLightClassManager.getInstance(project);
        manager.computeLightClassesSources();
        List<String> lightClassesPaths = manager.getLightClassesPaths(file);

        for (String path : lightClassesPaths) {
            File lightClass = new File(ProjectUtils.getKotlinProjectLightClassesPath(project) + "/" + path);
            if (!lightClass.exists()){
                lightClass.mkdirs();
            }    
            
            List<KtFile> ktFiles = manager.getSourceFiles(lightClass);
            String[] pathParts = path.split("/");
            String className = pathParts[pathParts.length-1];
            if (!ktFiles.isEmpty()) {
                AnalysisResult analysisResult
                        = KotlinAnalyzer.analyzeFiles(project, ktFiles).getAnalysisResult();
                GenerationState state = KotlinLightClassGeneration.INSTANCE.
                        buildLightClasses(analysisResult, project, ktFiles, className);
                if (state == null) {
                    return;
                }
                for (int i = 0; i < state.getFactory().asList().size(); i++) {
                    
                    byte[] lightClassText = state.getFactory().asList().get(i).asByteArray();
                    
                    if (lightClass.getAbsolutePath().replace('\\', '/').contains(
                            state.getFactory().asList().get(i).getRelativePath())) {
                        OutputStream stream = null;
                        try {
                            stream = new BufferedOutputStream(new FileOutputStream(lightClass));
                            stream.write(lightClassText);
                            stream.flush();
                        } catch (FileNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void generate(FileObject file, Project project, AnalysisResult analysisResult) {
        if (project == null) {
            return;
        }
        
        KotlinLightClassManager manager = KotlinLightClassManager.getInstance(project);
        manager.computeLightClassesSources();
        List<String> lightClassesPaths = manager.getLightClassesPaths(file);

        for (String path : lightClassesPaths) {
            File lightClass = new File(ProjectUtils.getKotlinProjectLightClassesPath(project) + ProjectUtils.FILE_SEPARATOR + path);
            if (!lightClass.exists()){
                lightClass.mkdirs();
            }    
            
            List<KtFile> ktFiles = manager.getSourceFiles(lightClass);
            String[] pathParts = path.split(Pattern.quote(ProjectUtils.FILE_SEPARATOR));
            String className = pathParts[pathParts.length-1];
            if (!ktFiles.isEmpty()) {
                GenerationState state = KotlinLightClassGeneration.INSTANCE.
                        buildLightClasses(analysisResult, project, ktFiles, className);
                if (state == null) {
                    return;
                }
                for (int i = 0; i < state.getFactory().asList().size(); i++) {
                    
                    byte[] lightClassText = state.getFactory().asList().get(i).asByteArray();
                    
                    if (lightClass.getAbsolutePath().replace('\\', '/').contains(
                            state.getFactory().asList().get(i).getRelativePath())) {
                        OutputStream stream = null;
                        try {
                            stream = new BufferedOutputStream(new FileOutputStream(lightClass));
                            stream.write(lightClassText);
                            stream.flush();
                        } catch (FileNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
}
