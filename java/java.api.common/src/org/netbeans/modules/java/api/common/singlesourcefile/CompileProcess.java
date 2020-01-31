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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Arunava Sinha
 */
class CompileProcess  {

    private static final Logger LOG = Logger.getLogger(CompileProcess.class.getName());
    FileObject fileObject;

    
    public CompileProcess(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public Process setupProcess() {
        File javaBinPath = new File(new File(System.getProperty("java.home")), "bin");  //NOI18N
        String javaPath = javaBinPath.getAbsolutePath() + "//java";  //NOI18N

        String javacPath = javaBinPath.getAbsolutePath() + "//javac";  //NOI18N
        List<String> compileCommandList = new ArrayList<>();
        if (Utilities.isUnix()) {
            compileCommandList.add("bash"); //NOI18N
            compileCommandList.add("-c"); //NOI18N
        }

        Object compilerVmOptionsObj = fileObject.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
        String vmOptions = compilerVmOptionsObj != null ? (String) compilerVmOptionsObj : "";

        compileCommandList.add(javacPath + " -g" + " " + vmOptions + " " + fileObject.getPath());
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
