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

package org.netbeans.modules.cnd.remote.sync.download;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/*package*/ class FileDownloadInfo {

    /**
     * A lock for changing state.
     * 
     * Hold this lock to be sure the state is not changed
     * (for example., while quering a collection of FileDownloadInfo by state)
     */
    public static final Object LOCK = new Object();

    public enum State {
        UNCONFIRMED,
        CONFIRMED,
        PENDING,
        COPYING,
        CANCELLED,
        DONE,
        ERROR
    }

    /**
     * UNCONFIRMED-->CONFIRMED------------>PENDING-->COPYING-->DONE
     *       |           |                   |        |   |
     *       +-----------+->+-->CANCELLED<---+<-------+   +--->ERROR
     */
    private State state;
    private final File localFile;
    private final String remoteFile;
    private final ExecutionEnvironment env;
    private Future<Integer> copyTask;

    public FileDownloadInfo(File localFile, String remoteFile, ExecutionEnvironment env) {
        super();
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.env = env;
        this.state = State.UNCONFIRMED;
    }

    public void download() {
        synchronized (LOCK) {
            state = State.COPYING;
            if (copyTask != null) {
                copyTask.cancel(true);
            }
            localFile.getParentFile().mkdirs();
            copyTask = CommonTasksSupport.downloadFile(remoteFile, env, localFile.getAbsolutePath(), null);
        }
        try {
            int rc = copyTask.get().intValue();
            synchronized (LOCK) {
                state = (rc == 0) ? State.DONE : State.ERROR;
            }
        } catch (InterruptedException ex) {
            synchronized (LOCK) {
                state = State.CANCELLED;
            }
        } catch (ExecutionException ex) {
            synchronized (LOCK) {
                state = State.ERROR;
            }
        } finally {
            synchronized (LOCK) {
                copyTask = null;
            }
        }
    }

    public File getLocalFile() {
        return localFile;
    }

    public void cancel() {
        Future<Integer> task = copyTask;
        if (task != null) {
            task.cancel(true);
        }
        state = State.CANCELLED;
    }

    public State getState() {
        return state;
    }

    /** To be called when the remote file is changed again */
    public void reset() {
        cancel(); // TODO: should we really cancel?
        state = State.UNCONFIRMED;
    }

    /** To be called when user  confirmed download */
    public void confirm() {
        state = State.PENDING;
    }

    /** To be called when user rejected download */
    public void reject() {
        state = State.CANCELLED; // TODO: what if it was previously copied?
    }

    @Override
    public String toString() {
        return localFile.getPath() + ' ' + state + ' ' + env + ' ' + remoteFile;
    }
}
