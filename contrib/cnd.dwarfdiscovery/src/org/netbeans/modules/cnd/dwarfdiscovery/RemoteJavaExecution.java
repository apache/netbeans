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

package org.netbeans.modules.cnd.dwarfdiscovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.dwarfdump.CompileLineService;
import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.LddService;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
public class RemoteJavaExecution {
    private final ExecutionEnvironment env;
    private final FileSystem fileSystem;
    private static final RequestProcessor RP = new RequestProcessor("ReadErrorStream", 2); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.remote.projectui.wizard.cnd.dwarf"); // NOI18N
    private static final Map<ExecutionEnvironment, FileObject> copyedJars = new HashMap<ExecutionEnvironment, FileObject>();
    
    public RemoteJavaExecution(FileSystem fileSystem) {
        this.env = FileSystemProvider.getExecutionEnvironment(fileSystem);
        this.fileSystem = fileSystem;
    }
    
    public List<SourceFile> getCompileLines(String executable, boolean transferDwarf) {
        NativeProcess process = null;
        Task errorTask = null;
        try {
            if (transferDwarf) {
                process = getJavaProcess(CompileLineService.class, env, new String[]{"-file", executable, "-dwarf"}); //NOI18N
            } else {
                process = getJavaProcess(CompileLineService.class, env, new String[]{"-file", executable}); //NOI18N
            }
            if (process == null) {
                return null;
            }
            if (process.getState() != State.ERROR){
                final NativeProcess startedProcess = process;
                final List<String> errors = new ArrayList<String>();
                errorTask = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            errors.addAll(ProcessUtils.readProcessError(startedProcess));
                        } catch (Throwable ex) {
                        }
                    }
                });

                BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("UTF-8"))); // NOI18N
                List<SourceFile> sourceProperties = CompileLineService.getSourceProperties(out);

                int rc = process.waitFor();
                logger.log(Level.FINE, "Return code {0}", rc); // NOI18N
                boolean hasException = false;
                for(String error : errors) {
                    if (error.indexOf("Exception") >= 0) { // NOI18N
                        hasException = true;
                    }
                    logger.log(Level.INFO, error); // NOI18N
                }
                if (rc == 0 && !hasException) {
                    logger.log(Level.FINE, "Read debug infirmation of {0} compilation units from executable file {1}", new Object[]{sourceProperties.size(), executable}); // NOI18N
                    return sourceProperties;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
        } catch (Throwable ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            if (errorTask != null){
                errorTask.cancel();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }
    
    public SharedLibraries getDlls(String executable) {
        NativeProcess process = null;
        Task errorTask = null;
        try {
            process = getJavaProcess(LddService.class, env, new String[]{executable});
            if (process == null) {
                return null;
            }
            if (process.getState() != State.ERROR){
                final NativeProcess startedProcess = process;
                final List<String> errors = new ArrayList<String>();
                errorTask = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            errors.addAll(ProcessUtils.readProcessError(startedProcess));
                        } catch (Throwable ex) {
                        }
                    }
                });

                BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("UTF-8"))); // NOI18N
                SharedLibraries pubNames = LddService.getPubNames(out);

                int rc = process.waitFor();
                logger.log(Level.FINE, "Return code {0}", rc); // NOI18N
                boolean hasException = false;
                for(String error : errors) {
                    if (error.indexOf("Exception") >= 0) { // NOI18N
                        hasException = true;
                    }
                    logger.log(Level.INFO, error); // NOI18N
                }
                if (rc == 0 && !hasException) {
                    logger.log(Level.FINE, "Read dlls infirmation from executable file {0}", new Object[]{executable}); // NOI18N
                    return pubNames;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
        } catch (Throwable ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            if (errorTask != null){
                errorTask.cancel();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }

        
    private FileObject copyJar() {
        try {
            String path =  Offset2LineService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            path = path.replace('\\', '/'); // NOI18N
            path = path.substring(path.lastIndexOf("/modules/")+1); // NOI18N
            if (path.indexOf('!') > 0) {
                path = path.substring(0, path.indexOf('!')); // NOI18N
            }
            String relPath = path;
            File jar = InstalledFileLocator.getDefault().locate(relPath, "org.netbeans.modules.cnd.dwarfdump", false); //NOI18N
            if (jar != null) {
                FileObject from = FileUtil.toFileObject(jar);
                synchronized (copyedJars) {
                    FileObject to = copyedJars.get(env);
                    if (to != null && to.isValid()) {
                        return to;
                    }
                    FileObject tempFolder = fileSystem.getTempFolder();
                    to = fileSystem.createTempFile(tempFolder, "dwarfdump", ".jar", true); //NOI18N
                    final OutputStream outputStream = to.getOutputStream();
                    final InputStream inputStream = from.getInputStream();
                    FileUtil.copy(inputStream, outputStream);
                    outputStream.close();
                    inputStream.close();
                    copyedJars.put(env, to);
                    return to;
                }
            }
        } catch (Throwable thr) {
            logger.log(Level.INFO, thr.getMessage(), thr);
        }
        return null;
    }


    private NativeProcess getJavaProcess(Class<?> clazz, ExecutionEnvironment env, String[] arguments) throws IOException{
        FileObject dwarfDump = copyJar();
        if (dwarfDump == null) {
            return null;
        }
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setCharset(Charset.forName("UTF-8")); // NOI18N
        npb.setExecutable("java"); //NOI18N
        List<String> args = new ArrayList<String>();
        args.add("-cp"); //NOI18N
        args.add(dwarfDump.getPath());
        args.add(clazz.getName());
        args.addAll(Arrays.asList(arguments));
        npb.setArguments(args.toArray(new String[args.size()]));
        return npb.call();
    }

    public String getSourceRoot(List<SourceFile> compileLines) {
        TreeMap<String,AtomicInteger> realRoots = new TreeMap<String,AtomicInteger>();
        Driver driver = DriverFactory.getDriver(null);
        for(SourceFile file : compileLines) {
            if (file.getCommandLine().length() > 0) {
                Artifacts artifacts = driver.gatherCompilerLine(file.getCommandLine(), CompileLineOrigin.DwarfCompileLine, true); //NOI18N
                for(String what : artifacts.getInput()) {
                    if (what == null){
                        continue;
                    }
                    String path;
                    String dir = file.getCompilationDir();
                    if (dir != null) {
                        if (what.startsWith("/")) { //NOI18N
                            path = what;
                        } else {
                            if (dir.endsWith("/")) { // NOI18N
                                path = dir+what;
                            } else {
                                path = dir+ '/' + what;
                            }
                        }
                    } else {
                        path = what;
                    }
                    path = PathUtilities.normalizeUnixPath(path);
                    int i = path.lastIndexOf('/');
                    if (i >= 0) {
                        String folder = path.substring(0, i);
                        AtomicInteger val = realRoots.get(folder);
                        if (val == null) {
                            val = new AtomicInteger();
                            realRoots.put(folder, val);
                        }
                        val.incrementAndGet();
                    }
                }
            } else {
                String path = file.getSourceFileAbsolutePath();
                path = PathUtilities.normalizeUnixPath(path);
                int i = path.lastIndexOf('/');
                if (i >= 0) {
                    String folder = path.substring(0, i);
                    AtomicInteger val = realRoots.get(folder);
                    if (val == null) {
                        val = new AtomicInteger();
                        realRoots.put(folder, val);
                    }
                    val.incrementAndGet();
                }
            }
        }
        return getRoot(realRoots);
    }
    
    private String getRoot(TreeMap<String,AtomicInteger> roots) {
        ArrayList<String> res = new ArrayList<String>();
        ArrayList<AtomicInteger> resCount = new ArrayList<AtomicInteger>();
        String current = null;
        AtomicInteger currentCount = null;
        for(Map.Entry<String,AtomicInteger> entry : roots.entrySet()) {
            if (current == null) {
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
                continue;
            }
            String s = getCommonPart(entry.getKey(), current);
            String[] split = s.split("/"); // NOI18N
            int length = (split.length > 0 && split[0].isEmpty()) ? split.length - 1 : split.length;
            if (length >= 2) {
                current = s;
                currentCount.addAndGet(entry.getValue().get());
            } else {
                res.add(current);
                resCount.add(currentCount);
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
            }
        }
        if (current != null) {
            res.add(current);
            resCount.add(currentCount);
        }
        TreeMap<String,AtomicInteger> newRoots = new TreeMap<String, AtomicInteger>();
        String bestRoot = null;
        int bestCount = 0;
        for(int i = 0; i < res.size(); i++) {
            newRoots.put(res.get(i), resCount.get(i));
            if (bestRoot == null) {
                bestRoot = res.get(i);
                bestCount = resCount.get(i).get();
            } else {
                if (bestCount < resCount.get(i).get()) {
                    bestRoot = res.get(i);
                    bestCount = resCount.get(i).get();
                }
            }
        }
        return bestRoot;
    }
    
    private String getCommonPart(String path, String commonRoot) {
        String[] splitPath = path.split("/"); // NOI18N
        ArrayList<String> list1 = new ArrayList<String>();
        boolean isUnixPath = false;
        for (int i = 0; i < splitPath.length; i++) {
            if (!splitPath[i].isEmpty()) {
                list1.add(splitPath[i]);
            } else {
                if (i == 0) {
                    isUnixPath = true;
                }
            }
        }
        String[] splitRoot = commonRoot.split("/"); // NOI18N
        ArrayList<String> list2 = new ArrayList<String>();
        boolean isUnixRoot = false;
        for (int i = 0; i < splitRoot.length; i++) {
            if (!splitRoot[i].isEmpty()) {
                list2.add(splitRoot[i]);
            } else {
                if (i == 0) {
                    isUnixRoot = true;
                }
            }
        }
        if (isUnixPath != isUnixRoot) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        if (isUnixPath) {
            buf.append('/');
        }
        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            if (list1.get(i).equals(list2.get(i))) {
                if (i > 0) {
                    buf.append('/');
                }
                buf.append(list1.get(i));
            } else {
                break;
            }
        }
        return buf.toString();
    }
}
