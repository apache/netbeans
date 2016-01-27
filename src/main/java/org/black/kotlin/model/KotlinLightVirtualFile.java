package org.black.kotlin.model;

import com.intellij.testFramework.LightVirtualFile;
import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;

/**
 *
 * @author Александр
 */
public class KotlinLightVirtualFile extends LightVirtualFile {
   
    private final String path;
    
    public KotlinLightVirtualFile(File file, String text){
        super(file.getName(),KotlinLanguage.INSTANCE, text);
        path = file.getPath();
    }
    
    @Override
    @NotNull
    public String getPath(){
        return path;
    }
}
