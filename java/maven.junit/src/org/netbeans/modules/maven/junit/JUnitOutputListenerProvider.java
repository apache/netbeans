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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private TestSession session;
    private String testType;
    private String reportNameSuffix;
    private final Pattern runningPattern;
    private final Pattern outDirPattern2;
    private final Pattern outDirPattern;
    private File outputDir;
    private String runningTestClass;
    private final Set<String> usedNames;
    private final long startTimeStamp;
    
    private static final Logger LOG = Logger.getLogger(JUnitOutputListenerProvider.class.getName());
    private final RunConfig config;
    private boolean surefireRunningInParallel = false;
    private ArrayList<String> runningTestClasses;
    private ArrayList<String> runningTestClassesInParallel;
    
    private static final String GROUP_FILE_NAME = "dir";
    
    public JUnitOutputListenerProvider(RunConfig config) {
        runningPattern = Pattern.compile("(?:\\[(?:INFO|surefire)\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile ("(?:\\[INFO\\] )?Surefire report directory\\: (?<" + GROUP_FILE_NAME + ">.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("(?:\\[INFO\\] )?Setting reports dir\\: (?<" + GROUP_FILE_NAME + ">.*)", Pattern.DOTALL); //NOI18N
        this.config = config;
        usedNames = new HashSet<String>();
        startTimeStamp = System.currentTimeMillis();
        runningTestClasses = new ArrayList<String>();
        runningTestClassesInParallel = new ArrayList<String>();
        surefireRunningInParallel = isSurefireRunningInParallel();
    }
    
    private boolean isSurefireRunningInParallel() {
        // http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html
        // http://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html
        String parallel = PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "parallel", "test", "parallel"); //NOI18N
        if (parallel != null) {
            return true;
        }
        String forkMode = PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "forkMode", "test", "forkMode"); //NOI18N
        if ("perthread".equals(forkMode)) {
            String threadCount = PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "threadCount", "test", "threadCount");
            if (threadCount != null) {
                if (Integer.parseInt(threadCount) > 1) {
                    return true;
                }
            }
        }
        String forkCount = PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
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
            outputDir = new File(match.group(GROUP_FILE_NAME));
            if (session == null) {
                createSession(outputDir);
            }
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            outputDir = new File(match.group(GROUP_FILE_NAME));
            if (session == null) {
                createSession(outputDir);
            }
            return;
        }
        
        if (session == null) {
            return;
        }
        match = runningPattern.matcher(line);
        if (match.matches()) {
            if (surefireRunningInParallel) {
                // make sure results are displayed in case of a failure
                runningTestClassesInParallel.add(match.group(1));
            } else {
                if (runningTestClass != null && outputDir != null) {
                    // match.group(1) should be the FQN of a running test class but let's check to be on the safe side
                    // If the matcher matches it means that we have a new test class running,
                    // if not it probably means that this is user's text, e.g. "Running my cool test", so we can safely ignore it
                    if (!isFullJavaId(match.group(1))) {
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
            if (runningTestClass != null && outputDir != null && !runningTestClasses.contains(runningTestClass)) {
                // When using reuseForks=true and a forkCount value larger than one,
                // the same output is produced many times, so show it only once in Test Results window
                runningTestClasses.add(runningTestClass);
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
            }
        }
    }
    
    private static final String SECONDS_REGEX = "s(?:ec(?:ond)?(?:s|\\(s\\))?)?"; //NOI18N
    private static final String TESTSUITE_STATS_REGEX = "Tests run: +([0-9]+), +Failures: +([0-9]+), +Errors: +([0-9]+), +Skipped: +([0-9]+), +Time elapsed: +(.+)" + SECONDS_REGEX + " - in (.*)";
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
        session = null;
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
                    "test", "${project.build.directory}/surefire-reports"); //NOI81N
            }
            reportNameSuffix = PluginPropertyUtils.getPluginProperty(
                config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SUREFIRE, "reportNameSuffix", "test", //NOI81N
                "surefire.reportNameSuffix"); //NOI81N
            testType = TESTTYPE_UNIT;
	} else if ("mojo-execute#failsafe:integration-test".equals(sequenceId)) {  //NOI81N
	    reportsDirectory = getReportsDirectory(
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_FAILSAFE,
                "integration-test", "${project.build.directory}/failsafe-reports");  //NOI81N
            reportNameSuffix = PluginPropertyUtils.getPluginProperty(
                config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_FAILSAFE, "reportNameSuffix", "integration-test",  //NOI81N
                "surefire.reportNameSuffix");  //NOI81N
            testType = TESTTYPE_INTEGRATION;  //NOI81N
        }
        if (null != reportsDirectory) {
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
            if (null != outputDir) {
                createSession(outputDir);
            }
        }
    }

    private String getReportsDirectory(String groupId, String artifactId, String goal, String fallbackExpression) {
        String reportsDirectory = PluginPropertyUtils.getPluginProperty(config.getMavenProject(),
           groupId, artifactId, "reportsDirectory", goal, null); // NOI18N
        if (null == reportsDirectory) {
            // fallback to default value
            try {
                Object defaultValue = PluginPropertyUtils
                    .createEvaluator(config.getMavenProject())
                    .evaluate(fallbackExpression);
                if (defaultValue instanceof String) {
                    reportsDirectory = (String) defaultValue;
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
        if(testType != null && testType.equals(TESTTYPE_INTEGRATION)) {
            name = Bundle.LBL_TESTTYPE_INTEGRATION(projectId);
        } else {
            name = Bundle.LBL_TESTTYPE_UNIT(projectId);
        }
        int index = 2;
        while (usedNames.contains(name)) {
            if (testType != null && testType.equals(TESTTYPE_INTEGRATION)) {
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
        if (session == null) {
            File fil = FileUtil.normalizeFile(nonNormalizedFile);
	    Project prj = FileOwnerQuery.getOwner(Utilities.toURI(fil));
	    if (prj != null) {
		NbMavenProject mvnprj = prj.getLookup().lookup(NbMavenProject.class);
		if (mvnprj != null) {
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
                    session = new TestSession(createSessionName(mvnprj.getMavenProject().getId()), prj, TestSession.SessionType.TEST);
		    session.setRerunHandler(new RerunHandler() {
			public @Override
			void rerun() {
			    RunUtils.executeMaven(config);
			}

			public @Override
			void rerun(Set<Testcase> tests) {
			    RunConfig brc = RunUtils.cloneRunConfig(config);
			    StringBuilder tst = new StringBuilder();
			    Map<String, Collection<String>> methods = new HashMap<String, Collection<String>>();
                            //#222776 calculate the approximate space the failed tests will occupy on the cmd line.
                            //important on windows which places a limit on the length.
                            int windowslimitcount = 0;
			    for (Testcase tc : tests) {
				//TODO just when is the classname null??
				if (tc.getClassName() != null) {
				    Collection<String> lst = methods.get(tc.getClassName());
				    if (lst == null) {
					lst = new ArrayList<String>();
					methods.put(tc.getClassName(), lst);
                                        windowslimitcount = windowslimitcount + tc.getClassName().length() + 1; // + 1 for ,
				    }
				    lst.add(tc.getName());
                                    windowslimitcount = windowslimitcount + tc.getName().length() + 1; // + 1 for # or +
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
        if (session == null) {
            return;
        }
        if (runningTestClass != null && outputDir != null) {
            generateTest();
        }
        CoreManager junitManager = getManagerProvider();
        if (junitManager != null) {
            junitManager.sessionFinished(session);
        }
        runningTestClass = null;
        outputDir = null;
        session = null;
        surefireRunningInParallel = false;
        runningTestClasses = null;
        runningTestClassesInParallel = null;
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
            List<String> lines = new ArrayList<String>();
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
        // try to get the failed test class. How can this be solved if it is not the first one in the list?
        if(surefireRunningInParallel) {
            if(runningTestClassesInParallel.isEmpty()) {
                // no test case is currently running, so do nothing (is this a more serious failure?)
                return;
            }
            runningTestClass = runningTestClassesInParallel.get(0);
        }
        sequenceEnd(sequenceId, visitor);
    }

    private void generateTest() {
        String suffix = reportNameSuffix;
        if (suffix == null) {
            suffix = "";
        } else {
            //#204480
            suffix = "-" + suffix;
        }
        File report = new File(outputDir, "TEST-" + runningTestClass + suffix + ".xml");
        if (!report.isFile() || report.lastModified() < startTimeStamp) { //#219097 ignore results from previous invokation.
            if(surefireRunningInParallel) { // try waiting a bit to give time for the result file to be created
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (!report.isFile() || report.lastModified() < startTimeStamp) { // and now try again
                return;
            }
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
                return;
            }
            Element testSuite = document.getRootElement();
            assert "testsuite".equals(testSuite.getName()) : "Root name " + testSuite.getName(); //NOI18N
            TestSuite suite = new JUnitTestSuite(testSuite.getAttributeValue("name"), session);
            session.addSuite(suite);
            CoreManager junitManager = getManagerProvider();
            if (junitManager != null) {
                junitManager.displaySuiteRunning(session, suite.getName());
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
        } catch (IOException x) {
            LOG.log(Level.WARNING, "parsing " + report, x);
        } catch (ParseException x) {
            LOG.log(Level.WARNING, "parsing " + report, x);
        }
    }

    private void logText(String text, Testcase test, boolean failure) {
        StringTokenizer tokens = new StringTokenizer(text, "\n"); //NOI18N
        List<String> lines = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            lines.add(tokens.nextToken());
        }
        CoreManager junitManager = getManagerProvider();
        if (junitManager != null) {
            junitManager.displayOutput(session, text, failure);
        }
        test.addOutputLines(lines);
    }

}
