/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cpplite.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cpplite.debugger.api.Debugger;
import org.netbeans.modules.cpplite.project.runner.Runner;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author lahvac
 */
public class ActionProviderImpl implements ActionProvider {
    private static final String[] SUPPORTED_ACTIONS = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,
    };

    private final CPPLiteProject prj;

    public ActionProviderImpl(CPPLiteProject prj) {
        this.prj = prj;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    private static final Pattern ERROR_LINE = Pattern.compile("(.*):(\\d+):(\\d+):.*");

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        BuildConfiguration config = prj.getActiveBuildConfiguration();
        File module = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-cpplite-project.jar", "org.netbeans.modules.cpplite.project", false);
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .showProgress(true)
                .showSuspended(true)
                .frontWindowOnError(true)
                .controllable(true)
                .errConvertorFactory(() -> new ErrorLineConvertor())
                .outConvertorFactory(() -> new ErrorLineConvertor());
        ExecutionService.newService(() -> {
            LifecycleManager.getDefault().saveAll();
            if (COMMAND_DEBUG.equals(command)) {
                List<List<String>> executablesFor = config.executablesFor(COMMAND_RUN);
                return Debugger.startInDebugger(executablesFor.get(0), FileUtil.toFile(prj.getProjectDirectory()));
            }
            List<List<String>> executablesFor;
            if (COMMAND_REBUILD.equals(command)) {
                executablesFor = new ArrayList<>();
                executablesFor.addAll(config.executablesFor(COMMAND_CLEAN));
                executablesFor.addAll(config.executablesFor(COMMAND_BUILD));
            } else {
                executablesFor = config.executablesFor(command);
            }
            String arg = executablesFor.stream().map(c -> quote(c.stream().map(p -> quote(p)).collect(Collectors.joining(" ")))).collect(Collectors.joining(" "));
            return new ProcessBuilder("java", "-classpath", module.getAbsolutePath(), Runner.class.getName(), arg).directory(FileUtil.toFile(prj.getProjectDirectory())).start();
        }, executionDescriptor, ProjectUtils.getInformation(prj).getDisplayName() + " - " + command).run();
    }

    private static String quote(String s) {
        return s.replace("_", "_u_").replace(" ", "_s_");
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DEBUG.equals(command)) {
            return isActionEnabled(COMMAND_RUN, context);
        } else if (COMMAND_REBUILD.equals(command)) {
            return isActionEnabled(COMMAND_CLEAN, context) && isActionEnabled(COMMAND_BUILD, context);
        } else {
            return prj.getActiveBuildConfiguration().executablesFor(command) != null;
        }
    }

    private static class ErrorLineConvertor implements LineConvertor {

        @Override
        public List<ConvertedLine> convert(String line) {
            Matcher matcher = ERROR_LINE.matcher(line);
            if (matcher.matches()) {
                String fileName = matcher.group(1);
                int lineNum = Integer.parseInt(matcher.group(2)) - 1;
                int columnNum = Integer.parseInt(matcher.group(3)) - 1;
                return Collections.singletonList(ConvertedLine.forText(line, new OutputListener() {
                    @Override
                    public void outputLineSelected(OutputEvent ev) {}
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        FileObject file = FileUtil.toFileObject(new File(fileName));
                        if (file == null) {
                            //TODO
                            return;
                        }
                        LineCookie lc = file.getLookup().lookup(LineCookie.class);
                        lc.getLineSet().getCurrent(lineNum).show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, columnNum);
                    }
                    @Override
                    public void outputLineCleared(OutputEvent ev) {}
                }));
            }
            return Collections.singletonList(ConvertedLine.forText(line, null));
        }
    }

}
