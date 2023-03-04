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

package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.openide.filesystems.*;

/** Copy of TestUtilHid from filesystems tests.
 * @author  rm111737
 */
public class TestUtilHid {

    public static FileSystem createLocalFileSystem(File workDir, String[] resources) throws IOException {
        workDir.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (workDir,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException iex) {
                    throw new IOException ("While creating " + resources[i] + " in " + workDir.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath() + " with resource list: " + Arrays.asList(resources));
                }
            }
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(workDir);
        } catch (Exception ex) {}
        
        return lfs;
    }

    public static final  void destroyLocalFileSystem (String testName) throws IOException {            
    }

    static class StatusFileSystem extends LocalFileSystem {
        StatusDecorator status = new StatusDecorator () {
            public String annotateName (String name, java.util.Set files) {
                return name;
            }

            @Override
            public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                return null;
            }
        };        
        
        @Override
        public StatusDecorator getDecorator() {
            return status;
        }
        
    }
}
