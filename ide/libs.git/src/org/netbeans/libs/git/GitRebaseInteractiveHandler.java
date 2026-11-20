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
package org.netbeans.libs.git;

import java.util.List;

/**
 * Callbacks invoked by the rebase driver while an
 * {@link GitClient#rebase(GitClient.RebaseOperationType, String, GitRebaseInteractiveHandler,
 * org.netbeans.libs.git.progress.ProgressMonitor) interactive rebase} is
 * running.
 *
 * <p>Callback methods are invoked from the thread running the rebase (not
 * the event thread); implementations that need to show a dialog must transfer
 * to the event thread themselves.
 *
 * @author mbien
 * @since 1.32
 */
public interface GitRebaseInteractiveHandler {

    /**
     * Invoked when the rebase driver establishes the list of steps it is
     * going to process: once when a rebase is started, and again with the
     * <em>remaining</em> steps whenever the rebase is continued.
     *
     * <p>The implementation customizes the rebase by modifying the list:
     * reordering steps, dropping steps (removing them from the list or
     * setting their action to
     * {@link GitRebaseTodoLine.RebaseAction#DROP}) and changing
     * {@link GitRebaseTodoLine#getAction() actions} through
     * {@link GitRebaseTodoLine#setAction(GitRebaseTodoLine.RebaseAction)}.
     * The list is processed exactly as left behind, so the method must be
     * idempotent when it is invoked again on a subset of the steps.
     *
     * @param steps the steps the rebase driver is about to process; modify
     *            this list in place
     */
    void prepareSteps(List<GitRebaseTodoLine> steps);

    /**
     * Invoked when a sequence of {@link GitRebaseTodoLine.RebaseAction#SQUASH
     * squash}/{@link GitRebaseTodoLine.RebaseAction#FIXUP fixup} steps is
     * combined into a single commit.
     *
     * @param message the combined commit message of the sequence
     * @return the final commit message of the combined commit
     */
    String modifyCommitMessage(String message);

}
