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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Arunava Sinha
 */
final class SingleSourceFileUtil {
    static final Logger LOG = Logger.getLogger(SingleSourceFileUtil.class.getPackage().getName());

    static int findJavaVersion() throws NumberFormatException {
        // JEP-330 is supported only on JDK-11 and above.
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N
        if (javaVersion.startsWith("1.")) { //NOI18N
            javaVersion = javaVersion.substring(2);
        }
        int version = Integer.parseInt(javaVersion);
        return version;
    }

    public static final String FILE_ARGUMENTS = "single_file_run_arguments"; //NOI18N
    public static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N

    static FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            if (isSingleSourceFile(fObj)) {
                return fObj;
            }
        }
        for (FileObject fObj : lookup.lookupAll(FileObject.class)) {
            if (isSingleSourceFile(fObj)) {
                return fObj;
            }
        }
        return null;
    }

    static boolean isSingleSourceFile(FileObject fObj) {
        Project p = FileOwnerQuery.getOwner(fObj);
        if (p != null || !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        return true;
    }

    static Process compileJavaSource(FileObject fileObject) {
        FileObject javac = JavaPlatformManager.getDefault().getDefaultPlatform().findTool("javac"); //NOI18N
        File javacFile = FileUtil.toFile(javac);
        String javacPath = javacFile.getAbsolutePath();
        List<String> compileCommandList = new ArrayList<>();
        Object compilerVmOptionsObj = fileObject.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
        compileCommandList.add(javacPath);
        compileCommandList.add("-g"); //NOI18N
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
            LOG.log(Level.WARNING, "Could not get InputStream of Compile Process"); //NOI18N
        }
        return null;
    }

    static boolean hasClassSibling(FileObject fo) {
        return fo.getParent().getFileObject(fo.getName(), "class") != null;
    }

}
