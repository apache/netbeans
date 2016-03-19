package org.black.kotlin.builder;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.model.KotlinLightVirtualFile;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.project.KotlinProjectConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

public class KotlinPsiManager {
    
    public static final KotlinPsiManager INSTANCE = new KotlinPsiManager();
    
    private KotlinPsiManager(){}
    
    @NotNull
    public Set<FileObject> getFilesByProject(KotlinProject project){
        Set<FileObject> ktFiles = Sets.newLinkedHashSet();
        
        for (SourceGroup srcGroup : project.getKotlinSources().
                getSourceGroups(KotlinProjectConstants.KOTLIN_SOURCE.toString())){
            for (FileObject file : srcGroup.getRootFolder().getChildren()){
                if (isKotlinFile(file)){
                    ktFiles.add(file);
                }
            }
        }
        
        return ktFiles;
    }
    
        /**
     * This method parses the input file. 
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return the result of {@link #parseText(java.lang.String, java.io.File) parseText} method
     * @throws IOException 
     */
    @Nullable
    public KtFile parseFile(@NotNull File file) throws IOException {
        return parseText(FileUtil.loadFile(file, null, true), file);
    }

    /**
     * This method parses text from the input file.
     * @param text Text of temporary file.
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return {@link KtFile}
     */
    @Nullable
    public KtFile parseText(@NotNull String text, @NotNull File file) {
        StringUtil.assertValidSeparators(text);

        com.intellij.openapi.project.Project project = KotlinEnvironment.getEnvironment(
                OpenProjects.getDefault().getOpenProjects()[0]).getProject();

        LightVirtualFile virtualFile = new KotlinLightVirtualFile(file, text);
        virtualFile.setCharset(CharsetToolkit.UTF8_CHARSET);

        PsiFileFactoryImpl psiFileFactory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);
        
        return (KtFile) psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false);
    }
    
    @Nullable
    public KtFile getParsedKtFile(@NotNull String text){
        StringUtil.assertValidSeparators(text);

        com.intellij.openapi.project.Project project = KotlinEnvironment.getEnvironment(
                OpenProjects.getDefault().getOpenProjects()[0]).getProject();

        PsiFileFactoryImpl psiFileFactory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);
        
        return (KtFile) psiFileFactory.createFileFromText(KotlinLanguage.INSTANCE, text);
    }
    
    public boolean isKotlinFile(@NotNull FileObject file){
        return KotlinFileType.INSTANCE.getDefaultExtension().equals(file.getExt());
    }
    
}
