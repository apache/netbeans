package org.black.kotlin.model;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.asJava.KtLightClassForFacade;
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension;
import org.jetbrains.kotlin.extensions.ExternalDeclarationsProvider;
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.kotlin.resolve.CodeAnalyzerInitializer;

import com.intellij.codeInsight.ContainerProvider;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.codeInsight.runner.JavaMainMethodProvider;
import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.core.CoreJavaFileManager;
import com.intellij.core.JavaCoreApplicationEnvironment;
import com.intellij.core.JavaCoreProjectEnvironment;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessor;
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy;
import com.intellij.psi.impl.file.impl.JavaFileManager;
import com.intellij.testFramework.LightVirtualFile;
import java.io.IOException;
import java.util.List;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.ui.OpenProjects;

/**
 * This class creates Kotlin environment for Kotlin project.
 * @author Александр
 */
@SuppressWarnings("deprecation")
public class KotlinEnvironment {

    public final static String KOTLIN_COMPILER_PATH = ProjectUtils.buildLibPath("kotlin-compiler");
    
    private static final Map<org.netbeans.api.project.Project, KotlinEnvironment> CACHED_ENVIRONMENT =
            new HashMap<org.netbeans.api.project.Project, KotlinEnvironment>();
    private static final Object ENVIRONMENT_LOCK = new Object();
    
    private final JavaCoreApplicationEnvironment applicationEnvironment;
    private final JavaCoreProjectEnvironment projectEnvironment;
    private final MockProject project;
    private final Set<VirtualFile> roots = new LinkedHashSet<VirtualFile>();
    
    private KotlinEnvironment(@NotNull org.netbeans.api.project.Project kotlinProject, @NotNull Disposable disposable) {

        applicationEnvironment = createJavaCoreApplicationEnvironment(disposable);
        
        projectEnvironment = new JavaCoreProjectEnvironment(disposable, applicationEnvironment) {
            @Override
            protected void preregisterServices() {
                registerProjectExtensionPoints(Extensions.getArea(getProject()));
            }
        };
        
        project = projectEnvironment.getProject();

//        For j2k converter
        project.registerService(NullableNotNullManager.class, new KotlinNullableNotNullManager(kotlinProject)); 
        
        PsiManager psiManager = project.getComponent(PsiManager.class);
        assert (psiManager != null);
        project.registerService(CoreJavaFileManager.class,
                (CoreJavaFileManager) ServiceManager.getService(project, JavaFileManager.class));
        
        CliLightClassGenerationSupport cliLightClassGenerationSupport = new CliLightClassGenerationSupport(project);
        project.registerService(LightClassGenerationSupport.class, cliLightClassGenerationSupport);
        project.registerService(CliLightClassGenerationSupport.class, cliLightClassGenerationSupport);
        project.registerService(KtLightClassForFacade.FacadeStubCache.class, new KtLightClassForFacade.FacadeStubCache(project));
        project.registerService(CodeAnalyzerInitializer.class, cliLightClassGenerationSupport);
        

        configureClasspath();
        
        //project.registerService(JvmVirtualFileFinderFactory.class, new EclipseVirtualFileFinder(javaProject));
        
        ExternalDeclarationsProvider.Companion.registerExtensionPoint(project);
        ExpressionCodegenExtension.Companion.registerExtensionPoint(project);
        
        for (String config : EnvironmentConfigFiles.JVM_CONFIG_FILES) {
            registerApplicationExtensionPointsAndExtensionsFrom(config);
        }
        
        CACHED_ENVIRONMENT.put(kotlinProject, this);
    }
    
    private static void registerProjectExtensionPoints(ExtensionsArea area) {
        CoreApplicationEnvironment.registerExtensionPoint(area, PsiTreeChangePreprocessor.EP_NAME, PsiTreeChangePreprocessor.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, PsiElementFinder.EP_NAME, PsiElementFinder.class);
    }
    
    private static void registerApplicationExtensionPointsAndExtensionsFrom(String configFilePath) {
        File pluginRoot = new File(KOTLIN_COMPILER_PATH);
        CoreApplicationEnvironment.registerExtensionPointAndExtensions(pluginRoot, configFilePath, Extensions.getRootArea());
    }
    
    @NotNull
    public static KotlinEnvironment getEnvironment(@NotNull org.netbeans.api.project.Project kotlinProject) {
        synchronized (ENVIRONMENT_LOCK) {
            if (!CACHED_ENVIRONMENT.containsKey(kotlinProject)) {
                CACHED_ENVIRONMENT.put(kotlinProject, new KotlinEnvironment(kotlinProject, Disposer.newDisposable()));
            }
            
            return CACHED_ENVIRONMENT.get(kotlinProject);
        }
    }

    private void configureClasspath() {
        List<String> classpath = ProjectUtils.getClasspath();
        
            for (String s : classpath) {
                File file = new File(s);
                
                addToClasspath(file);
            }
    }
    
    private JavaCoreApplicationEnvironment createJavaCoreApplicationEnvironment(@NotNull Disposable disposable) {
        Extensions.cleanRootArea(disposable);
        registerAppExtensionPoints();
        JavaCoreApplicationEnvironment javaApplicationEnvironment = new JavaCoreApplicationEnvironment(disposable);
        
        // ability to get text from annotations xml files
        javaApplicationEnvironment.registerFileType(PlainTextFileType.INSTANCE, "xml");
        
        javaApplicationEnvironment.registerFileType(KotlinFileType.INSTANCE, "kt");
        javaApplicationEnvironment.registerFileType(KotlinFileType.INSTANCE, "jet");
        javaApplicationEnvironment.registerFileType(KotlinFileType.INSTANCE, "ktm");
        
        javaApplicationEnvironment.registerParserDefinition(new KotlinParserDefinition());
        
        javaApplicationEnvironment.getApplication().registerService(KotlinBinaryClassCache.class,
                new KotlinBinaryClassCache());
        
        return javaApplicationEnvironment;
    }
    
    private static void registerAppExtensionPoints() {
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ContainerProvider.EP_NAME,
                ContainerProvider.class);
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ClsCustomNavigationPolicy.EP_NAME,
                ClsCustomNavigationPolicy.class);
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ClassFileDecompilers.EP_NAME,
                ClassFileDecompilers.Decompiler.class);
        
        // For j2k converter 
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), PsiAugmentProvider.EP_NAME, PsiAugmentProvider.class);
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), JavaMainMethodProvider.EP_NAME, JavaMainMethodProvider.class);
    }
    
    @NotNull
    public Project getProject() {
        return project;
    }


    private void addToClasspath(File path){
        if (path.isFile()) {
            VirtualFile jarFile = applicationEnvironment.getJarFileSystem().findFileByPath(path + "!/");
            if (jarFile == null) {
                return;
            }
            projectEnvironment.addJarToClassPath(path);
            roots.add(jarFile);
        } else {
            VirtualFile root = applicationEnvironment.getLocalFileSystem().findFileByPath(path.getAbsolutePath());
            if (root == null) {return;
            }
            projectEnvironment.addSourcesToClasspath(root);
            roots.add(root);
        }
    }
    
        /**
     * This method parses the input file. 
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return the result of {@link #parseText(java.lang.String, java.io.File) parseText} method
     * @throws IOException 
     */
    @Nullable
    public static KtFile parseFile(@NotNull File file) throws IOException {
        return parseText(FileUtil.loadFile(file, null, true), file);
    }

    /**
     * This method parses text from the input file.
     * @param text Text of temporary file.
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return {@link KtFile}
     */
    @Nullable
    public static KtFile parseText(@NotNull String text, @NotNull File file) {
        StringUtil.assertValidSeparators(text);

        Project project = KotlinEnvironment.getEnvironment(
                OpenProjects.getDefault().getOpenProjects()[0]).getProject();

        LightVirtualFile virtualFile = new KotlinLightVirtualFile(file, text);
        virtualFile.setCharset(CharsetToolkit.UTF8_CHARSET);

        PsiFileFactoryImpl psiFileFactory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);

        return (KtFile) psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false);
    }

}