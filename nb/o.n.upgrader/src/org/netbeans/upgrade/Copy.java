/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.upgrade;

import java.io.IOException;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/** Does copy of objects on filesystems.
 *
 * @author Jaroslav Tulach
 */
final class Copy extends Object {
     private final FileObject sourceRoot;
     private final FileObject targetRoot;
     private final Set<String> thoseToCopy;
     private final PathTransformation transformation;

     private Copy(FileObject source, FileObject target, Set<String> thoseToCopy, PathTransformation transformation) {
         this.sourceRoot = source;
         this.targetRoot = target;
         this.thoseToCopy = thoseToCopy;
         this.transformation = transformation;
     }
     
    /** Does a selective copy of one source tree to another.
     * @param source file object to copy from
     * @param target file object to copy to
     * @param thoseToCopy set on which contains (relativeNameOfAFileToCopy)
     *   is being called to find out whether to copy or not
     * @throws IOException if coping fails
     */
    public static void copyDeep(FileObject source, FileObject target, Set<String> thoseToCopy) throws IOException {
        copyDeep(source, target, thoseToCopy, null);
    }
    
    public static void copyDeep(FileObject source, FileObject target, Set<String> thoseToCopy, PathTransformation transformation) throws IOException {
        Copy instance = new Copy(source, target, thoseToCopy, transformation);
        instance.copyFolder(instance.sourceRoot);
    }
    
    
    private void copyFolder(FileObject sourceFolder) throws IOException {        
        FileObject[] srcChildren = sourceFolder.getChildren();        
        for (int i = 0; i < srcChildren.length; i++) {
            FileObject child = srcChildren[i];
            if (child.isFolder()) {
                copyFolder (child);
                // make sure 'include xyz/.*' copies xyz folder's attributes
                if ((thoseToCopy.contains(child.getPath()) || thoseToCopy.contains(child.getPath() + "/")) //NOI18N
                        && child.getAttributes().hasMoreElements()) {
                    copyFolderAttributes(child);
                }
            } else {                
                if (thoseToCopy.contains(child.getPath())) {
                    copyFile(child);                    
                }                
            }
        }
    }
    
    private void copyFolderAttributes(FileObject sourceFolder) throws IOException {
        FileObject targetFolder = FileUtil.createFolder(targetRoot, sourceFolder.getPath());
        if (sourceFolder.getAttributes().hasMoreElements()) {
            FileUtil.copyAttributes(sourceFolder, targetFolder);
        }
    }    
    
    private void copyFile(FileObject sourceFile) throws IOException {        
        String targetPath = (transformation != null) ? transformation.transformPath(sourceFile.getPath()) : sourceFile.getPath();
        boolean isTransformed = !targetPath.equals(sourceFile.getPath());
        FileObject tg = targetRoot.getFileObject(targetPath);
        try {
            if (tg == null) {
                // copy the file otherwise keep old content
                FileObject targetFolder;
                String name;
                String ext;
                if (isTransformed) {
                    FileObject targetFile = FileUtil.createData(targetRoot, targetPath);                
                    targetFolder = targetFile.getParent();
                    name = targetFile.getName();
                    ext = targetFile.getExt();                                        
                    targetFile.delete();                    
                } else {
                    targetFolder = FileUtil.createFolder(targetRoot, sourceFile.getParent().getPath());
                    name = sourceFile.getName();
                    ext = sourceFile.getExt();                    
                }                
                tg = FileUtil.copyFile(sourceFile, targetFolder, name, ext);
            }
        } catch (IOException ex) {
            if (sourceFile.getNameExt().endsWith("_hidden")) {
                return;
            }
            throw ex;
        }
        FileUtil.copyAttributes(sourceFile, tg);        
    }

}
