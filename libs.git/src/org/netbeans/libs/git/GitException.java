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

package org.netbeans.libs.git;

import java.text.MessageFormat;
import org.netbeans.libs.git.jgit.Utils;

/**
 * A general exception thrown when an error occurs while running git commands.
 * 
 * @author Ondra Vrabec
 */
public class GitException extends Exception {

    /**
     * Encapsulates a cause exception.
     * @param t cause exception
     */
    public GitException (Throwable t) {
        super(t);
    }

    /**
     * There is no cause exception but has an error message.
     * @param message error description
     */
    public GitException (String message) {
        super(message);
    }

    /**
     * There is a cause exception and has its own error message
     * @param message error description
     * @param ex cause exception
     */
    public GitException (String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Describes an error when a non existent git object is tried to be loaded.<br/>
     * Usually is thrown when a caller passes a name of a branch or tag or a non-existent commit id
     * to a git command.
     */
    public static class MissingObjectException extends GitException {
        private final String objectName;
        private final GitObjectType objectType;

        /**
         * 
         * @param objectName name or id of an object being resolved.
         * @param objectType type of an object being resolved.
         */
        public MissingObjectException (String objectName, GitObjectType objectType) {
            super(MessageFormat.format(Utils.getBundle(GitException.class).getString("MSG_Exception_ObjectDoesNotExist"), new Object[] { objectType.toString(), objectName })); //NOI18N
            this.objectName = objectName;
            this.objectType = objectType;
        }

        /**
         * 
         * @param objectName name or id of an object being resolved.
         * @param objectType type of an object being resolved.
         * @param ex cause exception
         */
        public MissingObjectException (String objectName, GitObjectType objectType, Throwable ex) {
            super(MessageFormat.format(Utils.getBundle(GitException.class).getString("MSG_Exception_ObjectDoesNotExist"), new Object[] { objectType.toString(), objectName }), ex); //NOI18N
            this.objectName = objectName;
            this.objectType = objectType;
        }

        /**
         * @return name or id of an object unable to been resolved.
         */
        public String getObjectName () {
            return objectName;
        }

        /**
         * @return kind of a git object unable to been resolved.
         */
        public GitObjectType getObjectType () {
            return objectType;
        }
    }
    
    /**
     * Thrown when a file cannot be checked out into the working tree because it would result in a local conflict.
     * <br/>
     * The code that handles the exception should resolve the conflicts before retrying the failed command.
     * {@link #getConflicts() } can be called to get the conflicted paths.
     */
    public static class CheckoutConflictException extends GitException {
        private final String[] conflicts;

        /**
         * @param conflicts array of conflicted paths
         * @param cause cause exception
         */
        public CheckoutConflictException (String[] conflicts, Throwable cause) {
            super(Utils.getBundle(GitException.class).getString("MSG_Exception_CheckoutConflicts"), cause);
            this.conflicts = conflicts;
        }

        /**
         * @param conflicts array of conflicted paths
         */
        public CheckoutConflictException (String[] conflicts) {
            this(conflicts, null);
        }

        /**
         * @return an array of relative paths to the root of the working tree that caused the command to fail.
         */
        public String[] getConflicts () {
            return conflicts;
        }
    }

    /**
     * Thrown when an authentication or authorization to a remote repository fails because of
     * incorrect credentials.
     */
    public static class AuthorizationException extends GitException {
        private final String repositoryUrl;

        /**
         * @param repositoryUrl remote repository URL
         * @param message explanation error message
         * @param t cause exception
         */
        public AuthorizationException (String repositoryUrl, String message, Throwable t) {
            super(message, t);
            this.repositoryUrl = repositoryUrl;
        }

        /**
         * @return remote repository URL that was contacted and refused the connection because of incorrect credentials.
         */
        public String getRepositoryUrl () {
            return repositoryUrl;
        }
    }

    public static class RefUpdateException extends GitException {
        private final GitRefUpdateResult result;

        public RefUpdateException (String message, GitRefUpdateResult result) {
            super(message);
            this.result = result;
        }

        public GitRefUpdateResult getResult () {
            return result;
        }
    }

    /**
     * Thrown to notify a caller of the fact that a revision he passed has not been
     * fully merged into a current HEAD yet.
     * <br/>
     * Common use case is when trying to delete a not yet merged branch without the 
     * <code>forceDeleteUnmerged</code> parameter, see {@link GitClient#deleteBranch(java.lang.String, boolean, org.netbeans.libs.git.progress.ProgressMonitor) }.
     * Then {@link #getUnmergedRevision() } returns the name of the unmerged branch.
     */
    public static class NotMergedException extends GitException {
        private final String unmergedRevision;

        /**
         * @param unmergedRevision id or name of the unmerged revision or branch.
         */
        public NotMergedException (String unmergedRevision) {
            super(unmergedRevision + " has not been fully merged yet");
            this.unmergedRevision = unmergedRevision;
        }
        
        /**
         * @return id or name of the unmerged revision or branch.
         */
        public String getUnmergedRevision () {
            return unmergedRevision;
        }
    }
}
