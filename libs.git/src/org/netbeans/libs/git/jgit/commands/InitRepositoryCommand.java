/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class InitRepositoryCommand extends GitCommand {
    private final File workDir;
    private final ProgressMonitor monitor;

    public InitRepositoryCommand (Repository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.workDir = getRepository().getWorkTree();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean repositoryExists = getRepository().getDirectory().exists();
        if (repositoryExists) {
            String message = MessageFormat.format(Utils.getBundle(InitRepositoryCommand.class).getString("MSG_Error_RepositoryExists"), //NOI18N
                    getRepository().getWorkTree(), getRepository().getDirectory());
            monitor.preparationsFailed(message);
            throw new GitException(message);
        }
        return true;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            if (!(workDir.exists() || workDir.mkdirs())) {
                throw new GitException(MessageFormat.format(Utils.getBundle(InitRepositoryCommand.class).getString("MSG_Exception_CannotCreateFolder"), workDir.getAbsolutePath())); //NOI18N
            }
            repository.create();
            StoredConfig cfg = repository.getConfig();
            cfg.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_BARE, false);
            cfg.unset(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF);
            cfg.save();
        } catch (IllegalStateException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git init ").append(workDir.getAbsolutePath()).toString(); //NOI18N
    }
}
