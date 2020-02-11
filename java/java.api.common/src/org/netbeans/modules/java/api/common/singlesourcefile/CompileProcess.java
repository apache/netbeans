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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Arunava Sinha
 */
class CompileProcess {

    private static final Logger LOG = Logger.getLogger(CompileProcess.class.getName());

    public Process setupProcess(FileObject fileObject) {

        FileObject javac = JavaPlatformManager.getDefault().getDefaultPlatform().findTool("javac"); //NOI18N
        File javacFile = FileUtil.toFile(javac);
        String javacPath = javacFile.getAbsolutePath();

        List<String> compileCommandList = new ArrayList<>();

        Object compilerVmOptionsObj = fileObject.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
        compileCommandList.add(javacPath);
        compileCommandList.add("-g");  //NOI18N

        String vmOptions = compilerVmOptionsObj != null ? ((String) compilerVmOptionsObj).trim() : ""; // NOI18N
        if (!vmOptions.isEmpty()) {
            compileCommandList.addAll(Arrays.asList(vmOptions.split(" "))); //NOI18N
        }

        compileCommandList.add(fileObject.getPath());
        ProcessBuilder compileProcessBuilder = new ProcessBuilder(compileCommandList);
        compileProcessBuilder.directory(new File(fileObject.getParent().getPath()));
        compileProcessBuilder.redirectErrorStream(true);
        compileProcessBuilder.redirectOutput();

        try {
            return compileProcessBuilder.start();
        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Could not get InputStream of Compile Process"); //NOI18N
        }
        return null;
    }
}
