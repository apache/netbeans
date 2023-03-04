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
package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=50)
public class FileHandlingFactory implements RefactoringPluginFactory {
   
    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        Collection<? extends FileObject> o = look.lookupAll(FileObject.class);
        NonRecursiveFolder folder = look.lookup(NonRecursiveFolder.class);
        if (refactoring instanceof RenameRefactoring) {
            if (!o.isEmpty()) {
                return new FileRenamePlugin((RenameRefactoring) refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            if (!o.isEmpty()) {
                return new FileMovePlugin((MoveRefactoring) refactoring);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            if (folder != null) {
                //Safe delete package
                return new PackageDeleteRefactoringPlugin((SafeDeleteRefactoring)refactoring);
            }
            if (! o.isEmpty()) {
                FileObject fObj = o.iterator().next();
                if (fObj.isFolder()) {
                    return new PackageDeleteRefactoringPlugin((SafeDeleteRefactoring)refactoring);
                } else {
                    return new FileDeletePlugin((SafeDeleteRefactoring) refactoring);
                }
            }
        } else if (refactoring instanceof SingleCopyRefactoring || refactoring instanceof CopyRefactoring) {
            if (!o.isEmpty()) {
                return new FilesCopyPlugin(refactoring);
            }
        }
        return null;
    }
    
        /**
     * creates or finds FileObject according to 
     * @param url
     * @return FileObject
     */
    static FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null)
                return result;
            File f = new File(url.toURI());
            
            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }

}
