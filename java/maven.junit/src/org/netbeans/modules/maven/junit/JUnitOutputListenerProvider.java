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
package org.netbeans.modules.maven.junit;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.api.UnitTestsUsage;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.junit.api.JUnitTestSuite;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class JUnitOutputListenerProvider implements OutputProcessor {
    private static final String TESTTYPE_UNIT = "UNIT";  //NOI81N
    private static final String TESTTYPE_INTEGRATION = "INTEGRATION";  //NOI81N

    private String testType;
    private String reportNameSuffix;
    private final Pattern runningPattern;
    private final Pattern outDirPattern2;
    private final Pattern outDirPattern;
    private String runningTestClass;
    private final Set<String> usedNames;
    private final long startTimeStamp;
    
    private static final Logger LOG = Logger.getLogger(JUnitOutputListenerProvider.class.getName());
    private final RunConfig config;
    private boolean surefireRunningInParallel = false;
    private final Map<String, Set<File>> runningTestClass2outputDirs = new HashMap<>();
    private final ArrayList<String> runningTestClassesInParallel = new ArrayList<>();
    private final Map<Project, File> project2outputDirs = new HashMap<>();
    private final Map<File, TestSession> outputDir2sessions = new HashMap<>();
    
    private static final String GROUP_FILE_NAME = "dir";
    
    public JUnitOutputListenerProvider(RunConfig config) {
        runningPattern = Pattern.compile("(?:\\[(?:INFO|surefire)\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile ("(?:\\[INFO\\] )?Surefire report directory\\: (?<" + GROUP_FILE_NAME + ">.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("(?:\\[INFO\\] )?Setting reports dir\\: (?<" + GROUP_FILE_NAME + ">.*)", Pattern.DOTALL); //NOI18N
        this.config = config;
        usedNames = new HashSet<>();
        startTimeStamp = System.currentTimeMillis();
        surefireRunningInParallel = isSurefireRunningInParallel();
    }
    
    private boolean isSurefireRunningInParallel() {
        // http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html
        // http://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html
        String parallel = config.getProperties().containsKey("parallel") ? config.getProperties().get("parallel") : PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "parallel", "test", "parallel"); //NOI18N
        if (parallel != null) {
            return true;
        }
        String forkMode = config.getProperties().containsKey("forkMode") ? config.getProperties().get("forkMode") : PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "forkMode", "test", "forkMode"); //NOI18N
        if ("perthread".equals(forkMode)) {
            String threadCount = config.getProperties().containsKey("threadCount") ? config.getProperties().get("threadCount") : PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "threadCount", "test", "threadCount");
            if (threadCount != null) {
                if (Integer.parseInt(threadCount) > 1) {
                    return true;
                }
            }
        }
        String forkCount = config.getProperties().containsKey("forkCount") ? config.getProperties().get("forkCount") : PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "forkCount", "test", "forkCount");
        if (forkCount != null) {
            int index = forkCount.indexOf("C");
            int cpuCores = 1;
            if (index != -1) {
                forkCount = forkCount.substring(0, index);
                cpuCores = Runtime.getRuntime().availableProcessors();
            }
            float forks;
            try {
                // example values: "1.5C", "4". http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#forkCount
                forks = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(forkCount).floatValue();
            } catch (ParseException ex) {
                LOG.log(Level.FINE, null, ex);
                forks = 1;
            }
            if (forks * cpuCores > 1) {
                return true;
            }
        }
        return false;
    }

    public @Override String[] getRegisteredOutputSequences() {
        return new String[] {
            "mojo-execute#surefire:test", //NOI18N
            "mojo-execute#failsafe:integration-test" //NOI18N
        };
    }

    public @Override void processLine(String line, OutputVisitor visitor) {
        Matcher match = outDirPattern.matcher(line);
        if (match.matches()) {
            File outputDir = new File(match.group(GROUP_FILE_NAME));
            LOG.log(Level.FINER, "Line matches reports directory: {0}", outputDir);
            createSession(outputDir);
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            File outputDir = new File(match.group(GROUP_FILE_NAME));
            LOG.log(Level.FINER, "Line matches reports directory: {0}", outputDir);
            createSession(outputDir);
            return;
        }

        if (outputDir2sessions.isEmpty()) {
            return;
        }
        match = runningPattern.matcher(line);
        if (match.matches()) {
            if (surefireRunningInParallel) {
                // make sure results are displayed in case of a failure
                runningTestClassesInParallel.add(match.group(1));
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Got running test: line {0}, class: {1}. Parallel run: {2}", new Object[] { line, match.group(1), 
                        runningTestClassesInParallel.toString() });
                }
            } else {
                if (runningTestClass != null) {
                    LOG.log(Level.FINE, "Got running test SINGLE: line {0}, class: {1}", new Object[] { line, match.group(1) });
                    // match.group(1) should be the FQN of a running test class but let's check to be on the safe side
                    // If the matcher matches it means that we have a new test class running,
                    // if not it probably means that this is user's text, e.g. "Running my cool test", so we can safely ignore it
                    if (!isFullJavaId(match.group(1))) {
                        LOG.log(Level.FINE, "Not full java id match!");
                        return;
                    }
                    // tests are running sequentially, so update Test Results Window
                    generateTest();
                }
                runningTestClass = match.group(1);
            }
        }
        match = testSuiteStatsPattern.matcher(line);
        if (match.matches() && surefireRunningInParallel) {
            runningTestClass = match.group(6);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Got test statistics: {0}, running classes: {1}", new Object[] { match.group(6), runningTestClassesInParallel.toString() });
            }
            if (runningTestClass != null) {
                // runningTestClass should be the FQN of a running test class but let's check to be on the safe side
                // If the matcher matches it means that we have a new test class running,
                // if not it probably means that this is user's text, e.g. "Running my cool test", so we can safely ignore it
                if (!isFullJavaId(runningTestClass)) {
                    return;
                }
                generateTest();
                // runningTestClass did not fail so remove it from the list
                runningTestClassesInParallel.remove(runningTestClass);
                // runningTestClass might be the last one so make it null to avoid appearing twice when sequenceEnd() is called
                runningTestClass = null;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Statistics done for {0}. Cleaning runnintTestClass, stillRunning: {1}", new Object[] { match.group(6), runningTestClassesInParallel.toString() });
                }
            }
        }
    }
    
    private static final String SECONDS_REGEX = "s(?:ec(?:ond)?(?:s|\\(s\\))?)?"; //NOI18N
    private static final String TESTSUITE_STATS_REGEX = "Tests run: +([0-9]+), +Failures: +([0-9]+), +Errors: +([0-9]+), +Skipped: +([0-9]+), +Time elapsed: +(.+)" + SECONDS_REGEX + " *(?: * <+ FAILURE! *)?-+ in (.*)";
    private static final Pattern testSuiteStatsPattern = Pattern.compile(TESTSUITE_STATS_REGEX);
    
    static boolean isTestSuiteStats(String line) {
        return testSuiteStatsPattern.matcher(line).matches();
    }
    
    static String getTestSuiteFromStats(String line) {
        Matcher matcher = testSuiteStatsPattern.matcher(line);
        return matcher.matches() ? matcher.group(6) : null;
    }
    
    private static final String JAVA_ID_START_REGEX = "\\p{javaJavaIdentifierStart}"; //NOI18N
    private static final String JAVA_ID_PART_REGEX = "\\p{javaJavaIdentifierPart}"; //NOI18N
    private static final String JAVA_ID_REGEX = "(?:" + JAVA_ID_START_REGEX + ')' + "(?:" + JAVA_ID_PART_REGEX + ")*"; //NOI18N
    private static final String JAVA_ID_REGEX_FULL = JAVA_ID_REGEX + "(?:\\." + JAVA_ID_REGEX + ")*"; //NOI18N
    private static final Pattern fullJavaIdPattern = Pattern.compile(JAVA_ID_REGEX_FULL);
    
    static boolean isFullJavaId(String possibleNewRunningTestClass) {
        return fullJavaIdPattern.matcher(possibleNewRunningTestClass).matches();
    }

    public @Override void sequenceStart(String sequenceId, OutputVisitor visitor) {
        reportNameSuffix = null;
        testType = null;
        String reportsDirectory = null;
	if (("mojo-execute#surefire:test".equals(sequenceId))) {  // NOI18N
            // Fix for #257563 / SUREFIRE-1158, where surefire 2.19.x
            // removed the printing of the output directory. Get the directory from
            // the configuration directly or fallback to the plugin standard
            if (usingSurefire219(this.config.getMavenProject())) {
                // http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#reportsDirectory
                reportsDirectory = getReportsDirectory(
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE,
                    "test", visitor, "${project.build.directory}/surefire-reports"); //NOI81N
            }
            reportNameSuffix = PluginPropertyUtils.getPluginProperty(
                config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SUREFIRE, "reportNameSuffix", "test", //NOI81N
                "surefire.reportNameSuffix"); //NOI81N
            testType = TESTTYPE_UNIT;
	} else if ("mojo-execute#failsafe:integration-test".equals(sequenceId)) {  //NOI81N
	    reportsDirectory = getReportsDirectory(
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_FAILSAFE,
                "integration-test", visitor, "${project.build.directory}/failsafe-reports");  //NOI81N
            reportNameSuffix = PluginPropertyUtils.getPluginProperty(
                config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_FAILSAFE, "reportNameSuffix", "integration-test",  //NOI81N
                "surefire.reportNameSuffix");  //NOI81N
            testType = TESTTYPE_INTEGRATION;  //NOI81N
        }
        if (reportsDirectory != null) {
            File outputDir = null;
            File absoluteFile = new File(reportsDirectory);
            // configuration might be "target/directory", which is relative
            // to the maven project or an absolute path
            File relativeFile = new File(this.config.getMavenProject().getBasedir(), reportsDirectory);
            if (absoluteFile.exists() && absoluteFile.isDirectory()) {
                outputDir = absoluteFile;
            } else {
                if (relativeFile.exists() && relativeFile.isDirectory()) {
                    outputDir = relativeFile;
                } else {
                    File parentFile = absoluteFile.getParentFile();
                    if (parentFile.exists() && parentFile.isDirectory()) {
                        absoluteFile.mkdir();
                        if (absoluteFile.exists() && absoluteFile.isDirectory()) {
                            outputDir = absoluteFile;
                        }
                    } else {
                        File parentRelativeFile = relativeFile.getParentFile();
                        if (parentRelativeFile.exists() && parentRelativeFile.isDirectory()) {
                            relativeFile.mkdir();
                            if (relativeFile.exists() && relativeFile.isDirectory()) {
                                outputDir = relativeFile;
                            }
                        }
                    }
                }
            }
            if (outputDir != null) {
                createSession(outputDir);
                OutputVisitor.Context context = visitor.getContext();
                // may be null when EventSpy is not active
                Project project = context != null && context.getCurrentProject() != null
                                ? context.getCurrentProject()
                                : FileOwnerQuery.getOwner(Utilities.toURI(outputDir));
                if (project != null) {
                    project2outputDirs.put(project, outputDir);
                }
            }
        }
    }

    private String getReportsDirectory(String groupId, String artifactId, String goal, OutputVisitor visitor, String fallbackExpression) {
        MavenProject currentProject = null;
        // get maven module from context if available
        OutputVisitor.Context context = visitor.getContext();
        if (context != null) {
            Project cp = context.getCurrentProject();
            if (cp != null) {
                NbMavenProject subProject = cp.getLookup().lookup(NbMavenProject.class);
                if (subProject != null) {
                    currentProject = subProject.getMavenProject();
                }
            }
        }
        if (currentProject == null) {
            currentProject = config.getMavenProject();
        }
        String reportsDirectory = PluginPropertyUtils.getPluginProperty(currentProject,
           groupId, artifactId, "reportsDirectory", goal, null); // NOI18N
        if (reportsDirectory == null) {
            // fallback to default value
            try {
                Object defaultValue = PluginPropertyUtils
                    .createEvaluator(currentProject)
                    .evaluate(fallbackExpression);
                if (defaultValue instanceof String str) {
                    reportsDirectory = str;
                }
            } catch (ExpressionEvaluationException ex) {
                // NOP could not resolved default value
            }
        }
        return reportsDirectory;
    }

    //#179703 allow multiple sessions per project, in case there are multiple executions of surefire plugin.
    @NbBundle.Messages({
        "# {0} - projectId",
        "LBL_TESTTYPE_UNIT={0} (Unit)",
        "# {0} - projectId",
        "LBL_TESTTYPE_INTEGRATION={0} (Integration)",
        "# {0} - projectId",
        "# {1} - index (1 based) of created session",
        "LBL_TESTTYPE_UNIT_INDEXED={0} (Unit) #{1}",
        "# {0} - projectId",
        "# {1} - index (1 based) of created session",
        "LBL_TESTTYPE_INTEGRATION_INDEXED={0} (Integration) #{1}"
    })
    private String createSessionName(String projectId) {
        String name;
        if (TESTTYPE_INTEGRATION.equals(testType)) {
            name = Bundle.LBL_TESTTYPE_INTEGRATION(projectId);
        } else {
            name = Bundle.LBL_TESTTYPE_UNIT(projectId);
        }
        int index = 2;
        while (usedNames.contains(name)) {
            if (TESTTYPE_INTEGRATION.equals(testType)) {
                name = Bundle.LBL_TESTTYPE_INTEGRATION_INDEXED(projectId, index);
            } else {
                name = Bundle.LBL_TESTTYPE_UNIT_INDEXED(projectId, index);
            }
            index++;
        }
        usedNames.add(name);
        return name;
    }

    private CoreManager getManagerProvider() {
        Collection<? extends Lookup.Item<CoreManager>> providers = Lookup.getDefault().lookupResult(CoreManager.class).allItems();
        for (Lookup.Item<CoreManager> provider : providers) {
            if(provider.getDisplayName().equals(CommonUtils.MAVEN_PROJECT_TYPE.concat("_").concat(CommonUtils.JUNIT_TF))) {
                return provider.getInstance();
            }
        }
        return null;
    }
    
    private void createSession(File nonNormalizedFile) {
        if (!outputDir2sessions.containsKey(nonNormalizedFile)) {
            File fil = FileUtil.normalizeFile(nonNormalizedFile);
            Project prj = FileOwnerQuery.getOwner(Utilities.toURI(fil));
            LOG.log(Level.FINE, "Creating session for project {0}", prj);
            if (prj != null) {
                NbMavenProject mvnprj = prj.getLookup().lookup(NbMavenProject.class);
                if (mvnprj != null) {
                    LOG.log(Level.FINE, "Maven project instance: {0}", mvnprj);
                    File projectFile = FileUtil.toFile(prj.getProjectDirectory());
                    if (projectFile != null) {
                        UnitTestsUsage.getInstance().logUnitTestUsage(Utilities.toURI(projectFile), getJUnitVersion(config.getMavenProject()));
                    }
                    TestSession.SessionType type = TestSession.SessionType.TEST;
                    String action = config.getActionName();
                    if (action != null) { //custom
                        if (action.contains("debug")) { //NOI81N
                            type = TestSession.SessionType.DEBUG;
                        }
                    }
                    final TestSession.SessionType fType = type;
                    CoreManager junitManager = getManagerProvider();
                    if (junitManager != null) {
                        junitManager.registerNodeFactory();
                    }
                    TestSession session = new TestSession(createSessionName(mvnprj.getMavenProject().getId()), prj, TestSession.SessionType.TEST);
                    outputDir2sessions.put(nonNormalizedFile, session);
                    LOG.log(Level.FINE, "Created session: {0}", session);
                    session.setRerunHandler(new RerunHandler() {
                        public @Override
                        void rerun() {
                            RunUtils.executeMaven(config);
                        }

                        public @Override
                        void rerun(Set<Testcase> tests) {
                            RunConfig brc = RunUtils.cloneRunConfig(config);
                            StringBuilder tst = new StringBuilder();
                            Map<String, Collection<String>> methods = new HashMap<>();
                            //#222776 calculate the approximate space the failed tests will occupy on the cmd line.
                            //important on windows which places a limit on the length.
                            int windowslimitcount = 0;
                            for (Testcase tc : tests) {
                                //TODO just when is the classname null??
                                String tcName= tc.getClassName();
                                if (tcName != null) {
                                    Collection<String> lst = methods.get(tc.getClassName());
                                    if (lst == null) {
                                        lst = new ArrayList<>();
                                        methods.put(tc.getClassName(), lst);
                                        windowslimitcount = windowslimitcount + tc.getClassName().length() + 1; // + 1 for ,
                                    }
                                    String mName = tc.getName();

                                    if (tcName!=null
                                            && mName.startsWith(tcName)
                                            && mName.charAt(tcName.length())=='.'){
                                        mName = mName.substring(tcName.length()+1);
                                    }
                                    lst.add(mName);
                                    windowslimitcount = windowslimitcount + mName.length() + 1; // + 1 for # or +
                                }
                            }
                            boolean exceedsWindowsLimit = Utilities.isWindows() && windowslimitcount > 6000; //just be conservative here, the limit is more (8000+)
                            for (Map.Entry<String, Collection<String>> ent : methods.entrySet()) {
                                tst.append(",");
                                if (exceedsWindowsLimit) {
                                    String clazzName = ent.getKey();
                                    int lastDot = ent.getKey().lastIndexOf(".");
                                    if (lastDot > -1) {
                                        clazzName = clazzName.substring(lastDot + 1);
                                    }
                                    tst.append(clazzName);
                                } else {
                                    tst.append(ent.getKey());
                                }

                                //#name only in surefire > 2.7.2 and junit > 4.0 or testng
                                // bug works with the setting also for junit 3.x
                                tst.append("#");
                                boolean first = true;
                                for (String meth : ent.getValue()) {
                                    if (!first) {
                                        tst.append("+");
                                    }
                                    first = false;
                                    tst.append(meth);
                                }
                            }
                            if (tst.length() > 0) {
                                brc.setProperty("test", tst.substring(1));
                            }
                            RunUtils.executeMaven(brc);
                        }

                        public @Override
                        boolean enabled(RerunType type) {
                            //debug should now properly update debug port in runconfig...
                            if (fType.equals(TestSession.SessionType.TEST) || fType.equals(TestSession.SessionType.DEBUG)) {
                                if (RerunType.ALL.equals(type)) {
                                    return true;
                                }
                                if (RerunType.CUSTOM.equals(type)) {
                                    if (usingTestNG(config.getMavenProject())) { //#214334 test for testng has to come first, as itself depends on junit
                                        return usingSurefire28(config.getMavenProject());
                                    } else if (usingJUnit4(config.getMavenProject())) { //#214334
                                        return usingSurefire2121(config.getMavenProject());
                                    } else if (getJUnitVersion(config.getMavenProject()).equals("JUNIT5")){
                                        return usingSurefire2220(config.getMavenProject());
                                    }
                                }
                            }
                            return false;
                        }

                        public @Override
                        void addChangeListener(ChangeListener listener) {
                        }

                        public @Override
                        void removeChangeListener(ChangeListener listener) {
                        }
                    });
                    if (junitManager != null) {
                        junitManager.testStarted(session);
                    }
                }
            }
        } else {
            LOG.log(Level.FINE, "Session for directory {0} already opened", nonNormalizedFile);
        }
    }
    
    private boolean usingSurefire219(MavenProject prj) {
	String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
	return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.19")) >= 0;
    }

    private boolean usingSurefire2121(MavenProject prj) {
        String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.12.1")) >= 0;
    }
    
    private boolean usingSurefire28(MavenProject prj) {
        String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.8")) >= 0;
    }

    private boolean usingSurefire2220(MavenProject prj) {
        String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.22.0")) >= 0;
    }
    
     private boolean usingTestNG(MavenProject prj) {
        for (Artifact a : prj.getArtifacts()) {
            if ("org.testng".equals(a.getGroupId()) && "testng".equals(a.getArtifactId())) {
                return true;
            }
        }
        return false;
    }   
    
    private String getJUnitVersion(MavenProject prj) {
        String juVersion = "";
        for (Artifact a : prj.getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && ("junit".equals(a.getArtifactId()) || "junit-dep".equals(a.getArtifactId()))) { //junit-dep  see #214238
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return "JUNIT4"; //NOI18N
                }
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("3.8")) >= 0) {
                    return "JUNIT3"; //NOI18N
                }
            }
            if ("org.junit.jupiter".equals(a.getGroupId()) && "junit-jupiter-api".equals(a.getArtifactId())) {
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("5.0")) >= 0) {
                    return "JUNIT5"; //NOI18N
                }
            }
        }
        return juVersion;
    }

    private boolean usingJUnit4(MavenProject prj) { // SUREFIRE-724
        for (Artifact a : prj.getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && ("junit".equals(a.getArtifactId()) || "junit-dep".equals(a.getArtifactId()))) { //junit-dep  see #214238
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public @Override void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        if (outputDir2sessions.isEmpty()) {
            return;
        }
        if (runningTestClass != null) {
            generateTest();
        }
        File outputDir = null;
        OutputVisitor.Context context = visitor.getContext();
        if (context != null && context.getCurrentProject() != null) {
            outputDir = project2outputDirs.remove(context.getCurrentProject());
        } else if (runningTestClass != null) {
            // fallback if EventSpy is not active
            Set<File> dirs = runningTestClass2outputDirs.get(runningTestClass);
            if (dirs != null) {
                for (File dir : outputDir2sessions.keySet()) {
                    if (dirs.contains(dir)) {
                        outputDir = dir;
                        break;
                    }
                }
            }
        }
        if (outputDir != null) {
            TestSession session = outputDir2sessions.remove(outputDir);
            if (session != null) {
                project2outputDirs.remove(session.getProject());
                CoreManager junitManager = getManagerProvider();
                if (junitManager != null) {
                    junitManager.sessionFinished(session);
                }
            }
        }
        runningTestClass = null;
    }

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(".*expected:<(.*)> but was:<(.*)>$"); //NOI18N
    private static final Pattern COMPARISON_PATTERN_AFTER_65 = Pattern.compile(".*expected \\[(.*)\\] but found \\[(.*)\\]$"); //NOI18N

    static Trouble constructTrouble(@NonNull String type, @NullAllowed String message, @NullAllowed String text, boolean error) {
        Trouble t = new Trouble(error);
        if (message != null) {
            Matcher match = COMPARISON_PATTERN.matcher(message);
            if (match.matches()) {
                t.setComparisonFailure(new Trouble.ComparisonFailure(match.group(1), match.group(2)));
            } else {
		match = COMPARISON_PATTERN_AFTER_65.matcher(message);
		if (match.matches()) {
		    t.setComparisonFailure(new Trouble.ComparisonFailure(match.group(1), match.group(2)));
		}
	    }
        }
        if (text != null) {
            String[] strs = StringUtils.split(text, "\n");
            List<String> lines = new ArrayList<>();
            if (message != null) {
                lines.add(message);
            }
            lines.add(type);
            for (int i = 1; i < strs.length; i++) {
                lines.add(strs[i]);
            }
            t.setStackTrace(lines.toArray(new String[0]));
        }
        return t;
    }

    public @Override void sequenceFail(String sequenceId, OutputVisitor visitor) {
        OutputVisitor.Context context = visitor.getContext();
        if (context != null) {
            Project currentProject = context.getCurrentProject();
            LOG.log(Level.FINE, "Got sequenceFail: {0}, line {1}", new Object[] {currentProject, visitor.getLine()});
            // try to get the failed test class. How can this be solved if it is not the first one in the list?
            if (currentProject != null && surefireRunningInParallel) {
                String saveRunningTestClass = runningTestClass;

                for (String s : runningTestClassesInParallel) {
                    File outputDir = locateOutputDirAndWait(s, false);
                    // match the output dir to the project
                    if (outputDir != null) {
                        Project outputOwner = FileOwnerQuery.getOwner(FileUtil.toFileObject(outputDir));
                        if (outputOwner == currentProject) {
                            LOG.log(Level.FINE, "Found unfinished test {0} in {1}, trying to finish", new Object[] {s, currentProject});
                            runningTestClass = s;
                            if (Objects.equals(saveRunningTestClass, s)) {
                                saveRunningTestClass = null;
                            }
                            generateTest();
                        }
                    }
                }
                runningTestClass = saveRunningTestClass;
            }
        }
        sequenceEnd(sequenceId, visitor);
    }
    
    private File locateOutputDirAndWait(String candidateClass, boolean consume) {
        String suffix = reportNameSuffix == null ? "" : "-" + reportNameSuffix;
        File outputDir = locateOutputDir(candidateClass, suffix, consume);
        if (outputDir == null && surefireRunningInParallel) {
            // try waiting a bit to give time for the result file to be created
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            outputDir = locateOutputDir(candidateClass, suffix, consume);
        }
        return outputDir;
    }

    private void generateTest() {
        LOG.log(Level.FINE, "generateTest called for class {0}, ", new Object[] { runningTestClass });
        File outputDir = locateOutputDirAndWait(runningTestClass, true);
        if (outputDir == null) {
            LOG.log(Level.WARNING, "Output directory is not created");
        }
        String suffix = reportNameSuffix == null ? "" : "-" + reportNameSuffix;
        File report = outputDir != null ? new File(outputDir, "TEST-" + runningTestClass + suffix + ".xml") : null;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Reading report file {0}, class {1}, timestamp {2}", new Object[] { report, runningTestClass, report == null ? -1 : 
                    new Date(report.lastModified()) });
        }
        TestSession session = outputDir != null ? outputDir2sessions.get(outputDir) : null;
        if (outputDir == null || report == null || session == null) {
            LOG.log(Level.FINE, "No session for outdir {0}", outputDir);
            return;
        }
        if (report.length() > 50 * 1024 * 1024) {
            LOG.log(Level.INFO, "Skipping report file as size is too big (> 50MB): {0}", report.getPath());
            return;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = null;
            try {
                document = builder.build(report);
            } catch (Exception x) {
                LOG.log(Level.WARNING, "Exception reading from file {0}", report);
                try { // maybe the report file was not created yet, try waiting a bit and then try again
                    Thread.sleep(500);
                    document = builder.build(report);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (JDOMException ex) {
                    LOG.log(Level.WARNING, "parsing " + report, x);
                }
            }
            if(document == null) {
                LOG.log(Level.WARNING, "No document read from dir {0}", outputDir);
                return;
            }
            Element testSuite = document.getRootElement();
            assert "testsuite".equals(testSuite.getName()) : "Root name " + testSuite.getName(); //NOI18N
            TestSuite suite = new JUnitTestSuite(testSuite.getAttributeValue("name"), session);
            session.addSuite(suite);
            CoreManager junitManager = getManagerProvider();
            if (junitManager != null) {
                junitManager.displaySuiteRunning(session, suite);
            }
            File output = new File(outputDir, runningTestClass + suffix + "-output.txt");
            
            @SuppressWarnings("unchecked")
            List<Element> testcases = testSuite.getChildren("testcase"); //NOI18N
            String nameSuffix = reportNameSuffix != null ? "(" + reportNameSuffix + ")" : "";
            for (Element testcase : testcases) {
                //#204480
                String name = testcase.getAttributeValue("name");
                if (name.endsWith(nameSuffix)) {
                    name = name.substring(0, name.length() - nameSuffix.length());
                }
                String displayName = name;
                String methodName = name;
                //In some cases (e.g. parameterized tests) Surefire appends some extra text to the test method name.
                //Remove anything past the first non-dot non-Java identifier character (i.e. anything that would not be part of a legal method name)
                int dotCodePoint = ".".codePointAt(0);
                for (int i = 0; i < name.length(); i++) {
                    int codePoint = name.codePointAt(i);
                    if (!Character.isJavaIdentifierPart(codePoint) && codePoint != dotCodePoint) {
                        methodName = methodName.substring(0, i);
                        break;
                    }
                }

                Testcase test = new JUnitTestcase(methodName, displayName, testType, session);
                Element stdout = testcase.getChild("system-out"); //NOI18N
                // If *-output.txt file exists do not log standard output here to avoid logging it twice.
                // By default surefire only reports standard output for failing testcases.
                if (!output.isFile() && stdout != null) {
                    logText(stdout.getText(), test, false);
                }
                Element failure = testcase.getChild("failure"); //NOI18N
                Status status = Status.PASSED;
                Trouble trouble = null;
                if (failure != null) {
                    status = Status.FAILED;
                    trouble = constructTrouble(failure.getAttributeValue("type"), failure.getAttributeValue("message"), failure.getText(), false);
                }
                Element error = testcase.getChild("error"); //NOI18N
                if (error != null) {
                    status = Status.ERROR;
                    trouble = constructTrouble(error.getAttributeValue("type"), error.getAttributeValue("message"), error.getText(), true);
                }
                Element skipped = testcase.getChild("skipped"); //NOI18N
                if (skipped != null) {
                    status = Status.SKIPPED;
                }
                test.setStatus(status);
                if (trouble != null) {
                    test.setTrouble(trouble);
                }
                String time = testcase.getAttributeValue("time");
                if (time != null) {
                    // the surefire plugin does not print out localised numbers, so use the english format
                    float fl = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(time).floatValue();
                    test.setTimeMillis((long)(fl * 1000));
                }
                String classname = testcase.getAttributeValue("classname");
                if (classname != null) {
                    //#204480
                    if (classname.endsWith(nameSuffix)) {
                        classname = classname.substring(0, classname.length() - nameSuffix.length());
                    }
                    test.setClassName(classname);
                    test.setLocation(test.getClassName().replace('.', '/') + ".java");
                }
                session.addTestCase(test);
            }
            String time = testSuite.getAttributeValue("time");
            // the surefire plugin does not print out localised numbers, so use the english format
            float fl = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(time).floatValue();
            long timeinmilis = (long) (fl * 1000);
            if (junitManager != null) {
                junitManager.displayReport(session, session.getReport(timeinmilis));
            } else { // update report status as a minimum
                session.getReport(timeinmilis).setCompleted(true);
            }
            session.finishSuite(suite);
            if (output.isFile()) {
                if (junitManager != null) {
                    junitManager.displayOutput(session, FileUtils.fileRead(output), false);
                }
            }
        } catch (IOException | ParseException x) {
            LOG.log(Level.WARNING, "parsing " + report, x);
        }
    }

    private File locateOutputDir(String runningTestClass, String suffix, boolean consume) {
        Set<File> outputDirs = runningTestClass2outputDirs.computeIfAbsent(runningTestClass, t -> new HashSet<>());
        LOG.log(Level.FINE, "trying output dirs for class {0}: {1}, sessions: {2}", new Object[] { runningTestClass, outputDirs, outputDir2sessions.keySet() });
        for (File outputDir : outputDir2sessions.keySet()) {
            if (!outputDirs.contains(outputDir)) {
                // When using reuseForks=true and a forkCount value larger than one,
                // the same output is produced many times, so show it only once in Test Results window
                File report = new File(outputDir, "TEST-" + runningTestClass + suffix + ".xml");
                if (report.isFile() && report.lastModified() >= startTimeStamp) { //#219097 ignore results from previous invokation.
                    LOG.log(Level.FINE, "Adding output dir {0} for report {1}", new Object[] { outputDir, report });
                    if (consume) {
                        outputDirs.add(outputDir);
                    }
                    return outputDir;
                } else {
                    LOG.log(Level.FINE, "Report file  {0} exists, but is old; ignoring", report);
                }
            } else {
                LOG.log(Level.FINE, "Output dir already created, not reporting again: {0}", outputDir.getAbsolutePath());
            }
        }
        return null;
    }

    private void logText(String text, Testcase test, boolean failure) {
        StringTokenizer tokens = new StringTokenizer(text, "\n"); //NOI18N
        List<String> lines = new ArrayList<>();
        while (tokens.hasMoreTokens()) {
            lines.add(tokens.nextToken());
        }
        CoreManager junitManager = getManagerProvider();
        if (junitManager != null) {
            junitManager.displayOutput(test.getSession(), text, failure);
        }
        test.addOutputLines(lines);
    }

}
