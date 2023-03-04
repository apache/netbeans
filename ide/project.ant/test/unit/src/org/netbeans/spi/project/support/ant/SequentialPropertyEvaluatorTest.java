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

package org.netbeans.spi.project.support.ant;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.util.test.MockPropertyChangeListener;

public class SequentialPropertyEvaluatorTest extends NbTestCase {

    public SequentialPropertyEvaluatorTest(String n) {
        super(n);
    }

    @Override protected void runTest() throws Throwable {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override public Void run() throws Exception {
                try {
                    SequentialPropertyEvaluatorTest.super.runTest();
                } catch (Exception x) {
                    throw x;
                } catch (Throwable x) {
                    throw new Exception(x);
                }
                return null;
            }
        });
    }

    public void testSequentialEvaluatorBasic() throws Exception {
        Map<String,String> defs1 = new HashMap<String,String>();
        defs1.put("key1", "val1");
        defs1.put("key2", "val2");
        defs1.put("key5", "5=${key1}");
        defs1.put("key6", "6=${key3}");
        Map<String,String> defs2 = new HashMap<String,String>();
        defs2.put("key3", "val3");
        defs2.put("key4", "4=${key1}:${key3}");
        defs2.put("key7", "7=${undef}");
        PropertyEvaluator eval = new SequentialPropertyEvaluator(null,
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2));
        String[] vals = {
            "val1",
            "val2",
            "val3",
            "4=val1:val3",
            "5=val1",
            "6=${key3}",
            "7=${undef}",
        };
        Map<String,String> all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "5=val1 x ${undef}", eval.evaluate("${key5} x ${undef}"));
        // And test the preprovider...
        Map<String,String> predefs = Collections.singletonMap("key3", "preval3");
        eval = new SequentialPropertyEvaluator(PropertyUtils.fixedPropertyProvider(predefs),
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2));
        vals = new String[] {
            "val1",
            "val2",
            "preval3",
            "4=val1:preval3",
            "5=val1",
            "6=preval3",
            "7=${undef}",
        };
        all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "4=val1:preval3 x ${undef} x preval3", eval.evaluate("${key4} x ${undef} x ${key3}"));
    }

    public void testSequentialEvaluatorChanges() throws Exception {
        AntBasedTestUtil.TestMutablePropertyProvider predefs = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        AntBasedTestUtil.TestMutablePropertyProvider defs1 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        AntBasedTestUtil.TestMutablePropertyProvider defs2 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        predefs.defs.put("x", "xval1");
        predefs.defs.put("y", "yval1");
        defs1.defs.put("a", "aval1");
        defs1.defs.put("b", "bval1=${x}");
        defs1.defs.put("c", "cval1=${z}");
        defs2.defs.put("m", "mval1");
        defs2.defs.put("n", "nval1=${x}:${b}");
        defs2.defs.put("o", "oval1=${z}");
        PropertyEvaluator eval = new SequentialPropertyEvaluator(predefs, defs1, defs2);
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        eval.addPropertyChangeListener(l);
        Map<String,String> result = new HashMap<String,String>();
        result.put("x", "xval1");
        result.put("y", "yval1");
        result.put("a", "aval1");
        result.put("b", "bval1=xval1");
        result.put("c", "cval1=${z}");
        result.put("m", "mval1");
        result.put("n", "nval1=xval1:bval1=xval1");
        result.put("o", "oval1=${z}");
        assertEquals("correct initial vals", result, eval.getProperties());
        l.assertEvents();
        // Change predefs.
        predefs.defs.put("x", "xval2");
        predefs.mutated();
        Map<String,String> oldvals = new HashMap<String,String>();
        oldvals.put("x", result.get("x"));
        oldvals.put("b", result.get("b"));
        oldvals.put("n", result.get("n"));
        Map<String,String> newvals = new HashMap<String,String>();
        newvals.put("x", "xval2");
        newvals.put("b", "bval1=xval2");
        newvals.put("n", "nval1=xval2:bval1=xval2");
        result.putAll(newvals);
        l.assertEventsAndValues(oldvals, newvals);
        assertEquals("right total values now", result, eval.getProperties());
        // Change some other defs.
        defs1.defs.put("z", "zval1");
        defs1.defs.remove("b");
        defs1.mutated();
        defs2.defs.put("m", "mval2");
        defs2.mutated();
        oldvals.clear();
        oldvals.put("b", result.get("b"));
        oldvals.put("c", result.get("c"));
        oldvals.put("m", result.get("m"));
        oldvals.put("n", result.get("n"));
        oldvals.put("o", result.get("o"));
        oldvals.put("z", result.get("z"));
        newvals.clear();
        newvals.put("b", null);
        newvals.put("c", "cval1=zval1");
        newvals.put("m", "mval2");
        newvals.put("n", "nval1=xval2:${b}");
        newvals.put("o", "oval1=zval1");
        newvals.put("z", "zval1");
        result.putAll(newvals);
        result.remove("b");
        l.assertEventsAndValues(oldvals, newvals);
        assertEquals("right total values now", result, eval.getProperties());
    }

    public void testEvaluate() throws Exception {
        // XXX check override order, property name evaluation, $$ escaping, bare or final $,
        // cyclic errors, undef'd property substitution, no substs in predefs, etc.
        Map<String,String> m1 = Collections.singletonMap("y", "val");
        Map<String,String> m2 = new HashMap<String,String>();
        m2.put("x", "${y}");
        m2.put("y", "y-${x}");
        List<Map<String,String>> m1m2 = new ArrayList<Map<String,String>>();
        m1m2.add(m1);
        m1m2.add(m2);
        assertEquals("x evaluates to former y", "val", evaluate("x", Collections.<String,String>emptyMap(), m1m2));
        assertEquals("first y defines it", "val", evaluate("y", Collections.<String,String>emptyMap(), m1m2));
        assertEquals("circularity error", null, evaluate("x", Collections.<String,String>emptyMap(), Collections.singletonList(m2)));
        assertEquals("circularity error", null, evaluate("y", Collections.<String,String>emptyMap(), Collections.singletonList(m2)));
        m2.clear();
        m2.put("y", "yval_${z}");
        m2.put("x", "xval_${y}");
        m2.put("z", "zval");
        Map<String,String> all = evaluateAll(Collections.<String,String>emptyMap(), Collections.singletonList(m2));
        assertNotNull("no circularity error", all);
        assertEquals("have three properties", 3, all.size());
        assertEquals("double substitution", "xval_yval_zval", all.get("x"));
        assertEquals("single substitution", "yval_zval", all.get("y"));
        assertEquals("no substitution", "zval", all.get("z"));
        // Yuck. But it failed once, so check it now.
        Properties p = new Properties();
        p.load(new ByteArrayInputStream("project.mylib=../mylib\njavac.classpath=${project.mylib}/build/mylib.jar\nrun.classpath=${javac.classpath}:build/classes".getBytes(StandardCharsets.US_ASCII)));
        all = evaluateAll(Collections.<String,String>emptyMap(), Collections.singletonList(NbCollections.checkedMapByFilter(p, String.class, String.class, true)));
        assertNotNull("no circularity error", all);
        assertEquals("javac.classpath correctly substituted", "../mylib/build/mylib.jar", all.get("javac.classpath"));
        assertEquals("run.classpath correctly substituted", "../mylib/build/mylib.jar:build/classes", all.get("run.classpath"));
    }

    public void testEvaluateString() throws Exception {
        Map<String,String> predefs = new HashMap<String,String>();
        predefs.put("homedir", "/home/me");
        Map<String,String> defs1 = new HashMap<String,String>();
        defs1.put("outdirname", "foo");
        defs1.put("outdir", "${homedir}/${outdirname}");
        Map<String,String> defs2 = new HashMap<String,String>();
        defs2.put("outdir2", "${outdir}/subdir");
        List<Map<String,String>> defs12 = new ArrayList<Map<String,String>>();
        defs12.add(defs1);
        defs12.add(defs2);
        assertEquals("correct evaluated string",
            "/home/me/foo/subdir is in /home/me",
            evaluateString("${outdir2} is in ${homedir}", predefs, defs12));
    }

    public void testSequentialPropertyEvaluatorStringAllocation() throws Exception {
        // #48449: too many String instances.
        // String constants used in the test are interned; make sure the results are the same.
        // Not necessary for the provider to intern strings, just to not copy them.
        Map<String,String> defs = new HashMap<String,String>();
        defs.put("pre-a", "pre-a-val");
        defs.put("pre-b", "pre-b-val");
        PropertyProvider preprovider = PropertyUtils.fixedPropertyProvider(defs);
        defs = new HashMap<String,String>();
        defs.put("main-1-a", "main-1-a-val");
        defs.put("main-1-b", "main-1-b-val+${pre-b}");
        PropertyProvider provider1 = PropertyUtils.fixedPropertyProvider(defs);
        defs = new HashMap<String,String>();
        defs.put("main-2-a", "main-2-a-val");
        defs.put("main-2-b", "main-2-b-val+${main-1-b}");
        PropertyProvider provider2 = PropertyUtils.fixedPropertyProvider(defs);
        PropertyEvaluator pp = new SequentialPropertyEvaluator(preprovider, provider1, provider2);
        defs = pp.getProperties();
        assertSame("uncopied pre-a", "pre-a-val", defs.get("pre-a"));
        assertSame("uncopied pre-b", "pre-b-val", defs.get("pre-b"));
        assertSame("uncopied main-1-a", "main-1-a-val", defs.get("main-1-a"));
        assertEquals("right main-1-b", "main-1-b-val+pre-b-val", defs.get("main-1-b"));
        assertSame("uncopied main-2-a", "main-2-a-val", defs.get("main-2-a"));
        assertEquals("right main-2-b", "main-2-b-val+main-1-b-val+pre-b-val", defs.get("main-2-b"));
    }

    private static PropertyEvaluator evaluator(Map<String,String> predefs, List<Map<String,String>> defs) {
        PropertyProvider[] mainProviders = new PropertyProvider[defs.size()];
        int i = 0;
        for (Map<String,String> def : defs) {
            mainProviders[i++] = PropertyUtils.fixedPropertyProvider(def);
        }
        return new SequentialPropertyEvaluator(PropertyUtils.fixedPropertyProvider(predefs), mainProviders);
    }

    private static String evaluate(String prop, Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).getProperty(prop);
    }

    private static Map<String,String> evaluateAll(Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).getProperties();
    }

    private static String evaluateString(String text, Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).evaluate(text);
    }

}
