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

package org.netbeans.libs.git.jgit;

import org.netbeans.libs.git.GitException;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author ondra
 */
public final class JGitRepository {
    private Repository repository;
    private final File location;

    public JGitRepository (File location) {
        this.location = location;
    }

    public synchronized void increaseClientUsage () throws GitException {
        if (repository == null) {
            repository = getRepository(location);
        } else {
            repository.incrementOpen();
        }
    }

    public synchronized void decreaseClientUsage () {
        repository.close();
    }

    private Repository getRepository (File workDir) throws GitException {
        try {
            return Utils.getRepositoryForWorkingDir(workDir);
        } catch (IOException ex) {
            throw new GitException(ex);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().matches("Repository config file (.*) invalid (.*)")) { //NOI18N
                throw new GitException("It seems the config file for the repository at [" + workDir.getAbsolutePath() + "] is corrupted.\nEnsure it ends with empty line.", ex); //NOI18N
            } else {
                throw new GitException(ex);
            }
        }
    }

    public Repository getRepository () {
        assert repository != null;
        return repository;
    }
}
