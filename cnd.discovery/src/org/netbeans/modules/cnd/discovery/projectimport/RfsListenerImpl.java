/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.api.RfsListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

/**
 *
 */
final class RfsListenerImpl implements RfsListener {
    private final Map<String, File> storage = new HashMap<>();
    private final ExecutionEnvironment execEnv;

    RfsListenerImpl(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    @Override
    public void fileChanged(ExecutionEnvironment env, File localFile, String remotePath) {
        if (env.equals(execEnv)) {
            storage.put(remotePath, localFile);
        }
    }

    void download() {
        Map<String, File> copy = new HashMap<>(storage);
        for (Map.Entry<String, File> entry : copy.entrySet()) {
            downloadImpl(entry.getKey(), entry.getValue());
        }
    }

    private void downloadImpl(String remoteFile, File localFile) {
        try {
            Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, execEnv, localFile.getAbsolutePath(), null);
            if (ImportProject.TRACE) {
                ImportProject.logger.log(Level.INFO, "#download file {0}", localFile.getAbsolutePath()); // NOI18N
            }
            task.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
