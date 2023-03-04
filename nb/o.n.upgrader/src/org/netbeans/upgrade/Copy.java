/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.upgrade;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openide.filesystems.*;

/** Does copy of objects on filesystems.
 *
 * @author Jaroslav Tulach
 */
final class Copy extends Object {
     private FileObject sourceRoot;
     private FileObject targetRoot;
     private Set thoseToCopy;
     private PathTransformation transformation;

     private Copy(FileObject source, FileObject target, Set thoseToCopy, PathTransformation transformation) {
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
    public static void copyDeep (FileObject source, FileObject target, Set thoseToCopy) 
    throws IOException {
        copyDeep(source, target, thoseToCopy, null);
    }
    
    public static void copyDeep (FileObject source, FileObject target, Set thoseToCopy, PathTransformation transformation) 
    throws IOException {
        Copy instance = new Copy(source, target, thoseToCopy, transformation);
        instance.copyFolder (instance.sourceRoot);
    }
    
    
    private void copyFolder (FileObject sourceFolder) throws IOException {        
        FileObject[] srcChildren = sourceFolder.getChildren();        
        for (int i = 0; i < srcChildren.length; i++) {
            FileObject child = srcChildren[i];
            if (child.isFolder()) {
                copyFolder (child);
                // make sure 'include xyz/.*' copies xyz folder's attributes
                if ((thoseToCopy.contains (child.getPath()) || thoseToCopy.contains (child.getPath() + "/")) && //NOI18N
                    child.getAttributes().hasMoreElements()
                ) {
                    copyFolderAttributes(child);
                }
            } else {                
                if (thoseToCopy.contains (child.getPath())) {
                    copyFile(child);                    
                }                
            }
        }
    }
    
    private void copyFolderAttributes(FileObject sourceFolder) throws IOException {
        FileObject targetFolder = FileUtil.createFolder (targetRoot, sourceFolder.getPath());
        if (sourceFolder.getAttributes ().hasMoreElements ()) {
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
                FileObject targetFolder = null;
                String name = null, ext = null;
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
    
    public static void appendSelectedLines(File sourceFile, File targetFolder, String[] regexForSelection)
    throws IOException {        
        if (!sourceFile.exists()) {
            return;
        }
        Pattern[] linePattern = new Pattern[regexForSelection.length];
        for (int i = 0; i < linePattern.length; i++) {
            linePattern[i] = Pattern.compile(regexForSelection[i]);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();        
        File targetFile = new File(targetFolder,sourceFile.getName());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        assert targetFolder.exists();
        
        if (!targetFile.exists()) {
            targetFile.createNewFile();
        } else {
            //read original content into  ByteArrayOutputStream
            FileInputStream targetIS = new FileInputStream(targetFile);
            try {
                FileUtil.copy(targetIS, bos);
            } finally {
                targetIS.close();
            }            
        }
        assert targetFile.exists();

        
        //append lines into ByteArrayOutputStream
        String line = null;        
        BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));
        try {
            while ((line = sourceReader.readLine()) != null) {
                if (linePattern != null) {
                    for (int i = 0; i < linePattern.length; i++) {
                        Matcher m = linePattern[i].matcher(line);
                        if (m.matches()) {
                            bos.write(line.getBytes());
                            bos.write('\n');
                            break;
                        }                        
                    }                    
                } else {
                    bos.write(line.getBytes());
                    bos.write('\n');
                }
            }
        } finally {
            sourceReader.close();
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
        FileOutputStream targetOS = new FileOutputStream(targetFile);
        try {
            FileUtil.copy(bin, targetOS);        
        } finally {
            bin.close();
            targetOS.close();
        }
    }
}
