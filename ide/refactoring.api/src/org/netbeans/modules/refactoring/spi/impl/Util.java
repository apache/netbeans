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

package org.netbeans.modules.refactoring.spi.impl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Jan Becicka
 */

public class Util {

    //copy - paste programming
    //http://ant.netbeans.org/source/browse/ant/src-bridge/org/apache/tools/ant/module/bridge/impl/BridgeImpl.java.diff?r1=1.15&r2=1.16
    //http:/java.netbeans.org/source/browse/java/javacore/src/org/netbeans/modules/javacore/Util.java    
    //http://core.netbeans.org/source/browse/core/ui/src/org/netbeans/core/ui/MenuWarmUpTask.java
    //http://core.netbeans.org/source/browse/core/src/org/netbeans/core/actions/RefreshAllFilesystemsAction.java
    //http://java.netbeans.org/source/browse/java/api/src/org/netbeans/api/java/classpath/ClassPath.java
        
    private static FileSystem[] fileSystems;

    private static FileSystem[] getFileSystems() {
        if (fileSystems != null) {
            return fileSystems;
        }
        File[] roots = File.listRoots();
        Set allRoots = new LinkedHashSet();
        assert roots != null && roots.length > 0 : "Could not list file roots"; // NOI18N
        
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject random = FileUtil.toFileObject(root);
            if (random == null) continue;
            
            FileSystem fs;
            try {
                fs = random.getFileSystem();
                allRoots.add(fs);
            } catch (FileStateInvalidException e) {
                throw new AssertionError(e);
            }
        }
        FileSystem[] retVal = new FileSystem [allRoots.size()];
        allRoots.toArray(retVal);
        assert retVal.length > 0 : "Could not get any filesystem"; // NOI18N
        
        return fileSystems = retVal;
    }
    
    /**
     * Adds given listener to all FileSystems
     * @param l listener to add
     */    
    public static void addFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].addFileChangeListener(l);
        }
    }
    
    /**
     * Removes given listener to all FileSystems
     * @param l listener to remove
     */    
    public static void removeFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].removeFileChangeListener(l);
        }
    }
}
