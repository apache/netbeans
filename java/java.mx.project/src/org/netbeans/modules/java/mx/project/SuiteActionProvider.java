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
package org.netbeans.modules.java.mx.project;

import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.LifecycleManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

final class SuiteActionProvider implements ActionProvider {
    private static final String TEST_MX_PATH = System.getProperty("org.netbeans.modules.java.mx.project.test.mxpath"); // NOI18N
    private static final RequestProcessor ASYNC = new RequestProcessor("Mx Async", 10);
    private static final List<String> SUPPORTED_ACTIONS = Arrays.asList(
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_BUILD,
        ActionProvider.COMMAND_COMPILE_SINGLE,
        ActionProvider.COMMAND_REBUILD,
        ActionProvider.COMMAND_TEST_SINGLE,
        ActionProvider.COMMAND_RUN_SINGLE,
        ActionProvider.COMMAND_DEBUG_TEST_SINGLE,
        ActionProvider.COMMAND_DEBUG_SINGLE,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD
    );
    private final SuiteProject prj;

    SuiteActionProvider(SuiteProject prj) {
        this.prj = prj;
    }

    @Override
    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS.toArray(new String[0]);
    }

    @NbBundle.Messages({
        "# {0} - name of mx suite",
        "MSG_Clean=mx clean {0}",
        "# {0} - name of mx suite",
        "MSG_Build=mx build {0}",
        "# {0} - name of mx suite",
        "# {1} - name of source group",
        "MSG_BuildOnly=mx build {0} --only {1}",
        "# {0} - name of mx suite",
        "MSG_Rebuild=mx rebuild {0}",
        "# {0} - name of mx suite",
        "MSG_Unittest=mx unittest {0}",
    })
    @Override
    @SuppressWarnings("fallthrough")
    public void invokeAction(String action, Lookup context) throws IllegalArgumentException {
        FileObject fo = context.lookup(FileObject.class);
        String testSuffix = "";
        CompletionStage<Integer> running;
        switch (action) {
            case ActionProvider.COMMAND_CLEAN:
                running = runMx(Bundle.MSG_Clean(prj.getName()), "clean"); // NOI18N
                break;
            case ActionProvider.COMMAND_BUILD:
                running = ensureBuilt(null);
                break;
            case ActionProvider.COMMAND_REBUILD:
                running = ensureBuilt(null);
                break;
            case ActionProvider.COMMAND_COMPILE_SINGLE: {
                running = ensureBuilt(fo);
                break;
            }
            case SingleMethod.COMMAND_RUN_SINGLE_METHOD: {
                SingleMethod m = context.lookup(SingleMethod.class);
                if (m != null) {
                    if (fo == null) {
                        fo = m.getFile();
                    }
                    testSuffix = "#" + m.getMethodName();
                }
                // fallthrough
            }
            case ActionProvider.COMMAND_TEST_SINGLE:
            case ActionProvider.COMMAND_RUN_SINGLE:
                if (fo == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                running = runBuildAndMx(null, Bundle.MSG_Unittest(fo.getName()), "unittest", fo.getName() + testSuffix); // NOI18N
                break;
            case SingleMethod.COMMAND_DEBUG_SINGLE_METHOD: {
                SingleMethod m = context.lookup(SingleMethod.class);
                if (m != null) {
                    if (fo == null) {
                        fo = m.getFile();
                    }
                    testSuffix = "#" + m.getMethodName();
                }
                // fallthrough
            }
            case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
                if (fo == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                ListeningDICookie ldic = ListeningDICookie.create(-1);
                DebuggerInfo di = DebuggerInfo.create(ListeningDICookie.ID, ldic);
                ASYNC.post(() -> {
                    DebuggerManager.getDebuggerManager().startDebugging(di);
                });
                int port = ldic.getPortNumber();
                running = runBuildAndMx(null, Bundle.MSG_Unittest(fo.getName()), "--attach", "" + port, "unittest", fo.getName() + testSuffix); // NOI18N
                break;
            default:
                throw new UnsupportedOperationException(action);
        }
        if (running != null) {
            ActionProgress progress = ActionProgress.start(context);
            running.handle((exitCode, error) -> {
                progress.finished(error == null && exitCode != null && exitCode == 0);
                return null;
            });
        }
    }

    private CompletionStage<Integer> ensureBuilt(FileObject fo) {
        if (fo != null) {
            SuiteSources.Group grp = prj.getSources().findGroup(fo);
            if (grp != null) {
                final String name = grp.getDisplayName();
                return runMx(Bundle.MSG_BuildOnly(prj.getName(), name), "build", "--only", name); // NOI18N
            }
        }
        return runMx(Bundle.MSG_Rebuild(prj.getName()), "build"); // NOI18N
    }

    private CompletionStage<Integer> runBuildAndMx(FileObject fo, String taskName, String... args) {
        return ensureBuilt(fo).thenCompose((exitCode) -> {
            return runMx(taskName, args);
        });
    }

    private CompletableFuture<Integer> runMx(String taskName, String... args) {
        final File suiteDir = FileUtil.toFile(prj.getProjectDirectory());
        if (!suiteDir.isDirectory()) {
            Toolkit.getDefaultToolkit().beep();
            return CompletableFuture.completedFuture(-1);
        }
        CompletableFuture<Future<Integer>> taskResult = new CompletableFuture<>();
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        LifecycleManager.getDefault().saveAll();
        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true).controllable(true)
                .errConvertorFactory(() -> {
                    return (String line) -> {
                        String[] segments = line.split(":");
                        if (segments.length > 2) {
                            File src = new File(segments[0]);
                            if (src.exists()) {
                                int lineNumber = parseLineNumber(segments) - 1;
                                return Collections.singletonList(ConvertedLine.forText(line, new OutputListener() {
                                    @Override
                                    public void outputLineSelected(OutputEvent ev) {
                                        openLine(Line.ShowOpenType.NONE, Line.ShowVisibilityType.FRONT);
                                    }

                                    @Override
                                    public void outputLineAction(OutputEvent ev) {
                                        openLine(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                    }

                                    private boolean openLine(final Line.ShowOpenType openType, final Line.ShowVisibilityType visibilityType) throws IndexOutOfBoundsException {
                                        FileObject fo = FileUtil.toFileObject(src);
                                        if (fo != null) {
                                            Lookup lkp = fo.getLookup();
                                            final LineCookie lines = lkp.lookup(LineCookie.class);
                                            if (lines != null) {
                                                Line open = lines.getLineSet().getOriginal(lineNumber);
                                                if (open != null) {
                                                    open.show(openType, visibilityType);
                                                    return true;
                                                }
                                            }
                                            Openable open = lkp.lookup(Openable.class);
                                            if (open != null) {
                                                open.open();
                                            } else {
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                        }
                                        return false;
                                    }

                                    @Override
                                    public void outputLineCleared(OutputEvent ev) {
                                    }
                                }));
                            }
                        }
                        return null;
                    };
                }).postExecution((exitCode) -> {
                    cf.complete(exitCode);
                });
        ProcessBuilder processBuilder = ProcessBuilder.getLocal();
        processBuilder.setWorkingDirectory(suiteDir.getPath());
        
        String executable = TEST_MX_PATH != null ? TEST_MX_PATH : "mx"; // NOI18N
        processBuilder.setExecutable(executable); // NOI18N
        processBuilder.setArguments(Arrays.asList(args));
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, taskName);
        Future<Integer> task = service.run();
        taskResult.complete(task);
        prj.registerTask(task);
        return cf;
    }

    private int parseLineNumber(String[] segments) {
        int lineNumber;
        try {
            lineNumber = Integer.parseInt(segments[1]);
        } catch (NumberFormatException ex) {
            lineNumber = 1;
        }
        return lineNumber;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public boolean isActionEnabled(String action, Lookup context) throws IllegalArgumentException {
        FileObject fo = context.lookup(FileObject.class);
        switch (action) {
            case SingleMethod.COMMAND_DEBUG_SINGLE_METHOD:
            case SingleMethod.COMMAND_RUN_SINGLE_METHOD:
                SingleMethod m = context.lookup(SingleMethod.class);
                if (fo == null && m != null) {
                    fo = m.getFile();
                }
                // fallthrough
            case ActionProvider.COMMAND_COMPILE_SINGLE:
            case ActionProvider.COMMAND_TEST_SINGLE:
            case ActionProvider.COMMAND_RUN_SINGLE:
            case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
                return fo != null;
            default:
                return SUPPORTED_ACTIONS.contains(action);
        }
    }
}
