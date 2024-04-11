/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

package org.netbeans.modules.gradle.output;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.output.OutputDisplayer;
import org.netbeans.modules.gradle.api.output.OutputListeners;
import org.netbeans.modules.gradle.api.output.OutputProcessor;
import org.netbeans.modules.gradle.api.output.OutputProcessorFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.IOColors;
import static org.openide.windows.IOColors.OutputType.LOG_DEBUG;
import static org.openide.windows.IOColors.OutputType.LOG_FAILURE;
import static org.openide.windows.IOColors.OutputType.LOG_SUCCESS;
import static org.openide.windows.IOColors.OutputType.LOG_WARNING;

/**
 *
 * @author eniko
 */
@ProjectServiceProvider(service = OutputProcessorFactory.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleProcessorFactory implements OutputProcessorFactory {

    @Override
    public Set<? extends OutputProcessor> createOutputProcessors(RunConfig cfg) {
        return Set.of(
                GRADLE_PROCESSOR,
                JAVAC_PROCESSOR,
                GROOVYC_PROCESSOR,
                new WarningModeAllProcessor(cfg)
        );
    }


    public static final OutputProcessor GRADLE_PROCESSOR = new OutputProcessor() {

        private static final Pattern GRADLE_ERROR = Pattern.compile("(Build file|Script) '(.*)\\.gradle' line: ([0-9]+)");

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = GRADLE_ERROR.matcher(line);
            if (m.matches()) {
                String buildFile = m.group(2);
                String lineNum = m.group(3);

                File gradleFile = FileUtil.normalizeFile(new File( buildFile + ".gradle"));
                FileObject file = FileUtil.toFileObject(gradleFile);
                int l = 1;
                try {
                    l = Integer.parseInt(lineNum);
                } catch (NumberFormatException ex) {
                }
                out.print("Script '" + buildFile + ".gradle' line: " + lineNum, OutputListeners.openFileAt(file, l, 1));
                return true;
            }
            return false;
        }
    };

    public static final OutputProcessor JAVAC_PROCESSOR = new OutputProcessor() {

        private static final Pattern JAVA_ERROR = Pattern.compile("(.*)\\.java\\:([0-9]+)\\: (error|warning)\\:(.*)");

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = JAVA_ERROR.matcher(line);
            if (m.matches()) {
                String classBase = m.group(1);
                String lineNum = m.group(2);
                String type = m.group(3);
                String text = m.group(4);

                File javaFile = FileUtil.normalizeFile(new File(classBase + ".java"));
                FileObject file = FileUtil.toFileObject(javaFile);
                int l = 1;
                try {
                    l = Integer.parseInt(lineNum);
                } catch (NumberFormatException ex) {
                }
                out.print(classBase + ".java:" + lineNum, OutputListeners.openFileAt(file, l, 1));
                out.print(": " + type + ":" + text);
                return true;
            }
            return false;
        }
    };

    public static final OutputProcessor GROOVYC_PROCESSOR = new OutputProcessor() {

        private static final Pattern GROOVY_ERROR = Pattern.compile("(.*)\\.groovy\\: ([0-9]+)\\: (.+)");
        private static final Pattern COLUMN_INFO = Pattern.compile(" @ line ([0-9]+), column ([0-9]+)\\.$");

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = GROOVY_ERROR.matcher(line);
            if (m.matches()) {
                String classBase = m.group(1);
                String text = m.group(3);
                String lineNum = m.group(2);
                String colNum = "1";

                Matcher colm = COLUMN_INFO.matcher(text);
                if (colm.find()) {
                    lineNum = colm.group(1);
                    colNum = colm.group(2);
                }

                File groovyFile = FileUtil.normalizeFile(new File(classBase + ".groovy"));
                FileObject file = FileUtil.toFileObject(groovyFile);
                int l = 1;
                int c = 1;
                try {
                    l = Integer.parseInt(lineNum);
                    c = Integer.parseInt(colNum);
                } catch (NumberFormatException ex) {
                }
                out.print(classBase + ".groovy: " + lineNum, OutputListeners.openFileAt(file, l, c));
                out.print(": " + text);
                return true;
            }
            return false;
        }

    };

    public static final OutputProcessor URL_PROCESSOR = new OutputProcessor() {

        private static final Pattern URL_PATTERN = Pattern.compile("(((https?|ftp|file)://|file:/)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = URL_PATTERN.matcher(line);
            int last = 0;
            while (m.find()) {
                String txt = line.substring(last, m.start());
                String lnk = line.substring(m.start(), m.end());
                last = m.end();
                out.print(txt);
                try {
                    out.print(lnk, OutputListeners.openURL(new URL(lnk)));
                } catch(MalformedURLException ex) {
                    out.print(lnk);
                }
            }
            if (last > 0) {
                out.print(line.substring(last));
            }
            return last > 0;
        }

    };

    public static final OutputProcessor TASK_LINE_PROCESSOR = new OutputProcessor() {
        final Pattern TASK_LINE = Pattern.compile("> Task (:[\\w:\\-]+)( [\\w\\-]+)?"); //NOI18N
        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = TASK_LINE.matcher(line);
            boolean ret = m.matches();
            if (ret) {
                String task = m.group(1);
                String state = m.group(2);
                out.print("> Task "); //NOI18N
                out.print(task);
                if (state != null) {
                    IOColors.OutputType type = LOG_WARNING;
                    if (state.endsWith("EXECUTED") || state.endsWith("UP-TO-DATE") || state.endsWith("FROM-CACHE")) { //NOI18N
                        type = LOG_SUCCESS;
                    } else if (state.endsWith("FAILED")) { //NOI18N
                        type = LOG_FAILURE;
                    }
                    out.print(state , null, type);
                }
            }
            return ret;
        }
    };

    public static final OutputProcessor STATIC_STRING_PROCESSOR = new OutputProcessor() {
        private static final String BUILD_FAILED_MSG = "BUILD FAILED"; //NOI18N
        private static final String BUILD_SUCCESS_MSG = "BUILD SUCCESSFUL"; //NOI18N
        private static final String COD_INCUBATION_MSG = "Configuration on demand is an incubating feature."; //NOI18N
        private static final String CONFIG_CACHE_MGS = "Configuration cache "; //NOI18N

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            IOColors.OutputType type = null;
            if (line.startsWith(COD_INCUBATION_MSG) || line.startsWith(CONFIG_CACHE_MGS)) {
                type = LOG_DEBUG;
            } else if (line.startsWith(BUILD_SUCCESS_MSG)) {
                type = LOG_SUCCESS;
            } else if (line.startsWith(BUILD_FAILED_MSG)) {
                type = LOG_FAILURE;
            }
            if (type != null) {
                out.print(line, null, type);
            }
            return type != null;
        }
    };

    static class WarningModeAllProcessor implements OutputProcessor {

        private static Pattern WARNING_MODE_ALL = Pattern.compile("(.+ ')(\\-\\-warning\\-mode all)('.+)");
        final RunConfig cfg;

        public WarningModeAllProcessor(RunConfig cfg) {
            this.cfg = cfg;
        }

        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher m = WARNING_MODE_ALL.matcher(line);
            if (m.matches()) {
                out.print(m.group(1));
                out.print(m.group(2), () -> {
                    GradleCommandLine cmd = new GradleCommandLine(cfg.getCommandLine());
                    cmd.addParameter(GradleCommandLine.Parameter.WARNING_MODE, "all");

                    RunConfig warnAll = cfg.withCommandLine(cmd);
                    RunUtils.executeGradle(warnAll, null);
                });
                out.print(m.group(3));
                return true;
            }
            return false;
        }

    }
}
