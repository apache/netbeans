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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Arunava Sinha
 */
public final class SingleSourceFileUtil {
    public static final Logger LOG = Logger.getLogger(SingleSourceFileUtil.class.getPackage().getName());

    // TODO this checks the runtime JDK of NB!
    public static int findJavaVersion() throws NumberFormatException {
        // JEP-330 is supported only on JDK-11 and above.
        return Runtime.version().feature();
    }

    public static final String GLOBAL_VM_OPTIONS = "java_file_launcher_global_vm_options"; //NOI18N
    public static final String GLOBAL_STOP_AND_RUN_OPTION = "java_file_launcher_global_stop_and_run_option"; //NOI18N

    // synced with JavaNode
    public static final String FILE_ARGUMENTS = "single_file_run_arguments"; //NOI18N
    public static final String FILE_JDK = "single_file_run_jdk"; //NOI18N
    public static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N
    public static final String FILE_REGISTER_ROOT = "register_root"; //NOI18N

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
    public static Process compileJavaSource(FileObject fileObject, JavaPlatform jdk) {
        FileObject javac = jdk.findTool("javac"); //NOI18N
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
        String globalVmOptions = NbPreferences.forModule(JavaPlatformManager.class).get(GLOBAL_VM_OPTIONS, "").trim(); // NOI18N
        if (!globalVmOptions.isEmpty()) {
            compileCommandList.addAll(Arrays.asList(globalVmOptions.split(" "))); //NOI18N
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

    public static ParsedFileOptions getOptionsFor(FileObject file) {
        if (MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT) {
            return null;
        }

        for (SingleFileOptionsQueryImplementation  i : Lookup.getDefault().lookupAll(SingleFileOptionsQueryImplementation.class)) {
            SingleFileOptionsQueryImplementation.Result r = i.optionsFor(file);

            if (r != null) {
                return new ParsedFileOptions(r);
            }
        }

        return null;
    }

    public static List<String> parseLine(String line, URI workingDirectory) {
        return PARSER.doParse(line, workingDirectory);
    }

    public static boolean isTrue(Object value) {
        return value instanceof Boolean b && b;
    }

    private static final LineParser PARSER = new LineParser();

    private static class LineParser extends CompilerOptionsQueryImplementation.Result {
        public List<String> doParse(String line, URI workingDirectory) {
            return parseLine(line, workingDirectory);
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

    public static final class ParsedFileOptions extends CompilerOptionsQueryImplementation.Result implements ChangeListener {

        private final ChangeSupport cs;
        private final SingleFileOptionsQueryImplementation.Result delegate;
        private final AtomicInteger updateCount = new AtomicInteger(0);
        private List<? extends String> arguments;

        private ParsedFileOptions(SingleFileOptionsQueryImplementation.Result delegate) {
            this.cs = new ChangeSupport(this);
            this.delegate = delegate;
            this.delegate.addChangeListener(this);
        }

        @Override
        public List<? extends String> getArguments() {
            int update;
            synchronized (this) {
                if (arguments != null) {
                    return arguments;
                }

                update = updateCount.get();
            }

            while (true) {
                List<String> newArguments =
                        Collections.unmodifiableList(parseLine(delegate.getOptions(),
                                                               delegate.getWorkDirectory()));

                synchronized (this) {
                    if (update == updateCount.get()) {
                        arguments = newArguments;
                        return newArguments;
                    }

                    //changed in the mean time, try again:
                    update = updateCount.get();
                }
            }
        }

        public URI getWorkDirectory() {
            return delegate.getWorkDirectory();
        }

        public boolean registerRoot() {
            return delegate.registerRoot();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            synchronized (this) {
                arguments = null;
                updateCount.incrementAndGet();
            }

            cs.fireChange();
        }
    }
}
