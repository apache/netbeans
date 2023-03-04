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
package org.netbeans.performance.benchmarks;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import junit.framework.*;
import org.netbeans.performance.benchmarks.bde.*;

/**
 * A suite for Benchmarks;
 */
public final class BenchmarkSuite implements Test {

    public static final String TESTS_SPECS = "tests.specs";

    public static final String TESTS_OUTPUT = "tests.output";

    private ArrayList benchmarks;
    private TestsSpecifications testSpecs;
    private LoadDefinition ldef;
    private StoreDefinition sdef;
    private List dataManagers;
    private Map class2DD;
    
    /** New Benchmark suite */
    public BenchmarkSuite(String testSpecs) {
        this.benchmarks = new ArrayList(10);
        this.testSpecs = new TestsSpecifications();
        this.dataManagers = new ArrayList(10);
        this.class2DD = new HashMap();
        
        if( testSpecs != null && testSpecs.length() != 0 ) {
            try {
                List list = TestSpecBuilder.parse(testSpecs);
                for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                    Object obj = iter.next();
                    if (obj instanceof LoadDefinition) {
                        ldef = (LoadDefinition) obj;
                    } else if (obj instanceof StoreDefinition) {
                        sdef = (StoreDefinition) obj;
                    } else {
                        TestDefinition testDef = (TestDefinition) obj;
                        this.testSpecs.add(testDef);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** New Benchmark suite */
    public BenchmarkSuite() {
        this(System.getProperty(TESTS_SPECS));
    }
    
    /** Adds a Test to this suite */
    public void addBenchmarkClass(Class klass) {
        try {
            if (testSpecs.implies(klass)) {
                benchmarks.addAll(createTestsForClass(klass));
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    /** Counts no of tests */
    public int countTestCases() {
        return benchmarks.size();
    }
    
    /** Run this suite */
    public void run(TestResult result) {
        Reporter reporter = null;
        String output = System.getProperty(TESTS_OUTPUT);
        
        if( output == null ) {
            reporter = new XMLReporter();
        } else {
            try {
                reporter = new XMLReporter( new FileOutputStream( output ), "UTF-8");
            } catch( IOException e ) {
                Assert.fail( e.getMessage() );
            }
        }
        
        Benchmark.setReporter( reporter );
        loadData(result);
        
        for (Iterator iter = benchmarks.iterator(); iter.hasNext(); ) {
            ((Test) iter.next()).run(result);
        }
        
        storeData(result);
        tearDownData(result);
        reporter.flush();
    }
    
    //----------------- impl methods -------------------
    
    /** Loads serialized data */
    private void loadData(TestResult result) {
        if (ldef == null) {
            return;
        }
        
        try {
            File store = ldef.getFile();
            ObjectInputStream obtis = new ObjectInputStream(new FileInputStream(store));
            class2DD = (Map) obtis.readObject();
            obtis.close();
        } catch (Exception e) {
            result.addError(this, e);
        }
    }
    
    /** Stores data */
    private void storeData(TestResult result) {
        if (sdef == null) {
            return;
        }
        
        try {
            File store = sdef.getFile();
            ObjectOutputStream obtos = new ObjectOutputStream(new FileOutputStream(store));
            obtos.writeObject(class2DD);
            obtos.close();
        } catch (Exception e) {
            result.addError(this, e);
        }
    }
    
    /** Calls tearDownData on all registered DataManagers */
    private void tearDownData(TestResult result) {    
        // tear down data managers
        for (Iterator it = dataManagers.iterator(); it.hasNext(); ) {
            DataManager dm = (DataManager) it.next();
            try {
                dm.tearDownData();
            } catch (Exception e) {
                result.addError((Test) dm, e);
            }
        }
    }
    
    /** Creates tests for given Class */
    private Collection createTestsForClass(Class klass) throws Exception {
        ArrayList list = new ArrayList(50);
        Method[] methods = klass.getMethods();
        Constructor constructor = klass.getConstructor(new Class[] { String.class });
        
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (isTestMethod(methods[i]) && testSpecs.implies(klass, method)) {
                Benchmark test = (Benchmark) constructor.newInstance(new Object[] { method });
                test.setSuite(this);
                testSpecs.setArguments(test);
                list.add(test);
            }
        }
        
        return list;
    }
    
    /** Does m comply to a definition public, no args, test, ... */
    private static boolean isTestMethod(Method m) {
        int mod = m.getModifiers();
        return  Modifier.isPublic(mod) && (m.getReturnType() == Void.TYPE) &&
                (m.getParameterTypes().length == 0) && m.getName().startsWith("test");
    }
    
    final void setUpDataFor(Benchmark bmark) throws Exception {
        // register
        dataManagers.add(bmark);
        DataDescriptor dd = getDataFor(bmark);
        DataManager dm = (DataManager) bmark;
        dm.setUpData(dd);
    }
    
    /** @return a DataDescriptor for a given Benchmark */
    private DataDescriptor getDataFor(Benchmark bmark) {
        DataManager dm = (DataManager) bmark;
        final DataDescriptor dd = dm.createDataDescriptor();
        String key = bmark.getClass().getName();
        dd.set(key, bmark.getName(), bmark.getArgument());
        List ddList = (List) class2DD.get(key);
        if (ddList != null) {
            for (Iterator it = ddList.iterator(); it.hasNext(); ) {
                Object next = it.next();
                if (dd.equals(next)) {
                    return (DataDescriptor) next;
                }
            }
            ddList.add(dd);
            return dd;
        }
        
        ddList = new ArrayList();
        ddList.add(dd);
        class2DD.put(key, ddList);
        return dd;
    }
    
    /** Filters tests to run */
    static final class TestsSpecifications {
        
        private Map testsSpecifications;
        
        /** New TestsSpecifications */
        public TestsSpecifications() {
            testsSpecifications = new HashMap();
        }
        
        /** Adds new TestDefinition to this list */
        public void add(TestDefinition testDef) throws Exception {
            Class klass = Class.forName(testDef.getClassName());
            testsSpecifications.put(klass, new TestSpecification(testDef));
        }
        
        /** Whether given class should be included */
        public boolean implies(Class klass) throws Exception {
            boolean ret = staticTest(klass);
            
            if (ret && (testsSpecifications.size() != 0)) {
                ret = (testsSpecifications.get(klass) != null);
            }
            
            return ret;
        }
        
        /** Whether given method should be included */
        public boolean implies(Class klass, String method) {
            TestSpecification spec = (TestSpecification) testsSpecifications.get(klass);
            if (spec == null) {
                return true;
            } else {
                return spec.implies(method);
            }
        }
        
        public void setArguments(Benchmark test) {
            TestSpecification spec = (TestSpecification) testsSpecifications.get(test.getClass());
            if (spec != null) {
                spec.setArguments(test);
            }
        }
        
        /** Tests needed conditions */
        private static boolean staticTest(Class klass) throws NoSuchMethodException {
            int mod = klass.getModifiers();
            return (Benchmark.class.isAssignableFrom(klass) && Modifier.isPublic(mod) &&
                    !Modifier.isAbstract(mod) && (klass.getConstructor(new Class[] { String.class }) != null)
            );
        }
    }
    
    /** Filters test methods */
    static final class TestSpecification {
        
        private List methodMatchers;
        private List argSeries;
        
        /** New TestsSpecifications */
        public TestSpecification(TestDefinition testDef) {
            setMethodMatchers(testDef.getMethodPatterns());
            setArgSeries(testDef.getArgumentSeries());
        }
        
        /** Creates MethodMetchers */
        private void setMethodMatchers(Iterator it) {
            methodMatchers = new ArrayList();
            while (it.hasNext()) {
                String pattern = (String) it.next();
                methodMatchers.add(MethodMatcher.create(pattern));
            }
        }
        
        /** Creates a List of ArgumentSeries */
        private void setArgSeries(Iterator it) {
            argSeries = new ArrayList();
            while (it.hasNext()) {
                argSeries.add(it.next());
            }
        }
        
        /** Whether given method should be included */
        public boolean implies(String method) {
            if (methodMatchers.size() == 0) {
                return true;
            }
            
            for (Iterator iter = methodMatchers.iterator(); iter.hasNext(); ) {
                MethodMatcher matcher = (MethodMatcher) iter.next();
                if (matcher.matches(method)) {
                    return true;
                }
            }
            
            return false;
        }
        
        /** Creates arguments for given params */
        private static void createArgs(String[] keys, int level, ArgumentSeries as, MapArgBenchmark mab, List mapList) {
            if (level < keys.length) {
                String key = keys[level];
                Iterator vals = as.getValues(key);
                while (vals.hasNext()) {
                    List localMapList = new ArrayList(11);
                    Object next = vals.next();
                    
                    if (next instanceof Interval) {
                        Interval interval = (Interval) next;
                        int i = interval.getStart();
                        List localMapList2 = new ArrayList(11);
                        for (; i < interval.getEnd(); i += interval.getStep()) {
                            createArgsAndPut(keys, level + 1, as, mab, localMapList2, key, new Integer(i));
                            localMapList.addAll(localMapList2);
                            localMapList2.clear();
                        }
                        if (i >= interval.getEnd()) {
                            createArgsAndPut(keys, level + 1, as, mab, localMapList2, key, new Integer(interval.getEnd()));
                            localMapList.addAll(localMapList2);
                        }
                    } else {
                        createArgsAndPut(keys, level + 1, as, mab, localMapList, key, next);
                    }
                    mapList.addAll(localMapList);
                }
            } else {
                mapList.add(mab.createDefaultMap());
            }
        }
            
        private static void createArgsAndPut(String[] keys, int level, ArgumentSeries as, MapArgBenchmark mab, List mapList, Object key, Object val) {
            createArgs(keys, level, as, mab, mapList);
            int size = mapList.size();
            for (int i = 0; i < size; i++) {
                Map map = (Map) mapList.get(i);
                map.put(key, val);
            }
        }
        
        /** Create a Collection of Benchmarks */
        public void setArguments(Benchmark bench) {
            if (bench instanceof MapArgBenchmark) {
                MapArgBenchmark test = (MapArgBenchmark) bench;
                List args = new ArrayList(20);
                
                final int size = argSeries.size();
                for (int i = 0; i < size; i++) {
                    ArgumentSeries as = (ArgumentSeries) argSeries.get(i);
                    createArgs(as.getKeys(), 0, as, test, args);
                }
                
                test.setArgumentArray((Map[]) args.toArray(new Map[args.size()]));
            }
        }
    }
    
    /** Simple matcher */
    static class MethodMatcher {
        
        private static final char WILDCARD = '*';
        
        private int type;
        private String prefix;
        private String suffix;
        
        private MethodMatcher() {
        }
        
        /** Matches given name? */
        public boolean matches(String method) {
            switch (type) {
                case 0: return prefix.equals(method);
                case 1: return method.endsWith(suffix);
                case 2: return method.startsWith(prefix);
                case 3: return method.endsWith(suffix) && method.startsWith(prefix);
                default : return false;
            }
        }
        
        /** Creates new Matcher */
        public static final MethodMatcher create(String pattern) {
            MethodMatcher matcher = new MethodMatcher();
            if (pattern.charAt(0) == WILDCARD) {
                matcher.type = 1;
                matcher.suffix = pattern.substring(1);
            } else if (pattern.charAt(pattern.length() - 1) == WILDCARD) {
                matcher.type = 2;
                matcher.prefix = pattern.substring(0, pattern.length() - 1);
            } else {
                int idx = pattern.indexOf(WILDCARD);
                if (idx < 0) {
                    matcher.type = 0;
                    matcher.prefix = pattern;
                } else {
                    matcher.type = 3;
                    matcher.prefix = pattern.substring(0, idx);
                    matcher.suffix = pattern.substring(idx + 1);
                }
            }
            return matcher;
        }
    }
}
