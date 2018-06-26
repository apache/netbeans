/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
