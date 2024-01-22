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
package org.netbeans.modules.java.file.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation.Result;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Arunava Sinha
 */
public final class SingleSourceFileUtil {
    public static final Logger LOG = Logger.getLogger(SingleSourceFileUtil.class.getPackage().getName());

    public static int findJavaVersion() throws NumberFormatException {
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

    public static FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
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

    public static boolean isSingleSourceFile(FileObject fObj) {
        if (!isSupportedFile(fObj) || !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        return true;
    }

    public static boolean isSupportedFile(FileObject file) {
        if (file == null) {
            return false;
        }
        try {
            FileObject dir = file.getParent();
            File dirFile = dir != null ? FileUtil.toFile(dir) : null;
            return !MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT
                    && FileOwnerQuery.getOwner(file) == null
                    && !file.getFileSystem().isReadOnly()
                    && !(dirFile != null
                    && dirFile.getName().startsWith("vcs-")
                    && dirFile.getAbsolutePath().startsWith(System.getProperty("java.io.tmpdir")));
        } catch (FileStateInvalidException ex) {
            return false;
        }
    }
    public static Process compileJavaSource(FileObject fileObject) {
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

    public static boolean hasClassSibling(FileObject fo) {
        return fo.getParent().getFileObject(fo.getName(), "class") != null;
    }

    public static Result getOptionsFor(FileObject file) {
        if (MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT) {
            return null;
        }

        for (SingleFileOptionsQueryImplementation  i : Lookup.getDefault().lookupAll(SingleFileOptionsQueryImplementation.class)) {
            Result r = i.optionsFor(file);

            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public static List<String> parseLine(String line) {
        return PARSER.doParse(line);
    }

    private static final LineParser PARSER = new LineParser();

    private static class LineParser extends CompilerOptionsQueryImplementation.Result {
        public List<String> doParse(String line) {
            return parseLine(line);
        }

        @Override
        public List<? extends String> getArguments() {
            return null;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {}

        @Override
        public void removeChangeListener(ChangeListener listener) {}
    }

}
