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
package org.netbeans.modules.javascript.nodejs.util;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.ui.Notifications;

public final class GraalVmUtils {

    private static final boolean RUNNING_ON;
    private static final AtomicBoolean OPTIONS_DETECTED = new AtomicBoolean(false);


    static {
        RUNNING_ON = new File(getNode()).isFile();
    }

    private GraalVmUtils() {
    }

    public static boolean isRunningOn() {
        return RUNNING_ON;
    }

    public static void detectOptions() {
        if (!isRunningOn()) {
            return;
        }
        if (!OPTIONS_DETECTED.compareAndSet(false, true)) {
            // already detected
            return;
        }
        if (!properPathsSet()) {
            Notifications.notifyGraalVmDetected();
        }
    }

    public static boolean properPathsSet() {
        NodeExecutable node = NodeExecutable.getDefault(null, false);
        if (node == null
                || !node.getExecutable().equals(getNode())) {
            return false;
        }
        NpmExecutable npm = NpmExecutable.getDefault(null, false);
        return npm != null
                && npm.getExecutable().equals(getNpm(false));
    }

    public static String getNode() {
        return getJavaHomeBinFile("node") // NOI18N
                .getAbsolutePath();
    }

    public static String getNpm(boolean withParams) {
        StringBuilder npm = new StringBuilder(50);
        npm.append(getJavaHomeBinFile("npm") // NOI18N
                .getAbsolutePath());
        if (withParams) {
            npm.append(" --force"); // NOI18N
        }
        return npm.toString();
    }

    private static File getJavaHomeBinFile(String filename) {
        File javaHome = new File(System.getProperty("java.home"));
        // first, detect graalvm 0.20+
        File parent = javaHome.getParentFile();
        if (parent != null) {
            File graalvmHome = parent.getParentFile();
            if (graalvmHome != null) {
                File file = new File(new File(graalvmHome, "bin"), filename);
                if (file.isFile()) {
                    return file;
                }
            }
        }
        // legacy graalvm versions
        return new File(new File(javaHome, "bin"), filename); // NOI18N
    }

}
