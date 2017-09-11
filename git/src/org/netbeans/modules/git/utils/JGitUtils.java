/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
