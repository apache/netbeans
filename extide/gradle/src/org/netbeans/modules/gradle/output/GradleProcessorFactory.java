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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author eniko
 */
@ProjectServiceProvider(service = OutputProcessorFactory.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleProcessorFactory implements OutputProcessorFactory {

    private static final OutputProcessor URL_PROCESSOR = new URLOutputProcessor();
    private static final OutputProcessor GRADLE_PROCESSOR = new GradleOutputProcessor();
    private static final OutputProcessor JAVAC_PROCESSOR = new JavaCompilerProcessor();
    private static final OutputProcessor GROOVYC_PROCESSOR = new GroovyCompilerProcessor();


    @Override
    public Set<? extends OutputProcessor> createOutputProcessors(RunConfig cfg) {
        return new HashSet<>(Arrays.asList(URL_PROCESSOR, GRADLE_PROCESSOR, JAVAC_PROCESSOR, GROOVYC_PROCESSOR));
    }

    static final class URLOutputProcessor implements OutputProcessor {

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

    }

    static class GradleOutputProcessor implements OutputProcessor {

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
    }

    static class JavaCompilerProcessor implements OutputProcessor {

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

    }

    static class GroovyCompilerProcessor implements OutputProcessor {

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

    }

}
