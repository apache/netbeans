/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
