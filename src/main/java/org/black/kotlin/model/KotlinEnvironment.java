package org.black.kotlin.model;

import java.io.File;
import java.util.Collections;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiManager;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.impl.PsiTreeChangePreprocessor;
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy;
import com.intellij.psi.impl.file.impl.JavaFileManager;
import java.util.List;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.idea.KotlinFileType;

/**
 * This class creates Kotlin environment for Kotlin project.
 * @author Александр
 */
@SuppressWarnings("deprecation")
public class KotlinEnvironment {
    
    public final static String KT_JDK_ANNOTATIONS_PATH = ProjectUtils.buildLibPath("kotlin-jdk-annotations");
    public final static String KOTLIN_COMPILER_PATH = ProjectUtils.buildLibPath("kotlin-compiler");
    
    private static final Map<org.netbeans.api.project.Project, KotlinEnvironment> cachedEnvironment = new HashMap();
    private static final Object environmentLock = new Object();
    
    private final JavaCoreApplicationEnvironment applicationEnvironment;
    private final JavaCoreProjectEnvironment projectEnvironment;
    private final MockProject project;
    private final org.netbeans.api.project.Project kotlinProject;
    private final Set<VirtualFile> roots = new LinkedHashSet();
    
    private KotlinEnvironment(@NotNull org.netbeans.api.project.Project kotlinProject, @NotNull Disposable disposable) {
        this.kotlinProject = kotlinProject;
        
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
        
        cachedEnvironment.put(kotlinProject, this);
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
        synchronized (environmentLock) {
            if (!cachedEnvironment.containsKey(kotlinProject)) {
                cachedEnvironment.put(kotlinProject, new KotlinEnvironment(kotlinProject, Disposer.newDisposable()));
            }
            
            return cachedEnvironment.get(kotlinProject);
        }
    }
    
    public static void updateKotlinEnvironment(@NotNull org.netbeans.api.project.Project kotlinProject) {
        synchronized (environmentLock) {
            if (cachedEnvironment.containsKey(kotlinProject)) {
                KotlinEnvironment environment = cachedEnvironment.get(kotlinProject);
                Disposer.dispose(environment.getJavaApplicationEnvironment().getParentDisposable());
            }
            cachedEnvironment.put(kotlinProject, new KotlinEnvironment(kotlinProject, Disposer.newDisposable()));
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
    
    @NotNull
    public JavaCoreApplicationEnvironment getJavaApplicationEnvironment() {
        return applicationEnvironment;
    }
    
    @Nullable
    public VirtualFile getVirtualFile(@NotNull String location) {
        return applicationEnvironment.getLocalFileSystem().findFileByIoFile(new File(location));
    }
    
    public VirtualFile getVirtualFileInJar(@NotNull String pathToJar, @NotNull String relativePath) {
        return applicationEnvironment.getJarFileSystem().findFileByPath(pathToJar + "!/" + relativePath);
    }
    
    public boolean isJarFile(@NotNull String pathToJar) {
        VirtualFile jarFile = applicationEnvironment.getJarFileSystem().findFileByPath(pathToJar + "!/");
        return jarFile != null && jarFile.isValid();
    }
    
    public Set<VirtualFile> getRoots() {
        return Collections.unmodifiableSet(roots);
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
}