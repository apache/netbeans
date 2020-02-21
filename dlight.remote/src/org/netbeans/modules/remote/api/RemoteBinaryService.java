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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.api;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * Makes a remote binary accessible locally
 * Gets a local path to a binary built on remote machine.
 * In the case it is shared, returns just a path to the same physical binary.
 * In the case it is not shared, ensures that the file is copied to the local host
 * and returns full path to the copy
 */
public abstract class RemoteBinaryService {

    private final static Map<RemoteBinaryID, Future<Boolean>> readiness =
            Collections.synchronizedMap(new HashMap<RemoteBinaryID, Future<Boolean>>());

    protected RemoteBinaryService() {
    }

    /**
     * Returns an ID to be used as a reference to the RemoteBinaryService.
     *
     * The method can be very slow.
     * It should never be called from AWT thread.
     */
    public static RemoteBinaryID getRemoteBinary(ExecutionEnvironment execEnv, String remotePath) {
        if (execEnv.isLocal()) {
            return new RemoteBinaryID(remotePath, ""+ new File(remotePath).lastModified()); // NOI18N
        } else {
            RemoteBinaryService rbs = Lookup.getDefault().lookup(RemoteBinaryService.class);

            if (rbs == null) {
                return null;
            }

            RemoteBinaryResult result = rbs.getRemoteBinaryImpl(execEnv, remotePath);

            if (result == null) {
                return null;
            }

            RemoteBinaryID id = new RemoteBinaryID(result.localFName, result.timeStamp);
            Future<Boolean> prevResult = readiness.put(id, result.syncResult);

            if (prevResult != null && prevResult != result.syncResult) {
                prevResult.cancel(true);
            }

            return id;
        }
    }

    public static String getFileName(RemoteBinaryID id) {
        return id.toIDString();
    }

    public static Future<Boolean> getResult(RemoteBinaryID id) {
        return readiness.get(id);
    }

    protected abstract RemoteBinaryResult getRemoteBinaryImpl(ExecutionEnvironment execEnv, String remotePath);

    protected static class RemoteBinaryResult {

        public final String localFName;
        public final Future<Boolean> syncResult;
        private String timeStamp;

        public RemoteBinaryResult(String localFName, Future<Boolean> syncResult) {
            this.localFName = localFName;
            this.syncResult = syncResult;
        }
        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getTimeStamp() {
            return timeStamp;
        }
    }

    public static class RemoteBinaryID {

        private final String id;
        private final String timeStamp;

        private RemoteBinaryID(String id, String timeStamp) {
            this.id = id;
            this.timeStamp = timeStamp;
        }

        public static RemoteBinaryID fromIDString(String str) {
            int i = str.lastIndexOf(';'); // NOI18N
            String timeStamp = "0"; // NOI18N
            if (i > 0) {
                timeStamp = str.substring(i+1);
                str = str.substring(0, i);
            }
            return new RemoteBinaryID(str, timeStamp);
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public String toIDString() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RemoteBinaryID)) {
                return false;
            }
            RemoteBinaryID that = (RemoteBinaryID)obj;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }
    }
}
