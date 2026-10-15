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

package org.netbeans.modules.nativeexecution.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Vladimir Kvashin
 */
public class NativeExecutionBaseTestSuite extends NbTestSuite {

    static {
        System.setProperty("remote.user.password.keep_in_memory", "true");
    }

    private final String defaultSection;

    /**
     * Constructs an empty TestSuite.
     */
    public NativeExecutionBaseTestSuite() {
        super();
        defaultSection = null;
    }

    /**
     * Constructs an empty TestSuite.
     * @param name suite name
     */
    public NativeExecutionBaseTestSuite(String name) {
        super(name);
        defaultSection = null;
    }

    /**
     * Constructs an empty TestSuite.
     * @param testClasses test classes to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    public NativeExecutionBaseTestSuite(Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super();
        this.defaultSection = null;
        for (Class<? extends NativeExecutionBaseTestCase> testClass : testClasses) {
            addTest(testClass);
        }
    }

    /**
     * Constructs an empty TestSuite.
     * @param name suite name
     * @param defaultSection default section for @ForAllEnvironments annotation
     */
    public NativeExecutionBaseTestSuite(String name, String defaultSection) {
        super(name);
        this.defaultSection = defaultSection;
    }

    /**
     * Constructs TestSuite that adds tests specified by classes parameters
     * @param name suite name
     * @param defaultSection default section for @ForAllEnvironments annotation
     * @param testClasses test class to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    public NativeExecutionBaseTestSuite(String name, String defaultSection, 
            Class<? extends NativeExecutionBaseTestCase>... testClasses) {

        this(name, defaultSection);
        for (Class<? extends NativeExecutionBaseTestCase> testClass : testClasses) {
            addTest(testClass);
        }
    }

    /**
     * Adds a test.
     * @param testClass test class to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    protected final void addTest(Class<? extends NativeExecutionBaseTestCase> testClass)  {
        
        TestClassData testData = findTestData(testClass);
        sortTestData(testData);
        if (testData.testMethods.isEmpty()) {
            addWarningTest("Class " + testClass.getName() + " has no runnable test metods");
        }

        for (TestMethodData methodData : testData.testMethods) {
            if (!checkConditionals(methodData, testClass)) {
                continue;
            }
            if (methodData.isForAllEnvironments()) {
                String[] platforms = NativeExecutionTestSupport.getPlatforms(methodData.envSection, this);
                for (String platform : platforms) {
                    if (testData.forAllEnvConstructor == null) {
                        addWarningTest("Class " + testClass.getName() +
                                " does not have a constructor with 2 parameters: String and ExecutionEnvironment");
                        break;
                    }
                    try {
                        ExecutionEnvironment execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(platform);
                        if (execEnv != null) {
                            addTest(createTest(testData.forAllEnvConstructor, methodData.name, execEnv));
                        } else {
                            addWarningTest(methodData.name + " [" + platform + "]",
                                    "Got null execution environment for " + platform);
                        }
                    } catch (IOException ioe) {
                        addWarningTest(methodData.name + " [" + platform + "]",
                                "Error getting execution environment for " + platform + ": " + NativeExecutionTestSupport.exceptionToString(ioe));
                    }
                }
            } else {
                if (testData.ordinaryConstructor == null) {
                    addWarningTest("Class " + testClass.getName() +
                            " does not have a constructor with 1 parameter of String type");
                    break;
                }
                addTest(createTest(testData.ordinaryConstructor, methodData.name));
            }
        }
    }
    
    public void addWarningTest(String testName, String warningText) {
        addTest(warning(testName, warningText));
    }

    public void addWarningTest(String warningText) {
        addTest(warning(warningText));
    }

    private Test createTest(Constructor<?> ctor, Object... parameters) {
        assert parameters != null;
        assert parameters.length > 0;
        String name = (String) parameters[0];
        try {
            return (Test) ctor.newInstance(parameters);
        } catch (InstantiationException e) {
			return warning("Cannot instantiate test case: "+name+" ("+NativeExecutionTestSupport.exceptionToString(e)+")");
		} catch (InvocationTargetException e) {
			return warning("Exception in constructor: "+name+" ("+NativeExecutionTestSupport.exceptionToString(e.getTargetException())+")");
		} catch (IllegalAccessException e) {
			return warning("Cannot access test case: "+name+" ("+NativeExecutionTestSupport.exceptionToString(e)+")");
		}
    }

    private static class TestMethodData {

        /** The name of the method */
        public final String name;

        /** 
         * In the case the method is annotated with @ForAllEnvironments, contains it's section
         * (or default one in the case it isn't specified in the annotation);
         * if the method is not annotated with @ForAllEnvironments, contains null
         */
        public final String envSection;
        public final String ifSection;
        public final String ifKey;
        public final boolean ifDefault;
        public final String ifdefSection;
        public final String ifdefKey;

        public TestMethodData(String name, String envSection, String condSection, String condKey, boolean condDefault,
                String ifdefSection, String ifdefKey) {
            this.name = name;
            this.envSection = envSection;
            this.ifSection = condSection;
            this.ifKey = condKey;
            this.ifDefault = condDefault;
            this.ifdefSection = ifdefSection;
            this.ifdefKey = ifdefKey;
        }


        public boolean isForAllEnvironments() {
            return envSection != null;
        }
    }

    private static class TestClassData {
        
        // making fields public would be unsafe if this class wasn't private static :-)
        public List<TestMethodData> testMethods = new ArrayList<>();
        public Constructor<?> ordinaryConstructor = null;
        public Constructor<?> forAllEnvConstructor = null;
        
        public boolean containsMethod(String name) {
            if (name != null) {
                for (TestMethodData md : testMethods) {
                    if (name.equals(md.name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Checking @conditional and @ignore annotations
     * @param method method to check
     * @return true in the case there are no @ignore annotation
     * and either there are no @conditional or it's condition is true
     */
    private boolean checkConditionals(TestMethodData methodData, Class<? extends NativeExecutionBaseTestCase> testClass) {
        try {
            final RcFile rcFile = NativeExecutionTestSupport.getRcFile();
            //
            // check @ignore for class and method
            //
            if (rcFile.containsKey("ignore", testClass.getName())) {
                return false;
            }
            if (rcFile.containsKey("ignore", testClass.getName() + '.' + methodData.name)) {
                return false;
            }
            //
            // check @If for method
            //
            if (methodData.ifSection != null && methodData.ifSection.length() != 0) {
                if (methodData.ifKey == null || methodData.ifKey.length() == 0) {
                    addWarningTest(methodData.name + " @If does not specify a key");
                    return false;
                }
                String value = rcFile.get(methodData.ifSection, methodData.ifKey);
                boolean result = (value == null) ? methodData.ifDefault : Boolean.parseBoolean(value);
                if (!result) {
                    return false;
                }
            }
            //
            // check @Ifdef for method
            //
            if (methodData.ifdefSection != null && methodData.ifdefSection.length() != 0) {
                if (methodData.ifdefKey == null || methodData.ifdefKey.length() == 0) {
                    addWarningTest(methodData.name + " @Ifdef does not specify a key");
                    return false;
                }
                if (!rcFile.containsKey(methodData.ifdefSection, methodData.ifdefKey)) {
                    return false;
                }
            }
            return true;
        } catch (FileNotFoundException ex) {
            // silently: just no file => condition is false, that's it
            return false;
        } catch (IOException ex) {
            addWarningTest("Error getting condition for " + methodData.name + ": " + ex.getMessage());
            return false;
        } catch (RcFile.FormatException ex) {
            addWarningTest("Error getting condition for " + methodData.name + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * Searches for 
     * - test methods
     * - constructors
     *
     * Test method is one that either is annotated with @Test
     * or its name starts with "test"
     *
     * NB: such method should be public and return type should be void
     * If it is not a warning is added to tests result
     *
     * @param testClass class to search methods in
     * @return an array of method names
     */
    private TestClassData findTestData(Class<?> testClass) {

        TestClassData result = new TestClassData();

        for(Class<?> superClass = testClass; Test.class.isAssignableFrom(superClass);
        superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (!result.containsMethod(method.getName())) {
                    ForAllEnvironments forAllEnvAnnotation = method.getAnnotation(ForAllEnvironments.class);
                    
                    if (method.getName().startsWith("test") 
                            || method.getAnnotation(org.junit.Test.class) != null
                            || forAllEnvAnnotation != null) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            addWarningTest("Method " + testClass.getName() + '.' + method.getName() + " should be public");
                        } else if (! method.getReturnType().equals(Void.TYPE)) {
                            addWarningTest("Method " + testClass.getName() + '.' + method.getName() + " should be void");
                        } else if (method.getParameterTypes().length > 0) {
                            addWarningTest("Method " + testClass.getName() + '.' + method.getName() + " should have no parameters");
                        } else {
                            if (method.getAnnotation(org.junit.Ignore.class) == null) {
                                If ifAnnotation = method.getAnnotation(If.class);
                                String condSection = (ifAnnotation == null) ? null : ifAnnotation.section();
                                String condKey = (ifAnnotation == null) ? null : ifAnnotation.key();
                                boolean condDefault = (ifAnnotation == null) ? false : ifAnnotation.defaultValue();
                                Ifdef ifdefAnnotation = method.getAnnotation(Ifdef.class);
                                String ifdefSection = (ifdefAnnotation == null) ? null : ifdefAnnotation.section();
                                String ifdefKey = (ifdefAnnotation == null) ? null : ifdefAnnotation.key();
                                if (forAllEnvAnnotation != null) {
                                    String envSection = forAllEnvAnnotation.section();
                                    if (envSection == null || envSection.length() == 0) {
                                        envSection = defaultSection;
                                    }
                                    if (envSection != null && envSection.length() > 0) {
                                        result.testMethods.add(new TestMethodData(method.getName(), envSection, condSection, condKey, condDefault, ifdefSection, ifdefKey));
                                    } else {
                                        addWarningTest("@ForAllEnvironments annotation for method " + testClass.getName() + '.' + method.getName() + " does not specify section");
                                    }
                                } else {
                                    result.testMethods.add(new TestMethodData(method.getName(), null, condSection, condKey, condDefault, ifdefSection, ifdefKey));
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Constructor<?> ctor : testClass.getConstructors()) {
            Class<?>[] parameters = ctor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].equals(String.class)) {
                result.ordinaryConstructor = ctor;
            }
            if (parameters.length == 2
                    && parameters[0].equals(String.class)
                    && parameters[1].equals(ExecutionEnvironment.class)) {
                result.forAllEnvConstructor = ctor;
            }
        }

        return result;
    }
    
    private static void sortTestData(TestClassData data) {
        data.testMethods.sort(new Comparator<TestMethodData>() {
            @Override
            public int compare(TestMethodData d1, TestMethodData d2) {
                return d1.name.compareTo(d2.name);
            }
        });
    }

    protected static Test warning(String testName, final String message) {
            return new TestCase(testName) {
                    @Override
                    protected void runTest() {
                            fail(message);
                    }
            };
    }
}
