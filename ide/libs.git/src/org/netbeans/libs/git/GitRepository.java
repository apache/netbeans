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

package org.netbeans.libs.git;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.merge.MergeConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.netbeans.libs.git.jgit.JGitRepository;
import org.netbeans.libs.git.jgit.JGitSshSessionFactory;

/**
 * A representation of a local Git repository, used to create git clients bound to the repository.
 * <p>Instances are bound to a local folder with actual Git repository. If the repository does not exist yet, you 
 * still have to provide a local file which indicates where the repository would be created when 
 * {@link GitClient#init(org.netbeans.libs.git.progress.ProgressMonitor) } was called.</p>
 * <p>To get an instance of <code>GitClient</code> to run git commands with, use {@link #createClient() } method. It <strong>always</strong> returns
 * a new instance of the <code>GitClient</code>, it is not shared among the callers.
 * <p>When done with the client - you finish calling all desired commands and do not
 * plan to use the client's instance any more - {@link GitClient#release() } must be called.
 * When all created clients are released this way, repository metadata are flushed,
 * all open metadata files are closed and thus do not block any external tools 
 * (such as a commandline client).</p>
 * <p>Internally the class keeps a map of its instances that are cached under
 * a weak reference to the instance of the local file passed in the {@link #getInstance(java.io.File) } method.
 * Along with the instance it caches also all repository metadata (branches, index, references etc.) 
 * needed to construct the client and operate with the actual Git repository.<br>
 * Every call to the <code>getInstance</code> method with the same instance of the file 
 * will always return the same instance of <code>GitRepository</code>. <strong>It is up to a caller's
 * responsibility</strong> to hold a strong reference to the file so a created client always works with 
 * the same instance of the git repository.</p>
 * 
 * @author Ondra Vrabec
 */
public final class GitRepository {

    private static final Map<File, GitRepository> repositoryPool = new WeakHashMap<File, GitRepository>(5);
    private final File repositoryLocation;
    private JGitRepository gitRepository;
    
    /**
     * Option specifying how to deal with merges and merge commits. Required by
     * {@link GitClient#merge(java.lang.String, org.netbeans.libs.git.GitRepository.FastForwardOption, org.netbeans.libs.git.progress.ProgressMonitor)}.
     * To get the repository's default value, get it with {@link #getDefaultFastForwardOption()}.
     *
     * @since 1.26
     */
    public enum FastForwardOption {

        /**
         * Merge will not create a new commit if possible and only update the
         * branch reference to the merged commit. This will usually happen if
         * the merged commit is a descendant of the branch's head commit.
         */
        FAST_FORWARD {

            @Override
            public String toString () {
                return "--ff"; //NOI18N
            }
            
        },
        
        /**
         * Merge will fail if fast forward is impossible, no merge commit will
         * be created under any circumstances.
         */
        FAST_FORWARD_ONLY {

            @Override
            public String toString () {
                return "--ff-only"; //NOI18N
            }
            
        },
        
        /**
         * Will always create a merge commit even if fast forward were possible.
         */
        NO_FAST_FORWARD {

            @Override
            public String toString () {
                return "--no-ff"; //NOI18N
            }
            
        };
    }

    /**
     * Returns the instance of {@link GitRepository} representing an existing or not yet existing repository
     * specified by the given local folder.
     * @param repositoryLocation repository root location, the file may or may not exist.
     *                           If you plan to create a local new repository, the repository
     *                           will be created at this location.
     * @return instance of <code>GitRepository</code>
     */
    public static synchronized GitRepository getInstance (File repositoryLocation) {
        synchronized (repositoryPool) {
            GitRepository repository = repositoryPool.get(repositoryLocation);
            if (repository == null) {
                // careful about keeping the reference to the repositoryRoot, rather create a new instance
                repositoryPool.put(repositoryLocation, repository = new GitRepository(new File(repositoryLocation.getAbsolutePath())));
            }
            return repository;
        }
    }

    private GitRepository (File repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

    /**
     * Creates and returns always a new instance of git client bound to the local git repository.
     * The repository may or may not exist yet, however most
     * git commands work only on an existing repository.
     * @return an instance of a git client
     * @throws GitException when an error occurs while loading repository data from disk.
     */
    public synchronized GitClient createClient () throws GitException {
        getRepository();
        return createClient(gitRepository);
    }
    
    /**
     * Parses the repository configuration file and returns the default fast-forward merge
     * option set for the repository and its current branch.
     * 
     * @return the default fast-forward option for the current repository and the active branch.
     * @throws GitException an error occurs
     * @since 1.26
     */
    public FastForwardOption getDefaultFastForwardOption () throws GitException {
        JGitRepository repository = getRepository();
        repository.increaseClientUsage();
        try {
            MergeConfig cfg = MergeConfig.getConfigForCurrentBranch(repository.getRepository());
            MergeCommand.FastForwardMode mode = cfg.getFastForwardMode();
            switch (mode) {
                case FF_ONLY:
                    return FastForwardOption.FAST_FORWARD_ONLY;
                case NO_FF:
                    return FastForwardOption.NO_FAST_FORWARD;
                default:
                    return FastForwardOption.FAST_FORWARD;
            }
        } finally {
            repository.decreaseClientUsage();
        }
    }

    private synchronized JGitRepository getRepository () {
        if (gitRepository == null) {
            gitRepository = new JGitRepository(repositoryLocation);
            SshSessionFactory.setInstance(JGitSshSessionFactory.getDefault());
        }
        return gitRepository;
    }

    /**
     * For tests
     */
    static void clearRepositoryPool() {
        synchronized(repositoryPool) {
            repositoryPool.clear();
        }
    }

    private GitClient createClient (JGitRepository repository) throws GitException {
        return new GitClient(repository);
    }

}
