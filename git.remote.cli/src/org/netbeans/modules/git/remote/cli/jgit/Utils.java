/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.remote.cli.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.commands.ListBranchCommand;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public final class Utils {
    private Utils () {
    }

//    public static Repository getRepositoryForWorkingDir (VCSFileProxy workDir) throws IOException, IllegalArgumentException {
//         //TODO: temporary to compile module
//         Repository repo = new FileRepositoryBuilder().setWorkTree(workDir.toFile()).build();
//         repo.getConfig().setBoolean("pack", null, "buildbitmaps", false);
//         return repo;
//    }

    public static VCSFileProxy getMetadataFolder (VCSFileProxy workDir) {
        return VCSFileProxy.createFileProxy(workDir, GitConstants.DOT_GIT);
    }

//    public static boolean checkExecutable (Repository repository) {
//        return repository.getConfig().getBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, true);
//    }
    
//    public static Collection<PathFilter> getPathFilters (VCSFileProxy workDir, VCSFileProxy[] roots) {
//        Collection<String> relativePaths = getRelativePaths(workDir, roots);
//        return getPathFilters(relativePaths);
//    }

//    public static TreeFilter getExcludeExactPathsFilter (VCSFileProxy workDir, VCSFileProxy[] roots) {
//        Collection<String> relativePaths = getRelativePaths(workDir, roots);
//        TreeFilter filter = null;
//        if (relativePaths.size() > 0) {
//            Collection<PathFilter> filters = getPathFilters(relativePaths);
//            List<TreeFilter> exactPathFilters = new LinkedList<TreeFilter>();
//            for (PathFilter f : filters) {
//                exactPathFilters.add(ExactPathFilter.create(f));
//            }
//            return NotTreeFilter.create(exactPathFilters.size() == 1 ? exactPathFilters.get(0) : OrTreeFilter.create(exactPathFilters));
//        }
//        return filter;
//    }

//    public static List<GitFileInfo> getDiffEntries (JGitRepository repository, TreeWalk walk, GitClassFactory fac) throws IOException {
//        List<GitFileInfo> result = new ArrayList<GitFileInfo>();
//        List<DiffEntry> entries = DiffEntry.scan(walk);
//        RenameDetector rd = new RenameDetector(repository.getRepository());
//        rd.addAll(entries);
//        entries = rd.compute();
//        for (DiffEntry e : entries) {
//            GitRevisionInfo.GitFileInfo.Status status;
//            VCSFileProxy oldFile = null;
//            String oldPath = null;
//            String path = e.getOldPath();
//            if (path == null) {
//                path = e.getNewPath();
//            }
//            switch (e.getChangeType()) {
//                case ADD:
//                    status = GitRevisionInfo.GitFileInfo.Status.ADDED;
//                    path = e.getNewPath();
//                    break;
//                case COPY:
//                    status = GitRevisionInfo.GitFileInfo.Status.COPIED;
//                    oldFile = VCSFileProxy.createFileProxy(repository.getLocation(), e.getOldPath());
//                    oldPath = e.getOldPath();
//                    path = e.getNewPath();
//                    break;
//                case DELETE:
//                    status = GitRevisionInfo.GitFileInfo.Status.REMOVED;
//                    path = e.getOldPath();
//                    break;
//                case MODIFY:
//                    status = GitRevisionInfo.GitFileInfo.Status.MODIFIED;
//                    path = e.getOldPath();
//                    break;
//                case RENAME:
//                    status = GitRevisionInfo.GitFileInfo.Status.RENAMED;
//                    oldFile = VCSFileProxy.createFileProxy(repository.getLocation(), e.getOldPath());
//                    oldPath = e.getOldPath();
//                    path = e.getNewPath();
//                    break;
//                default:
//                    status = GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
//            }
//            if (status == GitRevisionInfo.GitFileInfo.Status.RENAMED) {
//                result.add(fac.createFileInfo(VCSFileProxy.createFileProxy(repository.getLocation(), e.getOldPath()), e.getOldPath(), GitRevisionInfo.GitFileInfo.Status.REMOVED, null, null));
//            }
//            result.add(fac.createFileInfo(VCSFileProxy.createFileProxy(repository.getLocation(), path), path, status, oldFile, oldPath));
//        }
//        return result;
//    }

//    public static Collection<PathFilter> getPathFilters (Collection<String> relativePaths) {
//        Collection<PathFilter> filters = new ArrayList<>(relativePaths.size());
//        for (String path : relativePaths) {
//            filters.add(PathFilter.create(path));
//        }
//        return filters;
//    }

    public static List<String> getRelativePaths(VCSFileProxy workDir, VCSFileProxy[] roots) {
        List<String> paths = new ArrayList<String>(roots.length);
        for (VCSFileProxy root : roots) {
            if (workDir.equals(root)) {
                paths.clear();
                break;
            } else {
                paths.add(getRelativePath(workDir, root));
            }
        }
        return paths;
    }

    public static String getRelativePath (VCSFileProxy repo, final VCSFileProxy file) {
        return getRelativePath(repo, file, false);
    }

    private static String getRelativePath (VCSFileProxy repo, final VCSFileProxy file, boolean canonicalized) {
        StringBuilder relativePath = new StringBuilder("");
        VCSFileProxy parent = file;
        if (!parent.equals(repo)) {
            while (parent != null && !parent.equals(repo)) {
                relativePath.insert(0, "/").insert(0, parent.getName()); //NOI18N
                parent = parent.getParentFile();
            }
            if (parent == null) {
                if (!canonicalized) {
                    try {
                        return getRelativePath(VCSFileProxySupport.getCanonicalFile(repo), VCSFileProxySupport.getCanonicalFile(file), true);
                    } catch (IOException ex) {
                        Logger.getLogger(Utils.class.getName()).log(Level.FINE, null, ex);
                    }
                }
                throw new IllegalArgumentException(file.getPath() + " is not under " + repo.getPath());
            }
            relativePath.deleteCharAt(relativePath.length() - 1);
        }
        return relativePath.toString();
    }

//    /**
//     * Returns true if the current file/folder specified by the given TreeWalk lies under any of the given filters
//     * @param treeWalk
//     * @param filters
//     * @return
//     */
//    public static boolean isUnderOrEqual (TreeWalk treeWalk, Collection<PathFilter> filters) {
//        boolean retval = filters.isEmpty();
//        for (PathFilter filter : filters) {
//            if (filter.include(treeWalk) && treeWalk.getPathString().length() >= filter.getPath().length()) {
//                retval = true;
//                break;
//            }
//        }
//        return retval;
//    }
//
//    public static Collection<byte[]> getPaths (Collection<PathFilter> pathFilters) {
//        Collection<byte[]> paths = new LinkedList<byte[]>();
//        for (PathFilter filter : pathFilters) {
//            paths.add(Constants.encode(filter.getPath()));
//        }
//        return paths;
//    }
//
//    public static GitRevisionInfo findCommit (JGitRepository repository, String revision) throws GitException.MissingObjectException, GitException {
//        return findCommit(repository, revision, null);
//    }
//    
//    public static GitRevisionInfo findCommit (JGitRepository repository, String revision, RevWalk walk) throws GitException.MissingObjectException, GitException {
//        ObjectId commitId = parseObjectId(repository, revision);
//        if (commitId == null) {
//            throw new GitException.MissingObjectException(revision, GitObjectType.COMMIT);
//        }
//        return findCommit(repository, commitId, walk);
//    }
//    
//    public static GitRevisionInfo findCommit (JGitRepository repository, ObjectId commitId, RevWalk walk) throws GitException.MissingObjectException, GitException {
//        try {
//            return (walk == null ? new RevWalk(repository) : walk).parseCommit(commitId);
//        } catch (MissingObjectException ex) {
//            throw new GitException.MissingObjectException(commitId.name(), GitObjectType.COMMIT, ex);
//        } catch (IncorrectObjectTypeException ex) {
//            throw new GitException(MessageFormat.format(Utils.getBundle(Utils.class).getString("MSG_Exception_IdNotACommit"), commitId.name())); //NOI18N
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        }
//    }
//
//    public static ObjectId parseObjectId (Repository repository, String objectId) throws GitException {
//        try {
//            return repository.resolve(objectId);
//        } catch (RevisionSyntaxException ex) {
//            throw new GitException.MissingObjectException(objectId, GitObjectType.COMMIT, ex);
//        } catch (AmbiguousObjectException ex) {
//            throw new GitException(MessageFormat.format(Utils.getBundle(Utils.class).getString("MSG_Exception_IdNotACommit"), objectId), ex); //NOI18N
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        }
//    }
//
//    public static RevObject findObject (Repository repository, String objectId) throws GitException.MissingObjectException, GitException {
//        try {
//            ObjectId commitId = parseObjectId(repository, objectId);
//            if (commitId == null) {
//                throw new GitException.MissingObjectException(objectId, GitObjectType.UNKNOWN);
//            }
//            return new RevWalk(repository).parseAny(commitId);
//        } catch (MissingObjectException ex) {
//            throw new GitException.MissingObjectException(objectId, GitObjectType.UNKNOWN, ex);
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        }
//    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(VCSFileProxy file) {
        if (file.isDirectory()) {
            VCSFileProxy [] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteRecursively(files[i]);
                }
            }
        }
        VCSFileProxySupport.delete(file);
    }
    
    /**
     * Eliminates part of the ref's name that equals knon prefixes such as refs/heads/, refs/remotes/ etc.
     * @param ref
     * @return 
     */
    public static String getRefName (GitRef ref) {
        String name = ref.getName();
        for (String prefix : Arrays.asList(GitConstants.R_HEADS, GitConstants.R_REMOTES, GitConstants.R_TAGS, GitConstants.R_REFS)) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
            }
        }
        return name;
    }

    /**
     * Transforms references into GitBranches
     * @param allRefs all references found
     * @param prefix prefix denoting heads amongst references
     * @return 
     */
    public static Map<String, GitBranch> refsToBranches (Collection<GitRef> allRefs, String prefix, GitClassFactory factory) {
        Map<String, GitBranch> branches = new LinkedHashMap<String, GitBranch>();
        
        // try to find the head first - it usually is the active remote branch
        GitRef head = null;
        for (final GitRef ref : allRefs) {
            if (ref.getLeaf().getName().equals(GitConstants.HEAD)) {
                head = ref;
                break;
            }
        }
        
        // get all refs/heads
        //for (final GitRef ref : Collections.sort(allRefs)) {
        for (final GitRef ref : allRefs) {
            String refName = ref.getLeaf().getName();
            if (refName.startsWith(prefix)) {
                String name = refName.substring(prefix.length());
                String id = ref.getLeaf().getObjectId();
                if (id == null) {
                    // can happen, e.g. when the repository has no HEAD yet
                    Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Null object id for ref: {0}, {1}:{2}, {3}", //NOI18N
                            new Object[] { ref.toString(), ref.getName(), ref.getObjectId(), ref.getLeaf() } );
                    continue;
                }
                branches.put(
                    name, 
                    factory.createBranch(
                        name, 
                        false, 
                        head != null && ref.getObjectId().equals(head.getObjectId()), 
                        id));
            }
        }
        return branches;
    }

    /**
     * Transforms references into pairs of tag name/id
     * @param allRefs all references found
     * @return 
     */
    public static Map<String, String> refsToTags (Collection<GitRef> allRefs) {
        Map<String, String> tags = new LinkedHashMap<String, String>();
        
        // get all refs/tags
        //for (final GitRef ref : Collections.sort(allRefs)) {
        for (final GitRef ref : allRefs) {
            String refName = ref.getLeaf().getName();
            if (refName.startsWith(GitConstants.R_TAGS)) {
                String name = refName.substring(GitConstants.R_TAGS.length());
                tags.put(name, ref.getLeaf().getObjectId());
            }
        }
        return tags;
    }

    /**
     * Returns a resource bundle contained in the same package the given clazz is.
     * @param clazz
     * @return 
     */
    public static ResourceBundle getBundle (Class clazz) {
        String pref = clazz.getName();
        int last = pref.lastIndexOf('.');

        if (last >= 0) {
            pref = pref.substring(0, last + 1) + "Bundle"; //NOI18N
        } else {
            // base package, search for bundle
            pref = "Bundle"; // NOI18N
        }
        return ResourceBundle.getBundle(pref);
    }

    public static GitBranch getTrackedBranch (JGitConfig config, String branchName, Map<String, GitBranch> allBranches) {
        String remoteName = config.getString(JGitConfig.CONFIG_BRANCH_SECTION, branchName, JGitConfig.CONFIG_KEY_REMOTE);
        String trackedBranchName = config.getString(JGitConfig.CONFIG_BRANCH_SECTION, branchName, JGitConfig.CONFIG_KEY_MERGE);
        if (trackedBranchName != null) {
            if (trackedBranchName.startsWith(GitConstants.R_HEADS)) {
                trackedBranchName = trackedBranchName.substring(GitConstants.R_HEADS.length());
            } else if (trackedBranchName.startsWith(GitConstants.R_REMOTES)) {
                trackedBranchName = trackedBranchName.substring(GitConstants.R_REMOTES.length());
            }
        }
        if (trackedBranchName == null) {
            return null;
        } else {
            if (remoteName != null && ".".equals(remoteName)) { //NOI18N
                remoteName = ""; //NOI18N
            } else {
                remoteName = remoteName + "/"; //NOI18N
            }
            return allBranches.get(remoteName + trackedBranchName);
        }
    }

    public static Map getAllBranches (JGitRepository repository, GitClassFactory fac, ProgressMonitor monitor) throws GitException {
        ListBranchCommand cmd = new ListBranchCommand(repository, fac, true, monitor);
        cmd.execute();
        return cmd.getBranches();
    }
}
