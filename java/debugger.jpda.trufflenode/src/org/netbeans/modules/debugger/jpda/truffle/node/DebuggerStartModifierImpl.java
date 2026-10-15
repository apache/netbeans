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
package org.netbeans.modules.debugger.jpda.truffle.node;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.api.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifier;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@NbBundle.Messages("DESC_DebugGraalNode=Debug GraalVM Node.js")
public class DebuggerStartModifierImpl implements DebuggerStartModifier {

    @NbBundle.Messages("CTL_DebugName=GraalVM node Debugger")
    @Override
    public List<String> getArguments(Lookup context) {
        Project p = context.lookup(Project.class);
        if (p == null) {
            return Collections.emptyList();
        }
        NodeJsSupport s = NodeJsSupport.getInstance();
        if (!s.isEnabled(p)) {
            return Collections.emptyList();
        }
        final String node = s.getNode(p);
        File nodeFile = new File(node);
        nodeFile = FileUtil.normalizeFile(nodeFile);
        FileObject nodeFO = FileUtil.toFileObject(nodeFile);
        if (nodeFO == null) {
            return Collections.emptyList();
        }
        FileObject bin = nodeFO.getParent();
        if (bin == null || !isJavaPlatformBinDir(bin)) {
            return Collections.emptyList();
        }
        final String debugName = Bundle.CTL_DebugName();

        InputOutput io = IOProvider.getDefault().getIO(debugName, false);
        FileObject jdk = bin.getParent();
        if (jdk.getName().equals("jre")) {
            jdk = jdk.getParent();
        }
        JPDAStart start = new JPDAStart(io, debugName, jdk);
        String res = null;
        try {
            res = start.execute(p);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
        return Arrays.asList("--jvm.agentlib:jdwp=transport=dt_socket,address=" + res + ",server=n,suspend=y");
    }

    private static boolean isJavaPlatformBinDir(FileObject dir) {
        if (!"bin".equals(dir.getNameExt())) {
            return false;
        }
        FileObject file = dir.getFileObject("java", BaseUtilities.isWindows() ? "exe" : null);
        if (file == null) {
            return false;
        }
        file = dir.getFileObject("node", BaseUtilities.isWindows() ? "exe" : null);
        return file != null;
    }

    @Override
    public void processOutputLine(String line) {
    }

    @Override
    public boolean startProcessingDone() {
        return true;
    }
}
