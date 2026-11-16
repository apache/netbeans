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

/**
 * A single step of an interactive rebase todo list, similar to a line in the
 * <code>.git/rebase-merge/todo</code> file used by native git.
 *
 * <p>Instances are created by the implementation and passed to
 * {@link GitRebaseInteractiveHandler#prepareSteps(java.util.List)}; clients may
 * modify the <em>list</em> of steps (reorder, drop) and change individual
 * {@link #getAction() actions} through {@link #setAction(RebaseAction)}.
 *
 * @author mbien
 * @since 1.32
 */
public final class GitRebaseTodoLine {

    /**
     * The action to perform when the step is processed by the rebase driver.
     */
    public enum RebaseAction {
        /**
         * Applies the commit unchanged.
         */
        PICK,
        /**
         * Applies the commit and allows modifying its commit message.
         */
        REWORD,
        /**
         * Applies the commit and stops the rebase so that the commit can be
         * amended before continuing.
         */
        EDIT,
        /**
         * Merges the commit into the previous one, keeping its commit message
         * in the combined message.
         */
        SQUASH,
        /**
         * Merges the commit into the previous one, discarding its commit
         * message.
         */
        FIXUP,
        /**
         * Discards the commit: the step is not applied. Implementations
         * handle it by removing the step from the list of steps, so a
         * dropped step is the same as a step the list does not contain.
         */
        DROP,
        /**
         * A comment line in the todo list. Never processed.
         */
        COMMENT
    }

    private final String commit;
    private final String shortMessage;
    private RebaseAction action;

    public GitRebaseTodoLine(RebaseAction action, String commit, String shortMessage) {
        this.action = action;
        this.commit = commit;
        this.shortMessage = shortMessage;
    }

    /**
     * The action the step is processed with.
     * @return the action
     */
    public RebaseAction getAction() {
        return action;
    }

    /**
     * Changes the action the step is processed with.
     *
     * @param action the new action
     * @throws IllegalArgumentException if the action cannot be applied to this
     *             step (e.g. a comment line cannot become a regular step)
     */
    public void setAction(RebaseAction action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null"); //NOI18N
        }
        this.action = action;
    }

    /**
     * The id of the commit the step is applied to. The id may be an
     * abbreviation of the full commit id; clients matching lines against
     * their own data should compare by prefix. Empty for comment lines.
     * @return the commit id, never <code>null</code>
     */
    public String getCommit() {
        return commit;
    }

    /**
     * The subject of the commit the step is applied to, for display purposes.
     * May be empty for comment lines.
     * @return the short commit message, never <code>null</code>
     */
    public String getShortMessage() {
        return shortMessage;
    }

    @Override
    public String toString() {
        return action + " " + commit + " " + shortMessage; //NOI18N
    }

}
