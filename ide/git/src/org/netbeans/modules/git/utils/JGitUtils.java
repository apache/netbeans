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

package org.netbeans.modules.git.utils;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.RepositoryInfo.PushMode;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondra
 */
public class JGitUtils {

    public static RepositoryInfo.PushMode getPushMode (File root) {
        Repository repository = getRepository(root);
        if (repository != null) {
            try {
                String val = repository.getConfig().getString("push", null, "default"); //NOI18N
                if ("upstream".equals(val)) { //NOI18N
                    return PushMode.UPSTREAM;
                }
            } finally {
                repository.close();
            }
        }
        return PushMode.ASK;
    }

    public static boolean isValidRefName (String refName) {
        return Repository.isValidRefName(refName);
    }
    
    public static String normalizeBranchName (String refName) {
        return Repository.normalizeBranchName(refName);
    }
    
    public static boolean isUserSetup (File root) {
        Repository repository = getRepository(root);
        boolean userExists = true;
        if (repository != null) {
            try {
                StoredConfig config = repository.getConfig();
                String name = config.getString("user", null, "name"); //NOI18N
                String email = config.getString("user", null, "email"); //NOI18N
                if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
                    userExists = false;
                }
            } finally {
                repository.close();
            }
        }
        return userExists;
    }

    public static void persistUser (File root, GitUser author) throws GitException {
        Repository repository = getRepository(root);
        if (repository != null) {
            try {
                StoredConfig config = repository.getConfig();
                config.setString("user", null, "name", author.getName()); //NOI18N
                config.setString("user", null, "email", author.getEmailAddress()); //NOI18N
                try {
                    config.save();
                    FileUtil.refreshFor(new File(GitUtils.getGitFolderForRoot(root), "config"));
                } catch (IOException ex) {
                    throw new GitException(ex);
                }
            } finally {
                repository.close();
            }
        }
    }

    private static Repository getRepository (File root) {
        try {
            return new FileRepositoryBuilder().setWorkTree(root).setup().build();
        } catch (IOException ex) {
            return null;
        }
    }
    
    private JGitUtils () {
        
    }
    
}
