package org.black.kotlin.filesystem.lightclasses;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.black.kotlin.filesystem.KotlinLightClassManager;
import org.black.kotlin.model.KotlinEnvironment;
//import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.codegen.CompilationErrorHandler;
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade;
import org.jetbrains.kotlin.codegen.binding.PsiCodegenPredictor;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtScript;
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
                String internalName = PsiCodegenPredictor.getPredefinedJvmInternalName(classOrObject, NoResolveFileClassesProvider.INSTANCE);
                if (internalName == null) {
                    return false;
                }
                internalName = internalName.replace('/', '.').replace('\\', '.').
                        replace(ProjectUtils.FILE_SEPARATOR, ".");
                FqName fqName = new FqName(internalName);
                return checkByInternalName(fqName, requestedClassName);
            }

            @Override
            public boolean shouldGeneratePackagePart(KtFile ktFile) {
                FqName internalName
                        = NoResolveFileClassesProvider.INSTANCE.getFileClassInfo(ktFile).
                        getFileClassFqName();
                return checkByInternalName(internalName, requestedClassName);
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

        KotlinCodegenFacade.compileCorrectFiles(state, new CompilationErrorHandler() {
            @Override
            public void reportException(Throwable thrwbl, String string) {
            }

        });

        return state;
    }

    private boolean checkByInternalName(FqName internalName, String requestedClassFileName) {
        if (internalName.toString() == null) {
            return false;
        }

        List<Name> pathSegments = internalName.pathSegments();
        String classFileName = pathSegments.get(pathSegments.size() - 1).asString();
        String requestedInternalName = requestedClassFileName.
                substring(0, requestedClassFileName.length() - ".class".length());

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

    public void generate(FileObject file) {
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);

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
                for (int i = 0; i < state.getFactory().asList().size(); i++) {
                    
                    byte[] lightClassText = state.getFactory().asList().get(i).asByteArray();
                    
                    if (lightClass.getAbsolutePath().replace('\\', '/').contains(
                            state.getFactory().asList().get(i).getRelativePath())) {
                        try {
                            OutputStream stream = new BufferedOutputStream(new FileOutputStream(lightClass));
                            stream.write(lightClassText);
                            stream.flush();
                            stream.close();
                        } catch (FileNotFoundException ex) {

                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

}
