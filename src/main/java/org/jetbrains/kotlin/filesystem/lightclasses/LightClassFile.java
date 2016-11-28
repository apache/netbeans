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
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class LightClassFile {
    private final FileObject fileObject;
    private final File file;
    
    public LightClassFile(Project project, String path){
        file = new File(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath() + ProjectUtils.FILE_SEPARATOR + path);
        fileObject = FileUtil.toFileObject(file);
    }
    
    public LightClassFile(FileObject fo){
        fileObject = fo;
        file = FileUtil.toFile(fo);
    }
    
    public boolean exists(){
        return FileUtil.toFile(fileObject).exists();
    }
    
    public void refreshFile(){
        fileObject.refresh();
    }
    
    @NotNull
    public File asFile(){
        return file;
    }
    
    @NotNull
    public FileObject getResource(){
        return fileObject;
    }
    
}
