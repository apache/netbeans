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
package org.netbeans.modules.selenium2.webclient.mocha;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javascript.nodejs.api.NodeJsSupport;
import org.netbeans.modules.javascript.v8debug.api.Connector;
import org.netbeans.modules.selenium2.api.Utils;
import org.netbeans.modules.selenium2.webclient.api.RunInfo;
import org.netbeans.modules.selenium2.webclient.api.SeleniumRerunHandler;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.selenium2.webclient.api.TestRunnerReporter;
import org.netbeans.modules.selenium2.webclient.api.Utilities;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaJSPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaSeleniumPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaPreferencesValidator;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Theofanis Oikonomou
 */
public class MochaRunner {
    private static final Logger LOG = Logger.getLogger(MochaRunner.class.getName());
    private static final String DEBUG_HOST = "localhost"; // NOI18N
    private static final int DEBUG_PORT = 5858;
    
    public static void runTests(FileObject[] activatedFOs, boolean isSelenium) {
        internalMochaRunner(activatedFOs, isSelenium, false);
    }
    
    public static void debugTests(FileObject[] activatedFOs, boolean isSelenium) {
        internalMochaRunner(activatedFOs, isSelenium, true);
    }
    
    private static void internalMochaRunner(FileObject[] activatedFOs, boolean isSelenium, boolean debug) {
        assert !EventQueue.isDispatchThread();
        Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
        if (p == null) {
            return;
        }
        
        File mochaNBReporter = InstalledFileLocator.getDefault().locate(
                    "mocha/netbeans-reporter.js", "org.netbeans.modules.selenium2.webclient.mocha", false); // NOI18N
        if(mochaNBReporter == null) {
            return;
        }
        
        String node = getNode(p);
        if(node == null) {
            openNodeSettings(p);
            return;
        }
        
        FileObject testsFolder = isSelenium ? Utilities.getTestsSeleniumFolder(p, true) : Utilities.getTestsFolder(p, true);
        if(testsFolder == null) {
            Utilities.openCustomizer(p, WebClientProjectConstants.CUSTOMIZER_SOURCES_IDENT);
            return;
        }
        
        String mochaInstallFolder = isSelenium ? MochaSeleniumPreferences.getMochaDir(p) : MochaJSPreferences.getMochaDir(p);
        ValidationResult validationResult = new MochaPreferencesValidator()
                .validateMochaInstallFolder(mochaInstallFolder)
                .getResult();
        if(mochaInstallFolder == null || mochaInstallFolder.isEmpty() || !validationResult.isFaultless()) {
            Utilities.openCustomizer(p, isSelenium ? SeleniumTestingProviders.CUSTOMIZER_SELENIUM_TESTING_IDENT : JsTestingProviders.CUSTOMIZER_IDENT);
            return;
        }
        
        boolean testProject = activatedFOs.length == 1 && activatedFOs[0].equals(p.getProjectDirectory());
        
        RunInfo runInfo = getRunInfo(p, activatedFOs, testsFolder, mochaInstallFolder, isSelenium, testProject);

        String displayname = ProjectUtils.getInformation(runInfo.getProject()).getDisplayName() + (isSelenium ? " Selenium" : " Unit") + " Tests"; // NOI18N
        final ExternalExecutable externalexecutable = new ExternalExecutable(node)
                .workDir(FileUtil.toFile(p.getProjectDirectory()))
                .displayName(displayname)
                .additionalParameters(getAdditionalArguments(p, activatedFOs, testsFolder, mochaInstallFolder, mochaNBReporter, isSelenium, debug, testProject))
                .environmentVariables(runInfo.getEnvVars());
        TestRunnerReporter testRunnerReporter = new TestRunnerReporter(runInfo, "mocha-netbeans-reporter "); // NOI18N

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ExecutionDescriptor.LineConvertorFactory outputLineConvertorFactory = getOutputLineConvertorFactory(testRunnerReporter, runInfo, countDownLatch, debug);

        final ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .controllable(true)
                .showProgress(true)
                .showSuspended(true)
                .outLineBased(true)
                .errLineBased(true)
                .outConvertorFactory(outputLineConvertorFactory);

        final Future<Integer> task = externalexecutable.run(descriptor);
        
        if (debug) {
            try {
                countDownLatch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Connector.Properties props = createConnectorProperties(DEBUG_HOST, DEBUG_PORT, p, isSelenium);
            try {
                Connector.connect(props, new Runnable() {
                    @Override
                    public void run() {
                        task.cancel(true);
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Gets file path (<b>possibly with parameters!</b>) representing <tt>node</tt> executable of the given project
     * or {@code null} if not set/found. If the project is {@code null}, the default <tt>node</tt>
     * path is returned (path set in IDE Options).
     * @param project project to get <tt>node</tt> for, can be {@code null}
     * @return file path (<b>possibly with parameters!</b>) representing <tt>node</tt> executable, can be {@code null} if not set/found
     */
    @CheckForNull
    private static String getNode(@NullAllowed Project project) {
        return NodeJsSupport.getInstance().getNode(project);
    }
    
    /**
     * Opens <tt>node</tt> settings for the given project - if project specific <tt>node</tt> is used
     * (and node.js is enabled in the given project), Project Properties dialog is opened (otherwise
     * IDE Options). If project is {@code null}, opens IDE Options.
     * @param project project to be used, can be {@code null}
     */
    private static void openNodeSettings(@NullAllowed Project project) {
        NodeJsSupport.getInstance().openNodeSettings(project);
    }
    
    private static ExecutionDescriptor.LineConvertorFactory getOutputLineConvertorFactory(final TestRunnerReporter testRunnerReporter, final RunInfo runInfo, final CountDownLatch countDownLatch, boolean debug) {
        if (!debug) {
            return new ExecutionDescriptor.LineConvertorFactory() {
                @Override
                public LineConvertor newLineConvertor() {
                    return new OutputLineConvertor(testRunnerReporter, runInfo, null);
                }
            };
        }
        final Runnable countDownTask = new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        };
        return new ExecutionDescriptor.LineConvertorFactory() {
            @Override
            public LineConvertor newLineConvertor() {
                return new OutputLineConvertor(testRunnerReporter, runInfo, countDownTask);
            }
        };
    }
    
    private static ArrayList<String> getAdditionalArguments(Project p, FileObject[] activatedFOs, FileObject testsFolder, String mochaInstallFolder, File mochaNBReporter, boolean isSelenium, boolean debug, boolean testProject) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(mochaInstallFolder + "/bin/mocha");
        if(!isSelenium && MochaJSPreferences.isAutoWatch(p)) {
            arguments.add("-w");
        }
        if(debug) {
            arguments.add("--debug-brk");
        }
        arguments.add("-t");
        arguments.add(isSelenium ? Integer.toString(MochaSeleniumPreferences.getTimeout(p)) : Integer.toString(MochaJSPreferences.getTimeout(p)));
        arguments.add("-R");
        arguments.add(mochaNBReporter.getPath());
        
        if(testProject) {
            // run *.js files recursively under testsFolder. This could be Unit or Selenium Tests Folder set in project properties.
            String testFolder = FileUtil.getRelativePath(p.getProjectDirectory(), testsFolder);
            arguments.add(testFolder + "/**/*.js");
        } else {
            ArrayList<String> files2test = new ArrayList<>();
            for(FileObject fo : activatedFOs) {
                String file2test = FileUtil.getRelativePath(p.getProjectDirectory(), fo);
                if(file2test != null) {
                    if(!files2test.contains(file2test)) {
                        files2test.add(file2test);
                    }
                }
            }
            for(String file2test : files2test) {
                arguments.add(file2test);
            }
        }
        
        return arguments;
    }
    
    private static RunInfo getRunInfo(Project p, FileObject[] activatedFOs, FileObject testsFolder, String mochaInstallFolder, boolean isSelenium, boolean testProject) {
        RunInfo.Builder builder = new RunInfo.Builder(activatedFOs).setTestingProject(testProject)
                .addEnvVar("MOCHA_DIR", mochaInstallFolder)
                .setRerunHandler(new SeleniumRerunHandler(p, activatedFOs, CustomizerMochaPanel.IDENTIFIER, isSelenium))
                .setIsSelenium(isSelenium);
        if(activatedFOs.length == 1 && !activatedFOs[0].equals(p.getProjectDirectory())) {
            String testFile = FileUtil.getRelativePath(testsFolder, activatedFOs[0]);
            builder = builder.setTestFile(testFile);
        }
        return builder.build();
    }
    
    private static Connector.Properties createConnectorProperties(String host, int port, Project project, boolean isSelenium) {
        List<File> sourceRoots = getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5);
        List<File> siteRoots = getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT);
        List<File> testRoots = getRoots(project, isSelenium ? WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM : WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST);
        List<String> localPaths = new ArrayList<>(sourceRoots.size());
        List<String> localPathsExclusionFilter = Collections.emptyList();
        sourceRoots.addAll(testRoots);
        for (File src : sourceRoots) {
            localPaths.add(src.getAbsolutePath());
            for (File site : siteRoots) {
                if (isSubdirectoryOf(src, site)) {
                    if (localPathsExclusionFilter.isEmpty()) {
                        localPathsExclusionFilter = new ArrayList<>();
                    }
                    localPathsExclusionFilter.add(site.getAbsolutePath());
                }
            }
        }
        return new Connector.Properties(host, port, localPaths, Collections.<String>emptyList(), localPathsExclusionFilter);
    }
    
    private static List<File> getRoots(Project project, String type) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(type);
        List<File> roots = new ArrayList<>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            File root = FileUtil.toFile(rootFolder);
            assert root != null : rootFolder;
            roots.add(root);
        }
        return roots;
    }
    
    private static boolean isSubdirectoryOf(File folder, File child) {
        if (!folder.isDirectory()) {
            return false;
        }
        String fp;
        try {
            fp = folder.getCanonicalPath();
        } catch (IOException ioex) {
            fp = folder.getAbsolutePath();
        }
        String chp;
        try {
            chp = child.getCanonicalPath();
        } catch (IOException ioex) {
            chp = child.getAbsolutePath();
        }
        if (!chp.startsWith(fp)) {
            return false;
        }
        int fl = fp.length();
        if (chp.length() == fl) {
            return true;
        }
        char separ = chp.charAt(fl);
        if (File.separatorChar == separ) {
            return true;
        } else {
            return false;
        }
    }
    
    private static class OutputLineConvertor implements LineConvertor {
        private final TestRunnerReporter testRunnerReporter;
        private final RunInfo runInfo;
        private Runnable debuggerStartTask;

        public OutputLineConvertor(TestRunnerReporter testRunnerReporter, RunInfo runInfo, Runnable debuggerStartTask) {
            this.testRunnerReporter = testRunnerReporter;
            this.runInfo = runInfo;
            this.debuggerStartTask = debuggerStartTask;
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            // debugger?
            if (debuggerStartTask != null
                    && line.startsWith("debugger listening on port")) { // NOI18N
                debuggerStartTask.run();
                debuggerStartTask = null;
            }
            String output2display = testRunnerReporter.processLine(line);
            if(output2display == null) {
                return Collections.emptyList();
            }
            TestRunnerReporter.CallStackCallback callStackCallback = new TestRunnerReporter.CallStackCallback(runInfo.getProject());
            Pair<File, int[]> parsedLocation = callStackCallback.parseLocation(line, false);
            FileOutputListener fileOutputListener = parsedLocation == null ? null : new FileOutputListener(parsedLocation.first(), parsedLocation.second()[0], parsedLocation.second()[1]);
            return Collections.singletonList(ConvertedLine.forText(output2display, fileOutputListener));
        }
    }
    
    private static final class FileOutputListener implements OutputListener {

        final File file;
        final int line;
        final int column;

        public FileOutputListener(File file, int line, int column) {
            assert file != null;
            this.file = file;
            this.line = line;
            this.column =column;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            // noop
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    Utils.openFile(file, line, column);
                }
            });
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
            // noop
        }
    }
    
}
