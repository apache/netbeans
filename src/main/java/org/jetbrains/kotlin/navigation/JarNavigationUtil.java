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
package org.jetbrains.kotlin.navigation;

import java.io.File;
import kotlin.Pair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Александр
 */
public class JarNavigationUtil {
    public static FileObject getFileObjectFromJar(String path){
        Pair<String, String> pathParts = getJarAndInternalPaths(path);
        if (pathParts == null){
            return null;
        }
        
        File jar = new File(pathParts.getFirst());
        jar = jar.getAbsoluteFile();
        
        FileObject fob = FileUtil.toFileObject(jar);
        fob = FileUtil.getArchiveRoot(fob);
        
        String[] internalPathParts = pathParts.getSecond().split("/");
        for (String pathPart : internalPathParts){
            fob = fob.getFileObject(pathPart);
        }
        return fob;
    }
    
    private static Pair<String,String> getJarAndInternalPaths(String path){
        String separator = "!/";
        String[] pathParts = path.split(separator);
        if (pathParts.length == 1){
            return null;
        }
        
        return new Pair<String, String>(pathParts[0], pathParts[1]);
    }
    
}
