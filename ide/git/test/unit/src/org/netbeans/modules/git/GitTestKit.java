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

package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.EnumSet;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;

/**
 *
 * @author Tomas Stupka
 */
public class GitTestKit {
    
    public static FileInformation createFileInformation(FileInformation.Status status) {
        return createFileInformation(EnumSet.of(status));
    }
    
    public static FileInformation createFileInformation(EnumSet<FileInformation.Status> status) {
        return new FileInformation(status, false);
    }
    
    public static GitLocalFileNode createFileNode(File root, String file, FileInformation.Status status, boolean excluded) throws IOException {
        return new TestNode(root, file, status, excluded);
    }
    
    public static GitLocalFileNode createFileNode(File root, String file, FileInformation.Status status) throws IOException {
        return new TestNode(root, file, status);
    }
    
    public static GitLocalFileNode createFileNode(File root, String file, EnumSet<FileInformation.Status> status) throws IOException {
        return new TestNode(root, file, status);
    }
    
    public static GitUser createGitUser () throws Exception {
        Constructor<GitUser> cnst = GitUser.class.getConstructor(String.class, String.class);
        cnst.setAccessible(true);
        return cnst.newInstance("Test User", "test@user.org");
    }
    
    private static class TestNode extends GitFileNode.GitLocalFileNode {
        private FileInformation info;
        
        public TestNode(File root, String file, FileInformation.Status status, boolean excluded) throws IOException {
            this(root, file, status);
            if(excluded) {
                GitModuleConfig.getDefault().addExclusionPaths(Arrays.asList(new String[] {getFile().getAbsolutePath()}));
            }
        }
        
        public TestNode(File root, String file, FileInformation.Status status) throws IOException {
            super(root, new File(root, file));
            info = createFileInformation(status);
        }
        
        public TestNode(File root, String file, EnumSet<FileInformation.Status> status) throws IOException {
            super(root, new File(root, file));
            info = createFileInformation(status);
        }

        @Override
        public FileInformation getInformation() {
            return info;
        }        
    }    
}
