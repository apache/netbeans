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

package org.netbeans.junit;

import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import org.junit.Assert;
import org.netbeans.junit.internal.NbModuleLogHandler;

/**
 * Wraps a test class with proper NetBeans Runtime Container environment.
 * This allows to execute tests in a very similar environment to the 
 * actual invocation in the NetBeans IDE. To use write your test as
 * you are used to and add suite static method:
 * <pre>
 * public class YourTest extends NbTestCase {
 *   public YourTest(String s) { super(s); }
 * 
 *   public static Test suite() {
 *     return NbModuleSuite.create(YourTest.class);
 *   }
 * 
 *   public void testXYZ() { ... }
 *   public void testABC() { ... }
 * }
 * </pre>
 * For more advanced configuration see {@link #emptyConfiguration()} and {@link Configuration}.
 *
 * @since 1.46
 * @author Jaroslav Tulach &lt;jaroslav.tulach@netbeans.org&gt;
 */
public class NbModuleSuite {
    private static final Logger LOG;

    static {
        System.setProperty("org.netbeans.MainImpl.154417", "true");
        LOG = Logger.getLogger(NbModuleSuite.class.getName());
    }

    private NbModuleSuite() {}
    
    
    /** Settings object that allows one to configure execution of
     * whole {@link NbModuleSuite}. Chain the method invocations
     * (each method returns new instance of {@link Configuration})
     * and call {@link #suite()} at the end to generate the final
     * JUnit test class.
     * 
     * @since 1.48
     */
    public static final class Configuration extends Object {
        final List<Item> tests;
        final Class<? extends TestCase> latestTestCaseClass;
        final List<String> clusterRegExp;
        /** each odd is cluster reg exp, each even is module reg exp */
        final List<String> moduleRegExp;
        final List<String> startupArgs;
        final ClassLoader parentClassLoader;
        final boolean reuseUserDir;
        final boolean gui;
        final boolean enableClasspathModules;
        final boolean honorAutoEager;
        final boolean hideExtraModules;
        final Level failOnMessage;
        final Level failOnException;

        private Configuration(
            List<String> clusterRegExp,
            List<String> moduleRegExp,
            List<String> startupArgs,
            ClassLoader parent,
            List<Item> testItems,
            Class<? extends TestCase> latestTestCase,
            boolean reuseUserDir,
            boolean gui,
            boolean enableCPModules,
            boolean honorAutoEager,
            Level failOnMessage,
            Level failOnException,
            boolean hideExtraModules    
        ) {
            this.clusterRegExp = clusterRegExp;
            this.moduleRegExp = moduleRegExp;
            this.startupArgs = startupArgs;
            this.parentClassLoader = parent;
            this.tests = testItems;
            this.reuseUserDir = reuseUserDir;
            this.latestTestCaseClass = latestTestCase;
            this.gui = gui;
            this.enableClasspathModules = enableCPModules;
            this.honorAutoEager = honorAutoEager;
            this.failOnException = failOnException;
            this.failOnMessage = failOnMessage;
            this.hideExtraModules = hideExtraModules;
        }

        static Configuration create(Class<? extends TestCase> clazz) {            
            return new Configuration(
                null, null, null, ClassLoader.getSystemClassLoader().getParent(),
                Collections.<Item>emptyList(), clazz, false, true, true, false
                , null, null, false);
        }
        
        /** Regular expression to match clusters that shall be enabled.
         * To enable all cluster, one can use <code>".*"</code>. To enable
         * ide and java clusters, it is handy to pass in <code>"ide|java"</code>.
         * There is no need to request presence of <code>platform</code> cluster,
         * as that is available all the time by default.
         * <p>
         * Since version 1.55 this method can be called multiple times.
         * 
         * @param regExp regular expression to match cluster names
         * @return clone of this configuration with cluster set to regExp value
         */
        public Configuration clusters(String regExp) {
            ArrayList<String> list = new ArrayList<String>();
            if (clusterRegExp != null) {
                list.addAll(clusterRegExp);
            }
            if (regExp != null) {
                list.add(regExp);
            }
            if (list.isEmpty()) {
                list = null;
            }
            return new Configuration(
                list, moduleRegExp, startupArgs, parentClassLoader, tests,
                latestTestCaseClass, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException, hideExtraModules);
        }

        /** By default only modules on classpath of the test are enabled, 
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules
         * in all clusters pass in <code>".*"</code>. Since 1.55 this method
         * is cummulative.
         * 
         * @param regExp regular expression to match code name base of modules
         * @return clone of this configuration with enable modules set to regExp value
         */
        public Configuration enableModules(String regExp) {
            if (regExp == null) {
                return this;
            }
            return enableModules(".*", regExp);
        }

        /** By default only modules on classpath of the test are enabled,
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules in
         * one cluster, use this method and pass <code>".*"</code> as list of
         * modules. This method is cumulative.
         *
         * @param clusterRegExp regular expression to match clusters
         * @param moduleRegExp regular expression to match code name base of modules
         * @return clone of this configuration with enable modules set to regExp value
         * @since 1.55
         */
        public Configuration enableModules(String clusterRegExp, String moduleRegExp) {
            List<String> arr = new ArrayList<String>();
            if (this.moduleRegExp != null) {
                arr.addAll(this.moduleRegExp);
            }
            arr.add(clusterRegExp);
            arr.add(moduleRegExp);
            return new Configuration(
                this.clusterRegExp, arr, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules, honorAutoEager, failOnMessage, 
                failOnException, hideExtraModules);
        }
        
        /**
         * Appends one or more command line arguments which will be used to 
         * start the application.  Arguments which take a parameter should
         * usually be specified as two separate strings.  Also note that this
         * method cannot handle arguments which must be passed directly to the 
         * JVM (such as memory settings or system properties), those should be 
         * instead specified in the <code>test.run.args</code> property (e.g.
         * in the module's <code>project.properties</code> file).
         * 
         * @param arguments command line arguments to append; each value
         * specified here will be passed a separate argument when starting
         * the application under test.
         * @return clone of this configuration object with the specified
         * command line arguments appended to any which may have already 
         * been present
         * @since 1.67
         */
        public Configuration addStartupArgument(String... arguments) {
            if (arguments == null || arguments.length < 1){
                throw new IllegalStateException("Must specify at least one startup argument");
            }

            List<String> newArgs = new ArrayList<String>();
            if (startupArgs != null) {
                newArgs.addAll(startupArgs);
            }
            newArgs.addAll(Arrays.asList(arguments));

            return new Configuration(
                clusterRegExp, moduleRegExp, newArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules, honorAutoEager, failOnMessage, 
                failOnException, hideExtraModules);
        }

        Configuration classLoader(ClassLoader parent) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parent, tests,
                latestTestCaseClass, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException, hideExtraModules);
        }

        /** Adds new test name, or array of names into the configuration. By 
         * default the suite executes all <code>testXYZ</code> 
         * methods present in the test class
         * (the one passed into {@link NbModuleSuite#createConfiguration(java.lang.Class)}
         * method). However if there is a need to execute just some of them,
         * one can use this method to explicitly enumerate them by subsequent
         * calls to <code>addTest</code> method.
         * @param testNames list names to add to the test execution
         * @return clone of this configuration with testNames test added to the 
         *    list of executed tests
         */
        public Configuration addTest(String... testNames) {
            if (latestTestCaseClass == null){
                throw new IllegalStateException();
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            newTests.add(new Item(true, latestTestCaseClass, testNames));
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules, honorAutoEager, failOnMessage, 
                failOnException, hideExtraModules);
        }
        
        /** Adds new test class to run, together with a list of its methods
         * that shall be executed. The list can be empty and if so, the 
         * the suite executes all <code>testXYZ</code> 
         * methods present in the test class.
         * 
         * @param test the class to also execute in this suite
         * @param testNames list names to add to the test execution
         * @return clone of this configuration with testNames test added to the 
         *    list of executed tests
         * @since 1.50
         */
        public Configuration addTest(Class<? extends TestCase> test, String... testNames) {
            if (test.equals(latestTestCaseClass)){
                return addTest(testNames);
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            addLatest(newTests);
            if ((testNames != null) && (testNames.length != 0)){
                newTests.add(new Item(true, test, testNames));
            }
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                newTests, test, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException, hideExtraModules);
        }
        
        /**
         * Add new {@link  junit.framework.Test} to run. The implementation must
         * have no parameter constructor. TastCase can be also passed as an argument
         * of this method than it's delegated to
         * {@link Configuration#addTest(java.lang.Class, java.lang.String[]) }
         *  
         * @param test Test implementation to add
         * @return clone of this configuration with new Test added to the list
         *  of executed tests
         * @since 1.50
         */
        public Configuration addTest(Class<? extends Test> test) {
            if (TestCase.class.isAssignableFrom(test)){
                Class<? extends TestCase> tc = test.asSubclass(TestCase.class);
                return addTest(tc, new String[0]);
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            newTests.add(new Item(false, test, null));
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
            , failOnMessage, failOnException, hideExtraModules);
        }

        /** By default all modules on classpath are enabled (so you can link
         * with modules that you compile against), this method allows you to
         * disable this feature, which is useful if the test is known to not
         * link against any of classpath classes.
         *
         * @param enable pass false to ignore modules on classpath
         * @return new configuration clone
         * @since 1.56
         */
        public Configuration enableClasspathModules(boolean enable) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enable, honorAutoEager, failOnMessage, failOnException, hideExtraModules);
        }

        /** By default the {@link #enableModules(java.lang.String)} method
         * converts all autoloads into regular modules and enables them. This
         * is maybe useful in certain situations, but does not really mimic the
         * real behaviour of the system when it is executed. Those who need
         * to as closely as possible simulate the real run, can use
         * <code>honorAutoloadEager(true)</code>.
         *
         * @param honor true in case autoloads shall remain autoloads and eager modules eager
         * @return new configuration filled with this data
         * @since 1.57
         */
        public Configuration honorAutoloadEager(boolean honor) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honor
            , failOnMessage, failOnException, hideExtraModules);
        }
        
        /** Allows to limit what modules get enabled in the system.
         * The original purpose of {@link NbModuleSuite} was to enable
         * as much of modules as possible. This was believed to 
         * resemble the real situation in the running application the best.
         * However it turned out there
         * are situations when too much modules can break the system
         * and it is necessary to prevent loading some of them.
         * This method can achieve that.
         * <p>
         * The primary usage is for <em>Ant</em> based harness. It usually
         * contains full installation of various clusters and the application
         * picks just certain modules from that configuration. 
         * <code>hideExtraModules(true)</code> allows exclusion of these
         * modules as well.
         * <p>
         * The usefulness of this method in <em>Maven</em> based environment
         * is not that big. Usually the nbm plugin makes only necessary
         * JARs available. In combination with {@link #enableClasspathModules(boolean) 
         * enableClasspathModules(false)}, it may give you a subset of
         * the Platform loaded in a test. In a
         * Maven-based app declaring a dependency on the whole 
         * org.netbeans.cluster:platform use the following suite expression:
         * <pre>
         * NbModuleSuite.createConfiguration(ApplicationTest.class).
         *     gui(true).
         *     hideExtraModules(true).
         *     enableModules("(?!org.netbeans.modules.autoupdate|org.netbeans.modules.core.kit|org.netbeans.modules.favorites).*").
         *     enableClasspathModules(false).
         *     suite();
         * </pre>
         * 
         * @param hide true if all enabled not explicitly requested modules should
         *   be hidden
         * @return new configuration with holds the provided parameter
         * @since 1.72
         */
        public Configuration hideExtraModules(boolean hide) {
            return new Configuration(
                    clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                    tests, latestTestCaseClass, reuseUserDir,
                    gui, enableClasspathModules, honorAutoEager, failOnMessage, 
                    failOnException, hide);
        }

        /** Fails if there is a message sent to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @param level the minimal level of the message
         * @return new configuration filled with this data
         * @since 1.58
         */
        public Configuration failOnMessage(Level level) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
                , level, failOnException, hideExtraModules);
        }

        /** Fails if there is an exception reported to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @param level the minimal level of the message
         * @return new configuration filled with this data
         * @since 1.58
         */
        public Configuration failOnException(Level level) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
                , failOnMessage, level, hideExtraModules);
        }

        private void addLatest(List<Item> newTests) {
            if (latestTestCaseClass == null){
                return;
            }
            for (Item item : newTests) {
                if (item.clazz.equals(latestTestCaseClass)){
                    return;
                }
            }
            newTests.add(new Item(true, latestTestCaseClass, null));
        }

        private Configuration getReady() {
            List<Item> newTests = new ArrayList<Item>(tests);
            addLatest(newTests);

            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException, hideExtraModules);
        }
        
        /** Should the system run with GUI or without? The default behaviour
         * does not prevent any module to show UI. If <code>false</code> is 
         * used, then the whole system is instructed with <code>--nogui</code>
         * option that it shall run as much as possible in invisible mode. As
         * a result, the main window is not shown after the start, for example.
         * 
         * @param gui true or false
         * @return clone of this configuration with gui mode associated
         */
        public Configuration gui(boolean gui) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException, hideExtraModules);
        }

        /**
         * Enables or disables userdir reuse. By default it is disabled.
         * @param reuse true or false
         * @return clone of this configuration with userdir reuse mode associated
         * @since 1.52
         */
        public Configuration reuseUserDir(boolean reuse) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentClassLoader, tests,
                latestTestCaseClass, reuse, gui, enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException, hideExtraModules);
        }

        /**
         * Sets the parent ClassLoader on which the NB platform should start.
         * @param parentCL the parent ClassLoader
         * @return clone of this configuration with the parent ClassLoader set
         * @since 1.91
         */
        public Configuration parentClassLoader(ClassLoader parentCL) {
            return new Configuration(
                clusterRegExp, moduleRegExp, startupArgs, parentCL, tests,
                latestTestCaseClass, reuseUserDir, gui, enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException, hideExtraModules);
        }

        /**
         * Creates a test suite from this configuration.
         * Same as {@link #create(org.netbeans.junit.NbModuleSuite.Configuration)} but more fluid.
         * @return a suite ready for returning from a {@code public static Test suite()} method
         * @since org.netbeans.modules.junit/1 1.70
         */
        public Test suite() {
            return new S(this);
        }

    }

    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. The set of enabled modules is going to be
     * determined from the actual classpath of a module, which is common
     * when in all NetBeans tests. All other modules are kept disabled.
     * In addition,it allows one limit the clusters that shall be made available.
     * For example <code>ide|java</code> will start the container just
     * with platform, ide and java clusters.
     * 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param clustersRegExp regexp to apply to name of cluster to find out if it is supposed to be included
     *    in the runtime container setup or not
     * @param moduleRegExp by default all modules on classpath are turned on,
     *    however this regular expression can specify additional ones. If not
     *    null, the specified cluster will be searched for modules with such
     *    codenamebase and those will be turned on
     * @return runtime container ready test
     */
    public static Test create(Class<? extends TestCase> clazz, String clustersRegExp, String moduleRegExp) {
        return Configuration.create(clazz).clusters(clustersRegExp).enableModules(moduleRegExp).suite();
    }
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. The set of enabled modules is going to be
     * determined from the actual classpath of a module, which is common
     * when in all NetBeans tests. All other modules are kept disabled.
     * In addition,it allows one limit the clusters that shall be made available.
     * For example <code>ide|java</code> will start the container just
     * with platform, ide and java clusters.
     * 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param clustersRegExp regexp to apply to name of cluster to find out if it is supposed to be included
     *    in the runtime container setup or not
     * @param moduleRegExp by default all modules on classpath are turned on,
     *    however this regular expression can specify additional ones. If not
     *    null, the specified cluster will be searched for modules with such
     *    codenamebase and those will be turned on
     * @param tests names of test methods to execute from the <code>clazz</code>, if
     *    no test methods are specified, all tests in the class are executed
     * @return runtime container ready test
     * @since 1.49
     */
    public static Test create(Class<? extends TestCase> clazz, String clustersRegExp, String moduleRegExp, String... tests) {
        Configuration conf = Configuration.create(clazz).clusters(clustersRegExp).enableModules(moduleRegExp);
        if (tests.length > 0) {
            conf = conf.addTest(tests);
        } 
        return conf.suite();
    }
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. All modules, in all clusters, 
     * in the tested applicationwill be included in the test. 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param tests names of test methods to execute from the <code>clazz</code>, if
     *    no test methods are specified, all tests in the class are executed
     * @return runtime container ready test
     * @since 1.49
     */
    public static Test allModules(Class<? extends TestCase> clazz, String... tests) {
        return create(clazz, ".*", ".*", tests);
    }
    
    /** Creates default configuration wrapping a class that can be executed
     * with the {@link NbModuleSuite} support.
     * 
     * @param clazz the class to test, the actual instances will be created
     *   for the class of the same name, but loaded by different classloader
     * @return config object prefilled with default values; the defaults may
     *   be altered with its addition instance methods
     * @since 1.48
     */
    public static Configuration createConfiguration(Class<? extends TestCase> clazz) {
        return Configuration.create(clazz);
    }
    
    /** Creates empty configuration without any class assiciated. You need
     * to call {@link Configuration#addTest(java.lang.Class, java.lang.String[])}
     * then to register proper test classes.
     * 
     * @return config object prefilled with default values; the defaults may
     *   be altered with its addition instance methods
     * @since 1.50
     */
    public static Configuration emptyConfiguration() {
        return Configuration.create(null);
    }
    
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. This method allows better
     * customization and control over the executed set of tests.
     * Wraps the provided class into a test that set ups properly the
     * testing environment, read more in {@link Configuration}.
     * 
     * 
     * @param config the configuration for the test
     * @return runtime container ready test
     * @since 1.48
     * @see Configuration#suite
     */
    public static Test create(Configuration config) {
        return config.suite();
    }

    private static final class Item {
        boolean isTestCase;
        Class<?> clazz;
        String[] fileNames;

        public Item(boolean isTestCase, Class<?> clazz, String[] fileNames) {
            this.isTestCase = isTestCase;
            this.clazz = clazz;
            this.fileNames = fileNames;
        }
    }

    static final class S extends NbTestSuite {
        final Configuration config;
        private static int invocations;
        private static File lastUserDir;
        private int testCount = 0; 
        
        public S(Configuration config) {
            this.config = config.getReady();
        }

        @Override
        public int countTestCases() {
            return testCount;
        }

        @Override
        public void run(final TestResult result) {
            result.runProtected(this, new Protectable() {
                public @Override void protect() throws Throwable {
                    ClassLoader before = Thread.currentThread().getContextClassLoader();
                    try {
                        runInRuntimeContainer(result);
                    } finally {
                        Thread.currentThread().setContextClassLoader(before);
                    }
                }
            });
        }
        
        private static String[] tokenizePath(String path) {
            List<String> l = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
            char dosHack = '\0';
            char lastDelim = '\0';
            int delimCount = 0;
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                if (s.length() == 0) {
                    // Strip empty components.
                    continue;
                }
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if (c == ':' || c == ';') {
                        // Just a delimiter.
                        lastDelim = c;
                        delimCount++;
                        continue;
                    }
                }
                if (dosHack != '\0') {
                    // #50679 - "C:/something" is also accepted as DOS path
                    if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                        // We had a single letter followed by ':' now followed by \something or /something
                        s = "" + dosHack + ':' + s;
                        // and use the new token with the drive prefix...
                    } else {
                        // Something else, leave alone.
                        l.add(Character.toString(dosHack));
                        // and continue with this token too...
                    }
                    dosHack = '\0';
                }
                // Reset count of # of delimiters in a row.
                delimCount = 0;
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        // Probably a DOS drive letter. Leave it with the next component.
                        dosHack = c;
                        continue;
                    }
                }
                l.add(s);
            }
            if (dosHack != '\0') {
                //the dosHack was the last letter in the input string (not followed by the ':')
                //so obviously not a drive letter.
                //Fix for issue #57304
                l.add(Character.toString(dosHack));
            }
            return l.toArray(new String[0]);
        }

        static void findClusters(Collection<File> clusters, List<String> regExps) throws IOException {
            File plat = findPlatform().getCanonicalFile();
            String selectiveClusters = System.getProperty("cluster.path.final"); // NOI18N
            Set<File> path;
            if (selectiveClusters != null) {
                path = new TreeSet<File>();
                for (String p : tokenizePath(selectiveClusters)) {
                    File f = new File(p);
                    path.add(f.getCanonicalFile());
                }
            } else {
                File parent;
                String allClusters = System.getProperty("all.clusters"); // #194794
                if (allClusters != null) {
                    parent = new File(allClusters);
                } else {
                    parent = plat.getParentFile();
                }
                path = new TreeSet<File>(Arrays.asList(parent.listFiles()));
            }
            for (String c : regExps) {
                for (File f : path) {
                    if (f.equals(plat)) {
                        continue;
                    }
                    if (!f.getName().matches(c)) {
                        continue;
                    }
                    File m = new File(new File(f, "config"), "Modules");
                    if (m.exists()) {
                        clusters.add(f);
                    }
                }
            }
        }

        private void runInRuntimeContainer(TestResult result) throws Exception {
            System.getProperties().remove("netbeans.dirs");
            File platform = findPlatform();
            List<URL> bootCP = new ArrayList<URL>();
            List<File> dirs = new ArrayList<File>();
            dirs.add(new File(platform, "lib"));
            
            File jdkHome = new File(System.getProperty("java.home"));
            if (new File(jdkHome.getParentFile(), "lib").exists()) {
                jdkHome = jdkHome.getParentFile();
            }
            dirs.add(new File(jdkHome, "lib"));

            //in case we're running code coverage, load the coverage libraries
            if (System.getProperty("code.coverage.classpath") != null) {
                dirs.add(new File(System.getProperty("code.coverage.classpath")));
            }

            for (File dir: dirs) {
                File[] jars = dir.listFiles();
                if (jars != null) {
                    for (File jar : jars) {
                        if (jar.getName().endsWith(".jar")) {
                            bootCP.add(toURI(jar).toURL());
                        }
                    }
                }
            }
            
            // loader that does not see our current classloader
            JUnitLoader junit = new JUnitLoader(config.parentClassLoader, NbModuleSuite.class.getClassLoader());
            URLClassLoader loader = new URLClassLoader(bootCP.toArray(new URL[0]), junit);
            Class<?> main = loader.loadClass("org.netbeans.Main"); // NOI18N
            Assert.assertEquals("Loaded by our classloader", loader, main.getClassLoader());
            Method m = main.getDeclaredMethod("main", String[].class); // NOI18N            

            System.setProperty("java.util.logging.config", "-");
            System.setProperty("netbeans.logger.console", "true");
            if (System.getProperty("netbeans.logger.noSystem") == null) {
                System.setProperty("netbeans.logger.noSystem", "true");
            }
            System.setProperty("netbeans.home", platform.getPath());
            System.setProperty("netbeans.full.hack", "true");

            String branding = System.getProperty("branding.token"); // NOI18N
            if (branding != null) {
                try {
                    Method setBranding = loader.loadClass("org.openide.util.NbBundle").getMethod("setBranding", String.class); // NOI18N
                    setBranding.invoke(null, branding);
                } catch (Throwable ex) {
                    if (ex instanceof InvocationTargetException) {
                        ex = ((InvocationTargetException)ex).getTargetException();
                    }
                    LOG.log(Level.WARNING, "Cannot set branding to " + branding, ex); // NOI18N
                }
            }
            
            File ud = new File(new File(Manager.getWorkDirPath()), "userdir" + invocations++);
            if (config.reuseUserDir) {
                ud = lastUserDir != null ? lastUserDir : ud;
            } else {
                NbTestCase.deleteSubFiles(ud);
            }
            lastUserDir = ud;
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());

            TreeSet<String> modules = new TreeSet<String>();
            if (config.enableClasspathModules) {
                modules.addAll(findEnabledModules(NbTestSuite.class.getClassLoader()));
            }
            modules.add("org.openide.filesystems");
            modules.add("org.openide.modules");
            modules.add("org.openide.util");
            modules.add("org.openide.util.ui");
            modules.remove("org.netbeans.insane");
            modules.add("org.netbeans.core.startup");
            modules.add("org.netbeans.core.startup.base");
            modules.add("org.netbeans.bootstrap");
            turnModules(ud, !config.honorAutoEager, modules, config.moduleRegExp, platform);
            if (config.enableClasspathModules) {
                turnClassPathModules(ud, NbTestSuite.class.getClassLoader());
            }

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (File f : findClusters()) {
                turnModules(ud, !config.honorAutoEager, modules, config.moduleRegExp, f);
                sb.append(sep);
                sb.append(f.getPath());
                sep = File.pathSeparator;
            }
            System.setProperty("netbeans.dirs", sb.toString());

            if (config.hideExtraModules) {
                Collection<File> clusters = new LinkedHashSet<File>();
                if (config.clusterRegExp != null) {
                    findClusters(clusters, config.clusterRegExp);
                }
                clusters.add(findPlatform());
                for (File f : clusters) {
                    disableModules(ud, f);
                }
            }

            System.setProperty("netbeans.security.nocheck", "true");

            List<Class<?>> allClasses = new ArrayList<Class<?>>(config.tests.size());
            for (Item item : config.tests) {
                allClasses.add(item.clazz);
            }
            preparePatches(System.getProperty("java.class.path"), System.getProperties(), allClasses.toArray(new Class<?>[0]));
            
            List<String> args = new ArrayList<String>();
            args.add("--nosplash");
            if (!config.gui) {
                args.add("--nogui");
            }

            if (config.startupArgs != null) {
                args.addAll(config.startupArgs);
            }

            Test handler = NbModuleLogHandler.registerBuffer(config.failOnMessage, config.failOnException);
            m.invoke(null, (Object)args.toArray(new String[0]));

            ClassLoader global = Thread.currentThread().getContextClassLoader();
            Assert.assertNotNull("Global classloader is initialized", global);
            ClassLoader testLoader = global;
            try {
                testLoader.loadClass("junit.framework.Test");
                testLoader.loadClass("org.netbeans.junit.NbTestSuite");
                NbTestSuite toRun = new NbTestSuite();
                
                for (Item item : config.tests) {
                    if (item.isTestCase){
                        Class<? extends TestCase> sndClazz =
                            testLoader.loadClass(item.clazz.getName()).asSubclass(TestCase.class);
                        if (item.fileNames == null) {
                            MethodOrder.orderMethods(sndClazz, null);
                            toRun.addTest(new NbTestSuiteLogCheck(sndClazz));
                        } else {
                            NbTestSuite t = new NbTestSuiteLogCheck();
                            t.addTests(sndClazz, item.fileNames);
                            toRun.addTest(t);
                        }
                    }else{
                        Class<? extends Test> sndClazz =
                            testLoader.loadClass(item.clazz.getName()).asSubclass(Test.class);
                        toRun.addTest(sndClazz.getDeclaredConstructor().newInstance());
                    }
                }

                if (handler != null) {
                    toRun.addTest(handler);
                }

                testCount = toRun.countTestCases();
                toRun.run(result);
            } catch (ClassNotFoundException ex) {
                result.addError(this, ex);
            } catch (NoClassDefFoundError ex) {
                result.addError(this, ex);
            }
            if (handler != null) {
                NbModuleLogHandler.finish();
            }
            String n;
            if (config.latestTestCaseClass != null) {
                n = config.latestTestCaseClass.getName();
            } else {
                n = "exit"; // NOI18N
            }
            TestResult shutdownResult = new Shutdown(global, n).run();
            if (shutdownResult.failureCount() > 0) {
                final TestFailure tf = shutdownResult.failures().nextElement();
                result.addFailure(tf.failedTest(), (AssertionFailedError)tf.thrownException());
            }
            if (shutdownResult.errorCount() > 0) {
                final TestFailure tf = shutdownResult.errors().nextElement();
                result.addError(tf.failedTest(), tf.thrownException());
            }
        }

        static File findPlatform() {
            String clusterPath = System.getProperty("cluster.path.final"); // NOI18N
            if (clusterPath != null) {
                for (String piece : tokenizePath(clusterPath)) {
                    File d = new File(piece);
                    if (d.getName().matches("platform\\d*")) {
                        return d;
                    }
                }
            }
            String allClusters = System.getProperty("all.clusters"); // #194794
            if (allClusters != null) {
                File d = new File(allClusters, "platform"); // do not bother with old numbered variants
                if (d.isDirectory()) {
                    return d;
                }
            }
            try {
                Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
                File util = toFile(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
                Assert.assertTrue("Util exists: " + util, util.exists());

                return util.getParentFile().getParentFile();
            } catch (Exception ex) {
                try {
                    File nbjunit = toFile(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    File harness = nbjunit.getParentFile().getParentFile();
                    Assert.assertEquals(nbjunit + " is in a folder named 'harness'", "harness", harness.getName());
                    TreeSet<File> sorted = new TreeSet<File>();
                    for (File p : harness.getParentFile().listFiles()) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                    Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                    return sorted.last();
                } catch (Exception ex2) {
                    Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
                }
                return null;
            }
        }

        private File[] findClusters() throws IOException {
            Collection<File> clusters = new LinkedHashSet<File>();
            if (config.clusterRegExp != null) {
                findClusters(clusters, config.clusterRegExp);
            }

            if (config.enableClasspathModules) {
                // find "cluster" from
                // k/o.n.m.a.p.N/csam/testModule/build/cluster/modules/org-example-testModule.jar
                // tested in apisupport.project
                for (String s : tokenizePath(System.getProperty("java.class.path"))) {
                    File module = new File(s);
                    File cluster = module.getParentFile().getParentFile();
                    File m = new File(new File(cluster, "config"), "Modules");
                    if (m.exists() || cluster.getName().equals("cluster")) {
                        clusters.add(cluster);
                    }
                }
            }
            return clusters.toArray(new File[0]);
        }

        private static String cnb(Manifest m) {
            String cn = m.getMainAttributes().getValue("OpenIDE-Module");
            return cn != null ? cn.replaceFirst("/\\d+", "") : null;
        }
        
        /** Looks for all modules on classpath of given loader and builds 
         * their list from them.
         */
        static Set<String> findEnabledModules(ClassLoader loader) throws IOException {
            Set<String> cnbs = new TreeSet<String>();

            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                InputStream is = url.openStream();
                try {
                    String cnb = cnb(new Manifest(is));
                    if (cnb != null) {
                        cnbs.add(cnb);
                    }
                } finally {
                    is.close();
                }
            }

            return cnbs;
        }
        private static final Set<String> pseudoModules = new HashSet<String>(Arrays.asList(
                "org.openide.util",
                "org.openide.util.ui",
                "org.openide.util.lookup",
                "org.openide.modules",
                "org.netbeans.bootstrap",
                "org.openide.filesystems",
                "org.openide.filesystems.compat8",
                "org.netbeans.core.startup",
                "org.netbeans.core.startup.base",
                "org.netbeans.libs.asm"));
        static void turnClassPathModules(File ud, ClassLoader loader) throws IOException {
            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                Manifest m;
                InputStream is = url.openStream();
                try {
                    m = new Manifest(is);
                } catch (IOException x) {
                    throw new IOException("parsing " + url + ": " + x, x);
                } finally {
                    is.close();
                }
                String cnb = cnb(m);
                if (cnb != null) {
                    File jar = jarFromURL(url);
                    if (jar == null) {
                        continue;
                    }
                    if (pseudoModules.contains(cnb)) {
                        // Otherwise will get DuplicateException.
                        continue;
                    }
                    String mavenCP = m.getMainAttributes().getValue("Maven-Class-Path");
                    if (mavenCP != null) {
                        // Do not use ((URLClassLoader) loader).getURLs() as this does not work for Surefire Booter.
                        jar = rewrite(jar, mavenCP.split(" "), System.getProperty("java.class.path"));
                    }
                    String xml =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"" + cnb + "\">\n" +
"    <param name=\"autoload\">false</param>\n" +
"    <param name=\"eager\">false</param>\n" +
"    <param name=\"enabled\">true</param>\n" +
"    <param name=\"jar\">" + jar + "</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"</module>\n";
                    
                    File conf = new File(new File(ud, "config"), "Modules");
                    conf.mkdirs();
                    File f = new File(conf, cnb.replace('.', '-') + ".xml");
                    writeModule(f, xml);
                }
            }
        }
        private static File rewrite(File jar, String[] mavenCP, String classpath) throws IOException { // #190992
            String[] classpathEntries = tokenizePath(classpath);
            StringBuilder classPathHeader = new StringBuilder();
            for (String artifact : mavenCP) {
                String[] grpArtVers = artifact.split(":");
                String partOfPath = File.separatorChar + grpArtVers[0].replace('.', File.separatorChar) + File.separatorChar + grpArtVers[1] + File.separatorChar + grpArtVers[2] + File.separatorChar + grpArtVers[1] + '-' + grpArtVers[2];
                File dep = null;
                for (String classpathEntry : classpathEntries) {
                    if (classpathEntry.endsWith(".jar") && classpathEntry.contains(partOfPath)) {
                        dep = new File(classpathEntry);
                        break;
                    }
                }
                if (dep == null) {
                    throw new IOException("no match for " + artifact + " found in " + classpath);
                }
                File depCopy = Files.createTempFile(artifact.replace(':', '-') + '-', ".jar").toFile();
                depCopy.deleteOnExit();
                NbTestCase.copytree(dep, depCopy);
                if (classPathHeader.length() > 0) {
                    classPathHeader.append(' ');
                }
                classPathHeader.append(depCopy.getName());
            }
            String n = jar.getName();
            int dot = n.lastIndexOf('.');
            File jarCopy = Files.createTempFile(n.substring(0, dot) + '-', n.substring(dot)).toFile();
            jarCopy.deleteOnExit();
            InputStream is = new FileInputStream(jar);
            try {
                OutputStream os = new FileOutputStream(jarCopy);
                try {
                    JarInputStream jis = new JarInputStream(is);
                    Manifest mani = new Manifest(jis.getManifest());
                    mani.getMainAttributes().putValue("Class-Path", classPathHeader.toString());
                    JarOutputStream jos = new JarOutputStream(os, mani);
                    JarEntry entry;
                    while ((entry = jis.getNextJarEntry()) != null) {
                        if (entry.getName().matches("META-INF/.+[.]SF")) {
                            throw new IOException("cannot handle signed JARs");
                        }
                        jos.putNextEntry(entry);
                        byte[] buf = new byte[4092];
                        for (;;) {
                            int more = jis.read(buf, 0, buf.length);
                            if (more == -1) {
                                break;
                            }
                            jos.write(buf, 0, more);
                        }
                    }
                    jis.close();
                    jos.close();
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
            return jarCopy;
        }
        
        private static Pattern MANIFEST = Pattern.compile("jar:(file:.*)!/META-INF/MANIFEST.MF", Pattern.MULTILINE);
        private static File jarFromURL(URL u) {
            Matcher m = MANIFEST.matcher(u.toExternalForm());
            if (m.matches()) {
                return toFile(URI.create(m.group(1)));
            } else {
                if (!u.getProtocol().equals("file")) {
                    throw new IllegalStateException(u.toExternalForm());
                } else {
                    return null;
                }
            }
        }

        /**
         * JDK 7
         */
        private static Method fileToPath, pathToUri, pathsGet, pathToFile;

        static {
            try {
                fileToPath = File.class.getMethod("toPath");
            } catch (NoSuchMethodException x) {
                // fine, JDK 6
            }
            if (fileToPath != null) {
                try {
                    Class<?> path = Class.forName("java.nio.file.Path");
                    pathToUri = path.getMethod("toUri");
                    pathsGet = Class.forName("java.nio.file.Paths").getMethod("get", URI.class);
                    pathToFile = path.getMethod("toFile");
                } catch (Exception x) {
                    throw new ExceptionInInitializerError(x);
                }
            }
        }
        private static File toFile(URI u) throws IllegalArgumentException {
            if (pathsGet != null) {
                try {
                    return (File) pathToFile.invoke(pathsGet.invoke(null, u));
                } catch (Exception x) {
                    LOG.log(Level.FINE, "could not convert " + u + " to File", x);
                }
            }
            String host = u.getHost();
            if (host != null && !host.isEmpty() && "file".equals(u.getScheme())) {
                return new File("\\\\" + host + u.getPath().replace('/', '\\'));
            }
            return new File(u);
        }
        private static URI toURI(File f) {
            if (fileToPath != null) {
                try {
                    URI u = (URI) pathToUri.invoke(fileToPath.invoke(f));
                    if (u.toString().startsWith("file:///")) { // #214131 workaround
                        u = new URI(/* "file" */u.getScheme(), /* null */u.getUserInfo(), /* null (!) */u.getHost(), /* -1 */u.getPort(), /* "/..." */u.getPath(), /* null */u.getQuery(), /* null */u.getFragment());
                    }
                    return u;
                } catch (Exception x) {
                    LOG.log(Level.FINE, "could not convert " + f + " to URI", x);
                }
            }
            String path = f.getAbsolutePath();
            if (path.startsWith("\\\\")) { // UNC
                if (!path.endsWith("\\") && f.isDirectory()) {
                    path += "\\";
                }
                try {
                    return new URI("file", null, path.replace('\\', '/'), null);
                } catch (URISyntaxException x) {
                    LOG.log(Level.FINE, "could not convert " + f + " to URI", x);
                }
            }
            return f.toURI();
        }
        
        static void preparePatches(String path, Properties prop, Class<?>... classes) throws URISyntaxException {
            Pattern tests = Pattern.compile(".*\\" + File.separator + "([^\\" + File.separator + "]+)\\" + File.separator + "tests\\.jar");
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String jar : tokenizePath(path)) {
                Matcher m = tests.matcher(jar);
                if (m.matches()) {
                    // in case we need it one day, let's add a switch to Configuration
                    // and choose the following line instead of netbeans.systemclassloader.patches
                    // prop.setProperty("netbeans.patches." + m.group(1).replace('-', '.'), jar);
                    sb.append(sep).append(jar);
                    sep = File.pathSeparator;
                }
            }
            Set<URL> uniqueURLs = new HashSet<URL>();
            for (Class<?> c : classes) {
                URL test = c.getProtectionDomain().getCodeSource().getLocation();
                Assert.assertNotNull("URL found for " + c, test);
                if (uniqueURLs.add(test)) {
                    sb.append(sep).append(toFile(test.toURI()).getPath());
                    sep = File.pathSeparator;
                }
            }
            prop.setProperty("netbeans.systemclassloader.patches", sb.toString());
        }

        private static String asString(InputStream is, boolean close) throws IOException {
            StringBuilder builder = new StringBuilder();

            byte[] bytes = new byte[4096];
            try {
                for (int i; (i = is.read(bytes)) != -1;) {
                    builder.append(new String(bytes, 0, i, StandardCharsets.UTF_8));
                }
            } finally {
                if (close) {
                    is.close();
                }
            }
            for (;;) {
                int index = builder.indexOf("\r\n");
                if (index == -1) {
                    break;
                }
                builder.deleteCharAt(index);
            }
            return builder.toString();
        }

        private void disableModules(File ud, File cluster) throws IOException {
            File confDir = new File(new File(cluster, "config"), "Modules");
            for (File c : confDir.listFiles()) {
                if (!isModuleEnabled(c)) {
                    continue;
                }
                File udC = new File(new File(new File(ud, "config"), "Modules"), c.getName());
                if (!udC.exists()) {
                    File hidden = new File(udC.getParentFile(), c.getName() + "_hidden");
                    hidden.createNewFile();
                }
            }
        }
        
        private static boolean isModuleEnabled(File config) throws IOException {
            String xml = asString(new FileInputStream(config), true);
            Matcher matcherEnabled = ENABLED.matcher(xml);
            if (matcherEnabled.find()) {
                return "true".equals(matcherEnabled.group(1));
            }
            return false;
        }

        private static class Shutdown extends NbTestCase {
            Shutdown(ClassLoader global, String testClass) throws Exception {
                super("shuttingDown[" + testClass + "]");
                this.global = global;
            }

            @Override
            protected int timeOut() {
                return 180000; // 3 minutes for a shutdown
            }

            @Override
            protected Level logLevel() {
                return Level.FINE;
            }

            @Override
            protected String logRoot() {
                return "org.netbeans.core.NbLifecycleManager"; // NOI18N
            }
            
            private static void waitForAWT() throws InvocationTargetException, InterruptedException {
                final CountDownLatch cdl = new CountDownLatch(1);
                SwingUtilities.invokeLater(new Runnable() {
                    public @Override void run() {
                        cdl.countDown();
                    }
                });
                cdl.await(10, TimeUnit.SECONDS);
            }
            private final ClassLoader global;

            @Override
            protected void runTest() throws Throwable {
                JFrame shutDown;
                try {
                    shutDown = new JFrame("Shutting down NetBeans...");
                    shutDown.setBounds(new Rectangle(-100, -100, 50, 50));
                    shutDown.setVisible(true);
                } catch (HeadlessException ex) {
                    shutDown = null;
                }
                
                Class<?> lifeClazz = global.loadClass("org.openide.LifecycleManager"); // NOI18N
                Method getDefault = lifeClazz.getMethod("getDefault"); // NOI18N
                Method exit = lifeClazz.getMethod("exit");
                LOG.log(Level.FINE, "Closing via LifecycleManager loaded by {0}", lifeClazz.getClassLoader());
                Object life = getDefault.invoke(null);
                if (!life.getClass().getName().startsWith("org.openide.LifecycleManager")) { // NOI18N
                    System.setProperty("netbeans.close.no.exit", "true"); // NOI18N
                    System.setProperty("netbeans.close", "true"); // NOI18N
                    exit.invoke(life);
                    waitForAWT();
                    System.getProperties().remove("netbeans.close"); // NOI18N
                    System.getProperties().remove("netbeans.close.no.exit"); // NOI18N
                }
                
                if (shutDown != null) {
                    shutDown.setVisible(false);
                }
            }
        }
        

        private static final class JUnitLoader extends ClassLoader {
            private final ClassLoader junit;

            public JUnitLoader(ClassLoader parent, ClassLoader junit) {
                super(parent);
                this.junit = junit;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (isUnit(name)) {
                    return junit.loadClass(name);
                }
                return super.findClass(name);
            }

            @Override
            public URL findResource(String name) {
                if (isUnit(name)) {
                    return junit.getResource(name);
                }
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResource("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
                }
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                if (isUnit(name)) {
                    return junit.getResources(name);
                }
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResources("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
                }
                return super.findResources(name);
            }

            private boolean isUnit(String res) {
                if (res.startsWith("junit")) {
                    return true;
                }
                if (res.startsWith("org.junit") || res.startsWith("org/junit")) {
                    return true;
                }
                if (res.startsWith("org.hamcrest") || res.startsWith("org/hamcrest")) {
                    return true;
                }
                if (res.startsWith("org.netbeans.junit") || res.startsWith("org/netbeans/junit")) {
                    if (res.startsWith("org.netbeans.junit.ide") || res.startsWith("org/netbeans/junit/ide")) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }

        private static Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);
        private static Pattern AUTO = Pattern.compile("<param name=[\"']autoload[\"']>([^<]*)</param>", Pattern.MULTILINE);
        private static Pattern EAGER = Pattern.compile("<param name=[\"']eager[\"']>([^<]*)</param>", Pattern.MULTILINE);
        
        private static void turnModules(File ud, boolean autoloads, TreeSet<String> modules, List<String> regExp, File... clusterDirs) throws IOException {
            if (regExp == null) {
                return;
            }
            File config = new File(new File(ud, "config"), "Modules");
            config.mkdirs();

            Iterator<String> it = regExp.iterator();
            for (;;) {
                if (!it.hasNext()) {
                    break;
                }
                String clusterReg = it.next();
                String moduleReg = it.next();
                Pattern modPattern = Pattern.compile(moduleReg);
                for (File c : clusterDirs) {
                    if (!c.getName().matches(clusterReg)) {
                        continue;
                    }

                    File modulesDir = new File(new File(c, "config"), "Modules");
                    File[] allModules = modulesDir.listFiles();
                    if (allModules == null) {
                        continue;
                    }
                    for (File m : allModules) {
                        String n = m.getName();
                        if (n.endsWith(".xml")) {
                            n = n.substring(0, n.length() - 4);
                        }
                        n = n.replace('-', '.');

                        String xml = asString(new FileInputStream(m), true);

                        boolean contains = modules.contains(n);
                        if (!contains && modPattern != null) {
                            contains = modPattern.matcher(n).matches();
                        }
                        if (!contains) {
                            continue;
                        }
                        enableModule(xml, autoloads, contains, new File(config, m.getName()));
                    }
                }
            }
        }
        
        private static void enableModule(String xml, boolean autoloads, boolean enable, File target) throws IOException {
            boolean toEnable = false;
            {
                  Matcher matcherEnabled = ENABLED.matcher(xml);
                if (matcherEnabled.find()) {
                    toEnable = "false".equals(matcherEnabled.group(1));
                }
                Matcher matcherEager = EAGER.matcher(xml);
                if (matcherEager.find()) {
                    if ("true".equals(matcherEager.group(1))) {
                        return;
                    }
                }
                if (!autoloads) {
                    Matcher matcherAuto = AUTO.matcher(xml);
                    if (matcherAuto.find()) {
                        if ("true".equals(matcherAuto.group(1))) {
                            return;
                        }
                    }
                }
                if (toEnable) {
                    assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;
                    try {
                        String out = xml.substring(0, matcherEnabled.start(1)) + (enable ? "true" : "false") + xml.substring(matcherEnabled.end(1));
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw new IOException("Unparsable:\n" + xml, ex);
                    }
                }
            }
            {
                Matcher matcherEager = AUTO.matcher(xml);
                if (matcherEager.find()) {
                    int begin = xml.indexOf("<param name=\"autoload");
                    int end = xml.indexOf("<param name=\"jar");
                    String middle = "<param name=\"autoload\">false</param>\n" + "    <param name=\"eager\">false</param>\n" + "    <param name=\"enabled\">true</param>\n" + "    ";
                    String out = xml.substring(0, begin) + middle + xml.substring(end);
                    try {
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw new IOException("Unparsable:\n" + xml, ex);
                    }
                }
            }
        }

        private static void writeModule(File file, String xml) throws IOException {
            String previous;
            if (file.exists()) {
                previous = asString(new FileInputStream(file), true);
                if (previous.equals(xml)) {
                    return;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "rewrite module file: {0}", file);
                    charDump(Level.FINEST, previous);
                    LOG.finest("new----");
                    charDump(Level.FINEST, xml);
                    LOG.finest("end----");
                }
            }
            FileOutputStream os = new FileOutputStream(file);
            os.write(xml.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        private static void charDump(Level logLevel, String text) {
            StringBuilder sb = new StringBuilder(5 * text.length());
            for (int i = 0; i < text.length(); i++) {
                if (i % 8 == 0) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                } else {
                    sb.append(' ');
                }

                int ch = text.charAt(i);
                if (' ' <= ch && ch <= 'z') {
                    sb.append('\'').append((char)ch).append('\'');
                } else {
                    sb.append('x').append(two(Integer.toHexString(ch).toUpperCase()));
                }
            }
            sb.append('\n');
            LOG.log(logLevel, sb.toString());
        }

        private static String two(String s) {
            int len = s.length();
            switch (len) {
                case 0: return "00";
                case 1: return "0" + s;
                case 2: return s;
                default: return s.substring(len - 2);
            }
        }

    } // end of S

    private static class NbTestSuiteLogCheck extends NbTestSuite {
        public NbTestSuiteLogCheck() {
        }
        public NbTestSuiteLogCheck(Class<? extends TestCase> clazz) {
            super(clazz);
        }

        @Override
        public void runTest(Test test, TestResult result) {
            int e = result.errorCount();
            int f = result.failureCount();
            LOG.log(Level.FINE, "Running test {0}", test);
            super.runTest(test, result);
            LOG.log(Level.FINE, "Finished: {0}", test);
            if (e == result.errorCount() && f == result.failureCount()) {
                NbModuleLogHandler.checkFailures((TestCase) test, result, test instanceof NbTestCase ? ((NbTestCase) test).getWorkDirPath() : Manager.getWorkDirPath());
            }
        }
    }
}
