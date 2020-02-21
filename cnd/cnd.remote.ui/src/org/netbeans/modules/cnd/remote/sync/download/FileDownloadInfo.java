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
