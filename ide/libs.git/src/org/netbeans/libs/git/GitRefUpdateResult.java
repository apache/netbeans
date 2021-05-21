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
 * Result of a local or remote reference update.
 * 
 * @author Ondra Vrabec
 */
public enum GitRefUpdateResult {
    
    /** The ref update/delete has not been attempted by the caller. */
    NOT_ATTEMPTED,

    /**
     * The ref could not be locked for update/delete.
     * <p>
     * This is generally a transient failure and is usually caused by
     * another process trying to access the ref at the same time as this
     * process was trying to update it. It is possible a future operation
     * will be successful.
     */
    LOCK_FAILURE,

    /**
     * Same value already stored.
     * <p>
     * Both the old value and the new value are identical. No change was
     * necessary for an update. For delete the branch is removed.
     */
    NO_CHANGE,

    /**
     * The ref was created locally for an update, but ignored for delete.
     * <p>
     * The ref did not exist when the update started, but it was created
     * successfully with the new value.
     */
    NEW,

    /**
     * The ref had to be forcefully updated/deleted.
     * <p>
     * The ref already existed but its old value was not fully merged into
     * the new value. The configuration permitted a forced update to take
     * place, so ref now contains the new value. History associated with the
     * objects not merged may no longer be reachable.
     */
    FORCED,

    /**
     * The ref was updated/deleted in a fast-forward way.
     * <p>
     * The tracking ref already existed and its old value was fully merged
     * into the new value. No history was made unreachable.
     */
    FAST_FORWARD,

    /**
     * Not a fast-forward and not stored.
     * <p>
     * The tracking ref already existed but its old value was not fully
     * merged into the new value. The configuration did not allow a forced
     * update/delete to take place, so ref still contains the old value. No
     * previous history was lost.
     */
    REJECTED,

    /**
     * Rejected because trying to delete the current branch.
     * <p>
     * Has no meaning for update.
     */
    REJECTED_CURRENT_BRANCH,

    /**
     * The ref was probably not updated/deleted because of I/O error.
     * <p>
     * Unexpected I/O error occurred when writing new ref. Such error may
     * result in uncertain state, but most probably ref was not updated.
     * <p>
     * This kind of error doesn't include {@link #LOCK_FAILURE}, which is a
     * different case.
     */
    IO_FAILURE,

    /**
     * The ref was renamed from another name
     * <p>
     */
    RENAMED,

    /**
     * Remote ref was up to date, there was no need to update anything.
     */
    UP_TO_DATE,

    /**
     * Remote ref update was rejected, as it would cause non fast-forward
     * update.
     */
    REJECTED_NONFASTFORWARD,

    /**
     * Remote ref update was rejected, because remote side doesn't
     * support/allow deleting refs.
     */
    REJECTED_NODELETE,

    /**
     * Remote ref update was rejected, because old object id on remote
     * repository wasn't the same as defined expected old object.
     */
    REJECTED_REMOTE_CHANGED,

    /**
     * Remote ref update was rejected for other reason.
     */
    REJECTED_OTHER_REASON,

    /**
     * One or more objects aren't in the repository.
     * <p>
     * This is severe indication of either repository corruption on the
     * server side, or a bug in the client wherein the client did not supply
     * all required objects during the pack transfer.
     */
    REJECTED_MISSING_OBJECT,

    /**
     * Remote ref didn't exist. Can occur on delete request of a non
     * existing ref.
     */
    NON_EXISTING,

    /**
     * Push process is awaiting update report from remote repository. This
     * is a temporary state or state after critical error in push process.
     */
    AWAITING_REPORT,

    /**
     * Remote ref was successfully updated.
     */
    OK;
}
