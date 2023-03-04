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

package org.openide.windows;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.windows.IOFolding.FoldHandleDefinition;

/**
 * An object that refers to a fold in output window. It can be used to finish
 * the fold, or to create nested folds.
 *
 * @author jhavlin
 * @since openide.io/1.38
 */
public final class FoldHandle {

    private final FoldHandleDefinition definition;
    private static final Logger LOG = Logger.getLogger(FoldHandle.class.getName());
    private FoldHandle currentChild;
    private boolean finished = false;

    FoldHandle(FoldHandleDefinition definition) {
        this.definition = definition;
    }

    /**
     * Finish the fold at the current last line in the output window.
     *
     * @throws IllegalStateException if parent fold has been already finished,
     * or if there is an unfinished child fold.
     */
    public void finish() {
        definition.finish();
        finished = true;
    }

    /**
     * Start a nested fold at the current last line in output window.
     *
     * @param expanded True to expand the new fold, false to collapse it, parent
     * folds will not be collapsed/expanded.
     * @return Handle for the newly created fold.
     * @throws IllegalStateException if the fold has been already finished, or
     * if an unfinished nested fold exists.
     */
    public FoldHandle startFold(boolean expanded) {
        currentChild = new FoldHandle(definition.startFold(expanded));
        return currentChild;
    }

    /**
     * Set state of the fold.
     *
     * If a nested fold is expanded, all parent folds will be expanded too.
     *
     * @param expanded True to expand the fold, false to collapse it.
     */
    public void setExpanded(boolean expanded) {
        definition.setExpanded(expanded);
    }

    /**
     * Check whether this fold handle has been finished.
     *
     * @return True if {@link #finish()} or {@link #silentFinish()} has been
     * already called on this fold handle, false otherwise.
     *
     * @since openide.io/1.42
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Get handle created by the last invocation of {@link #startFold(boolean)}
     * or {@link #silentStartFold(boolean)}. The handle can be finished or
     * unfinished.
     *
     * @return The last started nested fold. Can be null.
     *
     * @since openide.io/1.42
     */
    public @CheckForNull FoldHandle getLastNestedFold() {
        return currentChild;
    }

    /**
     * Get current nested fold. Similar to {@link #getLastNestedFold()}, but
     * returns null if the last nested fold has been already finished.
     *
     * @return The last unfinished nested fold or null.
     *
     *  @since openide.io/1.42
     */
    public @CheckForNull FoldHandle getCurrentNestedFold() {
        return (currentChild != null && !currentChild.isFinished())
                ? currentChild
                : null;
    }

    /**
     * Similar to {@link #finish()}, but no exception is thrown if the fold
     * handle has been already finished. If an unfinished child fold exists, it
     * will be finished too. Any exception that could happen will be caught and
     * logged.
     *
     * @since openide.io/1.42
     */
    public void silentFinish() {
        if (!finished) {
            if (currentChild != null && !currentChild.finished) {
                currentChild.silentFinish();
            }
            try {
                finish();
            } catch (IllegalStateException ex) {
                LOG.log(Level.FINE, "Cannot finish fold", ex);          //NOI18N
            }
        }
    }

    /**
     * Similar to {@link #startFold(boolean)}, but no exception is thrown if the
     * fold is already finished, as well as if an unfinished nested fold exists.
     * If an unfinished nested fold exists, it will be finished before creation
     * of the new one.
     *
     * @param expanded True to expand the new fold, false to collapse it, parent
     * folds will not be collapsed/expanded.
     * @return The new fold handle, or null if it cannot be created.
     *
     *  @since openide.io/1.42
     */
    public @CheckForNull FoldHandle silentStartFold(boolean expanded) {
        if (!finished) {
            if (currentChild != null && !currentChild.finished) {
                currentChild.silentFinish();
            }
            try {
                return startFold(expanded);
            } catch (IllegalStateException ex) {
                LOG.log(Level.FINE, "Cannot start fold", ex);           //NOI18N
                return null;
            }
        } else {
            LOG.log(Level.FINE, "silentStartFold - already finished");  //NOI18N
            return null;
        }
    }
}
