/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.rebase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.git.GitClient.RebaseOperationType;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRebaseInteractiveHandler;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRebaseResult.RebaseStatus;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;

/**
 * Simulates the plan of an interactive rebase: the plan is replayed exactly
 * like a real rebase (the same driver, the same handler) but in a temporary
 * scratch repository, so the real repository is never touched.
 *
 * <p>The scratch repository borrows the objects of the real repository
 * through <code>objects/info/alternates</code> (nothing is copied) and
 * carries over the repository settings that affect how the commits are
 * applied (line ending handling). It is checked out at the head of the
 * range and the range is rebased onto its base, like the real rebase.
 *
 * <p>The simulation stops at the first commit that does not apply cleanly
 * (like a real rebase) and reports the commit and the files in conflict;
 * the commits after the stopping commit are not predicted, the outcome of
 * their application depends on how the conflict is resolved.
 */
public final class RebaseSimulator {

    private RebaseSimulator() {}

    /**
     * The result of a simulation.
     */
    public record Result(Kind kind, String stoppedCommit, List<String> conflicts, String message) {

        public enum Kind {
            /** Every kept commit of the plan applied cleanly. */
            CLEAN,
            /** The rebase would stop at a conflicting commit. */
            STOPPED,
            /** The simulation was canceled by the user. */
            CANCELED,
            /** The simulation could not be performed. */
            FAILED
        }

        static Result clean() {
            return new Result(Kind.CLEAN, null, List.of(), null);
        }

        static Result stopped(String stoppedCommit, List<String> conflicts) {
            return new Result(Kind.STOPPED, stoppedCommit, List.copyOf(conflicts), null);
        }

        static Result canceled() {
            return new Result(Kind.CANCELED, null, List.of(), null);
        }

        static Result failed(String message) {
            return new Result(Kind.FAILED, null, List.of(), message);
        }
    }

    /**
     * Simulates the rebase: the commits of the range
     * <code>baseRevision</code>..<code>tipRevision</code> are replayed with
     * the actions and the order the handler settles, in a temporary
     * repository that borrows the objects of the given repository.
     *
     * @param repository the repository the plan was settled in (never modified)
     * @param baseRevision the commit the range is rebased onto
     * @param tipRevision the head of the range, the newest commit of the plan
     * @param handlerFor a function creating the handler applying the plan
     *        for the repository the replay runs in: the handler persists
     *        the plan into the rebase folder of the given repository, the
     *        real rebase persists it in the real repository, the
     *        simulation persists it in the temporary one
     * @param monitor the progress monitor, the user can cancel the
     *        simulation by canceling it
     * @return the result of the simulation, never <code>null</code>
     */
    public static Result simulate(File repository, String baseRevision, String tipRevision,
            Function<File, GitRebaseInteractiveHandler> handlerFor, ProgressMonitor monitor) {
        File scratch;
        try {
            scratch = Files.createTempDirectory("nb-rebase-simulation-").toFile();
        } catch (IOException ex) {
            return Result.failed(ex.getMessage());
        }
        try {
            prepareScratch(repository, scratch, tipRevision);
            if (monitor.isCanceled()) {
                return Result.canceled();
            }
            try (GitClient client = org.netbeans.modules.git.Git.getInstance().getClient(scratch)) {
                GitRebaseInteractiveHandler handler = handlerFor.apply(scratch);
                GitRebaseResult result = client.rebase(
                        RebaseOperationType.BEGIN,
                        baseRevision,
                        handler,
                        monitor
                );
                return monitor.isCanceled() ? Result.canceled() : interpret(scratch, result);
            }
        } catch (GitException | IOException | RevisionSyntaxException ex) {
            return monitor.isCanceled() ? Result.canceled() : Result.failed(ex.getMessage());
        } catch (RuntimeException ex) {
            // the driver throws a plain RuntimeException for a plan dropping
            // every step, the message is reported as-is
            return monitor.isCanceled() ? Result.canceled() : Result.failed(ex.getMessage());
        } finally {
            // decouple from RP
            Thread.ofVirtual().name(RebaseSimulator.class.getName() + "-cleanup") //NOI18N
                              .start(() -> deleteRecursively(scratch));
        }
    }

    /**
     * Creates the scratch repository: an empty repository that borrows the
     * objects of the real repository and is checked out at the head of the
     * range, so that rebasing the range onto its base replays exactly the
     * commits of the range.
     */
    private static void prepareScratch(File repository, File scratch, String tipRevision)
            throws IOException, GitException, RevisionSyntaxException {
        File gitDir = GitUtils.getGitFolderForRoot(repository);
        // the skeleton of the repository, the wiring to the borrowed objects
        try (Git git = Git.init().setDirectory(scratch).setInitialBranch("main").call()) { //NOI18N
            Repository repo = git.getRepository();
            File alternates = new File(new File(new File(repo.getCommonDirectory(), "objects"), "info"), "alternates"); //NOI18N
            Files.createDirectories(alternates.getParentFile().toPath());
            Files.writeString(alternates.toPath(), new File(gitDir, "objects").getAbsolutePath() + "\n", StandardCharsets.UTF_8);
            carryOverConfig(gitDir, scratch);
        } catch (GitAPIException | ConfigInvalidException ex) {
            throw new GitException(ex);
        }
        // a fresh view of the repository, seeing the borrowed objects
        try (Git git = Git.open(scratch)) {
            // point a scratch branch at the head of the range and check it
            // out, the rebase is then replayed onto the base of the range
            RefUpdate update = git.getRepository().updateRef("refs/heads/nb-simulate"); //NOI18N
            update.setNewObjectId(git.getRepository().resolve(tipRevision));
            update.setRefLogMessage("rebase simulation", false); //NOI18N
            RefUpdate.Result result = update.update();
            if (result != RefUpdate.Result.NEW) {
                throw new GitException("Could not prepare the simulation: " + result); //NOI18N
            }
            git.checkout().setName("nb-simulate").call(); //NOI18N
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    /**
     * Carries over the settings of the real repository that affect how the
     * commits are applied to the working tree.
     */
    private static void carryOverConfig(File gitDir, File scratch) throws IOException, ConfigInvalidException {
        FileBasedConfig real = new FileBasedConfig(new File(gitDir, "config"), FS.DETECTED);
        real.load();
        FileBasedConfig config = new FileBasedConfig(new File(new File(scratch, ".git"), "config"), FS.DETECTED);
        config.load();
        for (String key : List.of("autocrlf", "eol", "safecrlf")) { //NOI18N
            String value = real.getString("core", null, key);
            if (value != null) {
                config.setString("core", null, key, value);
            }
        }
        config.save();
    }

    /**
     * The result of the driver run as the outcome of the simulation.
     *
     * @param workTree the root of the scratch repository the replay ran in,
     *        the conflicts are reported relative to it
     */
    private static Result interpret(File workTree, GitRebaseResult result) {
        RebaseStatus status = result.getRebaseStatus();
        if (status == RebaseStatus.STOPPED) {
            Path root = workTree.toPath();
            List<String> conflicts = new ArrayList<>();
            for (File file : result.getConflicts()) {
                conflicts.add(root.relativize(file.toPath()).toString());
            }
            conflicts.sort(String::compareTo);
            return Result.stopped(result.getCurrentCommit(), conflicts);
        }
        if (status.isSuccessful() || status == RebaseStatus.NOTHING_TO_COMMIT) {
            // a pick that applies no changes (the changes are already
            // contained in the result of the earlier picks of the plan)
            // stops a real rebase for a confirmation, the simulation
            // reports it as a clean outcome
            return Result.clean();
        }
        return Result.failed(status.name());
    }

    /**
     * Best-effort recursive deletion of the scratch repository.
     */
    private static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.deleteIfExists(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignore) {
            // the scratch repository is temporary, it is left for the
            // system's temp cleaner
        }
    }

}