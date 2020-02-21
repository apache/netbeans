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
package org.netbeans.modules.cnd.discovery.buildsupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 */
public class ToolsWrapperUtility {

    private static final Map<FileObject, CompilerSet> cache = new ConcurrentHashMap<FileObject, CompilerSet>();
    private static final String codeNameBase = "org.netbeans.modules.cnd.actions"; //NOI18N
    private static final String windowsWrapperResource = "bin/tools-wrapper/Windows/wrapper.exe"; //NOI18N
    private static final String unixWrapperResource = "bin/tools-wrapper/Unix/wrapper"; //NOI18N
    private static final String toolsWrapperLocation = "nbproject/private/tools/"; //NOI18N
    private static final String compilerPropertiesName = "compiler.properties"; //NOI18N
    private final ExecutionEnvironment execEnv;
    private final MakeConfiguration conf;
    private final Project project;

    public ToolsWrapperUtility(ExecutionEnvironment execEnv, MakeConfiguration conf, Project project) {
        this.execEnv = execEnv;
        this.conf = conf;
        this.project = project;
    }

    public CompilerSet getToolsWrapper() {
        try {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            String eeID = ExecutionEnvironmentFactory.toUniqueID(execEnv).replace(':', '_').replace('@', '_');
            if (conf.getFileSystemHost().equals(execEnv)) {
                // full remote project
                eeID = "localhost"; //NOI18N
            }
            FileObject wrapperFolder = FileUtil.createFolder(project.getProjectDirectory(), toolsWrapperLocation + eeID + "/" + compilerSet.getName()); //NOI18N
            CompilerSet res = cache.get(wrapperFolder);
            if (res != null) {
                return res;
            }
            String wrapperBinaryFile = getLocalFileLocationFor(execEnv);
            String cPath = null;
            Tool toolC = null;
            {
                toolC = compilerSet.getTool(PredefinedToolKind.CCompiler);
                if (toolC != null) {
                    cPath = toolC.getPath();
                    String cName = PathUtilities.getBaseName(cPath);
                    FileObject fo = wrapperFolder.getFileObject(cName);
                    if (fo == null) {
                        fo = copyFile(wrapperBinaryFile, wrapperFolder, cName);
                        if (fo != null) {
                            fixScript(fo, cPath);
                        }
                    }
                }
            }
            String cppPath = null;
            Tool toolCpp = null;
            {
                toolCpp = compilerSet.getTool(PredefinedToolKind.CCCompiler);
                if (toolCpp != null) {
                    cppPath = toolCpp.getPath();
                    String cppName = PathUtilities.getBaseName(cppPath);
                    FileObject fo = wrapperFolder.getFileObject(cppName);
                    if (fo == null) {
                        fo = copyFile(wrapperBinaryFile, wrapperFolder, cppName);
                        if (fo != null) {
                            fixScript(fo, cppPath);
                        }
                    }
                }
            }
            //writePropertiesFile(wrapperFolder, cPath, cppPath);
            ExecutionEnvironment projectFSenv = FileSystemProvider.getExecutionEnvironment(wrapperFolder);
            String path = wrapperFolder.getPath();
            if (!projectFSenv.equals(execEnv)) {
                RemotePathMap pathMap = RemotePathMap.getPathMap(execEnv);
                if (pathMap != null) {
                    String remotePath = pathMap.getRemotePath(path);
                    if (remotePath != null) {
                        path = remotePath;
                    }
                }
            }
            res = CompilerSetFactory.createCompilerSetWrapper(compilerSet, execEnv, path);
            cache.put(wrapperFolder, res);
            return res;
        } catch (MissingResourceException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        } catch (ExecutionException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    private String getLocalFileLocationFor(final ExecutionEnvironment env) throws MissingResourceException {
        String resource;
        if (env.isLocal() && Utilities.isWindows()) {
            resource = windowsWrapperResource;
        } else {
            resource = unixWrapperResource;
        }
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        File file = fl.locate(resource, codeNameBase, false);
        if (file == null || !file.exists()) {
            throw new MissingResourceException(resource, null, null);
        }
        return file.getAbsolutePath();
    }

    private void fixScript(FileObject f, String realTool) throws IOException {
        if (execEnv.isLocal() && Utilities.isWindows()) {
            byte[] content = f.asBytes();
            byte [] pattern = new byte[]{'e', 'c', 'h', 'o', ' ', 'm', 'a', 'g', 'i', 'c', 'e', 'c', 'h', 'o', ' ', 'm', 'a', 'g', 'i', 'c'};
            for(int i = 0; i < content.length - 1000; i++) {
                boolean find = true;
                for(int j = 0; j < pattern.length; j++) {
                    if (content[i+j] != pattern[j]) {
                        find = false;
                        break;
                    }
                }
                if (find) {
                    byte[] tool_bytes = realTool.getBytes();
                    for(int k = 0; k < tool_bytes.length && k < 1000; k++) {
                        content[i+k] = tool_bytes[k];
                    }
                    if (tool_bytes.length < 1000) {
                        content[i+tool_bytes.length] = 0;
                    }
                    OutputStream outputStream = f.getOutputStream();
                    outputStream.write(content);
                    outputStream.close();
                    break;
                }
            }
        } else {
            List<String> lines = new ArrayList<String>(f.asLines());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("real_tool=")) { //NOI18N
                    lines.set(i, "real_tool=" + realTool); //NOI18N
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(f.getOutputStream(), "UTF-8")) { //NOI18N
                @Override
                public void newLine() throws IOException {
                    write("\n"); //NOI18N
                }

            };
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
    }

//    private void writePropertiesFile(FileObject wrapperFolder, String cPath, String cppPath) throws IOException {
//        if (execEnv.isLocal() && Utilities.isWindows()) {
//            FileObject fo = wrapperFolder.getFileObject(compilerPropertiesName);
//            if (fo == null || !fo.isValid()) {
//                PrintStream stream = new PrintStream(wrapperFolder.createAndOpen(compilerPropertiesName));
//                if (cPath != null) {
//                    stream.println("C=" + cPath); //NOI18N
//                }
//                if (cppPath != null) {
//                    stream.println("CPP=" + cppPath); //NOI18N
//                }
//                stream.close();
//            }
//        }
//    }

    private FileObject copyFile(String wrapperBinaryFile, FileObject folder, String name) throws IOException, InterruptedException, ExecutionException {
        String ext = "";
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            ext = name.substring(i + 1);
            name = name.substring(0, i);
        }
        ExecutionEnvironment projectFSenv = FileSystemProvider.getExecutionEnvironment(folder);
        if (projectFSenv.isLocal()) {
            FileObject copyFile = FileUtil.copyFile(FileUtil.toFileObject(new File(wrapperBinaryFile)), folder, name, ext);
            Future<Integer> task = CommonTasksSupport.chmod(projectFSenv, copyFile.getPath(), 0755, null);
            Integer retCode = task.get();
            return copyFile;
        } else {
            Future<CommonTasksSupport.UploadStatus> uploadTask = CommonTasksSupport.uploadFile(wrapperBinaryFile, projectFSenv, folder.getPath() + "/" + name, 0755, true); //NOI18N
            CommonTasksSupport.UploadStatus status = uploadTask.get();
            if (!status.isOK()) {
                throw new IOException("Unable to upload " + wrapperBinaryFile + " to " + projectFSenv.getDisplayName() + ':' + folder.getPath() + "/" + name // NOI18N
                        + " rc=" + status.getExitCode() + ' ' + status.getError()); // NOI18N
            }
            folder.refresh();
            return folder.getFileObject(name);
        }
    }
}
