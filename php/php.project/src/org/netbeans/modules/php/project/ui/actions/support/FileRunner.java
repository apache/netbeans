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
package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Run or debug a file.
 * <p>
 * This class is thread safe.
 */
public final class FileRunner {

    static final Logger LOGGER = Logger.getLogger(FileRunner.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(FileRunner.class);
    public static final ExecutionDescriptor.LineConvertorFactory PHP_LINE_CONVERTOR_FACTORY = new PhpLineConvertorFactory();

    final File file;

    volatile PhpProject project;
    volatile String command;
    volatile String phpArgs;
    volatile String fileArgs;
    volatile String workDir;
    volatile boolean debug = false;


    public FileRunner(File file) {
        this.file = file;
    }

    public FileRunner project(PhpProject project) {
        this.project = project;
        return this;
    }

    public FileRunner command(String command) {
        this.command = command;
        return this;
    }

    public FileRunner phpArgs(String phpArgs) {
        this.phpArgs = phpArgs;
        return this;
    }

    public FileRunner fileArgs(String fileArgs) {
        this.fileArgs = fileArgs;
        return this;
    }

    public FileRunner workDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.run.displayName=Run ({0})",
    })
    public void run() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                PhpExecutable executable = getExecutable(Bundle.FileRunner_run_displayName(getDisplayName()));
                executable.run(getDescriptor(getPostExecution(executable)));
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.debug.displayName=Debug ({0})",
    })
    public void debug() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                PhpExecutable executable = getExecutable(Bundle.FileRunner_debug_displayName(getDisplayName()));
                try {
                    executable.debug(FileUtil.toFileObject(file), getDescriptor(getPostExecution(executable)));
                } catch (ExecutionException ex) {
                    UiUtils.processExecutionException(ex);
                }
            }
        });
    }

    PhpExecutable getExecutable(String displayName) {
        PhpExecutable executable = new PhpExecutable(command);
        if (StringUtils.hasText(workDir)) {
            executable.workDir(new File(workDir));
        } else {
            executable.workDir(file.getParentFile());
        }
        executable
                .displayName(displayName)
                .viaAutodetection(false)
                .viaPhpInterpreter(false)
                .additionalParameters(getParams());
        return executable;
    }

    String getDisplayName() {
        return project != null ? project.getName() : file.getName();
    }

    private List<String> getParams() {
        List<String> params = new ArrayList<>();
        if (StringUtils.hasText(phpArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(phpArgs)));
        }
        params.add(file.getAbsolutePath());
        if (StringUtils.hasText(fileArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(fileArgs)));
        }
        return params;
    }

    ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .charset(Charset.forName(getEncoding()))
                .controllable(true)
                .optionsPath(UiUtils.OPTIONS_PATH)
                .outConvertorFactory(PHP_LINE_CONVERTOR_FACTORY);
        if (!getPhpOptions().isOpenResultInOutputWindow()) {
            descriptor = descriptor.inputOutput(InputOutput.NULL)
                    .frontWindow(false)
                    .frontWindowOnError(false);
        }
        if (postExecution != null) {
            descriptor = descriptor.postExecution(postExecution);
        }
        return descriptor;
    }

    private Runnable getPostExecution(PhpExecutable executable) {
        // open in browser or editor?
        if (getRedirectToFile()) {
            File tmpFile = createTempFile();
            if (tmpFile != null) {
                String charset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(file)).name();
                executable.fileOutput(tmpFile, charset, false);
                return new PostExecution(tmpFile);
            }
        }
        return null;
    }

    private String getEncoding() {
        return project != null ? ProjectPropertiesSupport.getEncoding(project) : FileEncodingQuery.getDefaultEncoding().name();
    }

    boolean getRedirectToFile() {
        return getPhpOptions().isOpenResultInBrowser() || getPhpOptions().isOpenResultInEditor();
    }

    File createTempFile() {
        try {
            File tmpFile = Files.createTempFile(file.getName(), ".html").toFile(); // NOI18N
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    private PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

    //~ Inner classes

    private static final class PhpLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        @Override
        public LineConvertor newLineConvertor() {
            LineConvertor[] lineConvertors = new LineConvertor[PhpInterpreter.LINE_PATTERNS.length];
            int i = 0;
            for (Pattern linePattern : PhpInterpreter.LINE_PATTERNS) {
                lineConvertors[i++] = LineConvertors.filePattern(null, linePattern, null, 1, 2);
            }
            return LineConvertors.proxy(lineConvertors);
        }
    }

    private static final class PostExecution implements Runnable {

        private final File tmpFile;


        public PostExecution(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        @Override
        public void run() {
            PhpOptions options = PhpOptions.getInstance();
            try {
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(tmpFile).toURL());
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            if (options.isOpenResultInEditor()) {
                FileUtils.openFile(tmpFile);
            }
        }

    }

}
