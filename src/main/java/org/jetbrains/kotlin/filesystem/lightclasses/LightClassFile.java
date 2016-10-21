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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class LightClassFile {
    private final FileObject fileObject;
    
    public LightClassFile(Project project, String path){
        FileObject f = KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getFileObject(path);
        this.fileObject = createIfNotExists(f, project, path);
    }
    
    public LightClassFile(FileObject fo){
        fileObject = fo;
        
    }
    
    public boolean exists(){
        return FileUtil.toFile(fileObject).exists();
    }

    public FileObject createIfNotExists(FileObject file, Project project, String path) {
        if (file != null){
            return file;
        } 
        
        String[] pathParts = path.split(Pattern.quote(ProjectUtils.FILE_SEPARATOR));
        
        if (pathParts.length == 1){
            pathParts = path.split("/");
        }
        
        StringBuilder packages = new StringBuilder();
        for (int i = 0; i < pathParts.length-1;i++){
            packages.append(pathParts[i]).append(ProjectUtils.FILE_SEPARATOR);
        }
        
        File f = new File(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath() + ProjectUtils.FILE_SEPARATOR + packages.toString());
        if (!f.exists()){
            f.mkdirs();
        }
        
        f = new File(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath() + ProjectUtils.FILE_SEPARATOR + path);  

        try {
            if (f.exists()){
                f.delete();
            }
            f.createNewFile();
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        
        return FileUtil.toFileObject(f);
    }
    
    public void refreshFile(){
        fileObject.refresh();
    }
    
    @NotNull
    public File asFile(){
        return FileUtil.toFile(fileObject);
    }
    
    @NotNull
    public FileObject getResource(){
        return fileObject;
    }
    
}
