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
package org.black.kotlin.model;

import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Александр
 */
public class KotlinLightVirtualFile extends LightVirtualFile {
   
    private final String path;
    
    public KotlinLightVirtualFile(FileObject file, String text){
        super(file.getName(),KotlinLanguage.INSTANCE, text);
        path = file.getPath();
    }
    
    @Override
    @NotNull
    public String getPath(){
        return path;
    }
}
